package rewqazwas.minformax.custom.index;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

public record FarmerData(List<String> allowedItems, List<String> prohibitedItems, List<String> allowedTags, List<String> prohibitedTags) {
    public static final Codec<FarmerData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.listOf().fieldOf("allowed_items").forGetter(FarmerData::allowedItems),
            Codec.STRING.listOf().fieldOf("prohibited_items").forGetter(FarmerData::prohibitedItems),
            Codec.STRING.listOf().fieldOf("allowed_tags").forGetter(FarmerData::allowedTags),
            Codec.STRING.listOf().fieldOf("prohibited_tags").forGetter(FarmerData::prohibitedTags)
    ).apply(instance, FarmerData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, FarmerData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), FarmerData::allowedItems,
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), FarmerData::prohibitedItems,
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), FarmerData::allowedTags,
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), FarmerData::prohibitedTags,
            FarmerData::new
    );
}
