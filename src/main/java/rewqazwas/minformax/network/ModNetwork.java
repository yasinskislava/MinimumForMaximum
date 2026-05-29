package rewqazwas.minformax.network;

import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import rewqazwas.minformax.custom.index.PlayerIndex;
import rewqazwas.minformax.network.packet.PayloadKeybindPacket;
import rewqazwas.minformax.network.packet.RequestIndexSyncPayload;
import rewqazwas.minformax.network.packet.SideConfigPayload;
import rewqazwas.minformax.network.packet.SyncJeiDataPacket;
import rewqazwas.minformax.screen.custom.MobIndexScreen;

@EventBusSubscriber
public class ModNetwork {
    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");

        registrar.playToClient(
                SyncJeiDataPacket.TYPE,
                SyncJeiDataPacket.STREAM_CODEC,
                SyncJeiDataPacket::handle
        );

        registrar.playToServer(
                SideConfigPayload.TYPE,
                SideConfigPayload.STREAM_CODEC,
                SideConfigPayload::handle
        );

        registrar.playToServer(
                RequestIndexSyncPayload.TYPE,
                RequestIndexSyncPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                        java.util.List<String> scanned = PlayerIndex.getLocalIndex(serverPlayer);
                        PacketDistributor.sendToPlayer(serverPlayer, new PayloadKeybindPacket(scanned));
                    }
                })
        );

        registrar.playToClient(
                PayloadKeybindPacket.TYPE,
                PayloadKeybindPacket.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> ClientPayloadHandler.handlePayloadKeybind(payload))
        );
    }

    private static class ClientPayloadHandler {
        public static void handlePayloadKeybind(PayloadKeybindPacket payload) {
            Minecraft.getInstance().setScreen(new MobIndexScreen(payload.scannedMobs()));
        }
    }
}