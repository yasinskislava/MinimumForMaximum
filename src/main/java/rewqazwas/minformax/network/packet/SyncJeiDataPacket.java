package rewqazwas.minformax.network.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import rewqazwas.minformax.MinForMax;
import rewqazwas.minformax.compat.DetailedInfoPlugin;
import rewqazwas.minformax.custom.index.*;

import java.util.HashMap;
import java.util.Map;

public record SyncJeiDataPacket(
        Map<String, HolderClass> mobDrops,
        Map<String, FluidReplicatorData> fluidReplicatorData,
        Map<String, BlockReplicatorData> blockReplicatorData,
        Map<String, FarmerData> farmerData) implements CustomPacketPayload {

    public static final Type<SyncJeiDataPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MinForMax.MOD_ID, "sync_jei_data"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncJeiDataPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, HolderClass.STREAM_CODEC),
            SyncJeiDataPacket::mobDrops,
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, FluidReplicatorData.STREAM_CODEC),
            SyncJeiDataPacket::fluidReplicatorData,
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, BlockReplicatorData.STREAM_CODEC),
            SyncJeiDataPacket::blockReplicatorData,
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, FarmerData.STREAM_CODEC),
            SyncJeiDataPacket::farmerData,
            SyncJeiDataPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncJeiDataPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            MinForMax.LOGGER.info("Received JEI data sync packet on client");
            MinForMax.LOGGER.info("Data sizes - Mob: {}, Fluid: {}, Block: {}, Farmer: {}",
                    packet.mobDrops.size(),
                    packet.fluidReplicatorData.size(),
                    packet.blockReplicatorData.size(),
                    packet.farmerData.size()
            );

            // Sync all maps to the client-side data listener
            ModDataReloadListener.MOB_DROPS = packet.mobDrops;
            ModDataReloadListener.FLUID_REPLICATOR_DATA = packet.fluidReplicatorData;
            ModDataReloadListener.BLOCK_REPLICATOR_DATA = packet.blockReplicatorData;
            ModDataReloadListener.FARMER_DATA = packet.farmerData;

            DetailedInfoPlugin.refresh();
        });
    }
}