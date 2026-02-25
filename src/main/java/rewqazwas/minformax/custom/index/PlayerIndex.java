package rewqazwas.minformax.custom.index;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import rewqazwas.minformax.MinForMax;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class PlayerIndex {
    private static final String TAG_KEY = "minformax_player_index";
    private static final Codec<List<String>> LIST_CODEC = Codec.STRING.listOf();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static List<String> load(ServerPlayer player) {
        CompoundTag pd = player.getPersistentData();
        if (pd.contains(TAG_KEY)) {
            Tag tag = pd.get(TAG_KEY);
            DataResult<List<String>> result = LIST_CODEC.parse(NbtOps.INSTANCE, tag);
            return result.result().orElseGet(ArrayList::new);
        } else {
            // Try to load from file
            List<String> fromFile = loadFromFile(player);
            if (fromFile != null) {
                // Save to NBT so we don't need to read file next time
                saveToNbt(player, fromFile);
                return fromFile;
            }
            // If file doesn't exist, save empty list to NBT to avoid checking file again
            saveToNbt(player, new ArrayList<>());
            return new ArrayList<>();
        }
    }

    public static void save(ServerPlayer player, List<String> list) {
        saveToNbt(player, list);
        saveToFile(player, list);
    }

    private static void saveToNbt(ServerPlayer player, List<String> list) {
        DataResult<Tag> encoded = LIST_CODEC.encodeStart(NbtOps.INSTANCE, list);
        Optional<Tag> opt = encoded.result();
        if (opt.isPresent()) {
            player.getPersistentData().put(TAG_KEY, opt.get());
        }
    }

    private static Path getIndexDir(ServerPlayer player) {
        if (player.getServer() == null) return null;
        return player.getServer().getWorldPath(LevelResource.ROOT).resolve("minformax_indices");
    }

    private static void saveToFile(ServerPlayer player, List<String> list) {
        Path dir = getIndexDir(player);
        if (dir == null) return;

        try {
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }

            File file = dir.resolve(player.getStringUUID() + ".json").toFile();
            try (FileWriter writer = new FileWriter(file)) {
                GSON.toJson(list, writer);
            }
        } catch (IOException e) {
            MinForMax.LOGGER.error("Failed to save player index to file for {}", player.getName().getString(), e);
        }
    }

    private static List<String> loadFromFile(ServerPlayer player) {
        Path dir = getIndexDir(player);
        if (dir == null) return null;

        File file = dir.resolve(player.getStringUUID() + ".json").toFile();
        if (!file.exists()) return null;

        try (FileReader reader = new FileReader(file)) {
            Type listType = new TypeToken<ArrayList<String>>(){}.getType();
            return GSON.fromJson(reader, listType);
        } catch (IOException e) {
            MinForMax.LOGGER.error("Failed to load player index from file for {}", player.getName().getString(), e);
            return null;
        }
    }

    public static boolean contains(ServerPlayer player, String key) {
        return load(player).contains(key);
    }

    public static List<String> getLocalIndex(ServerPlayer player) { return Collections.unmodifiableList(load(player)); }

    public static void add(ServerPlayer player, String key) {
        List<String> indexList = new ArrayList<>(getLocalIndex(player));
        if(!indexList.contains(key)) {
            indexList.add(key);
            save(player, indexList);
        }
    }

    public static void clear(ServerPlayer player) {
        save(player, new ArrayList<>());
    }
}
