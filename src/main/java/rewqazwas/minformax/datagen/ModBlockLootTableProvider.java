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
        dropSelf(ModBlocks.INDEX_LAB.get());
        dropSelf(ModBlocks.ETERNAL_GENERATOR_TIER1.get());
        dropSelf(ModBlocks.ETERNAL_GENERATOR_TIER2.get());
        dropSelf(ModBlocks.ETERNAL_GENERATOR_TIER3.get());
        dropSelf(ModBlocks.ETERNAL_GENERATOR_TIER4.get());
        dropSelf(ModBlocks.CREATIVE_ENERGY.get());

    }




    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ModBlocks.BLOCKS.getEntries().stream().map(Holder::value)::iterator;
    }
}
