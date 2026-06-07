package rewqazwas.minformax.compat;

import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import rewqazwas.limlog.api.LimLogAPI;
import rewqazwas.minformax.custom.ModBlockEntities;
import rewqazwas.minformax.custom.blocks.Multiblocks.GateHatchEnergyWrapper;
import rewqazwas.minformax.custom.blocks.Multiblocks.GateOfBabylonBlockEntity;
import rewqazwas.minformax.custom.blocks.PandoraBox.PandoraBoxCoreBlockEntity;
import rewqazwas.minformax.custom.blocks.PandoraBox.PandoraCoreEnergyWrapper;
import rewqazwas.minformax.custom.blocks.ModBlocks;

public class LimLogCompat {

    public static void registerLimLogCaps(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(LimLogAPI.LONG_ENERGY_BLOCK, ModBlockEntities.PANDORA_BOX_CORE_BE.get(),
                (be, context) -> new PandoraCoreEnergyWrapper(be));

        event.registerBlockEntity(LimLogAPI.LONG_ENERGY_BLOCK, ModBlockEntities.PANDORA_BOX_DUMMY_BE.get(), (be, side) -> {
            if (be.getBlockState().is(ModBlocks.PANDORA_BOX_HATCH.get())) {
                BlockPos corePos = be.getCorePos();
                if (corePos != null && be.getLevel().getBlockEntity(corePos) instanceof PandoraBoxCoreBlockEntity core) {
                    return new PandoraCoreEnergyWrapper(core);
                }
            }
            return null;
        });

        event.registerBlockEntity(LimLogAPI.LONG_ENERGY_BLOCK, ModBlockEntities.GATE_OF_BABYLON_HATCH_BE.get(), (be, side) -> {
            GateOfBabylonBlockEntity master = be.getMasterBlockEntity();
            return master != null ? new GateHatchEnergyWrapper(master, be.getBlockState()) : null;
        });
    }
}