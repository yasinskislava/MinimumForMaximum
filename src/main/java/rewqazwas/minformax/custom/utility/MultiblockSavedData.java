package rewqazwas.minformax.custom.utility;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MultiblockSavedData extends SavedData {

    // Custom record/class to hold structural bounds
    public static record StructureBounds(BlockPos masterPos, BlockPos min, BlockPos max) {}

    private final Map<UUID, StructureBounds> structures = new HashMap<>();

    public MultiblockSavedData() {}

    public static MultiblockSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        MultiblockSavedData data = new MultiblockSavedData();
        CompoundTag list = tag.getCompound("structures");
        for (String key : list.getAllKeys()) {
            UUID id = UUID.fromString(key);
            CompoundTag structTag = list.getCompound(key);

            BlockPos master = BlockPos.of(structTag.getLong("master"));
            BlockPos min = BlockPos.of(structTag.getLong("min"));
            BlockPos max = BlockPos.of(structTag.getLong("max"));

            data.structures.put(id, new StructureBounds(master, min, max));
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        CompoundTag list = new CompoundTag();
        structures.forEach((id, bounds) -> {
            CompoundTag structTag = new CompoundTag();
            structTag.putLong("master", bounds.masterPos().asLong());
            structTag.putLong("min", bounds.min().asLong());
            structTag.putLong("max", bounds.max().asLong());
            list.put(id.toString(), structTag);
        });
        tag.put("structures", list);
        return tag;
    }

    public static MultiblockSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(MultiblockSavedData::new, MultiblockSavedData::load, null),
                "minformax_multiblocks"
        );
    }

    public void register(UUID id, BlockPos masterPos, BlockPos min, BlockPos max) {
        structures.put(id, new StructureBounds(masterPos, min, max));
        setDirty();
    }

    public void remove(UUID id) {
        structures.remove(id);
        setDirty();
    }

    public Map<UUID, StructureBounds> getStructures() {
        return this.structures;
    }

    public BlockPos getMaster(UUID id) {
        StructureBounds bounds = structures.get(id);
        return bounds != null ? bounds.masterPos() : null;
    }

    public boolean isMaster(BlockPos pos) {
        return this.structures.containsValue(pos);
    }
}