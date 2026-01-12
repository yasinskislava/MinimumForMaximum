package rewqazwas.minformax.datagen;

import net.minecraft.data.PackOutput;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;
import rewqazwas.minformax.MinForMax;
import rewqazwas.minformax.custom.blocks.ModBlocks;



public class ModBlockStateProvider extends BlockStateProvider {

    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, MinForMax.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        blockWithItem(ModBlocks.ETERNAL_GENERATOR_TIER1);
        blockWithItem(ModBlocks.ETERNAL_GENERATOR_TIER2);
        blockWithItem(ModBlocks.ETERNAL_GENERATOR_TIER3);
        blockWithItem(ModBlocks.ETERNAL_GENERATOR_TIER4);
        blockWithItem(ModBlocks.INDEX_LAB);
        blockWithItem(ModBlocks.CREATIVE_ENERGY);
        blockWithItem(ModBlocks.FLUID_REPLICATOR);
        blockWithItem(ModBlocks.BLOCK_REPLICATOR);
    }




    private void blockWithItem(DeferredBlock<?> deferredBlock) {
        simpleBlockWithItem(deferredBlock.get(), cubeAll(deferredBlock.get()));
    }

    private void blockItem(DeferredBlock<?> deferredBlock) {
        simpleBlockItem(deferredBlock.get(), new ModelFile.UncheckedModelFile("minformax:block/" + deferredBlock.getId().getPath()));
    }
    private void blockItem(DeferredBlock<?> deferredBlock, String appendix) {
        simpleBlockItem(deferredBlock.get(), new ModelFile.UncheckedModelFile("minformax:block/" + deferredBlock.getId().getPath() + appendix));
    }
}
