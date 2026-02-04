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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import rewqazwas.minformax.MinForMax;
import rewqazwas.minformax.custom.blocks.ModBlocks;

public class BlockReplicatorCategory implements IRecipeCategory<JeiBlockReplicatorRecipe> {
    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(MinForMax.MOD_ID, "block_replicator");
    public static final RecipeType<JeiBlockReplicatorRecipe> RECIPE_TYPE = RecipeType.create(MinForMax.MOD_ID, "block_replicator", JeiBlockReplicatorRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;

    public BlockReplicatorCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(ResourceLocation.fromNamespaceAndPath(MinForMax.MOD_ID, "textures/gui/jei_replicator.png"), 0, 0, 150, 60);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.BLOCK_REPLICATOR.get()));
    }

    @Override
    public RecipeType<JeiBlockReplicatorRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("block.minformax.block_replicator");
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, JeiBlockReplicatorRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 10, 10)
                .addItemStacks(recipe.items());

        builder.addSlot(RecipeIngredientRole.OUTPUT, 10, 10)
                .addItemStacks(recipe.items());
    }

    @Override
    public void draw(JeiBlockReplicatorRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        Minecraft minecraft = Minecraft.getInstance();

        guiGraphics.drawString(minecraft.font, Component.translatable("jei.minformax.duration", recipe.data().duration()), 10, 35, 0xFFFFFF, false);
        guiGraphics.drawString(minecraft.font, Component.translatable("jei.minformax.energy", recipe.data().energyMultiplier()), 10, 45, 0xFFFFFF, false);
    }
}
