package rewqazwas.minformax.custom.index;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record BlockReplicatorData(int duration, int energyMultiplier) {
    public static final Codec<BlockReplicatorData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("duration").forGetter(BlockReplicatorData::duration),
            Codec.INT.fieldOf("energy_multiplier").forGetter(BlockReplicatorData::energyMultiplier)
    ).apply(instance, BlockReplicatorData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, BlockReplicatorData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, BlockReplicatorData::duration,
            ByteBufCodecs.INT, BlockReplicatorData::energyMultiplier,
            BlockReplicatorData::new
    );
}
