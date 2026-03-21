package rewqazwas.minformax.compat;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import rewqazwas.minformax.MinForMax;
import rewqazwas.minformax.custom.blocks.ModBlocks;

public class FarmerCategory implements IRecipeCategory<JeiFarmerRecipe> {
    public static final RecipeType<JeiFarmerRecipe> RECIPE_TYPE = RecipeType.create(MinForMax.MOD_ID, "farmer", JeiFarmerRecipe.class);
    private static final ResourceLocation SLOT = ResourceLocation.fromNamespaceAndPath(MinForMax.MOD_ID, "textures/gui/slot.png");

    private final IDrawable background;
    private final IDrawable icon;

    public FarmerCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(ResourceLocation.fromNamespaceAndPath(MinForMax.MOD_ID, "textures/gui/jei_replicator.png"), 0, 0, 150, 60);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.FARMER.get()));
    }

    @Override
    public RecipeType<JeiFarmerRecipe> getRecipeType() { return RECIPE_TYPE; }

    @Override
    public Component getTitle() { return Component.translatable("block.minformax.farmer"); }

    @Override
    public IDrawable getBackground() { return background; }

    @Override
    public IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, JeiFarmerRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 10, 10)
                .addItemStack(recipe.input());

        builder.addSlot(RecipeIngredientRole.OUTPUT, 120, 10)
                .addItemStacks(recipe.outputs());
    }

    @Override
    public void draw(JeiFarmerRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        guiGraphics.blit(SLOT, 119,  9, 0, 0, 18, 18, 18, 18);
        IRecipeCategory.super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);
    }
}