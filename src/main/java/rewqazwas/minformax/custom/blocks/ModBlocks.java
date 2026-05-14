package rewqazwas.minformax.custom.blocks;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import rewqazwas.minformax.MinForMax;
import rewqazwas.minformax.custom.blocks.PandoraBox.PandoraBoxCore;
import rewqazwas.minformax.custom.blocks.PandoraBox.PandoraBoxDummy;
import rewqazwas.minformax.custom.blocks.PandoraBox.PandoraBoxEnergyHatch;
import rewqazwas.minformax.custom.blocks.PandoraBox.PandoraBoxItem;
import rewqazwas.minformax.custom.items.ModItems;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MinForMax.MOD_ID);

    public static final DeferredBlock<Block> INDEX_INSCRIBER = registerBlock("index_inscriber", () -> new IndexLabBlock(BlockBehaviour.Properties.of().strength(3f).noOcclusion().requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> ETERNAL_GENERATOR = registerBlock("eternal_generator", () -> new EternalGeneratorBlock(BlockBehaviour.Properties.of().strength(3.5f).noOcclusion().requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> CREATIVE_ENERGY = registerBlock("creative_energy", () -> new CreativeEnergyBlock(BlockBehaviour.Properties.of()));
    public static final DeferredBlock<Block> FLUID_REPLICATOR = registerBlock("fluid_replicator", () -> new FluidReplicator(BlockBehaviour.Properties.of().strength(3f).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<Block> BLOCK_REPLICATOR = registerBlock("block_replicator", () -> new BlockReplicator(BlockBehaviour.Properties.of().strength(3f).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<Block> ORE_COALESCER = registerBlock("ore_coalescer", () -> new OreCoalescerBlock(BlockBehaviour.Properties.of().strength(3f).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<Block> FARMER = registerBlock("farmer", () -> new FarmerBlock(BlockBehaviour.Properties.of().strength(3f).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<Block> PANDORA_BOX_CORE = registerBlock("pandora_box", () -> new PandoraBoxCore(BlockBehaviour.Properties.of().strength(3f).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<Block> PANDORA_BOX_HATCH = registerBlock("pandora_box_hatch", () -> new PandoraBoxEnergyHatch(BlockBehaviour.Properties.of().strength(3f).noOcclusion().requiresCorrectToolForDrops().noLootTable()));
    public static final DeferredBlock<Block> PANDORA_BOX_DUMMY = registerBlock("pandora_box_dummy", () -> new PandoraBoxDummy(BlockBehaviour.Properties.of().strength(3f).noOcclusion().requiresCorrectToolForDrops().noLootTable()));

    //Automated register for block item
    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<? extends T> block) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        if (name.equals("pandora_box")) {
            ModItems.ITEMS.register(name, () -> new PandoraBoxItem(toReturn.get(), new Item.Properties()));
        } else {
            registerBlockItem(name, toReturn);
        }
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }
}
