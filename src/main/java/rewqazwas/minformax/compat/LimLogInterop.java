package rewqazwas.minformax.compat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import rewqazwas.limlog.api.LimLogAPI;
import rewqazwas.limlog.api.ILongEnergyStorage;
import rewqazwas.minformax.custom.utility.Utils;

public class LimLogInterop {

    /**
     * Attempts to find LimLog's LONG_ENERGY_BLOCK capability and transfer energy into it.
     * Returns true if energy was successfully transferred.
     */
    public static boolean tryPushLimLogEnergy(Level level, BlockPos targetPos, Direction targetSide, Utils.EnergyGenStorage energyHandler) {
        ILongEnergyStorage longStorage = level.getCapability(LimLogAPI.LONG_ENERGY_BLOCK, targetPos, targetSide);
        if (longStorage != null) {
            long remaining = energyHandler.getLongEnergyStored();
            long accepted = longStorage.insertEnergyLong(remaining, false);
            if (accepted > 0) {
                energyHandler.setEnergy(remaining - accepted);
                return true;
            }
        }
        return false;
    }
}