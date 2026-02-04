package rewqazwas.minformax.compat;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
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
    public void registerRecipes(IRecipeRegistration registration) {
        List<JeiBlockReplicatorRecipe> blockRecipes = new ArrayList<>();
        ModDataReloadListener.BLOCK_REPLICATOR_DATA.forEach((key, data) -> {
            if (key.startsWith("#")) {
                var tagKey = TagKey.create(BuiltInRegistries.BLOCK.key(), ResourceLocation.parse(key.substring(1)));
                List<ItemStack> items = new ArrayList<>();
                BuiltInRegistries.BLOCK.getTag(tagKey).ifPresent(tag -> {
                    for (var holder : tag) {
                        items.add(new ItemStack(holder.value()));
                    }
                });
                if (!items.isEmpty()) {
                    blockRecipes.add(new JeiBlockReplicatorRecipe(items, data));
                }
            } else {
                var resourceLocation = ResourceLocation.parse(key);
                if (BuiltInRegistries.ITEM.containsKey(resourceLocation)) {
                    var item = BuiltInRegistries.ITEM.get(resourceLocation);
                    if (item != Items.AIR) {
                        blockRecipes.add(new JeiBlockReplicatorRecipe(List.of(new ItemStack(item)), data));
                    }
                }
            }
        });
        registration.addRecipes(BlockReplicatorCategory.RECIPE_TYPE, blockRecipes);

        List<JeiFluidReplicatorRecipe> fluidRecipes = new ArrayList<>();
        ModDataReloadListener.FLUID_REPLICATOR_DATA.forEach((key, data) -> {
            var resourceLocation = ResourceLocation.parse(key);
            if (BuiltInRegistries.FLUID.containsKey(resourceLocation)) {
                var fluid = BuiltInRegistries.FLUID.get(resourceLocation);
                if (fluid != null) {
                    fluidRecipes.add(new JeiFluidReplicatorRecipe(fluid, data));
                }
            }
        });
        registration.addRecipes(FluidReplicatorCategory.RECIPE_TYPE, fluidRecipes);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.BLOCK_REPLICATOR.get()), BlockReplicatorCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.FLUID_REPLICATOR.get()), FluidReplicatorCategory.RECIPE_TYPE);
    }
}
