package rewqazwas.minformax.custom;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;
import rewqazwas.minformax.MinForMax;
import rewqazwas.minformax.custom.blocks.*;
import rewqazwas.minformax.custom.blocks.Multiblocks.GateOfBabylonBlockEntity;
import rewqazwas.minformax.custom.blocks.PandoraBox.PandoraBoxCoreBlockEntity;
import rewqazwas.minformax.custom.blocks.PandoraBox.PandoraBoxDummyBlockEntity;

import java.util.function.Supplier;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MinForMax.MOD_ID);

    public static final Supplier<BlockEntityType<IndexLabBlockEntity>> INDEX_LAB_BE = BLOCK_ENTITY_TYPES.register("index_lab", () -> BlockEntityType.Builder.of(IndexLabBlockEntity::new, ModBlocks.INDEX_INSCRIBER.get()).build(null));
    public static final Supplier<BlockEntityType<CreativeEnergyBlockEntity>> CREATIVE_ENERGY_BE = BLOCK_ENTITY_TYPES.register("creative_energy", () -> BlockEntityType.Builder.of(CreativeEnergyBlockEntity::new, ModBlocks.CREATIVE_ENERGY.get()).build(null));
    public static final Supplier<BlockEntityType<EternalGeneratorBlockEntity>> ETERNAL_GENERATOR_BE = BLOCK_ENTITY_TYPES.register("eternal_generator", () -> BlockEntityType.Builder.of(EternalGeneratorBlockEntity::new, ModBlocks.ETERNAL_GENERATOR.get()).build(null));
    public static final Supplier<BlockEntityType<FluidReplicatorBlockEntity>> FLUID_REPLICATOR_BE = BLOCK_ENTITY_TYPES.register("fluid_replicator", () -> BlockEntityType.Builder.of(FluidReplicatorBlockEntity::new, ModBlocks.FLUID_REPLICATOR.get()).build(null));
    public static final Supplier<BlockEntityType<BlockReplicatorBlockEntity>> BLOCK_REPLICATOR_BE = BLOCK_ENTITY_TYPES.register("block_replicator", () -> BlockEntityType.Builder.of(BlockReplicatorBlockEntity::new, ModBlocks.BLOCK_REPLICATOR.get()).build(null));
    public static final Supplier<BlockEntityType<FarmerBlockEntity>> FARMER_BE = BLOCK_ENTITY_TYPES.register("farmer", () -> BlockEntityType.Builder.of(FarmerBlockEntity::new, ModBlocks.FARMER.get()).build(null));
    public static final Supplier<BlockEntityType<OreCoalescerBlockEntity>> ORE_COALESCER_BE = BLOCK_ENTITY_TYPES.register("ore_coalescer", () -> BlockEntityType.Builder.of(OreCoalescerBlockEntity::new, ModBlocks.ORE_COALESCER.get()).build(null));
    public static final Supplier<BlockEntityType<MultiblockPartBlockEntity>> MULTIBLOCK_PART_BE =
            BLOCK_ENTITY_TYPES.register("multiblock_part", () -> BlockEntityType.Builder.of(
                    MultiblockPartBlockEntity::new,
                    ModBlocks.SAKURADITE_CASING.get(),
                    ModBlocks.SAKURADITE_PANEL.get(),
                    ModBlocks.SAKURADITE_INPUT.get(),
                    ModBlocks.SAKURADITE_OUTPUT.get()
            ).build(null));

    // Your custom Gate of Babylon Master entity, which CAN occupy any of your generic blocks when formed
    public static final Supplier<BlockEntityType<GateOfBabylonBlockEntity>> GATE_OF_BABYLON_BE =
            BLOCK_ENTITY_TYPES.register("gate_of_babylon", () -> BlockEntityType.Builder.of(
                    GateOfBabylonBlockEntity::new,
                    ModBlocks.SAKURADITE_CASING.get(),
                    ModBlocks.SAKURADITE_PANEL.get(),
                    ModBlocks.SAKURADITE_INPUT.get(),
                    ModBlocks.SAKURADITE_OUTPUT.get()
            ).build(null));

    public static final Supplier<BlockEntityType<PandoraBoxCoreBlockEntity>> PANDORA_BOX_CORE_BE = BLOCK_ENTITY_TYPES.register("pandora_box_core", () -> BlockEntityType.Builder.of(PandoraBoxCoreBlockEntity::new, ModBlocks.PANDORA_BOX_CORE.get()).build(null));
    public static final Supplier<BlockEntityType<PandoraBoxDummyBlockEntity>> PANDORA_BOX_DUMMY_BE = BLOCK_ENTITY_TYPES.register("pandora_box_dummy", () -> BlockEntityType.Builder.of(PandoraBoxDummyBlockEntity::new, ModBlocks.PANDORA_BOX_DUMMY.get(), ModBlocks.PANDORA_BOX_HATCH.get()).build(null));
}