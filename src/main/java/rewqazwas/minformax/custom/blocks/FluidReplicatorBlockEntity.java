package rewqazwas.minformax.custom.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;
import rewqazwas.minformax.custom.ModBlockEntities;
import rewqazwas.minformax.custom.index.ModDataReloadListener;
import rewqazwas.minformax.custom.items.upgrades.SpeedUpgrade;
import rewqazwas.minformax.custom.items.upgrades.ProcessingUpgrade;

import static rewqazwas.minformax.custom.utility.Utils.getFluidHandlers;


public class FluidReplicatorBlockEntity extends BlockEntity {
    public final FluidTank fluidHandler = new FluidTank(1000) {
        @Override
        protected void onContentsChanged() {
            setChanged();
            if(!level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }

        @Override
        public boolean isFluidValid(FluidStack stack) {
            var key = BuiltInRegistries.FLUID.getKey(stack.getFluid()).toString();
            return ModDataReloadListener.FLUID_REPLICATOR_DATA.containsKey(key);
        }
    };

    public final ItemStackHandler upgradeHandler = new ItemStackHandler(2) {
        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stack.getItem() instanceof SpeedUpgrade || stack.getItem() instanceof ProcessingUpgrade;
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

    public final EnergyStorage energyHandler = new EnergyStorage(500000) {};

    private int process = 0;
    private int maxProcess = 256;

    public FluidReplicatorBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.FLUID_REPLICATOR_BE.get(), pos, blockState);
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
        SimpleContainer inv = new SimpleContainer(upgradeHandler.getSlots());
        for(int i = 0; i < upgradeHandler.getSlots(); i++){
            inv.setItem(i, upgradeHandler.getStackInSlot(i));
        }
        Containers.dropContents(this.level, this.worldPosition, inv);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("fluid_tank", fluidHandler.writeToNBT(registries, new CompoundTag()));
        tag.put("upgrades", upgradeHandler.serializeNBT(registries));
        tag.putInt("fluid_replicator.process", this.process);
        tag.putInt("fluid_replicator.max_process", this.maxProcess);
        tag.putInt("fluid_replicator.energy", this.energyHandler.getEnergyStored());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        fluidHandler.readFromNBT(registries, tag.getCompound("fluid_tank"));
        upgradeHandler.deserializeNBT(registries, tag.getCompound("upgrades"));
        this.process = tag.getInt("fluid_replicator.process");
        this.maxProcess = tag.getInt("fluid_replicator.max_process");
        this.energyHandler.receiveEnergy(tag.getInt("fluid_replicator.energy"), false);
    }

    public void tick(Level level, BlockPos blockPos, BlockState blockState, FluidReplicatorBlockEntity blockEntity) {
        if(level.isClientSide()) return;

        var sourceStack = fluidHandler.getFluid();
        if (sourceStack.isEmpty()) return;

        if (sourceStack.getAmount() < fluidHandler.getCapacity()) return;

        var key = BuiltInRegistries.FLUID.getKey(sourceStack.getFluid()).toString();
        if (!ModDataReloadListener.FLUID_REPLICATOR_DATA.containsKey(key)) return;
        var data = ModDataReloadListener.FLUID_REPLICATOR_DATA.get(key);

        int speedModifier = 1;
        int stackMultiplier = 1;

        for(int i = 0; i < upgradeHandler.getSlots(); i++) {
            var upgrade = upgradeHandler.getStackInSlot(i).getItem();
            if(upgrade instanceof SpeedUpgrade speedUpgrade) {
                speedModifier = speedUpgrade.getModifier();
            } else if(upgrade instanceof ProcessingUpgrade processingUpgrade) {
                stackMultiplier = processingUpgrade.getMultiplier();
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
            for(IFluidHandler side : getFluidHandlers(level, blockPos)) {
                if(side != null) {
                    toFill.setAmount(Math.min(amount, sourceStack.getAmount()));
                    amount -= side.fill(toFill, IFluidHandler.FluidAction.EXECUTE);
                    if(amount <= 0) break;
                }
            }
            process = 0;
        }
        setChanged(level, blockPos, blockState);
    }
}
