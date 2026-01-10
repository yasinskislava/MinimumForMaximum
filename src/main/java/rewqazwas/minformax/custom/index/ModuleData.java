package rewqazwas.minformax.custom.index;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

public record ModuleData(List<String> allowedItems, List<String> prohibitedItems, List<String> allowedTags, List<String> prohibitedTags, int xp) {
    public static final Codec<ModuleData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.list(Codec.STRING).fieldOf("allowed_items").forGetter(ModuleData::allowedItems),
            Codec.list(Codec.STRING).fieldOf("prohibited_items").forGetter(ModuleData::prohibitedItems),
            Codec.list(Codec.STRING).fieldOf("allowed_tags").forGetter(ModuleData::allowedTags),
            Codec.list(Codec.STRING).fieldOf("prohibited_tags").forGetter(ModuleData::prohibitedTags),
            Codec.INT.fieldOf("xp").forGetter(ModuleData::xp)
    ).apply(instance, ModuleData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ModuleData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), ModuleData::allowedItems,
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), ModuleData::prohibitedItems,
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), ModuleData::allowedTags,
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), ModuleData::prohibitedTags,
            ByteBufCodecs.INT, ModuleData::xp,
            ModuleData::new
    );
}
