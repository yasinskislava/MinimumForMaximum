package rewqazwas.minformax.custom.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import rewqazwas.minformax.custom.ModBlockEntities;

import static rewqazwas.minformax.custom.utility.Utils.getEnergyHandlers;


public class CreativeEnergyBlockEntity extends BlockEntity {
    public final EnergyStorage energyHandler = new EnergyStorage(1000_000_000);

    public CreativeEnergyBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.CREATIVE_ENERGY_BE.get(), pos, blockState);
        energyHandler.receiveEnergy(energyHandler.getMaxEnergyStored(), false);
    }

    public void tick(Level level, BlockPos blockPos, BlockState blockState, CreativeEnergyBlockEntity blockEntity) {
        if(level.isClientSide()) return;
        for(IEnergyStorage side : getEnergyHandlers(level, blockPos)) {
            if(side != null && side.canReceive()) {
                side.receiveEnergy(energyHandler.getEnergyStored(), false);
            }
        }
    }


}
