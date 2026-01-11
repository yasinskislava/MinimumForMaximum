package rewqazwas.minformax.custom;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;
import rewqazwas.minformax.MinForMax;
import rewqazwas.minformax.custom.blocks.*;

import java.util.function.Supplier;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MinForMax.MOD_ID);

    public static final Supplier<BlockEntityType<IndexLabBlockEntity>> INDEX_LAB_BE = BLOCK_ENTITY_TYPES.register("index_lab", () -> BlockEntityType.Builder.of(IndexLabBlockEntity::new, ModBlocks.INDEX_LAB.get()).build(null));
    public static final Supplier<BlockEntityType<CreativeEnergyBlockEntity>> CREATIVE_ENERGY_BE = BLOCK_ENTITY_TYPES.register("creative_energy", () -> BlockEntityType.Builder.of(CreativeEnergyBlockEntity::new, ModBlocks.CREATIVE_ENERGY.get()).build(null));
    public static final Supplier<BlockEntityType<EternalGeneratorBlockEntity>> ETERNAL_GENERATOR_BE = BLOCK_ENTITY_TYPES.register("eternal_generator", () -> BlockEntityType.Builder.of(EternalGeneratorBlockEntity::new,
            ModBlocks.ETERNAL_GENERATOR_TIER1.get(),
            ModBlocks.ETERNAL_GENERATOR_TIER2.get(),
            ModBlocks.ETERNAL_GENERATOR_TIER3.get(),
            ModBlocks.ETERNAL_GENERATOR_TIER4.get())
            .build(null));
}
