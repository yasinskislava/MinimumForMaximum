package rewqazwas.minformax.compat;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
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
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import rewqazwas.minformax.MinForMax;
import rewqazwas.minformax.custom.blocks.ModBlocks;

import java.util.HashMap;
import java.util.Map;

public class IndexInscriberCategory implements IRecipeCategory<JeiIndexInscriberRecipe> {
    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(MinForMax.MOD_ID, "index_inscriber");
    public static final RecipeType<JeiIndexInscriberRecipe> RECIPE_TYPE = RecipeType.create(MinForMax.MOD_ID, "index_inscriber", JeiIndexInscriberRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final Map<ResourceLocation, Entity> entityCache = new HashMap<>();

    public IndexInscriberCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(ResourceLocation.fromNamespaceAndPath(MinForMax.MOD_ID, "textures/gui/jei_replicator.png"), 0, 0, 150, 80);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.INDEX_LAB.get()));
    }

    @Override
    public RecipeType<JeiIndexInscriberRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("block.minformax.index_inscriber");
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
    public void setRecipe(IRecipeLayoutBuilder builder, JeiIndexInscriberRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.OUTPUT, 80, 10)
                .addItemStack(recipe.data().mainDrop());

        int x = 100;
        for (ItemStack stack : recipe.data().additionalDrop()) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, x, 10)
                    .addItemStack(stack);
            x += 18;
        }
    }

    @Override
    public void draw(JeiIndexInscriberRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        Minecraft minecraft = Minecraft.getInstance();

        guiGraphics.drawString(minecraft.font, Component.translatable("jei.minformax.xp", recipe.data().xp()), 80, 35, 0xFFFFFF, false);
        guiGraphics.drawString(minecraft.font, Component.translatable("jei.minformax.duration", recipe.data().duration()), 80, 45, 0xFFFFFF, false);
        
    }


}
