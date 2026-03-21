package rewqazwas.minformax.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import org.jetbrains.annotations.NotNull;
import rewqazwas.minformax.custom.blocks.ModBlocks;
import rewqazwas.minformax.custom.component.ModDataComponents;
import rewqazwas.minformax.custom.items.ModItems;


import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(@NotNull RecipeOutput recipeOutput) {

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.MEMORY_SHARD.get())
                .pattern(" rc")
                .pattern("rdr")
                .pattern("cr ")
                .define('r', Ingredient.of(Items.REDSTONE_BLOCK))
                .define('d', Items.DIAMOND_BLOCK)
                .define('c', Items.COPPER_BLOCK)
                .unlockedBy("has_redstone_block", has(Items.REDSTONE_BLOCK))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.CHAOS_SHARD.get())
                .pattern(" rc")
                .pattern("rdr")
                .pattern("cr ")
                .define('r', Ingredient.of(Items.REDSTONE_BLOCK))
                .define('d', Items.NETHERITE_BLOCK)
                .define('c', Items.BLACK_CONCRETE_POWDER)
                .unlockedBy("has_redstone_block", has(Items.REDSTONE_BLOCK))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.QUANTUM_INGOT.get())
                .pattern("rqr")
                .pattern("gni")
                .pattern("rqr")
                .define('r', Ingredient.of(Items.REDSTONE_BLOCK))
                .define('q', Items.QUARTZ)
                .define('g', Items.GOLD_INGOT)
                .define('i', Items.IRON_INGOT)
                .define('n', Items.COPPER_INGOT)
                .unlockedBy("has_diamond", has(Items.DIAMOND))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.FROZEN_CRYSTAL.get())
                .pattern(" dp")
                .pattern("dld")
                .pattern("pd ")
                .define('d', Items.DIAMOND_BLOCK)
                .define('p', Items.PACKED_ICE)
                .define('l', Items.SEA_LANTERN)
                .unlockedBy("has_diamond", has(Items.DIAMOND))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.FROZEN_CORE.get())
                .pattern("ccc")
                .pattern("csc")
                .pattern("ccc")
                .define('c', ModItems.FROZEN_CRYSTAL)
                .define('s', Items.NETHER_STAR)
                .unlockedBy("has_diamond", has(Items.DIAMOND))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.LINKER.get())
                .pattern(" pl")
                .pattern(" ri")
                .pattern(" ii")
                .define('i', Items.IRON_INGOT)
                .define('r', Items.REDSTONE_BLOCK)
                .define('p', Items.HEAVY_WEIGHTED_PRESSURE_PLATE)
                .define('l', Items.LIGHTNING_ROD)
                .unlockedBy("has_diamond", has(Items.DIAMOND))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.PANDORA_BOX_CORE.get())
                .pattern("non")
                .pattern("ncn")
                .pattern("non")
                .define('c', ModItems.FROZEN_CORE)
                .define('n', Items.NETHERITE_BLOCK)
                .define('o', Items.OBSIDIAN)
                .unlockedBy("has_diamond", has(Items.DIAMOND))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.CHEF_MODULE.get())
                .pattern("gag")
                .pattern("bqc")
                .pattern("gdg")
                .define('a', Ingredient.of(Items.WHEAT))
                .define('b', Ingredient.of(Items.POTATO))
                .define('c', Ingredient.of(Items.CARROT))
                .define('d', Ingredient.of(Items.BEETROOT))
                .define('g', Ingredient.of(Items.GOLD_BLOCK))
                .define('q', Ingredient.of(ModItems.QUANTUM_FOAM))
                .unlockedBy("has_quantum_foam", has(ModItems.QUANTUM_FOAM))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.FISHER_MODULE.get())
                .pattern("gag")
                .pattern("bqc")
                .pattern("gdg")
                .define('a', Ingredient.of(Items.COD))
                .define('b', Ingredient.of(Items.SALMON))
                .define('c', Ingredient.of(Items.TROPICAL_FISH))
                .define('d', Ingredient.of(Items.PUFFERFISH))
                .define('g', Ingredient.of(Items.GOLD_BLOCK))
                .define('q', Ingredient.of(ModItems.QUANTUM_FOAM))
                .unlockedBy("has_quantum_foam", has(ModItems.QUANTUM_FOAM))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.FARMER_MODULE.get())
                .pattern("gag")
                .pattern("bqc")
                .pattern("gdg")
                .define('a', Ingredient.of(Items.WHEAT_SEEDS))
                .define('b', Ingredient.of(Items.MELON_SEEDS))
                .define('c', Ingredient.of(Items.PUMPKIN_SEEDS))
                .define('d', Ingredient.of(Items.BEETROOT_SEEDS))
                .define('g', Ingredient.of(Items.GOLD_BLOCK))
                .define('q', Ingredient.of(ModItems.QUANTUM_FOAM))
                .unlockedBy("has_quantum_foam", has(ModItems.QUANTUM_FOAM))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.GATHERER_MODULE.get())
                .pattern("gag")
                .pattern("bqc")
                .pattern("gdg")
                .define('a', Ingredient.of(Items.CHORUS_FRUIT))
                .define('b', Ingredient.of(Items.NETHER_WART))
                .define('c', Ingredient.of(Items.GLOW_BERRIES))
                .define('d', Ingredient.of(Items.COCOA_BEANS))
                .define('g', Ingredient.of(Items.GOLD_BLOCK))
                .define('q', Ingredient.of(ModItems.QUANTUM_FOAM))
                .unlockedBy("has_quantum_foam", has(ModItems.QUANTUM_FOAM))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.BUTCHER_MODULE.get())
                .pattern("gag")
                .pattern("bqc")
                .pattern("gdg")
                .define('a', DataComponentIngredient.of(true, ModDataComponents.MOB_INDEX.get(), "entity.minecraft.cow", ModItems.MEMORY_SHARD))
                .define('b', DataComponentIngredient.of(true, ModDataComponents.MOB_INDEX.get(), "entity.minecraft.rabbit", ModItems.MEMORY_SHARD))
                .define('c', DataComponentIngredient.of(true, ModDataComponents.MOB_INDEX.get(), "entity.minecraft.chicken", ModItems.MEMORY_SHARD))
                .define('d', DataComponentIngredient.of(true, ModDataComponents.MOB_INDEX.get(), "entity.minecraft.pig", ModItems.MEMORY_SHARD))
                .define('g', Ingredient.of(Items.GOLD_BLOCK))
                .define('q', Ingredient.of(ModItems.QUANTUM_FOAM))
                .unlockedBy("has_quantum_foam", has(ModItems.QUANTUM_FOAM))
                .save(recipeOutput);

        //SPECIAL
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.INVERTED_UPGRADE.get())
                .pattern("qgq")
                .pattern("gdg")
                .pattern("qgq")
                .define('d', Ingredient.of(ModItems.EXTRA_DROP_UPGRADE_TIER4))
                .define('g', Ingredient.of(Items.PINK_STAINED_GLASS))
                .define('q', Ingredient.of(ModItems.QUANTUM_INGOT))
                .unlockedBy("has_quantum_ingot", has(ModItems.QUANTUM_INGOT))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.AUTO_SMELTING_UPGRADE.get())
                .pattern("ibi")
                .pattern("lfl")
                .pattern("ibi")
                .define('i', Ingredient.of(ModItems.QUANTUM_INGOT))
                .define('f', Ingredient.of(ModBlocks.FLUID_REPLICATOR))
                .define('l', Ingredient.of(Items.LAVA_BUCKET))
                .define('b', Ingredient.of(Items.BLAZE_ROD))
                .unlockedBy("has_quantum_ingot", has(ModItems.QUANTUM_INGOT))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.WATERING_UPGRADE.get())
                .pattern("iwi")
                .pattern("wsw")
                .pattern("iwi")
                .define('i', Ingredient.of(ModItems.QUANTUM_INGOT))
                .define('w', Ingredient.of(Items.WATER_BUCKET))
                .define('s', Ingredient.of(Items.SUGAR))
                .unlockedBy("has_quantum_ingot", has(ModItems.QUANTUM_INGOT))
                .save(recipeOutput);

        //SPEED
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SPEED_UPGRADE_TIER1.get())
                .pattern("qrq")
                .pattern("rdr")
                .pattern("qrq")
                .define('r', Ingredient.of(Items.REDSTONE))
                .define('d', Ingredient.of(Items.GLASS_PANE))
                .define('q', Ingredient.of(ModItems.QUANTUM_INGOT))
                .unlockedBy("has_quantum_ingot", has(ModItems.QUANTUM_INGOT))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SPEED_UPGRADE_TIER2.get())
                .pattern("lll")
                .pattern("usu")
                .pattern("lll")
                .define('l', Ingredient.of(Items.LAPIS_LAZULI))
                .define('s', Ingredient.of(Items.SLIME_BALL))
                .define('u', Ingredient.of(ModItems.SPEED_UPGRADE_TIER1))
                .unlockedBy("has_quantum_ingot", has(ModItems.QUANTUM_INGOT))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SPEED_UPGRADE_TIER3.get())
                .pattern("eee")
                .pattern("usu")
                .pattern("eee")
                .define('e', Ingredient.of(Items.EMERALD))
                .define('s', Ingredient.of(Items.SLIME_BALL))
                .define('u', Ingredient.of(ModItems.SPEED_UPGRADE_TIER2))
                .unlockedBy("has_quantum_ingot", has(ModItems.QUANTUM_INGOT))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SPEED_UPGRADE_TIER4.get())
                .pattern("ggg")
                .pattern("usu")
                .pattern("ggg")
                .define('g', Ingredient.of(Items.GOLD_INGOT))
                .define('s', Ingredient.of(Items.SLIME_BALL))
                .define('u', Ingredient.of(ModItems.SPEED_UPGRADE_TIER3))
                .unlockedBy("has_quantum_ingot", has(ModItems.QUANTUM_INGOT))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.ULTIMATE_SPEED_UPGRADE.get())
                .pattern("psp")
                .pattern("php")
                .pattern("psp")
                .define('p', Ingredient.of(ModItems.ULTIMATE_PROCESSING_UPGRADE))
                .define('s', Ingredient.of(ModItems.SPEED_UPGRADE_TIER4))
                .define('h', Ingredient.of(ModItems.FROZEN_CORE))
                .unlockedBy("has_quantum_ingot", has(ModItems.QUANTUM_INGOT))
                .save(recipeOutput);

        //PROCESSES
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.PROCESSING_UPGRADE_TIER1.get())
                .pattern("qrq")
                .pattern("rdr")
                .pattern("qrq")
                .define('r', Ingredient.of(Items.NETHERITE_SCRAP))
                .define('d', Ingredient.of(Items.GLASS_PANE))
                .define('q', Ingredient.of(ModItems.QUANTUM_INGOT))
                .unlockedBy("has_quantum_ingot", has(ModItems.QUANTUM_INGOT))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.PROCESSING_UPGRADE_TIER2.get())
                .pattern("lul")
                .pattern("usu")
                .pattern("lul")
                .define('l', Ingredient.of(Items.LAPIS_LAZULI))
                .define('s', Ingredient.of(Items.SLIME_BALL))
                .define('u', Ingredient.of(ModItems.PROCESSING_UPGRADE_TIER1))
                .unlockedBy("has_quantum_ingot", has(ModItems.QUANTUM_INGOT))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.PROCESSING_UPGRADE_TIER3.get())
                .pattern("eue")
                .pattern("usu")
                .pattern("eue")
                .define('e', Ingredient.of(Items.EMERALD))
                .define('s', Ingredient.of(Items.SLIME_BALL))
                .define('u', Ingredient.of(ModItems.PROCESSING_UPGRADE_TIER2))
                .unlockedBy("has_quantum_ingot", has(ModItems.QUANTUM_INGOT))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.PROCESSING_UPGRADE_TIER4.get())
                .pattern("gug")
                .pattern("usu")
                .pattern("gug")
                .define('g', Ingredient.of(Items.GOLD_INGOT))
                .define('s', Ingredient.of(Items.SLIME_BALL))
                .define('u', Ingredient.of(ModItems.PROCESSING_UPGRADE_TIER3))
                .unlockedBy("has_quantum_ingot", has(ModItems.QUANTUM_INGOT))
                .save(recipeOutput);

        //EXTRA
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.EXTRA_DROP_UPGRADE_TIER1.get())
                .pattern("qrq")
                .pattern("rdr")
                .pattern("qrq")
                .define('r', Ingredient.of(Items.LAPIS_LAZULI))
                .define('d', Ingredient.of(Items.GLASS_PANE))
                .define('q', Ingredient.of(ModItems.QUANTUM_INGOT))
                .unlockedBy("has_quantum_ingot", has(ModItems.QUANTUM_INGOT))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.EXTRA_DROP_UPGRADE_TIER2.get())
                .pattern("lll")
                .pattern("usu")
                .pattern("lll")
                .define('l', Ingredient.of(Items.LAPIS_LAZULI))
                .define('s', Ingredient.of(Items.SLIME_BALL))
                .define('u', Ingredient.of(ModItems.EXTRA_DROP_UPGRADE_TIER1))
                .unlockedBy("has_quantum_ingot", has(ModItems.QUANTUM_INGOT))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.EXTRA_DROP_UPGRADE_TIER3.get())
                .pattern("eee")
                .pattern("usu")
                .pattern("eee")
                .define('e', Ingredient.of(Items.EMERALD))
                .define('s', Ingredient.of(Items.SLIME_BALL))
                .define('u', Ingredient.of(ModItems.EXTRA_DROP_UPGRADE_TIER2))
                .unlockedBy("has_quantum_ingot", has(ModItems.QUANTUM_INGOT))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.EXTRA_DROP_UPGRADE_TIER4.get())
                .pattern("ggg")
                .pattern("usu")
                .pattern("ggg")
                .define('g', Ingredient.of(Items.GOLD_INGOT))
                .define('s', Ingredient.of(Items.SLIME_BALL))
                .define('u', Ingredient.of(ModItems.EXTRA_DROP_UPGRADE_TIER3))
                .unlockedBy("has_quantum_ingot", has(ModItems.QUANTUM_INGOT))
                .save(recipeOutput);

        //GENERATOR
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.ETERNAL_GENERATOR_TIER1.get())
                .pattern("idi")
                .pattern("dbd")
                .pattern("idi")
                .define('i', Ingredient.of(Items.IRON_BLOCK))
                .define('d', Ingredient.of(Items.ANCIENT_DEBRIS))
                .define('b', Ingredient.of(Items.DIAMOND))
                .unlockedBy("has_diamond", has(Items.DIAMOND))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.ETERNAL_GENERATOR_TIER2.get())
                .pattern("lll")
                .pattern("usu")
                .pattern("lll")
                .define('l', Ingredient.of(Items.LAPIS_BLOCK))
                .define('s', Ingredient.of(ModItems.QUANTUM_FOAM))
                .define('u', Ingredient.of(ModBlocks.ETERNAL_GENERATOR_TIER1))
                .unlockedBy("has_quantum_foam", has(ModItems.QUANTUM_FOAM))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.ETERNAL_GENERATOR_TIER3.get())
                .pattern("eee")
                .pattern("usu")
                .pattern("eee")
                .define('e', Ingredient.of(Items.EMERALD_BLOCK))
                .define('s', Ingredient.of(ModItems.QUANTUM_FOAM))
                .define('u', Ingredient.of(ModBlocks.ETERNAL_GENERATOR_TIER2))
                .unlockedBy("has_quantum_foam", has(ModItems.QUANTUM_FOAM))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.ETERNAL_GENERATOR_TIER4.get())
                .pattern("ggg")
                .pattern("usu")
                .pattern("ggg")
                .define('g', Ingredient.of(Items.GOLD_BLOCK))
                .define('s', Ingredient.of(ModItems.QUANTUM_FOAM))
                .define('u', Ingredient.of(ModBlocks.ETERNAL_GENERATOR_TIER3))
                .unlockedBy("has_quantum_foam", has(ModItems.QUANTUM_FOAM))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.INDEX_INSCRIBER.get())
                .pattern("bdb")
                .pattern("coc")
                .pattern("did")
                .define('b', Ingredient.of(Items.BLAZE_ROD))
                .define('d', Ingredient.of(Items.DIAMOND))
                .define('c', Ingredient.of(Items.CRAFTING_TABLE))
                .define('o', Ingredient.of(Items.OBSIDIAN))
                .define('i', Ingredient.of(Items.IRON_BLOCK))
                .unlockedBy("has_diamond", has(Items.DIAMOND))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SCANNER.get())
                .pattern("ddd")
                .pattern("sgs")
                .pattern("ddd")
                .define('s', Ingredient.of(Items.STICK))
                .define('d', Ingredient.of(Items.DIAMOND))
                .define('g', Ingredient.of(Items.GLASS_PANE))
                .unlockedBy("has_diamond", has(Items.DIAMOND))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.FLUID_REPLICATOR.get())
                .pattern("iii")
                .pattern("bnb")
                .pattern("iii")
                .define('i', Ingredient.of(Items.IRON_BLOCK))
                .define('n', Ingredient.of(Items.NETHERITE_INGOT))
                .define('b', Ingredient.of(Items.BUCKET))
                .unlockedBy("has_netherite", has(Items.NETHERITE_INGOT))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.BLOCK_REPLICATOR.get())
                .pattern("iii")
                .pattern("gng")
                .pattern("iii")
                .define('i', Ingredient.of(Items.IRON_BLOCK))
                .define('n', Ingredient.of(Items.NETHERITE_INGOT))
                .define('g', Ingredient.of(Items.GLASS))
                .unlockedBy("has_netherite", has(Items.NETHERITE_INGOT))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.ORE_COALESCER.get())
                .pattern("ibi")
                .pattern("bnb")
                .pattern("ibi")
                .define('i', Ingredient.of(Items.IRON_BLOCK))
                .define('n', Ingredient.of(Blocks.NETHERITE_BLOCK))
                .define('b', Ingredient.of(Items.BLAZE_ROD))
                .unlockedBy("has_netherite", has(Items.NETHERITE_INGOT))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.FARMER.get())
                .pattern("iri")
                .pattern("rnr")
                .pattern("iri")
                .define('i', Ingredient.of(ModItems.QUANTUM_INGOT))
                .define('r', Ingredient.of(Blocks.REDSTONE_BLOCK))
                .define('n', Ingredient.of(Items.NETHERITE_HOE))
                .unlockedBy("has_quantum_ingot", has(ModItems.QUANTUM_INGOT))
                .save(recipeOutput);

        //FORTUNE
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.FORTUNE_UPGRADE_TIER1.get())
                .pattern("qrq")
                .pattern("rdr")
                .pattern("qrq")
                .define('r', Ingredient.of(Items.DIAMOND))
                .define('d', Ingredient.of(Items.GLASS_PANE))
                .define('q', Ingredient.of(ModItems.QUANTUM_INGOT))
                .unlockedBy("has_quantum_ingot", has(ModItems.QUANTUM_INGOT))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.FORTUNE_UPGRADE_TIER2.get())
                .pattern("lul")
                .pattern("usu")
                .pattern("lul")
                .define('l', Ingredient.of(Items.LAPIS_LAZULI))
                .define('s', Ingredient.of(Items.SLIME_BALL))
                .define('u', Ingredient.of(ModItems.FORTUNE_UPGRADE_TIER1))
                .unlockedBy("has_quantum_ingot", has(ModItems.QUANTUM_INGOT))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.FORTUNE_UPGRADE_TIER3.get())
                .pattern("eue")
                .pattern("usu")
                .pattern("eue")
                .define('e', Ingredient.of(Items.EMERALD))
                .define('s', Ingredient.of(Items.SLIME_BALL))
                .define('u', Ingredient.of(ModItems.FORTUNE_UPGRADE_TIER2))
                .unlockedBy("has_quantum_ingot", has(ModItems.QUANTUM_INGOT))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.FORTUNE_UPGRADE_TIER4.get())
                .pattern("gug")
                .pattern("usu")
                .pattern("gug")
                .define('g', Ingredient.of(Items.GOLD_INGOT))
                .define('s', Ingredient.of(Items.SLIME_BALL))
                .define('u', Ingredient.of(ModItems.FORTUNE_UPGRADE_TIER3))
                .unlockedBy("has_quantum_ingot", has(ModItems.QUANTUM_INGOT))
                .save(recipeOutput);

        //ULTIMATE
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.ULTIMATE_PROCESSING_UPGRADE.get())
                .pattern("uuu")
                .pattern("udu")
                .pattern("uuu")
                .define('u', Ingredient.of(ModItems.PROCESSING_UPGRADE_TIER4.get()))
                .define('d', Ingredient.of(Blocks.DRAGON_EGG))
                .unlockedBy("has_dragon_egg", has(Blocks.DRAGON_EGG))
                .save(recipeOutput);
    }
}
