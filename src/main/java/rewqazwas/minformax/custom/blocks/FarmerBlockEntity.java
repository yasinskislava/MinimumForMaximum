package rewqazwas.minformax.custom.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;
import rewqazwas.minformax.config.DataConfigs;
import rewqazwas.minformax.custom.ModBlockEntities;
import rewqazwas.minformax.custom.ModTags;
import rewqazwas.minformax.custom.index.FarmerData;
import rewqazwas.minformax.custom.index.ModDataReloadListener;
import rewqazwas.minformax.custom.items.ModItems;
import rewqazwas.minformax.custom.items.upgrades.FortuneUpgrade;
import rewqazwas.minformax.custom.items.upgrades.ProcessingUpgrade;
import rewqazwas.minformax.custom.items.upgrades.SpeedUpgrade;
import rewqazwas.minformax.custom.utility.Utils;
import rewqazwas.minformax.screen.custom.FarmerMenu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class FarmerBlockEntity extends MachineBaseEntity implements MenuProvider {
    public FarmerBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.FARMER_BE.get(), pos, blockState);

        this.enabledSides[Direction.DOWN.get3DDataValue()] = true;
        this.enabledSides[Direction.UP.get3DDataValue()] = true;
    }
    //Handlers

    public final Utils.UpgradeItemHandler upgradeHandler = new Utils.UpgradeItemHandler(4) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stack.is(ModItems.WATERING_UPGRADE)
                    || stack.is(ModTags.FORTUNE_UPGRADES)
                    || stack.is(ModItems.COMPRESSING_UPGRADE)
                    || super.isItemValid(slot, stack);
        }

        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            cacheDirty = true;
            setChanged();
        }
    };

    public final Utils.SingleItemHandler itemHandler = new Utils.SingleItemHandler(1) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            String itemString = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();

            for (FarmerData data : ModDataReloadListener.FARMER_DATA.values()) {
                // Prohibited check
                if (data.prohibitedItems().contains(itemString)) return false;
                for (String tag : data.prohibitedTags()) {
                    if (stack.is(TagKey.create(BuiltInRegistries.ITEM.key(), ResourceLocation.parse(tag)))) return false;
                }

                // Allowed check
                if (data.allowedItems().contains(itemString)) return true;
                for (String tag : data.allowedTags()) {
                    if (stack.is(TagKey.create(BuiltInRegistries.ITEM.key(), ResourceLocation.parse(tag)))) return true;
                }
            }
            return false; // Only allow items found in config
        }

        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            cacheDirty = true;
            setChanged();
        }
    };

    public final EnergyStorage energyHandler = new EnergyStorage(40_960_000);

    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> FarmerBlockEntity.this.process;
                case 1 -> FarmerBlockEntity.this.maxProcess;
                case 2 -> FarmerBlockEntity.this.energyHandler.getEnergyStored();
                case 3 -> FarmerBlockEntity.this.consumptionRate;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> FarmerBlockEntity.this.process = value;
                case 1 -> FarmerBlockEntity.this.maxProcess = value;
                case 2 -> FarmerBlockEntity.this.energyHandler.receiveEnergy(value, false);
                case 3 -> FarmerBlockEntity.this.consumptionRate = value;
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    };

    //Variables
    private int process = 0;
    private int maxProcess = 256;
    private int duration = maxProcess;
    private int consumptionRate = 0;

    // Cache
    private boolean cacheDirty = true;
    private ModifierData cachedModifiers;
    private List<ItemStack> cachedDrops = new ArrayList<>();
    private int cachedEnergyCost = 0;
    private boolean hasValidSource = false;
    private final Map<Item, Utils.EssenceRecipeInfo> compressionCache = new HashMap<>();

    //Extra
    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    protected List<IItemHandler> getDroppableHandlers() {
        return List.of(itemHandler, upgradeHandler);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", itemHandler.serializeNBT(registries));
        tag.put("upgrades", upgradeHandler.serializeNBT(registries));
        tag.putInt("farmer.process", this.process);
        tag.putInt("farmer.max_process", this.maxProcess);
        tag.putInt("farmer.energy", this.energyHandler.getEnergyStored());
        int[] toSave = new int[6];
        for (int i = 0; i < 6; i++) {
            toSave[i] = enabledSides[i] ? 1 : 0;
        }
        tag.putIntArray("farmer.enabled_sides", toSave);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        itemHandler.deserializeNBT(registries, tag.getCompound("inventory"));
        upgradeHandler.deserializeNBT(registries, tag.getCompound("upgrades"));
        this.process = tag.getInt("farmer.process");
        this.maxProcess = tag.getInt("farmer.max_process");
        this.energyHandler.receiveEnergy(tag.getInt("farmer.energy"), false);
        int[] savedSides = tag.getIntArray("farmer.enabled_sides");
        for (int i = 0; i < Math.min(savedSides.length, 6); i++) {
            enabledSides[i] = savedSides[i] == 1;
        }
        this.cacheDirty = true;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.minformax.farmer");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new FarmerMenu(containerId, playerInventory, this, this.data);
    }

    //Utility
    public static int wateringChance() {
        double chance = ThreadLocalRandom.current().nextDouble(100.0);
        if (chance < 90.0) {
            return 2;
        } else if (chance < 99.0) {
            return 4;
        } else {
            return 8;
        }
    }

    public void toggleSide(Direction dir) {
        enabledSides[dir.get3DDataValue()] = !enabledSides[dir.get3DDataValue()];
        this.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    public boolean isSideEnabled(Direction dir) {
        return enabledSides[dir.get3DDataValue()];
    }

    private void recalculateCache(ServerLevel level) {
        int speedModifier = 1;
        int stackMultiplier = 1;
        int fortuneMultiplier = 1;
        boolean hasWatering = false;
        boolean hasCompressing = false;

        for(int i = 0; i < upgradeHandler.getSlots(); i++) {
            var stack = upgradeHandler.getStackInSlot(i);
            var upgrade = stack.getItem();
            if(upgrade instanceof SpeedUpgrade speedUpgrade) {
                speedModifier = speedUpgrade.getModifier();
            } else if(upgrade instanceof ProcessingUpgrade processingUpgrade) {
                stackMultiplier = processingUpgrade.getMultiplier();
            } else if(upgrade instanceof FortuneUpgrade fortuneUpgrade) {
                fortuneMultiplier = fortuneUpgrade.getMultiplier();
            } else if(stack.is(ModItems.WATERING_UPGRADE)) {
                hasWatering = true;
            } else if(stack.is(ModItems.COMPRESSING_UPGRADE.get())) {
                hasCompressing = true;
            }
        }

        this.cachedModifiers = new ModifierData(speedModifier, stackMultiplier, fortuneMultiplier, hasWatering, hasCompressing);

        var sourceStack = itemHandler.getStackInSlot(0);
        this.hasValidSource = !sourceStack.isEmpty();

        this.compressionCache.clear();
        if (this.hasValidSource) {
            this.cachedDrops = Utils.getFarmerDrops(level, sourceStack);

            if (hasCompressing) {
                for (ItemStack drop : this.cachedDrops) {
                    Item item = drop.getItem();
                    if (Utils.isCompressibleEssence(item)) {
                        Utils.EssenceRecipeInfo info = Utils.getEssenceRecipe(level, item);
                        if (info != null) {
                            this.compressionCache.put(item, info);
                        }
                    }
                }
            }
        } else {
            this.cachedDrops = List.of();
        }

        this.cachedEnergyCost = stackMultiplier * speedModifier * DataConfigs.farmCoefficient.get();
        if (speedModifier == 9999) {
            this.cachedEnergyCost = 0;
        }

        this.cacheDirty = false;
    }

    private List<ItemStack> mergeStacks(List<ItemStack> stacks) {
        List<ItemStack> merged = new ArrayList<>();
        for (ItemStack stack : stacks) {
            if (stack.isEmpty()) continue;
            boolean found = false;
            for (ItemStack m : merged) {
                if (ItemStack.isSameItemSameComponents(m, stack)) {
                    m.grow(stack.getCount());
                    found = true;
                    break;
                }
            }
            if (!found) {
                merged.add(stack);
            }
        }
        return merged;
    }

    //Main
    public void tick(Level level, BlockPos blockPos, BlockState blockState, FarmerBlockEntity blockEntity) {
        if(level.isClientSide()) return;
        this.consumptionRate = 0;
        if (this.cacheDirty) {
            recalculateCache((ServerLevel) level);
        }

        if(!this.hasValidSource) {
            if (process != 0) {
                process = 0;
                setChanged(level, blockPos, blockState);
            }
            return;
        }

        boolean dirty = false;
        int currentEnergy = energyHandler.getEnergyStored();
        int energyCost = this.cachedEnergyCost;

        if (currentEnergy < energyCost) return;

        var modifiers = this.cachedModifiers; // Define modifiers here

        List<ItemStack> potentialDrops = new ArrayList<>();
        for (ItemStack drop : this.cachedDrops) {
            ItemStack copy = drop.copy();
            copy.setCount(copy.getCount() * modifiers.stackMultiplier * modifiers.fortuneMultiplier);
            potentialDrops.add(copy);
        }

        potentialDrops = mergeStacks(potentialDrops);

        if (modifiers.hasCompressing && !compressionCache.isEmpty()) {
            potentialDrops = Utils.applyCompression(potentialDrops, compressionCache);
            potentialDrops = mergeStacks(potentialDrops);
        }

        boolean canFitAny = false;
        for (ItemStack drop : potentialDrops) {
            if (Utils.canInsertAtLeastOneNetwork(level, blockPos, drop, this.enabledSides)) {
                canFitAny = true;
                break;
            }
        }
        if (!canFitAny) return;

        energyHandler.extractEnergy(energyCost, false);
        if (energyHandler.getEnergyStored() != currentEnergy) {
            dirty = true;
        }
        this.consumptionRate = energyCost;

        int effectiveWateringMultiplier = modifiers.hasWatering ? wateringChance() : 1;

        if(process == 0){
            maxProcess = Math.max(duration / modifiers.speedModifier / effectiveWateringMultiplier, 1);
            dirty = true;
        }

        process++;
        dirty = true; // Process changed

        if(process >= maxProcess) {
            for (ItemStack drop : potentialDrops) {
                Utils.moveItem(level, blockPos, drop, enabledSides);
            }
            process = 0;
        }

        if (dirty) {
            setChanged(level, blockPos, blockState);
        }
    }

    // Update the ModifierData record at the bottom of the class
    private record ModifierData(int speedModifier, int stackMultiplier, int fortuneMultiplier, boolean hasWatering, boolean hasCompressing) {}


}