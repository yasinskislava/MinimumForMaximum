package rewqazwas.minformax.custom;

import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import rewqazwas.minformax.MinForMax;
import rewqazwas.minformax.custom.index.HolderClass;
import rewqazwas.minformax.custom.index.IndexDataSync;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Supplier;

public class ModAttachmentTypes {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MinForMax.MOD_ID);

    public static final Supplier<AttachmentType<HashMap<String, HolderClass>>> INDEX_SYNC = ATTACHMENT_TYPES.register(
            "index_sync", () -> AttachmentType.builder(() -> new HashMap<String, HolderClass>())
                    .sync(new IndexDataSync())
                    .build()
    );
}
