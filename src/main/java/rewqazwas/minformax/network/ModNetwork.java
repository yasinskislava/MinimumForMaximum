package rewqazwas.minformax.network;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import rewqazwas.minformax.MinForMax;

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
    }
}
