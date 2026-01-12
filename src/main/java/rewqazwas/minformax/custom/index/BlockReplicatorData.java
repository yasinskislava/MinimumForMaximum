package rewqazwas.minformax.custom.index;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record BlockReplicatorData(int basicAmountGenerated, int duration, int energyMultiplier) {
    public static final Codec<BlockReplicatorData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("basic_amount_generated").forGetter(BlockReplicatorData::basicAmountGenerated),
            Codec.INT.fieldOf("duration").forGetter(BlockReplicatorData::duration),
            Codec.INT.fieldOf("energy_multiplier").forGetter(BlockReplicatorData::energyMultiplier)
    ).apply(instance, BlockReplicatorData::new));
}
