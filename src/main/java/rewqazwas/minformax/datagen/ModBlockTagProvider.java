package rewqazwas.minformax.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;
import rewqazwas.minformax.MinForMax;
import rewqazwas.minformax.custom.blocks.ModBlocks;


import java.util.concurrent.CompletableFuture;

public class ModBlockTagProvider extends BlockTagsProvider {
    public ModBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, MinForMax.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(ModBlocks.ETERNAL_GENERATOR_TIER1.get())
                .add(ModBlocks.ETERNAL_GENERATOR_TIER2.get())
                .add(ModBlocks.ETERNAL_GENERATOR_TIER3.get())
                .add(ModBlocks.ETERNAL_GENERATOR_TIER4.get())
                .add(ModBlocks.INDEX_LAB.get());

    }
}
