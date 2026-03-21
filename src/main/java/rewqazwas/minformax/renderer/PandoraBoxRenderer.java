package rewqazwas.minformax.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import rewqazwas.minformax.custom.blocks.PandoraBox.PandoraBoxCoreBlockEntity;

public class PandoraBoxRenderer implements BlockEntityRenderer<PandoraBoxCoreBlockEntity> {
    private static final ResourceLocation BEAM_LOCATION = ResourceLocation.parse("textures/entity/end_portal.png");

    public PandoraBoxRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(PandoraBoxCoreBlockEntity be, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        long gameTime = be.getLevel().getGameTime();
        
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);

        float time = gameTime + partialTicks;
        float intensity = 0.02f;
        float ox = (float)Math.sin(time * 0.8f) * intensity;
        float oy = (float)Math.cos(time * 0.7f) * intensity;
        float oz = (float)Math.sin(time * 0.9f) * intensity;
        
        poseStack.translate(ox, oy, oz);

        float scale = 0.65f + (float)Math.sin(time * 0.1f) * 0.05f;
        poseStack.scale(scale, scale, scale);

        VertexConsumer vertexconsumer = buffer.getBuffer(RenderType.entitySolid(BEAM_LOCATION));
        renderCube(poseStack, vertexconsumer, 1.0f, 1.0f, 1.0f, 1.0f);

        poseStack.popPose();
    }

    private static void renderCube(PoseStack poseStack, VertexConsumer consumer, float r, float g, float b, float a) {
        Matrix4f matrix = poseStack.last().pose();
        float min = -0.5f;
        float max = 0.5f;

        // Down
        addVertex(matrix, consumer, r, g, b, a, min, min, min, 0, 0, 0, -1, 0);
        addVertex(matrix, consumer, r, g, b, a, min, max, min, 1, 0, 0, -1, 0);
        addVertex(matrix, consumer, r, g, b, a, min, max, max, 1, 1, 0, -1, 0);
        addVertex(matrix, consumer, r, g, b, a, min, min, max, 0, 1, 0, -1, 0);

        // Up
        addVertex(matrix, consumer, r, g, b, a, max, min, max, 0, 1, 0, 1, 0);
        addVertex(matrix, consumer, r, g, b, a, max, max, max, 1, 1, 0, 1, 0);
        addVertex(matrix, consumer, r, g, b, a, max, max, min, 1, 0, 0, 1, 0);
        addVertex(matrix, consumer, r, g, b, a, max, min, min, 0, 0, 0, 1, 0);

        // North
        addVertex(matrix, consumer, r, g, b, a, min, min, min, 1, 1, 0, 0, -1);
        addVertex(matrix, consumer, r, g, b, a, max, min, min, 0, 1, 0, 0, -1);
        addVertex(matrix, consumer, r, g, b, a, max, max, min, 0, 0, 0, 0, -1);
        addVertex(matrix, consumer, r, g, b, a, min, max, min, 1, 0, 0, 0, -1);

        // South
        addVertex(matrix, consumer, r, g, b, a, min, max, max, 0, 0, 0, 0, 1);
        addVertex(matrix, consumer, r, g, b, a, max, max, max, 1, 0, 0, 0, 1);
        addVertex(matrix, consumer, r, g, b, a, max, min, max, 1, 1, 0, 0, 1);
        addVertex(matrix, consumer, r, g, b, a, min, min, max, 0, 1, 0, 0, 1);

        // West
        addVertex(matrix, consumer, r, g, b, a, min, min, max, 1, 1, -1, 0, 0);
        addVertex(matrix, consumer, r, g, b, a, max, min, max, 0, 1, -1, 0, 0);
        addVertex(matrix, consumer, r, g, b, a, max, min, min, 0, 0, -1, 0, 0);
        addVertex(matrix, consumer, r, g, b, a, min, min, min, 1, 0, -1, 0, 0);

        // East
        addVertex(matrix, consumer, r, g, b, a, min, max, min, 0, 0, 1, 0, 0);
        addVertex(matrix, consumer, r, g, b, a, max, max, min, 1, 0, 1, 0, 0);
        addVertex(matrix, consumer, r, g, b, a, max, max, max, 1, 1, 1, 0, 0);
        addVertex(matrix, consumer, r, g, b, a, min, max, max, 0, 1, 1, 0, 0);
    }

    private static void addVertex(Matrix4f matrix, VertexConsumer consumer, float red, float green, float blue, float alpha, float y, float x, float z, float u, float v, float nx, float ny, float nz) {
        consumer.addVertex(matrix, x, y, z)
                .setColor(red, green, blue, alpha)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(15728880)
                .setNormal(nx, ny, nz);
    }
}
