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
import net.neoforged.neoforge.items.ItemStackHandler;
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
    public EternalGeneratorBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.ETERNAL_GENERATOR_BE.get(), pos, blockState);
        EternalGeneratorBlock block = (EternalGeneratorBlock) blockState.getBlock();
        this.tier = block.tier;

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
    public Utils.SingleItemHandler itemHandler = new Utils.SingleItemHandler(8);

    public final Utils.UpgradeItemHandler upgradeHandler = new Utils.UpgradeItemHandler(4) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stack.is(ModTags.EXTRA_DROP_UPGRADES)
                    || stack.is(ModItems.INVERTED_UPGRADE)
                    || super.isItemValid(slot, stack);
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

    //Extra
    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) { return saveWithoutMetadata(registries); }


    public void drops() {
        SimpleContainer inv = new SimpleContainer(itemHandler.getSlots() + upgradeHandler.getSlots());
        for(int i = 0; i < itemHandler.getSlots(); i++){
            inv.setItem(i, itemHandler.getStackInSlot(i));
        }
        for(int i = 0; i < upgradeHandler.getSlots(); i++){
            inv.setItem(i + itemHandler.getSlots(), upgradeHandler.getStackInSlot(i));
        }
        Containers.dropContents(this.level, this.worldPosition, inv);
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
        setChanged();
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

    private void moveItems(Level level, BlockPos pos, ModifierData modifiers, List<ItemStack> mainDrop, List<ItemStack> additionalDrop, boolean isShard) {
        List<ItemStack> mainDropProcessed = new ArrayList<>();
        for (ItemStack stack : mainDrop) {
            ItemStack copy = stack.copy();
            copy.setCount(modifiers.operationMultiplier());
            mainDropProcessed.add(copy);
        }

        List<ItemStack> additionalDropProcessed = new ArrayList<>();
        for (ItemStack stack : additionalDrop) {
            ItemStack copy = stack.copy();
            copy.setCount(modifiers.operationMultiplier());
            additionalDropProcessed.add(copy);
        }

        if (isShard && modifiers.inverted()) {
            List<ItemStack> temp = mainDropProcessed;
            mainDropProcessed = additionalDropProcessed;
            additionalDropProcessed = temp;
        }

        // Process Main Drops
        for (ItemStack main : mainDropProcessed) {
            ItemStack remaining = Utils.moveItem(level, pos, main);
            if (!remaining.isEmpty()) {
                this.overload = (int) Math.min((long) this.overload + remaining.getCount(), Integer.MAX_VALUE);
            }
        }

        // Process Additional Drops
        for (ItemStack extra : additionalDropProcessed) {
            if (Math.random() * 100 < modifiers.extraDropPercentage()) {
                ItemStack remaining = Utils.moveItem(level, pos, extra);
                if (!remaining.isEmpty()) {
                    this.overload = (int) Math.min((long) this.overload + remaining.getCount(), Integer.MAX_VALUE);
                }
            }
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

    public void consumeOverload(int value) {
        this.overload -= value;
    }

    public boolean canConsumeOverload() {
        return this.overload >= 5120;
    }

    public int getOverload() {return this.overload;}

    //Main
    public void tick(Level level, BlockPos blockPos, BlockState blockState, EternalGeneratorBlockEntity blockEntity) {
        if(level.isClientSide()) return;
        this.currentEnergy = energyHandler.getEnergyStored();

        var modifiers = getModifier();
        List<ItemStack> loaders = new ArrayList<>();
        var duration = 0;

        //Gather data from all loaders
        for(int i = 0; i < Math.pow(2, tier - 1); i++) {
            var currentLoader = blockEntity.itemHandler.getStackInSlot(i);
            if(!currentLoader.isEmpty()) {
                if(currentLoader.getItem() instanceof ModuleItem) {
                    duration += 1024;
                } else if(currentLoader.getItem() instanceof AccShard) {
                    var key = currentLoader.get(ModDataComponents.MOB_INDEX);
                    var index = ModDataReloadListener.MOB_DROPS;
                    if(index.containsKey(key)) {
                        var loot = index.get(key);
                        duration += loot.duration();
                    }
                }
                loaders.add(currentLoader);
            }
        }

        var size = loaders.size();

        if(size > 0) {
            int effectiveSpeed = Math.min(modifiers.speedModifier, duration / size);
            var requiredEnergy = modifiers.operationMultiplier * effectiveSpeed * DataConfigs.mobCoefficient.get() * size;
            if (modifiers.speedModifier == 9999) {
                requiredEnergy = 0;
            }
            if(currentEnergy < requiredEnergy) return;
            energyHandler.extractEnergy(requiredEnergy, false);

            process++;
            maxProcess = Math.max((duration / modifiers.speedModifier / size), 1);
            setChanged(level, blockPos, blockState);

            if(process >= maxProcess) {
                var isShard = false;
                for(var loader: loaders) {
                    var loaderItem = loader.getItem();
                    List<ItemStack> mainDrop = new ArrayList<>();
                    List<ItemStack> additionalDrop = new ArrayList<>();
                    if(loaderItem instanceof ModuleItem) {
                        var identifier = ModuleDropsReloadListener.rulesForModule(loaderItem);
                        this.addXp((long) identifier.xp() * modifiers.operationMultiplier);
                        mainDrop.addAll(ModuleDropsReloadListener.mainDropsFromModule(loaderItem));
                    } else if(loaderItem instanceof AccShard) {
                        var key = loader.get(ModDataComponents.MOB_INDEX);
                        var index = ModDataReloadListener.MOB_DROPS;
                        if(index.containsKey(key)) {
                            var loot = index.get(key);
                            mainDrop.add(loot.mainDrop());
                            additionalDrop.addAll(loot.additionalDrop());
                            this.addXp((long) loot.xp() * modifiers.operationMultiplier);
                            isShard = true;
                        }
                        ResourceLocation inferiumEssenceRL = ResourceLocation.parse("mysticalagriculture:inferium_essence");
                        if (BuiltInRegistries.ITEM.containsKey(inferiumEssenceRL)) {
                            Item inferiumEssenceItem = BuiltInRegistries.ITEM.get(inferiumEssenceRL);
                            mainDrop.add(new ItemStack(inferiumEssenceItem, modifiers.operationMultiplier));
                        }
                    }

                    moveItems(level, blockPos, modifiers, mainDrop, additionalDrop, isShard);
                    setChanged(level, blockPos, blockState);
                }
                resetProcess();
            }
        } else {
            resetProcess();
        }
        setChanged(level, blockPos, blockState);
    }

    private record ModifierData(int speedModifier, int operationMultiplier, int extraDropPercentage, boolean inverted) {}
}
