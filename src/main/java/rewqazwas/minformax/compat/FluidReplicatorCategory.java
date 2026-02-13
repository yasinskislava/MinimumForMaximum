package rewqazwas.minformax.compat;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.neoforge.NeoForgeTypes;
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
import net.neoforged.neoforge.fluids.FluidStack;
import rewqazwas.minformax.MinForMax;
import rewqazwas.minformax.custom.blocks.ModBlocks;
import rewqazwas.minformax.custom.utility.Utils;

public class FluidReplicatorCategory implements IRecipeCategory<JeiFluidReplicatorRecipe> {
    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(MinForMax.MOD_ID, "fluid_replicator");
    public static final RecipeType<JeiFluidReplicatorRecipe> RECIPE_TYPE = RecipeType.create(MinForMax.MOD_ID, "fluid_replicator", JeiFluidReplicatorRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;

    public FluidReplicatorCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(ResourceLocation.fromNamespaceAndPath(MinForMax.MOD_ID, "textures/gui/jei_replicator.png"), 0, 0, 150, 60);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.FLUID_REPLICATOR.get()));
    }

    @Override
    public RecipeType<JeiFluidReplicatorRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("block.minformax.fluid_replicator");
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
    public void setRecipe(IRecipeLayoutBuilder builder, JeiFluidReplicatorRecipe recipe, IFocusGroup focuses) {
            builder.addSlot(RecipeIngredientRole.INPUT, 10, 10)
                    .addIngredient(NeoForgeTypes.FLUID_STACK, new FluidStack(recipe.fluid(), 1000));

            builder.addSlot(RecipeIngredientRole.OUTPUT, 10, 10)
                    .addIngredient(NeoForgeTypes.FLUID_STACK, new FluidStack(recipe.fluid(), recipe.data().basicAmountGenerated()));
    }

    @Override
    public void draw(JeiFluidReplicatorRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        Minecraft minecraft = Minecraft.getInstance();
        
        guiGraphics.drawString(minecraft.font, Component.translatable("jei.minformax.duration", recipe.data().duration()), 10, 35, 0xFFFFFF, false);
        guiGraphics.drawString(minecraft.font, Component.translatable("jei.minformax.energy", recipe.data().energyMultiplier()), 10, 45, 0xFFFFFF, false);
        guiGraphics.drawString(minecraft.font, Component.literal(Utils.simpleFluidDisplay(recipe.data().basicAmountGenerated())), 28, 20, 0xFFFFFF, false);
    }
}
