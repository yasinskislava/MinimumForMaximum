package rewqazwas.minformax.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import rewqazwas.minformax.MinForMax;

public record RequestIndexSyncPayload() implements CustomPacketPayload {
    public static final Type<RequestIndexSyncPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MinForMax.MOD_ID, "request_index_sync"));

    public static final StreamCodec<FriendlyByteBuf, RequestIndexSyncPayload> STREAM_CODEC = StreamCodec.unit(new RequestIndexSyncPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}