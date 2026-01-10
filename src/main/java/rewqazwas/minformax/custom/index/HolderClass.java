package rewqazwas.minformax.custom.index;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record HolderClass(ItemStack mainDrop, List<ItemStack> additionalDrop, int xp, int duration) {
    private static final Codec<ItemStack> ITEM_STACK_CODEC = Codec.STRING.xmap(
            s -> new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(s))),
            itemStack -> BuiltInRegistries.ITEM.getKey(itemStack.getItem()).toString()
    );

    public static final Codec<HolderClass> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ITEM_STACK_CODEC.fieldOf("main_drop").forGetter(HolderClass::mainDrop),
            Codec.list(ITEM_STACK_CODEC).fieldOf("additional_drop").forGetter(HolderClass::additionalDrop),
            Codec.INT.fieldOf("xp").forGetter(HolderClass::xp),
            Codec.INT.fieldOf("duration").forGetter(HolderClass::duration)
    ).apply(instance, HolderClass::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, HolderClass> STREAM_CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC, HolderClass::mainDrop,
            ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list(3)), HolderClass::additionalDrop,
            ByteBufCodecs.INT, HolderClass::xp,
            ByteBufCodecs.INT, HolderClass::duration,
            HolderClass::new
    );
}
