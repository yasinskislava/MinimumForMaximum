package rewqazwas.minformax.custom.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;
import rewqazwas.minformax.custom.ModBlockEntities;
import rewqazwas.minformax.custom.index.FluidReplicatorData;
import rewqazwas.minformax.custom.index.ModDataReloadListener;
import rewqazwas.minformax.custom.items.upgrades.ProcessingUpgrade;
import rewqazwas.minformax.custom.items.upgrades.SpeedUpgrade;
import rewqazwas.minformax.custom.utility.Utils;

import java.util.List;


public class FluidReplicatorBlockEntity extends MachineBaseEntity {
    public FluidReplicatorBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.FLUID_REPLICATOR_BE.get(), pos, blockState);
        this.enabledSides[Direction.DOWN.get3DDataValue()] = true;
        this.enabledSides[Direction.UP.get3DDataValue()] = true;
        this.enabledSides[Direction.NORTH.get3DDataValue()] = true;
        this.enabledSides[Direction.SOUTH.get3DDataValue()] = true;
        this.enabledSides[Direction.EAST.get3DDataValue()] = true;
        this.enabledSides[Direction.WEST.get3DDataValue()] = true;
    }
    //Handlers

    public final FluidTank fluidHandler = new FluidTank(1000) {
        @Override
        protected void onContentsChanged() {
            cacheDirty = true;
            setChanged();
            if(level != null && !level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }

        @Override
        public boolean isFluidValid(FluidStack stack) {
            var key = BuiltInRegistries.FLUID.getKey(stack.getFluid()).toString();
            return ModDataReloadListener.FLUID_REPLICATOR_DATA.containsKey(key);
        }
    };

    public final Utils.UpgradeItemHandler upgradeHandler = new Utils.UpgradeItemHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            cacheDirty = true;
            setChanged();
        }
    };

    public final EnergyStorage energyHandler = new EnergyStorage(40_960_000);

    //Variables
    private int process = 0;
    private int maxProcess = 256;
    
    // Cache
    private boolean cacheDirty = true;
    private FluidReplicatorData cachedData;
    private int cachedSpeedModifier = 1;
    private int cachedStackMultiplier = 1;

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
        return List.of(upgradeHandler);
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
        this.cacheDirty = true;
    }

    private void recalculateCache() {
        var sourceStack = fluidHandler.getFluid();
        if (!sourceStack.isEmpty()) {
            var key = BuiltInRegistries.FLUID.getKey(sourceStack.getFluid()).toString();
            this.cachedData = ModDataReloadListener.FLUID_REPLICATOR_DATA.getOrDefault(key, null);
        } else {
            this.cachedData = null;
        }

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
        this.cachedSpeedModifier = speedModifier;
        this.cachedStackMultiplier = stackMultiplier;
        
        this.cacheDirty = false;
    }

    //Main
    public void tick(Level level, BlockPos blockPos, BlockState blockState, FluidReplicatorBlockEntity blockEntity) {
        if(level.isClientSide()) return;
        
        if (this.cacheDirty) {
            recalculateCache();
        }

        if (this.cachedData == null) {
            if (process != 0) {
                process = 0;
                setChanged(level, blockPos, blockState);
            }
            return;
        }

        var sourceStack = fluidHandler.getFluid();
        if (sourceStack.isEmpty() || sourceStack.getAmount() < fluidHandler.getCapacity()) {
             return;
        }

        boolean dirty = false;
        
        int speedModifier = this.cachedSpeedModifier;
        int stackMultiplier = this.cachedStackMultiplier;
        
        int effectiveSpeed = Math.min(speedModifier, this.cachedData.duration());
        int energyCost = this.cachedData.energyMultiplier() * effectiveSpeed * stackMultiplier;
        
        if (speedModifier == 9999) {
            energyCost = 0;
        }
        
        int currentEnergy = energyHandler.getEnergyStored();
        if (currentEnergy < energyCost) return;
        
        if (!Utils.canInsertAtLeastOneNetwork(level, blockPos, sourceStack, this.enabledSides)) return;

        energyHandler.extractEnergy(energyCost, false);
        if (energyHandler.getEnergyStored() != currentEnergy) {
            dirty = true;
        }

        process++;
        maxProcess = this.cachedData.duration();
        dirty = true;

        if (process >= Math.max(1, maxProcess / speedModifier)) {
            int totalToGenerate = this.cachedData.basicAmountGenerated() * stackMultiplier;
            Utils.moveFluidNetwork(level, blockPos, sourceStack, totalToGenerate, this.enabledSides);
            process = 0;
        }
        
        if (dirty) {
            setChanged(level, blockPos, blockState);
        }
    }
}
