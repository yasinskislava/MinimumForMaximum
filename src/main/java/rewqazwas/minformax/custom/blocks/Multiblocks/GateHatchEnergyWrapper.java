package rewqazwas.minformax.custom.blocks.Multiblocks;

import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import rewqazwas.limlog.api.ILongEnergyStorage;
import rewqazwas.minformax.custom.blocks.ModBlocks;

public class GateHatchEnergyWrapper implements IEnergyStorage, ILongEnergyStorage {
    private final GateOfBabylonBlockEntity master;
    private final boolean isInput;

    public GateHatchEnergyWrapper(GateOfBabylonBlockEntity master, BlockState state) {
        this.master = master;
        this.isInput = state.is(ModBlocks.SAKURADITE_INPUT.get());
    }

    // --- IEnergyStorage (Standard NeoForge) ---
    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        return isInput ? master.energyHandler.receiveEnergy(maxReceive, simulate) : 0;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return !isInput ? master.energyHandler.extractEnergy(maxExtract, simulate) : 0;
    }

    @Override
    public int getEnergyStored() {
        return master.energyHandler.getEnergyStored();
    }

    @Override
    public int getMaxEnergyStored() {
        return master.energyHandler.getMaxEnergyStored();
    }

    @Override
    public boolean canExtract() {
        return !isInput;
    }

    @Override
    public boolean canReceive() {
        return isInput;
    }

    // --- ILongEnergyStorage (LimLog API) ---
    @Override
    public long insertEnergyLong(long amount, boolean simulate) {
        if (!isInput || amount <= 0) return 0;
        long stored = master.energyHandler.getLongEnergyStored();
        long space = master.energyHandler.getMaxCapacityLong() - stored;
        long toInsert = Math.min(space, amount);
        if (!simulate && toInsert > 0) {
            master.energyHandler.setEnergy(stored + toInsert);
            master.setChanged();
        }
        return toInsert;
    }

    @Override
    public long extractEnergyLong(long amount, boolean simulate) {
        if (isInput || amount <= 0) return 0;
        long stored = master.energyHandler.getLongEnergyStored();
        long toExtract = Math.min(stored, amount);
        if (!simulate && toExtract > 0) {
            master.energyHandler.setEnergy(stored - toExtract);
            master.setChanged();
        }
        return toExtract;
    }

    @Override
    public long getEnergyStoredLong() {
        return master.energyHandler.getLongEnergyStored();
    }

    @Override
    public long getMaxEnergyStoredLong() {
        return master.energyHandler.getMaxCapacityLong();
    }
}