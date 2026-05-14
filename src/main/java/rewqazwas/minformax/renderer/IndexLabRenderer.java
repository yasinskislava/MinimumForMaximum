package rewqazwas.minformax.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import rewqazwas.minformax.MinForMax;
import rewqazwas.minformax.custom.blocks.IndexLabBlockEntity;

public class IndexLabRenderer implements BlockEntityRenderer<IndexLabBlockEntity> {
    // You should create a simple glow texture (a white circle with soft edges)
    private static final ResourceLocation GLOW_TEXTURE = ResourceLocation.fromNamespaceAndPath(MinForMax.MOD_ID, "textures/entity/glow.png");

    public IndexLabRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(IndexLabBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        float progress = blockEntity.getProcessPercentage();
        if (progress <= 0) return;

        poseStack.pushPose();

        poseStack.translate(0.5, 0.5, 0.5);
        // --- PULSE MATH ---
        float speed = 5000f;
        float time = (System.currentTimeMillis() % (int)speed) / speed;

        float pulse = 1.0f + (float) Math.sin(time * Math.PI * 10) * 0.4f;

        float scale = (0.5f + (progress * 0.4f)) * pulse;
        poseStack.scale(scale, scale, scale);

        VertexConsumer buffer = bufferSource.getBuffer(RenderType.entityTranslucentEmissive(GLOW_TEXTURE));

        drawQuad(poseStack.last().pose(), buffer, 255, 255, 255, 200);

        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(90));
        drawQuad(poseStack.last().pose(), buffer, 255, 255, 255, 200);

        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(90));
        drawQuad(poseStack.last().pose(), buffer, 255, 255, 255, 200);

        poseStack.popPose();
    }

    private void drawQuad(Matrix4f matrix, VertexConsumer buffer, int r, int g, int b, int a) {
        int overlay = net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY;
        int light = 15728880;

        // Vertex 1
        buffer.addVertex(matrix, -0.5f, -0.5f, 0)
                .setColor(r, g, b, a)
                .setUv(0, 1)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(0, 0, 1);

        // Vertex 2
        buffer.addVertex(matrix, 0.5f, -0.5f, 0)
                .setColor(r, g, b, a)
                .setUv(1, 1)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(0, 0, 1);

        // Vertex 3
        buffer.addVertex(matrix, 0.5f, 0.5f, 0)
                .setColor(r, g, b, a)
                .setUv(1, 0)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(0, 0, 1);

        // Vertex 4
        buffer.addVertex(matrix, -0.5f, 0.5f, 0)
                .setColor(r, g, b, a)
                .setUv(0, 0)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(0, 0, 1);
    }
}