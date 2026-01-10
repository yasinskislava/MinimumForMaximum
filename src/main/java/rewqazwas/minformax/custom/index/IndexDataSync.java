package rewqazwas.minformax.custom.index;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.attachment.AttachmentSyncHandler;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class IndexDataSync implements AttachmentSyncHandler<HashMap<String, HolderClass>> {
    public static final StreamCodec<? super RegistryFriendlyByteBuf, HashMap<String, HolderClass>> STREAM_CODEC = ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, HolderClass.STREAM_CODEC);

    @Override
    public void write(RegistryFriendlyByteBuf buf, HashMap<String, HolderClass> attachment, boolean initialSync) {
        STREAM_CODEC.encode(buf, attachment);
    }

    @Override
    public @Nullable HashMap<String, HolderClass> read(IAttachmentHolder holder, RegistryFriendlyByteBuf buf, @Nullable HashMap<String, HolderClass> previous) {
        return STREAM_CODEC.decode(buf);
    }
}
