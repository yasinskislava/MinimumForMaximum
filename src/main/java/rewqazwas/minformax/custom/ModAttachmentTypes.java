package rewqazwas.minformax.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import rewqazwas.minformax.MinForMax;
import rewqazwas.minformax.custom.index.HolderClass;
import rewqazwas.minformax.custom.index.IndexDataSync;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ModAttachmentTypes {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MinForMax.MOD_ID);

    public static final Supplier<AttachmentType<HashMap<String, HolderClass>>> INDEX_SYNC = ATTACHMENT_TYPES.register(
            "index_sync", () -> AttachmentType.builder(() -> new HashMap<String, HolderClass>())
                    .sync(new IndexDataSync())
                    .build()
    );

    public static final Supplier<AttachmentType<Map<BlockPos, BlockPos>>> MULTIBLOCK_PROXY = ATTACHMENT_TYPES.register(
            "multiblock_proxy", () -> AttachmentType.builder(() -> (Map<BlockPos, BlockPos>) new HashMap<BlockPos, BlockPos>())
                    .serialize(new IAttachmentSerializer<CompoundTag, Map<BlockPos, BlockPos>>() {
                        @Override
                        public Map<BlockPos, BlockPos> read( IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider provider) {
                            Map<BlockPos, BlockPos> map = new HashMap<>();
                            for (String key : tag.getAllKeys()) {
                                try {
                                    BlockPos slave = BlockPos.of(Long.parseLong(key));
                                    BlockPos master = BlockPos.of(tag.getLong(key));
                                    map.put(slave, master);
                                } catch (NumberFormatException ignored) {}
                            }
                            return map;
                        }

                        @Override
                        public @Nullable CompoundTag write(Map<BlockPos, BlockPos> map, HolderLookup.Provider provider) {
                            CompoundTag tag = new CompoundTag();
                            map.forEach((slave, master) -> tag.putLong(String.valueOf(slave.asLong()), master.asLong()));
                            return tag;
                        }
                    })
                    .build()
    );
}
