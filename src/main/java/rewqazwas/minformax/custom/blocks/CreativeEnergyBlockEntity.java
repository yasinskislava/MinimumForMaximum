package rewqazwas.minformax.custom.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import rewqazwas.minformax.custom.ModBlockEntities;
import rewqazwas.minformax.custom.utility.Utils;


public class CreativeEnergyBlockEntity extends BlockEntity {
    public CreativeEnergyBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.CREATIVE_ENERGY_BE.get(), pos, blockState);
        energyHandler.receiveEnergy(energyHandler.getMaxEnergyStored(), false);
    }
    //Handlers

    public final EnergyStorage energyHandler = new EnergyStorage(1000_000_000);

    //Main
    public void tick(Level level, BlockPos blockPos, BlockState blockState, CreativeEnergyBlockEntity blockEntity) {
        if(level.isClientSide()) return;
        Utils.forEachNeighborCapability(Capabilities.EnergyStorage.BLOCK, level, blockPos, (neighbor, side) -> {
            int maxExtract = this.energyHandler.extractEnergy(this.energyHandler.getEnergyStored(), true);

            if (maxExtract > 0 && neighbor.canReceive()) {
                int accepted = neighbor.receiveEnergy(maxExtract, false);

                if (accepted > 0) {
                    this.energyHandler.extractEnergy(accepted, false);
                    setChanged();
                }
            }
        });
    }


}
