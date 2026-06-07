package rewqazwas.minformax.custom.blocks.PandoraBox;

import rewqazwas.limlog.api.ILongEnergyStorage;

public class PandoraCoreEnergyWrapper implements ILongEnergyStorage {
    private final PandoraBoxCoreBlockEntity core;

    public PandoraCoreEnergyWrapper(PandoraBoxCoreBlockEntity core) {
        this.core = core;
    }

    @Override
    public long insertEnergyLong(long amount, boolean simulate) {
        return 0;
    }

    @Override
    public long extractEnergyLong(long amount, boolean simulate) {
        long stored = core.energyHandler.getLongEnergyStored();
        long toExtract = Math.min(stored, amount);
        if (!simulate && toExtract > 0) {
            core.energyHandler.setEnergy(stored - toExtract);
            core.setChanged();
        }
        return toExtract;
    }

    @Override
    public long getEnergyStoredLong() {
        return core.energyHandler.getLongEnergyStored();
    }

    @Override
    public long getMaxEnergyStoredLong() {
        return core.energyHandler.getMaxCapacityLong();
    }
}