package rewqazwas.minformax.custom.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;
import rewqazwas.minformax.custom.ModBlockEntities;
import rewqazwas.minformax.custom.component.ModDataComponents;
import rewqazwas.minformax.custom.index.ModDataReloadListener;
import rewqazwas.minformax.custom.index.ModuleDropsReloadListener;
import rewqazwas.minformax.custom.items.BossModuleItem;
import rewqazwas.minformax.custom.items.MemoryShard;
import rewqazwas.minformax.custom.items.ModuleItem;
import rewqazwas.minformax.custom.items.upgrades.*;
import rewqazwas.minformax.screen.custom.EternalGeneratorMenu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class EternalGeneratorBlockEntity extends BlockEntity implements MenuProvider {

    public ItemStackHandler itemHandler = new ItemStackHandler(8) {
        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }
    };
    public final ItemStackHandler upgradeHandler = new ItemStackHandler(4) {
        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stack.getItem() instanceof UpgradeItem;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            var size = this.getSlots();
            for (int i = 0; i < size; i++) {
                var filter = this.getStackInSlot(i).getItem();
                if(filter == stack.getItem()) {
                    return stack;
                }
            }
            return super.insertItem(slot, stack, simulate);
        }
    };
    public final EnergyStorage energyHandler = new EnergyStorage(1000000) {
        @Override
        public int receiveEnergy(int toReceive, boolean simulate) {
            return super.receiveEnergy(toReceive, simulate);
        }
    };

    protected final ContainerData data;
    private int process = 0;
    private int maxProcess = 0;
    private int currentEnergy = 0;
    private int totalXp = 0;
    private int overload;
    public int tier;


    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) { return saveWithoutMetadata(registries); }

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
                    default -> 0;
                };
            }

            @Override
            public void set(int i, int value) {
                switch (i) {
                    case 0: EternalGeneratorBlockEntity.this.process = value;
                    case 1: EternalGeneratorBlockEntity.this.maxProcess = value;
                    case 2: EternalGeneratorBlockEntity.this.currentEnergy = value;
                    case 3: EternalGeneratorBlockEntity.this.totalXp = value;
                    case 4: EternalGeneratorBlockEntity.this.overload = value;
                }
            }


            @Override
            public int getCount() {
                return 5;
            }
        };
    }


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

    private void resetProcess() {
        this.process = 0;
        setChanged();
    }

    private void moveItems(IItemHandler[] sides, ModifierData modifiers, List<ItemStack> mainDrop, List<ItemStack> additionalDrop, boolean isShard) {
        mainDrop.forEach(stack -> stack.setCount(modifiers.operationMultiplier));
        additionalDrop.forEach(extra -> extra.setCount(modifiers.operationMultiplier));
        List<IItemHandler> filteredSides = new ArrayList<>();
        if(isShard && modifiers.inverted) {
            var temp = mainDrop;
            mainDrop = additionalDrop;
            additionalDrop = temp;
        }
        Arrays.stream(sides).filter(Objects::nonNull).forEach(filteredSides::add);

        if(!filteredSides.isEmpty()) {
            for(ItemStack main: mainDrop) {
                var remainder = ItemHandlerHelper.insertItemStacked(filteredSides.getFirst(), main, false);
                overload += remainder.getCount();
            }
            for(ItemStack extra: additionalDrop) {
                if(Math.random() * 100 < modifiers.extraDropPercentage){
                    var remainder = ItemHandlerHelper.insertItemStacked(filteredSides.getFirst(), extra, false);
                    overload += remainder.getCount();
                }
            }
        } else {
            overload += mainDrop.size() * modifiers.operationMultiplier + additionalDrop.size() * modifiers.operationMultiplier;
        }
    }

    private IItemHandler[] getSides(Level level, BlockPos blockPos) {
        return new IItemHandler[]{
                level.getCapability(Capabilities.ItemHandler.BLOCK, blockPos.above(), Direction.DOWN),
                level.getCapability(Capabilities.ItemHandler.BLOCK, blockPos.below(), Direction.UP),
                level.getCapability(Capabilities.ItemHandler.BLOCK, blockPos.north(), Direction.SOUTH),
                level.getCapability(Capabilities.ItemHandler.BLOCK, blockPos.south(), Direction.NORTH),
                level.getCapability(Capabilities.ItemHandler.BLOCK, blockPos.east(), Direction.WEST),
                level.getCapability(Capabilities.ItemHandler.BLOCK, blockPos.west(), Direction.EAST)
        };
    }

    private ModifierData getModifier() {
        int speedModifier = 1;
        int stackMultiplier = 1;
        int percentage = 0;
        boolean inverted = false;
        boolean isStrong = false;
        for(int i = 0; i < upgradeHandler.getSlots(); i++) {
            var upgrade = upgradeHandler.getStackInSlot(i).getItem();

            if(upgrade instanceof SpeedUpgrade speedUpgrade) {
                speedModifier = speedUpgrade.getModifier();
            } else if(upgrade instanceof StackUpgrade stackUpgrade) {
                stackMultiplier = stackUpgrade.getMultiplier();
            } else if(upgrade instanceof ExtraDropUpgrade extraDropUpgrade) {
                percentage = extraDropUpgrade.getPercentage();
            } else if(upgrade instanceof InvertedUpgrade) {
                inverted = true;
            } else if(upgrade instanceof StrengthUpgrade) {
                isStrong = true;
            }
        }
        return new ModifierData(speedModifier, stackMultiplier, percentage, inverted, isStrong);
    }

    public void consumeOverload() {
        this.overload -= 5120;
    }

    public boolean canConsumeOverload() {
        return this.overload >= 5120;
    }

    public void tick(Level level, BlockPos blockPos, BlockState blockState, EternalGeneratorBlockEntity blockEntity) {
        if(level.isClientSide()) return;
        this.currentEnergy = energyHandler.getEnergyStored();

        var modifiers = getModifier();
        List<ItemStack> loaders = new ArrayList<>();
        var duration = 0;
        var pass = modifiers.isStrong();

        for(int i = 0; i < Math.pow(2, tier - 1); i++) {
            var currentLoader = blockEntity.itemHandler.getStackInSlot(i);
            if(!currentLoader.isEmpty()) {
                if(currentLoader.getItem() instanceof ModuleItem) {
                    duration += 1024;
                } else if(currentLoader.getItem() instanceof MemoryShard) {
                    var key = currentLoader.get(ModDataComponents.MOB_INDEX);
                    var index = ModDataReloadListener.MOB_DROPS;
                    if(index.containsKey(key)) {
                        var loot = index.get(key);
                        duration += loot.duration();
                    }
                } else if(currentLoader.getItem() instanceof BossModuleItem && pass) {
                    duration += 2048;
                }
                if(pass || !(currentLoader.getItem() instanceof BossModuleItem)) {
                    loaders.add(currentLoader);
                }
            }
        }

        var size = loaders.size();

        if(size > 0) {
            var requiredEnergy = modifiers.operationMultiplier * modifiers.speedModifier * 4 * size;
            if(currentEnergy < requiredEnergy) return;
            energyHandler.extractEnergy(requiredEnergy, false);

            process++;
            maxProcess = Math.max((duration / modifiers.speedModifier / size), 1);
            setChanged(level, blockPos, blockState);

            if(process >= maxProcess) {
                var isShard = false;
                for(var loader: loaders) {
                    var loaderItem = loader.getItem();
                    List<ItemStack> mainDrop = List.of();
                    List<ItemStack> additionalDrop = List.of();
                    if(loaderItem instanceof ModuleItem) {
                        var identifier = ModuleDropsReloadListener.rulesForModule(loaderItem);
                        this.totalXp += identifier.xp() * modifiers.operationMultiplier;
                        mainDrop = ModuleDropsReloadListener.mainDropsFromModule(loaderItem);
                    } else if(loaderItem instanceof MemoryShard) {
                        var key = loader.get(ModDataComponents.MOB_INDEX);
                        var index = ModDataReloadListener.MOB_DROPS;
                        if(index.containsKey(key)) {
                            var loot = index.get(key);
                            mainDrop = List.of(loot.mainDrop());
                            additionalDrop = loot.additionalDrop();
                            this.totalXp += loot.xp() * modifiers.operationMultiplier;
                            isShard = true;
                        }
                    }
                    var sides = getSides(level, blockPos);
                    moveItems(sides, modifiers, mainDrop, additionalDrop, isShard);
                    setChanged(level, blockPos, blockState);
                }
                resetProcess();
            }
        } else {
            resetProcess();
        }
        setChanged(level, blockPos, blockState);
    }

    private record ModifierData(int speedModifier, int operationMultiplier, int extraDropPercentage, boolean inverted, boolean isStrong) {}
}
