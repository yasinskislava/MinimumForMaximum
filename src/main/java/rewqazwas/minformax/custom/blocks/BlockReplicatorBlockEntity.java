package rewqazwas.minformax.custom.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.BlockItem;
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
import rewqazwas.minformax.custom.index.ModDataReloadListener;
import rewqazwas.minformax.custom.items.upgrades.SpeedUpgrade;
import rewqazwas.minformax.custom.items.upgrades.StackUpgrade;
import rewqazwas.minformax.custom.items.upgrades.UpgradeItem;

import static rewqazwas.minformax.custom.utility.Utils.getItemHandlers;

public class BlockReplicatorBlockEntity extends BlockEntity {
    public final ItemStackHandler upgradeHandler = new ItemStackHandler(2) {
        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stack.getItem() instanceof SpeedUpgrade || stack.getItem() instanceof StackUpgrade;
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

    public final ItemStackHandler itemHandler = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if(!level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (!(stack.getItem() instanceof BlockItem)) return false;
            var key = BuiltInRegistries.BLOCK.getKey(((BlockItem) stack.getItem()).getBlock()).toString();
            return ModDataReloadListener.BLOCK_REPLICATOR_DATA.containsKey(key);
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        protected int getStackLimit(int slot, ItemStack stack) {
            return 1;
        }
    };

    public final EnergyStorage energyHandler = new EnergyStorage(500000) {
        @Override
        public int receiveEnergy(int toReceive, boolean simulate) {
            setChanged();
            return super.receiveEnergy(toReceive, simulate);
        }

        @Override
        public int extractEnergy(int toExtract, boolean simulate) {
            setChanged();
            return super.extractEnergy(toExtract, simulate);
        }
    };

    private int process = 0;
    private int maxProcess = 256;

    public BlockReplicatorBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.BLOCK_REPLICATOR_BE.get(), pos, blockState);
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    public void drops() {
        SimpleContainer inv = new SimpleContainer(upgradeHandler.getSlots() + itemHandler.getSlots());
        for(int i = 0; i < upgradeHandler.getSlots(); i++){
            inv.setItem(i, upgradeHandler.getStackInSlot(i));
        }
        for(int i = 0; i < itemHandler.getSlots(); i++){
            inv.setItem(i + upgradeHandler.getSlots(), itemHandler.getStackInSlot(i));
        }
        Containers.dropContents(this.level, this.worldPosition, inv);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", itemHandler.serializeNBT(registries));
        tag.put("upgrades", upgradeHandler.serializeNBT(registries));
        tag.putInt("block_replicator.process", this.process);
        tag.putInt("block_replicator.max_process", this.maxProcess);
        tag.putInt("block_replicator.energy", this.energyHandler.getEnergyStored());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        itemHandler.deserializeNBT(registries, tag.getCompound("inventory"));
        upgradeHandler.deserializeNBT(registries, tag.getCompound("upgrades"));
        this.process = tag.getInt("block_replicator.process");
        this.maxProcess = tag.getInt("block_replicator.max_process");
        this.energyHandler.receiveEnergy(tag.getInt("block_replicator.energy"), false);
    }

    public void tick(Level level, BlockPos blockPos, BlockState blockState, BlockReplicatorBlockEntity blockEntity) {
        if(level.isClientSide()) return;

        var sourceStack = itemHandler.getStackInSlot(0);
        if (sourceStack.isEmpty() || !(sourceStack.getItem() instanceof BlockItem)) return;

        var key = BuiltInRegistries.BLOCK.getKey(((BlockItem) sourceStack.getItem()).getBlock()).toString();
        if (!ModDataReloadListener.BLOCK_REPLICATOR_DATA.containsKey(key)) return;
        var data = ModDataReloadListener.BLOCK_REPLICATOR_DATA.get(key);

        int speedModifier = 1;
        int stackMultiplier = 1;

        for(int i = 0; i < upgradeHandler.getSlots(); i++) {
            var upgrade = upgradeHandler.getStackInSlot(i).getItem();
            if(upgrade instanceof SpeedUpgrade speedUpgrade) {
                speedModifier = speedUpgrade.getModifier();
            } else if(upgrade instanceof StackUpgrade stackUpgrade) {
                stackMultiplier = stackUpgrade.getMultiplier();
            }
        }

        int effectiveSpeed = Math.min(speedModifier, data.duration());
        int energyCost = data.energyMultiplier() * effectiveSpeed * stackMultiplier;
        if (speedModifier == 9999) {
            energyCost = 0;
        }
        if (energyHandler.getEnergyStored() < energyCost) return;
        energyHandler.extractEnergy(energyCost, false);

        process++;
        maxProcess = data.duration();

        if(process >= maxProcess / speedModifier) {
            var amount = data.basicAmountGenerated() * stackMultiplier;
            var toFill = sourceStack.copy();
            toFill.setCount(amount);
            
            for(IItemHandler side : getItemHandlers(level, blockPos)) {
                if(side != null) {
                    var remainder = ItemHandlerHelper.insertItemStacked(side, toFill, false);
                    toFill = remainder;
                    if(toFill.isEmpty()) break;
                }
            }
            process = 0;
        }
        setChanged(level, blockPos, blockState);
    }
}
