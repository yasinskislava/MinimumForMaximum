package rewqazwas.minformax.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import rewqazwas.minformax.custom.blocks.EternalGeneratorBlockEntity;

import java.util.HashMap;
import java.util.Map;

import static rewqazwas.minformax.custom.utility.Utils.convertKey;

public class EternalGeneratorRenderer implements BlockEntityRenderer<EternalGeneratorBlockEntity> {
    private final Map<String, Entity> entityCache = new HashMap<>();

    public EternalGeneratorRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(EternalGeneratorBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {String rawName = blockEntity.getMobNameInShard(); // This is "entity.minecraft.chicken"
        if (rawName == null || blockEntity.getLevel() == null) return;

        String mobName = convertKey(rawName);
        Entity entity = entityCache.computeIfAbsent(mobName, name -> {
            try {
                ResourceLocation loc = ResourceLocation.parse(name);
                return BuiltInRegistries.ENTITY_TYPE.getOptional(loc)
                        .map(type -> type.create(blockEntity.getLevel()))
                        .orElse(null);
            } catch (Exception e) {
                return null;
            }
        });
        if (entity == null) return;

        poseStack.pushPose();

        poseStack.translate(0.5, 0.2, 0.5);

        poseStack.mulPose(Axis.YP.rotationDegrees(45f));

        // Scale logic
        float scale = 0.4f;
        float entityHeight = entity.getBbHeight();
        if (entityHeight > 1.0f) {
            scale /= entityHeight;
        }
        poseStack.scale(scale, scale, scale);

        // Set entity's previous position and rotation to current to prevent vibration
        entity.xo = entity.getX();
        entity.yo = entity.getY();
        entity.zo = entity.getZ();
        entity.xRotO = entity.getXRot();
        entity.yRotO = entity.getYRot();

        // Render without shadows
        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        dispatcher.setRenderShadow(false);
        dispatcher.render(entity, 0.0D, 0.0D, 0.0D, 0.0F, 0.0F, poseStack, bufferSource, LightTexture.FULL_BRIGHT);
        dispatcher.setRenderShadow(true);

        poseStack.popPose();
    }
}