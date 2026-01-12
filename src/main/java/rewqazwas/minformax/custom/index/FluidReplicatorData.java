package rewqazwas.minformax.custom.index;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record FluidReplicatorData(int basicAmountGenerated, int duration, int energyMultiplier) {
    public static final Codec<FluidReplicatorData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("basic_amount_generated").forGetter(FluidReplicatorData::basicAmountGenerated),
            Codec.INT.fieldOf("duration").forGetter(FluidReplicatorData::duration),
            Codec.INT.fieldOf("energy_multiplier").forGetter(FluidReplicatorData::energyMultiplier)
    ).apply(instance, FluidReplicatorData::new));
}
