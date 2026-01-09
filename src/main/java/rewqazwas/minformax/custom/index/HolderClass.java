package rewqazwas.minformax.custom.index;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record HolderClass(ItemStack mainDrop, List<ItemStack> additionalDrop, int xpAmount, int duration) {
    public static final Codec<HolderClass> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.CODEC.fieldOf("main_drop").forGetter(HolderClass::mainDrop),
            Codec.list(ItemStack.CODEC).fieldOf("additional_drop").forGetter(HolderClass::additionalDrop),
            Codec.INT.fieldOf("xp_amount").forGetter(HolderClass::xpAmount),
            Codec.INT.fieldOf("duration").forGetter(HolderClass::duration)
    ).apply(instance, HolderClass::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, HolderClass> STREAM_CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC, HolderClass::mainDrop,
            ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list(3)), HolderClass::additionalDrop,
            ByteBufCodecs.INT, HolderClass::xpAmount,
            ByteBufCodecs.INT, HolderClass::duration,
            HolderClass::new
    );

}
