package rewqazwas.minformax.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.textures.FluidSpriteCache;
import net.neoforged.neoforge.fluids.FluidStack;
import rewqazwas.minformax.config.DataConfigs;
import rewqazwas.minformax.custom.blocks.FluidReplicatorBlockEntity;

public class FluidReplicatorRenderer implements BlockEntityRenderer<FluidReplicatorBlockEntity> {
    public FluidReplicatorRenderer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public void render(FluidReplicatorBlockEntity blockEntity, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        if (!DataConfigs.shouldRender.get()) {
            return;
        }

        FluidStack fluidStack = blockEntity.fluidHandler.getFluid();
        if (fluidStack.isEmpty()) {
            return;
        }

        Level level = blockEntity.getLevel();
        if (level == null) return;

        BlockPos pos = blockEntity.getBlockPos();

        IClientFluidTypeExtensions attributes = IClientFluidTypeExtensions.of(fluidStack.getFluid());
        ResourceLocation stillTexture = attributes.getStillTexture(fluidStack);

        FluidState state = fluidStack.getFluid().defaultFluidState();
        TextureAtlasSprite fluidSprite = FluidSpriteCache.getSprite(stillTexture);


        int tintColor = attributes.getTintColor(state, level, pos);

        VertexConsumer builder = bufferSource.getBuffer(ItemBlockRenderTypes.getRenderLayer(state));

        poseStack.pushPose();
        
        float u0 = fluidSprite.getU0();
        float u1 = fluidSprite.getU1();
        float v0 = fluidSprite.getV0();
        float v1 = fluidSprite.getV1();

        float min = 0.001f;
        float max = 0.999f;

        renderFace(builder, poseStack, Direction.UP, min, max, u0, u1, v0, v1, tintColor, combinedLight);
        renderFace(builder, poseStack, Direction.DOWN, min, max, u0, u1, v0, v1, tintColor, combinedLight);
        renderFace(builder, poseStack, Direction.NORTH, min, max, u0, u1, v0, v1, tintColor, combinedLight);
        renderFace(builder, poseStack, Direction.SOUTH, min, max, u0, u1, v0, v1, tintColor, combinedLight);
        renderFace(builder, poseStack, Direction.WEST, min, max, u0, u1, v0, v1, tintColor, combinedLight);
        renderFace(builder, poseStack, Direction.EAST, min, max, u0, u1, v0, v1, tintColor, combinedLight);

        poseStack.popPose();
    }

    private void renderFace(VertexConsumer builder, PoseStack poseStack, Direction direction, float min, float max, float u0, float u1, float v0, float v1, int color, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0.5f, 0.5f, 0.5f);

        switch (direction) {
            case DOWN -> poseStack.mulPose(Axis.XP.rotationDegrees(180));
            case NORTH -> {
                poseStack.mulPose(Axis.XP.rotationDegrees(90));
                poseStack.mulPose(Axis.YP.rotationDegrees(180));
            }
            case SOUTH -> poseStack.mulPose(Axis.XP.rotationDegrees(-90));
            case WEST -> {
                poseStack.mulPose(Axis.ZP.rotationDegrees(90));
                poseStack.mulPose(Axis.YP.rotationDegrees(90));
            }
            case EAST -> {
                poseStack.mulPose(Axis.ZP.rotationDegrees(-90));
                poseStack.mulPose(Axis.YP.rotationDegrees(-90));
            }
        }

        poseStack.translate(-0.5f, -0.5f, -0.5f);

        addVertex(builder, poseStack, min, max, min, u0, v0, color, packedLight);
        addVertex(builder, poseStack, min, max, max, u0, v1, color, packedLight);
        addVertex(builder, poseStack, max, max, max, u1, v1, color, packedLight);
        addVertex(builder, poseStack, max, max, min, u1, v0, color, packedLight);

        poseStack.popPose();
    }

    private void addVertex(VertexConsumer builder, PoseStack poseStack, float x, float y, float z, float u, float v, int color, int packedLight) {
        builder.addVertex(poseStack.last().pose(), x, y, z)
                .setColor(color)
                .setUv(u, v)
                .setLight(packedLight)
                .setNormal(poseStack.last(),0, 1, 0);
    }
}