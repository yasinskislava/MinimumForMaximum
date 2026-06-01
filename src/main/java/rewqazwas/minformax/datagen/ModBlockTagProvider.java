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
                .add(ModBlocks.ETERNAL_GENERATOR.get())
                .add(ModBlocks.INDEX_INSCRIBER.get())
                .add(ModBlocks.ORE_COALESCER.get())
                .add(ModBlocks.FLUID_REPLICATOR.get())
                .add(ModBlocks.BLOCK_REPLICATOR.get())
                .add(ModBlocks.FARMER.get())
                .add(ModBlocks.PANDORA_BOX_CORE.get())
                .add(ModBlocks.PANDORA_BOX_DUMMY.get())
                .add(ModBlocks.PANDORA_BOX_HATCH.get())
                .add(ModBlocks.SAKURADITE_CASING.get())
                .add(ModBlocks.SAKURADITE_PANEL.get())
                .add(ModBlocks.SAKURADITE_INPUT.get())
                .add(ModBlocks.SAKURADITE_OUTPUT.get());
    }
}
