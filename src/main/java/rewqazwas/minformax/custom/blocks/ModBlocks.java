package rewqazwas.minformax.custom.blocks;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import rewqazwas.minformax.MinForMax;
import rewqazwas.minformax.custom.items.ModItems;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MinForMax.MOD_ID);

    public static final DeferredBlock<Block> INDEX_LAB = registerBlock("index_lab", () -> new IndexLabBlock(BlockBehaviour.Properties.of().strength(3f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> ETERNAL_GENERATOR_TIER1 = registerBlock("eternal_generator_tier1", () -> new EternalGeneratorBlock(1));
    public static final DeferredBlock<Block> ETERNAL_GENERATOR_TIER2 = registerBlock("eternal_generator_tier2", () -> new EternalGeneratorBlock(2));
    public static final DeferredBlock<Block> ETERNAL_GENERATOR_TIER3 = registerBlock("eternal_generator_tier3", () -> new EternalGeneratorBlock(3));
    public static final DeferredBlock<Block> ETERNAL_GENERATOR_TIER4 = registerBlock("eternal_generator_tier4", () -> new EternalGeneratorBlock(4));
    public static final DeferredBlock<Block> CREATIVE_ENERGY = registerBlock("creative_energy", () -> new CreativeEnergyBlock(BlockBehaviour.Properties.of()));

    //Automated register for block item
    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<? extends T> block) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }
}
