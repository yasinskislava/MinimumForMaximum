package rewqazwas.minformax.datagen;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import rewqazwas.minformax.custom.blocks.ModBlocks;


import java.util.Set;

public class ModBlockLootTableProvider extends BlockLootSubProvider {
    protected ModBlockLootTableProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        dropSelf(ModBlocks.INDEX_INSCRIBER.get());
        dropSelf(ModBlocks.ETERNAL_GENERATOR.get());
        dropSelf(ModBlocks.CREATIVE_ENERGY.get());
        dropSelf(ModBlocks.FLUID_REPLICATOR.get());
        dropSelf(ModBlocks.BLOCK_REPLICATOR.get());
        dropSelf(ModBlocks.ORE_COALESCER.get());
        dropSelf(ModBlocks.FARMER.get());
        dropSelf(ModBlocks.PANDORA_BOX_CORE.get());
        dropSelf(ModBlocks.SAKURADITE_CASING.get());
        dropSelf(ModBlocks.SAKURADITE_PANEL.get());
        dropSelf(ModBlocks.SAKURADITE_INPUT.get());
        dropSelf(ModBlocks.SAKURADITE_OUTPUT.get());
    }




    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ModBlocks.BLOCKS.getEntries().stream().map(Holder::value)::iterator;
    }
}
