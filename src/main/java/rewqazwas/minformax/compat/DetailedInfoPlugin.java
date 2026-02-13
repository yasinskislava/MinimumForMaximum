package rewqazwas.minformax.compat;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import rewqazwas.minformax.MinForMax;
import rewqazwas.minformax.custom.blocks.ModBlocks;
import rewqazwas.minformax.custom.index.ModDataReloadListener;

import java.util.ArrayList;
import java.util.List;

@JeiPlugin
public class DetailedInfoPlugin implements IModPlugin {
    private static IJeiRuntime jeiRuntime;
    private static List<JeiBlockReplicatorRecipe> currentBlockRecipes = new ArrayList<>();
    private static List<JeiFluidReplicatorRecipe> currentFluidRecipes = new ArrayList<>();

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(MinForMax.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new BlockReplicatorCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new FluidReplicatorCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.BLOCK_REPLICATOR.get()), BlockReplicatorCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.FLUID_REPLICATOR.get()), FluidReplicatorCategory.RECIPE_TYPE);
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        DetailedInfoPlugin.jeiRuntime = jeiRuntime;
        MinForMax.LOGGER.info("JEI runtime launched");
    }

    public static void refresh() {
        if (jeiRuntime == null) return;

        try {
            //jeiRuntime.getRecipeManager().hideRecipes(FluidReplicatorCategory.RECIPE_TYPE, currentFluidRecipes);
            currentFluidRecipes = getFluidRecipes();
            jeiRuntime.getRecipeManager().addRecipes(FluidReplicatorCategory.RECIPE_TYPE, currentFluidRecipes);
            MinForMax.LOGGER.info("Refreshed {} fluid recipes", currentFluidRecipes.size());
        } catch (Exception e) {
            MinForMax.LOGGER.error("Failed to refresh fluid recipes", e);
        }

        try {
            //jeiRuntime.getRecipeManager().hideRecipes(BlockReplicatorCategory.RECIPE_TYPE, currentBlockRecipes);
            currentBlockRecipes = getBlockRecipes();
            jeiRuntime.getRecipeManager().addRecipes(BlockReplicatorCategory.RECIPE_TYPE, currentBlockRecipes);
            MinForMax.LOGGER.info("Refreshed {} block recipes", currentBlockRecipes.size());
        } catch (Exception e) {
            MinForMax.LOGGER.error("Failed to refresh block recipes", e);
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
                    if (fluid != null) {
                        recipes.add(new JeiFluidReplicatorRecipe(fluid, data));
                    }
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
}
