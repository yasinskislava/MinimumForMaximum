package rewqazwas.minformax.custom.index;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record FluidReplicatorData(int basicAmountGenerated, int duration, int energyMultiplier) {
    public static final Codec<FluidReplicatorData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("basic_amount_generated").forGetter(FluidReplicatorData::basicAmountGenerated),
            Codec.INT.fieldOf("duration").forGetter(FluidReplicatorData::duration),
            Codec.INT.fieldOf("energy_multiplier").forGetter(FluidReplicatorData::energyMultiplier)
    ).apply(instance, FluidReplicatorData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, FluidReplicatorData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, FluidReplicatorData::basicAmountGenerated,
            ByteBufCodecs.INT, FluidReplicatorData::duration,
            ByteBufCodecs.INT, FluidReplicatorData::energyMultiplier,
            FluidReplicatorData::new
    );
}
