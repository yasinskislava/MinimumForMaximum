package rewqazwas.minformax.custom.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.*;
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
import rewqazwas.minformax.custom.component.ModDataComponents;
import rewqazwas.minformax.custom.index.ModDataReloadListener;
import rewqazwas.minformax.custom.index.ModuleDropsReloadListener;
import rewqazwas.minformax.custom.items.AccShard;
import rewqazwas.minformax.custom.items.ModItems;
import rewqazwas.minformax.custom.items.ModuleItem;
import rewqazwas.minformax.custom.items.upgrades.*;
import rewqazwas.minformax.custom.utility.Utils;
import rewqazwas.minformax.screen.custom.EternalGeneratorMenu;

import java.util.ArrayList;
import java.util.List;

public class EternalGeneratorBlockEntity extends MachineBaseEntity implements MenuProvider {
    private static final ResourceLocation INFERIUM_ESSENCE_RL = ResourceLocation.parse("mysticalagriculture:inferium_essence");

    public EternalGeneratorBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.ETERNAL_GENERATOR_BE.get(), pos, blockState);
        EternalGeneratorBlock block = (EternalGeneratorBlock) blockState.getBlock();
        this.tier = block.tier;
        this.loaderSlots = (int) Math.pow(2, tier - 1);

        data = new ContainerData() {
            @Override
            public int get(int i) {
                return switch (i) {
                    case 0 -> EternalGeneratorBlockEntity.this.process;
                    case 1 -> EternalGeneratorBlockEntity.this.maxProcess;
                    case 2 -> EternalGeneratorBlockEntity.this.currentEnergy;
                    case 3 -> EternalGeneratorBlockEntity.this.totalXp;
                    case 4 -> EternalGeneratorBlockEntity.this.overload;
                    case 5 -> EternalGeneratorBlockEntity.this.overflowXp;
                    default -> 0;
                };
            }

            @Override
            public void set(int i, int value) {
                switch (i) {
                    case 0: EternalGeneratorBlockEntity.this.process = value; break;
                    case 1: EternalGeneratorBlockEntity.this.maxProcess = value; break;
                    case 2: EternalGeneratorBlockEntity.this.currentEnergy = value; break;
                    case 3: EternalGeneratorBlockEntity.this.totalXp = value; break;
                    case 4: EternalGeneratorBlockEntity.this.overload = value; break;
                    case 5: EternalGeneratorBlockEntity.this.overflowXp = value; break;
                }
            }


            @Override
            public int getCount() {
                return 6;
            }
        };
    }

    //Handlers
    public Utils.SingleItemHandler itemHandler = new Utils.SingleItemHandler(8) {
        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            cacheDirty = true;
            setChanged();
        }
    };

    public final Utils.UpgradeItemHandler upgradeHandler = new Utils.UpgradeItemHandler(4) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stack.is(ModTags.EXTRA_DROP_UPGRADES)
                    || stack.is(ModItems.INVERTED_UPGRADE)
                    || super.isItemValid(slot, stack);
        }

        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            cacheDirty = true;
            setChanged();
        }
    };

    public final EnergyStorage energyHandler = new EnergyStorage(1000000);

    //Variables
    protected final ContainerData data;
    private int process = 0;
    private int maxProcess = 0;
    private int currentEnergy = 0;
    private int totalXp = 0;
    private int overflowXp = 0;
    private int overload;
    public int tier;

    // Cache
    private boolean cacheDirty = true;
    private ModifierData cachedModifiers;
    private final List<CachedLoader> cachedLoaders = new ArrayList<>();
    private int cachedDuration = 0;
    private int cachedSize = 0;
    private int cachedMaxProcess = 1;
    private final int loaderSlots;

    //Extra
    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) { return saveWithoutMetadata(registries); }

    @Override
    protected List<IItemHandler> getDroppableHandlers() {
        return List.of(itemHandler, upgradeHandler);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        itemHandler.deserializeNBT(registries, tag.getCompound("inventory"));
        upgradeHandler.deserializeNBT(registries, tag.getCompound("upgrades"));
        this.process = tag.getInt("eternal_generator.process");
        this.maxProcess = tag.getInt("eternal_generator.max_process");
        this.energyHandler.receiveEnergy(tag.getInt("eternal_generator.energy"), false);
        this.currentEnergy = tag.getInt("eternal_generator.energy");
        this.totalXp = tag.getInt("eternal_generator.total_xp");
        this.overload = tag.getInt("eternal_generator.overload");
        this.overflowXp = tag.getInt("eternal_generator.overflow_xp");
        this.cacheDirty = true;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        tag.put("inventory", itemHandler.serializeNBT(registries));
        tag.put("upgrades", upgradeHandler.serializeNBT(registries));
        tag.putInt("eternal_generator.process", this.process);
        tag.putInt("eternal_generator.max_process", this.maxProcess);
        tag.putInt("eternal_generator.energy", this.energyHandler.getEnergyStored());
        tag.putInt("eternal_generator.total_xp", this.totalXp);
        tag.putInt("eternal_generator.overload", this.overload);
        tag.putInt("eternal_generator.overflow_xp", this.overflowXp);
        super.saveAdditional(tag, registries);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.minformax.eternal_generator");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new EternalGeneratorMenu(containerId, playerInventory, this, this.data);
    }

    //Utility
    private void resetProcess() {
        this.process = 0;
    }

    private void addXp(long value) {
        long result = (long) totalXp + value;
        if (result > Integer.MAX_VALUE) {
            overflowXp += (int) (result / Integer.MAX_VALUE);
            totalXp = (int) (result % Integer.MAX_VALUE);
        } else {
            totalXp = (int) result;
        }
    }

    private ModifierData getModifier() {
        int speedModifier = 1;
        int stackMultiplier = 1;
        int percentage = 0;
        boolean inverted = false;
        for(int i = 0; i < upgradeHandler.getSlots(); i++) {
            var stack = upgradeHandler.getStackInSlot(i);
            var upgrade = stack.getItem();

            if(upgrade instanceof SpeedUpgrade speedUpgrade) {
                speedModifier = speedUpgrade.getModifier();
            } else if(upgrade instanceof ProcessingUpgrade processingUpgrade) {
                stackMultiplier = processingUpgrade.getMultiplier();
            } else if(upgrade instanceof ExtraDropUpgrade extraDropUpgrade) {
                percentage = extraDropUpgrade.getPercentage();
            } else if(stack.is(ModItems.INVERTED_UPGRADE)) {
                inverted = true;
            }
        }
        return new ModifierData(speedModifier, stackMultiplier, percentage, inverted);
    }
    
    private void recalculateCache() {
        this.cachedModifiers = getModifier();
        this.cachedLoaders.clear();
        this.cachedDuration = 0;
        
        Item inferiumEssenceItem = null;
        if (BuiltInRegistries.ITEM.containsKey(INFERIUM_ESSENCE_RL)) {
            inferiumEssenceItem = BuiltInRegistries.ITEM.get(INFERIUM_ESSENCE_RL);
        }

        for(int i = 0; i < loaderSlots; i++) {
            var currentLoader = this.itemHandler.getStackInSlot(i);
            if(!currentLoader.isEmpty()) {
                long xp = 0;
                List<ItemStack> mainDrop = new ArrayList<>();
                List<ItemStack> additionalDrop = new ArrayList<>();
                boolean isShard = false;

                if(currentLoader.getItem() instanceof ModuleItem) {
                    this.cachedDuration += 1024;
                    var identifier = ModuleDropsReloadListener.rulesForModule(currentLoader.getItem());
                    xp = identifier.xp();
                    for (ItemStack s : ModuleDropsReloadListener.mainDropsFromModule(currentLoader.getItem())) {
                        mainDrop.add(s.copy());
                    }
                } else if(currentLoader.getItem() instanceof AccShard) {
                    var key = currentLoader.get(ModDataComponents.MOB_INDEX);
                    var index = ModDataReloadListener.MOB_DROPS;
                    if(index.containsKey(key)) {
                        var loot = index.get(key);
                        this.cachedDuration += loot.duration();
                        mainDrop.add(loot.mainDrop().copy());
                        for(ItemStack s : loot.additionalDrop()) {
                            additionalDrop.add(s.copy());
                        }
                        xp = loot.xp();
                        isShard = true;
                    }
                    if (inferiumEssenceItem != null) {
                        mainDrop.add(new ItemStack(inferiumEssenceItem));
                    }
                }
                this.cachedLoaders.add(new CachedLoader(xp, mainDrop, additionalDrop, isShard));
            }
        }
        this.cachedSize = this.cachedLoaders.size();
        
        if (this.cachedSize > 0) {
            this.cachedMaxProcess = Math.max((this.cachedDuration / this.cachedModifiers.speedModifier / this.cachedSize), 1);
        } else {
            this.cachedMaxProcess = 1;
        }
        
        this.cacheDirty = false;
    }

    public void consumeOverload(int value) {
        this.overload -= value;
    }

    public boolean canConsumeOverload() {
        return this.overload >= 5120;
    }

    public int getOverload() {return this.overload;}

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
    public void tick(Level level, BlockPos blockPos, BlockState blockState, EternalGeneratorBlockEntity blockEntity) {
        if(level.isClientSide()) return;
        
        boolean dirty = false;
        
        // Update energy and check for change
        int newEnergy = energyHandler.getEnergyStored();
        if (newEnergy != this.currentEnergy) {
            this.currentEnergy = newEnergy;
            dirty = true;
        }
        
        if (this.cacheDirty) {
            recalculateCache();
        }

        var modifiers = this.cachedModifiers;
        var size = this.cachedSize;

        if(size > 0) {
            int effectiveSpeed = Math.min(modifiers.speedModifier, this.cachedDuration / size);
            var requiredEnergy = modifiers.operationMultiplier * effectiveSpeed * DataConfigs.mobCoefficient.get() * size;
            if (modifiers.speedModifier == 9999) {
                requiredEnergy = 0;
            }
            
            if(currentEnergy >= requiredEnergy) {
                energyHandler.extractEnergy(requiredEnergy, false);
                this.currentEnergy = energyHandler.getEnergyStored();
                dirty = true;

                process++;
                maxProcess = this.cachedMaxProcess;

                if(process >= maxProcess) {
                    List<ItemStack> dropsToDistribute = new ArrayList<>();
                    long xpToAdd = 0;

                    for(var loader : this.cachedLoaders) {
                        xpToAdd += loader.xp * modifiers.operationMultiplier;

                        List<ItemStack> mainDropProcessed = new ArrayList<>();
                        for (ItemStack stack : loader.mainDrops) {
                            ItemStack copy = stack.copy();
                            copy.setCount(modifiers.operationMultiplier());
                            mainDropProcessed.add(copy);
                        }

                        List<ItemStack> additionalDropProcessed = new ArrayList<>();
                        for (ItemStack stack : loader.additionalDrops) {
                            ItemStack copy = stack.copy();
                            copy.setCount(modifiers.operationMultiplier());
                            additionalDropProcessed.add(copy);
                        }

                        if (loader.isShard && modifiers.inverted()) {
                            List<ItemStack> temp = mainDropProcessed;
                            mainDropProcessed = additionalDropProcessed;
                            additionalDropProcessed = temp;
                        }

                        dropsToDistribute.addAll(mainDropProcessed);

                        for (ItemStack extra : additionalDropProcessed) {
                            if (Math.random() * 100 < modifiers.extraDropPercentage()) {
                                dropsToDistribute.add(extra);
                            }
                        }
                    }

                    if (xpToAdd > 0) {
                        this.addXp(xpToAdd);
                    }

                    dropsToDistribute = mergeStacks(dropsToDistribute);

                    for (ItemStack stack : dropsToDistribute) {
                        ItemStack remaining = Utils.moveItem(level, blockPos, stack);
                        if (!remaining.isEmpty()) {
                            this.overload = (int) Math.min((long) this.overload + remaining.getCount(), Integer.MAX_VALUE);
                        }
                    }

                    resetProcess();
                }
            }
        } else {
            if (process != 0) {
                resetProcess();
                dirty = true;
            }
        }

        if (dirty) {
            setChanged(level, blockPos, blockState);
        }
    }

    private record ModifierData(int speedModifier, int operationMultiplier, int extraDropPercentage, boolean inverted) {}
    private record CachedLoader(long xp, List<ItemStack> mainDrops, List<ItemStack> additionalDrops, boolean isShard) {}
}
