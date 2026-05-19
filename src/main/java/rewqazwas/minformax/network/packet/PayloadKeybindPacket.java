package rewqazwas.minformax.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import rewqazwas.minformax.MinForMax;

import java.util.ArrayList;
import java.util.List;

public record PayloadKeybindPacket(List<String> scannedMobs) implements CustomPacketPayload {
    public static final Type<PayloadKeybindPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MinForMax.MOD_ID, "sync_player_index"));

    public static final StreamCodec<FriendlyByteBuf, PayloadKeybindPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.STRING_UTF8),
            PayloadKeybindPacket::scannedMobs,
            PayloadKeybindPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}