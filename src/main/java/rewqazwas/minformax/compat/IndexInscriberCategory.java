package rewqazwas.minformax.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
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
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import rewqazwas.minformax.MinForMax;
import rewqazwas.minformax.custom.blocks.ModBlocks;

import java.util.List;

public class IndexInscriberCategory implements IRecipeCategory<JeiIndexInscriberRecipe> {
    public static final RecipeType<JeiIndexInscriberRecipe> RECIPE_TYPE =
            RecipeType.create(MinForMax.MOD_ID, "index_inscriber", JeiIndexInscriberRecipe.class);
    private static final ResourceLocation SLOT = ResourceLocation.fromNamespaceAndPath(MinForMax.MOD_ID, "textures/gui/slot.png");

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable slot;

    public IndexInscriberCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(176, 120);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.INDEX_INSCRIBER.get()));
        this.slot = guiHelper.createDrawable(SLOT, -1, -1, 18, 18);
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
        return this.background;
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, JeiIndexInscriberRecipe recipe, IFocusGroup focuses) {
        // Main Drop slot
        if(recipe.getData().mainDrop().getItem() != Items.AIR) builder.addSlot(RecipeIngredientRole.OUTPUT, 116, 20).addItemStack(recipe.getData().mainDrop());

        // Additional Drops slots
        List<ItemStack> additionalDrops = recipe.getData().additionalDrop();
        for (int i = 0; i < additionalDrops.size(); i++) {
            int x = 116 + (i % 4) * 18;
            int y = 58 + (i / 4) * 18;
            if(additionalDrops.get(i).getItem() != Items.AIR) {
                builder.addSlot(RecipeIngredientRole.OUTPUT, x, y).addItemStack(additionalDrops.get(i));
            }
        }
    }

    @Override
    public void draw(JeiIndexInscriberRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        renderEntity(guiGraphics, 40, 100, recipe.getMobId(), mouseX, mouseY);
        drawInfo(guiGraphics, recipe, recipe.getMobId());
    }

    private void renderEntity(GuiGraphics guiGraphics, int x, int y, ResourceLocation entityId, double mouseX, double mouseY) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(entityId);
        if (entityType != null) {
            Entity entity = entityType.create(mc.level);
            if (entity != null) {
                // Use vanilla's logic for entity rotation based on mouse position
                float yawAngle = (float) Math.atan((x - mouseX) / 40.0F);
                float pitchAngle = (float) Math.atan(((y - 50) - mouseY) / 40.0F);

                if (entity instanceof LivingEntity livingEntity) {
                    livingEntity.yBodyRot = 180.0F + yawAngle * 20.0F;
                    livingEntity.setYRot(180.0F + yawAngle * 40.0F);
                    livingEntity.setXRot(-pitchAngle * 20.0F);
                    livingEntity.yHeadRot = livingEntity.getYRot();
                } else {
                    entity.setYRot(180.0F + yawAngle * 40.0F);
                    entity.setXRot(-pitchAngle * 20.0F);
                }

                PoseStack poseStack = guiGraphics.pose();
                poseStack.pushPose();
                poseStack.translate(x, y, 50.0D);

                // Calculate dynamic scale
                float entityHeight = entity.getBbHeight();
                float entityWidth = entity.getBbWidth();
                float maxDimension = Math.max(entityHeight, entityWidth);
                float baseScale = 40.0F / maxDimension;
                float finalScale = Math.min(baseScale, 35.0F);
                finalScale = Math.max(finalScale, 10.0F);

                poseStack.scale(finalScale, finalScale, -finalScale); // Negative Z to face camera, which inverts the model
                poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F)); // Correct the inversion from the Z-flip

                // Render the entity
                EntityRenderDispatcher entityRenderer = mc.getEntityRenderDispatcher();
                MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
                entityRenderer.render(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, poseStack, bufferSource, 15728880); // 15728880 is full brightness
                bufferSource.endBatch();
                poseStack.popPose();
            }
        }
    }

    private void drawInfo(GuiGraphics guiGraphics, JeiIndexInscriberRecipe recipe, ResourceLocation entityId) {
        Minecraft mc = Minecraft.getInstance();
        guiGraphics.drawString(mc.font, Component.translatable("jei.minformax.duration", recipe.getData().duration()), 10, 10, 0xFF404040, false);
        guiGraphics.drawString(mc.font, Component.translatable("jei.minformax.xp", recipe.getData().xp()), 10, 20, 0xFF404040, false);

        // Display mob name
        EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(entityId);
        if (entityType != null) {
            guiGraphics.drawString(mc.font, entityType.getDescription(), 10, 30, 0xFF404040, false);
        }

        // New text labels for drops
        guiGraphics.drawString(mc.font, Component.translatable("jei.minformax.main_drop"), 116, 10, 0xFF404040, false);
        guiGraphics.drawString(mc.font, Component.translatable("jei.minformax.additional_drops"), 116, 48, 0xFF404040, false);
    }
}
