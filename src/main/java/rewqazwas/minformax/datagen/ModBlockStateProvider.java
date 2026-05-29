package rewqazwas.minformax.datagen;

import net.minecraft.data.PackOutput;
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
        simpleBlockWithItem(ModBlocks.ETERNAL_GENERATOR.get(), models().getExistingFile(modLoc("block/eternal_generator")));
        simpleBlockWithItem(ModBlocks.INDEX_INSCRIBER.get(), models().getExistingFile(modLoc("block/index_inscriber")));
        blockWithItem(ModBlocks.CREATIVE_ENERGY);
        simpleBlockWithItem(ModBlocks.FLUID_REPLICATOR.get(), models().getExistingFile(modLoc("block/replicator")));
        simpleBlockWithItem(ModBlocks.BLOCK_REPLICATOR.get(), models().getExistingFile(modLoc("block/replicator")));
        simpleBlockWithItem(ModBlocks.ORE_COALESCER.get(), models().getExistingFile(modLoc("block/ore_coalescer")));
        simpleBlockWithItem(ModBlocks.FARMER.get(), models().getExistingFile(modLoc("block/farmer")));

        simpleBlock(ModBlocks.PANDORA_BOX_CORE.get(), new ModelFile.UncheckedModelFile("minformax:block/pandora_box"));
        simpleBlock(ModBlocks.PANDORA_BOX_HATCH.get(), new ModelFile.UncheckedModelFile("minformax:block/pandora_box"));
        simpleBlock(ModBlocks.PANDORA_BOX_DUMMY.get(), new ModelFile.UncheckedModelFile("minformax:block/pandora_box"));

        blockWithItem(ModBlocks.SAKURADITE_CASING);
        blockWithItem(ModBlocks.SAKURADITE_PANEL);
        blockWithItem(ModBlocks.SAKURADITE_INPUT);
        blockWithItem(ModBlocks.SAKURADITE_OUTPUT);
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
