package rewqazwas.minformax.network;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import rewqazwas.minformax.custom.blocks.EternalGeneratorBlockEntity;
import rewqazwas.minformax.custom.blocks.FarmerBlockEntity;
import rewqazwas.minformax.custom.blocks.OreCoalescerBlockEntity;

public record SideConfigPayload(BlockPos pos, int directionIndex) implements CustomPacketPayload {
    public static final Type<SideConfigPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("minformax", "side_config"));

    public static final StreamCodec<FriendlyByteBuf, SideConfigPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, SideConfigPayload::pos,
            ByteBufCodecs.VAR_INT, SideConfigPayload::directionIndex,
            SideConfigPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(SideConfigPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            var check = context.player().level().getBlockEntity(payload.pos);
            if (check instanceof EternalGeneratorBlockEntity be) {
                be.toggleSide(Direction.values()[payload.directionIndex]);
            } else if (check instanceof FarmerBlockEntity be) {
                be.toggleSide(Direction.values()[payload.directionIndex]);
            } else if (check instanceof OreCoalescerBlockEntity be) {
                be.toggleSide(Direction.values()[payload.directionIndex]);
            }
        });
    }
}