package rewqazwas.minformax.compat;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.SaplingBlock;
import rewqazwas.minformax.MinForMax;
import rewqazwas.minformax.custom.blocks.ModBlocks;
import rewqazwas.minformax.custom.index.ModDataReloadListener;
import rewqazwas.minformax.custom.utility.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@JeiPlugin
public class DetailedInfoPlugin implements IModPlugin {
    private static IJeiRuntime jeiRuntime;
    private static List<JeiBlockReplicatorRecipe> currentBlockRecipes = new ArrayList<>();
    private static List<JeiFluidReplicatorRecipe> currentFluidRecipes = new ArrayList<>();
    private static List<JeiIndexInscriberRecipe> currentIndexInscriberRecipes = new ArrayList<>();
    private static List<JeiFarmerRecipe> currentFarmerRecipes = new ArrayList<>();

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(MinForMax.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new BlockReplicatorCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new FluidReplicatorCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new IndexInscriberCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new FarmerCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.BLOCK_REPLICATOR.get()), BlockReplicatorCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.FLUID_REPLICATOR.get()), FluidReplicatorCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.INDEX_INSCRIBER.get()), IndexInscriberCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.FARMER.get()), FarmerCategory.RECIPE_TYPE);
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        DetailedInfoPlugin.jeiRuntime = jeiRuntime;
        MinForMax.LOGGER.info("JEI runtime launched");
    }

    public static void refresh() {
        if (jeiRuntime == null) return;

        try {
            if (!currentFluidRecipes.isEmpty()) {
                jeiRuntime.getRecipeManager().hideRecipes(FluidReplicatorCategory.RECIPE_TYPE, currentFluidRecipes);
            }
            currentFluidRecipes = getFluidRecipes();
            jeiRuntime.getRecipeManager().addRecipes(FluidReplicatorCategory.RECIPE_TYPE, currentFluidRecipes);
            MinForMax.LOGGER.info("Refreshed {} fluid recipes", currentFluidRecipes.size());
        } catch (Exception e) {
            MinForMax.LOGGER.error("Failed to refresh fluid recipes", e);
        }

        try {
            if (!currentBlockRecipes.isEmpty()) {
                jeiRuntime.getRecipeManager().hideRecipes(BlockReplicatorCategory.RECIPE_TYPE, currentBlockRecipes);
            }
            currentBlockRecipes = getBlockRecipes();
            jeiRuntime.getRecipeManager().addRecipes(BlockReplicatorCategory.RECIPE_TYPE, currentBlockRecipes);
            MinForMax.LOGGER.info("Refreshed {} block recipes", currentBlockRecipes.size());
        } catch (Exception e) {
            MinForMax.LOGGER.error("Failed to refresh block recipes", e);
        }

        try {
            if (!currentIndexInscriberRecipes.isEmpty()) {
                jeiRuntime.getRecipeManager().hideRecipes(IndexInscriberCategory.RECIPE_TYPE, currentIndexInscriberRecipes);
            }
            currentIndexInscriberRecipes = getIndexInscriberRecipes();
            jeiRuntime.getRecipeManager().addRecipes(IndexInscriberCategory.RECIPE_TYPE, currentIndexInscriberRecipes);
            MinForMax.LOGGER.info("Refreshed {} Index Inscriber recipes", currentIndexInscriberRecipes.size());
        } catch (Exception e) {
            MinForMax.LOGGER.error("Failed to refresh Index Inscriber recipes", e);
        }

        try {
            if (!currentFarmerRecipes.isEmpty()) {
                jeiRuntime.getRecipeManager().hideRecipes(FarmerCategory.RECIPE_TYPE, currentFarmerRecipes);
            }
            currentFarmerRecipes = getFarmerRecipes();
            jeiRuntime.getRecipeManager().addRecipes(FarmerCategory.RECIPE_TYPE, currentFarmerRecipes);
            MinForMax.LOGGER.info("Refreshed {} Farmer recipes", currentFarmerRecipes.size());
        } catch (Exception e) {
            MinForMax.LOGGER.error("Failed to refresh Farmer recipes", e);
        }
    }

    private static List<JeiBlockReplicatorRecipe> getBlockRecipes() {
        List<JeiBlockReplicatorRecipe> recipes = new ArrayList<>();
        ModDataReloadListener.BLOCK_REPLICATOR_DATA.forEach((key, data) -> {
            try {
                if (key.startsWith("#")) {
                    var tagKey = TagKey.create(BuiltInRegistries.BLOCK.key(), ResourceLocation.parse(key.substring(1)));
                    List<ItemStack> items = new ArrayList<>();
                    BuiltInRegistries.BLOCK.getTag(tagKey).ifPresent(tag -> {
                        for (var holder : tag) {
                            items.add(new ItemStack(holder.value()));
                        }
                    });
                    if (!items.isEmpty()) {
                        recipes.add(new JeiBlockReplicatorRecipe(items, data));
                    }
                } else {
                    var resourceLocation = ResourceLocation.parse(key);
                    if (BuiltInRegistries.ITEM.containsKey(resourceLocation)) {
                        var item = BuiltInRegistries.ITEM.get(resourceLocation);
                        if (item != Items.AIR) {
                            recipes.add(new JeiBlockReplicatorRecipe(List.of(new ItemStack(item)), data));
                        }
                    }
                }
            } catch (Exception e) {
                MinForMax.LOGGER.error("Error processing block recipe for key: " + key, e);
            }
        });
        MinForMax.LOGGER.info("Loaded {} block recipes", recipes.size());
        return recipes;
    }

    private static List<JeiFluidReplicatorRecipe> getFluidRecipes() {
        List<JeiFluidReplicatorRecipe> recipes = new ArrayList<>();
        ModDataReloadListener.FLUID_REPLICATOR_DATA.forEach((key, data) -> {
            try {
                var resourceLocation = ResourceLocation.parse(key);
                if (BuiltInRegistries.FLUID.containsKey(resourceLocation)) {
                    var fluid = BuiltInRegistries.FLUID.get(resourceLocation);
                    recipes.add(new JeiFluidReplicatorRecipe(fluid, data));
                } else {
                    MinForMax.LOGGER.warn("Fluid not found for key: {}", key);
                }
            } catch (Exception e) {
                MinForMax.LOGGER.error("Error processing fluid recipe for key: " + key, e);
            }
        });
        MinForMax.LOGGER.info("Loaded {} fluid recipes", recipes.size());
        return recipes;
    }

    private static List<JeiIndexInscriberRecipe> getIndexInscriberRecipes() {
        List<JeiIndexInscriberRecipe> recipes = new ArrayList<>();
        ModDataReloadListener.MOB_DROPS.forEach((mobIdString, data) -> {
            try {
                ResourceLocation mobId;
                if (mobIdString.contains(":")) {
                    mobId = ResourceLocation.parse(mobIdString);
                } else {
                    String processedMobIdString = mobIdString;
                    if (processedMobIdString.startsWith("entity.")) {
                        processedMobIdString = processedMobIdString.substring("entity.".length());
                    }
                    processedMobIdString = processedMobIdString.replaceFirst("\\.", ":");
                    mobId = ResourceLocation.parse(processedMobIdString);
                }
                if (BuiltInRegistries.ENTITY_TYPE.containsKey(mobId)) {
                    recipes.add(new JeiIndexInscriberRecipe(mobId, data));
                } else {
                    MinForMax.LOGGER.warn("Mob not found for ID: " + mobIdString + " (parsed as " + mobId + ")");
                }
            } catch (Exception e) {
                MinForMax.LOGGER.error("Error processing Index Inscriber recipe for mob ID: " + mobIdString, e);
            }
        });
        MinForMax.LOGGER.info("Loaded {} Index Inscriber recipes", recipes.size());
        return recipes;
    }

    private static List<JeiFarmerRecipe> getFarmerRecipes() {
        List<JeiFarmerRecipe> recipes = new ArrayList<>();

        ModDataReloadListener.FARMER_DATA.forEach((id, data) -> {
            Set<Item> seedItems = new java.util.HashSet<>();

            // 1. Resolve Allowed Items & Tags
            for (String itemName : data.allowedItems()) {
                Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemName));
                if (item != Items.AIR) seedItems.add(item);
            }
            for (String tagName : data.allowedTags()) {
                TagKey<Item> tagKey = TagKey.create(BuiltInRegistries.ITEM.key(), ResourceLocation.parse(tagName));
                BuiltInRegistries.ITEM.getTag(tagKey).ifPresent(tag -> tag.forEach(h -> seedItems.add(h.value())));
            }

            // 2. Filter Prohibited Items & Tags
            for (String itemName : data.prohibitedItems()) {
                seedItems.remove(BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemName)));
            }
            for (String tagName : data.prohibitedTags()) {
                TagKey<Item> tagKey = TagKey.create(BuiltInRegistries.ITEM.key(), ResourceLocation.parse(tagName));
                BuiltInRegistries.ITEM.getTag(tagKey).ifPresent(tag -> tag.forEach(h -> seedItems.remove(h.value())));
            }

            // 3. Create individual recipes for each unique seed
            for (Item item : seedItems) {
                ItemStack input = new ItemStack(item);
                // We use a specialized JEI-safe method from Utils
                List<ItemStack> outputs = calculateOutputs(input);
                recipes.add(new JeiFarmerRecipe(input, outputs, data));
            }
        });

        return recipes;
    }



    private static List<ItemStack> calculateOutputs(ItemStack seedStack) {
        Minecraft mc = Minecraft.getInstance();
        List<ItemStack> drops = Utils.getFarmerDrops(mc.level, seedStack);

        for (ItemStack s : drops) {
            if (seedStack.getItem() instanceof BlockItem bi && bi.getBlock() instanceof SaplingBlock) {
                if (s.getItem().toString().contains("log")) s.setCount(1);
            }
        }
        return drops;
    }
}
