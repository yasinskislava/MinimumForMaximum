package rewqazwas.minformax.custom.index;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.fml.loading.FMLPaths;
import rewqazwas.minformax.MinForMax;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class ModDataReloadListener implements ResourceManagerReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static Map<String, HolderClass> MOB_DROPS = new HashMap<>();
    public static Map<String, FarmerData> FARMER_DATA = new HashMap<>();
    public static Map<String, FluidReplicatorData> FLUID_REPLICATOR_DATA = new HashMap<>();
    public static Map<String, BlockReplicatorData> BLOCK_REPLICATOR_DATA = new HashMap<>();

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        // Clear old data
        MOB_DROPS.clear();
        FARMER_DATA.clear();
        FLUID_REPLICATOR_DATA.clear();
        BLOCK_REPLICATOR_DATA.clear();


        // 1. Process Mob Drops
        handleCategory("mob_drops.json", "mob_drops", HolderClass.CODEC, MOB_DROPS, resourceManager);

        // 2. Process Fluid Data
        handleCategory("fluid_replicator.json", "fluid_replicator_data", FluidReplicatorData.CODEC, FLUID_REPLICATOR_DATA, resourceManager);

        // 3. Process Block Data
        handleCategory("block_replicator.json", "block_replicator_data", BlockReplicatorData.CODEC, BLOCK_REPLICATOR_DATA, resourceManager);

        // 4. Process Farmer Data
        handleCategory("farmer_data.json", "farmer_data", FarmerData.CODEC, FARMER_DATA, resourceManager);

        MinForMax.LOGGER.info("Data Reload Complete. Mobs: {}, Fluids: {}, Blocks: {}, Farmer: {}",
                MOB_DROPS.size(), FLUID_REPLICATOR_DATA.size(), BLOCK_REPLICATOR_DATA.size(), FARMER_DATA.size());
    }

    /**
     * Generic handler to load from datapacks, then override/supplement with a specific config file.
     */
    private <T> void handleCategory(String fileName, String dataPackFolder, Codec<T> codec, Map<String, T> targetMap, ResourceManager rm) {
        // Load Defaults from Datapacks first
        var resources = rm.listResources(dataPackFolder, id -> id.getPath().endsWith(".json") && id.getNamespace().equals("minformax"));
        resources.forEach((location, resource) -> {
            try (Reader reader = resource.openAsReader()) {
                JsonObject json = GSON.fromJson(reader, JsonObject.class);
                if (json != null) {
                    parseJsonToMap(json, codec, targetMap);
                }
            } catch (Exception e) {
                MinForMax.LOGGER.error("Error loading datapack resource: " + location, e);
            }
        });

        // Load/Create Config File
        File configFile = FMLPaths.CONFIGDIR.get().resolve("minformax/" + fileName).toFile();
        if (!configFile.getParentFile().exists()) configFile.getParentFile().mkdirs();

        if (!configFile.exists()) {
            saveMapToFile(configFile, codec, targetMap);
        } else {
            try (FileReader reader = new FileReader(configFile)) {
                JsonObject json = GSON.fromJson(reader, JsonObject.class);
                if (json != null) {
                    parseJsonToMap(json, codec, targetMap);
                }
            } catch (IOException e) {
                MinForMax.LOGGER.error("Error reading config: " + fileName, e);
            }
        }
    }

    private <T> void parseJsonToMap(JsonObject json, Codec<T> codec, Map<String, T> targetMap) {
        json.entrySet().forEach(entry -> {
            codec.parse(JsonOps.INSTANCE, entry.getValue())
                    .resultOrPartial(error -> MinForMax.LOGGER.error("Parsing error: " + error))
                    .ifPresent(data -> {
                        String key = parseKey(entry.getKey());
                        targetMap.put(key, data);
                    });
        });
    }

    private <T> void saveMapToFile(File file, Codec<T> codec, Map<String, T> map) {
        try (FileWriter writer = new FileWriter(file)) {
            JsonObject root = new JsonObject();
            map.forEach((key, value) -> {
                codec.encodeStart(JsonOps.INSTANCE, value)
                        .result()
                        .ifPresent(json -> root.add(key, json));
            });
            GSON.toJson(root, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String parseKey(String key) {
        return key;
    }
}