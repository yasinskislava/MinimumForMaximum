package rewqazwas.minformax.custom.index;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.server.level.ServerLevel;

import java.util.*;


public class MasterIndex extends SavedData {
    private static final String STORAGE_NAME = "minformax_master_index";
    private static final Codec<Map<String, HolderClass>> INDEX_CODEC = Codec.unboundedMap(Codec.STRING, HolderClass.CODEC);

    private Map<String, HolderClass> index = new HashMap<>();

    public MasterIndex() {}

    public static MasterIndex get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(new Factory<>(MasterIndex::new ,MasterIndex::load), STORAGE_NAME);
    }

    private static MasterIndex load(CompoundTag nbt, HolderLookup.Provider registries) {
        MasterIndex data = new MasterIndex();
        Tag tag = nbt.get("index");
        if (tag == null) return data;
        DataResult<Map<String, HolderClass>> result = INDEX_CODEC.parse(NbtOps.INSTANCE, tag);
        result.result().ifPresent(m -> data.index = new HashMap<>(m));
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag nbt, HolderLookup.Provider registries) {
        DataResult<Tag> encoded = INDEX_CODEC.encodeStart(NbtOps.INSTANCE, index);
        Optional<Tag> opt = encoded.result();
        opt.ifPresent(t -> nbt.put("index", t));
        return nbt;
    }

    public Map<String, HolderClass> getIndex() { return index; }

    public HolderClass getValue(String key) { return index.get(key); }

    public boolean contains(String key) { return index.containsKey(key); }
}


