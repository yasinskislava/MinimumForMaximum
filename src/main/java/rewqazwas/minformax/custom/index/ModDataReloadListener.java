package rewqazwas.minformax.custom.index;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.fml.loading.FMLPaths;
import rewqazwas.minformax.MinForMax;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

public class ModDataReloadListener implements ResourceManagerReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    public static Map<String, HolderClass> MOB_DROPS = new HashMap<>();
    public static Map<String, ModuleData> MODULE_DROPS = new HashMap<>();
    public static Map<String, FluidReplicatorData> FLUID_REPLICATOR_DATA = new HashMap<>();
    public static Map<String, BlockReplicatorData> BLOCK_REPLICATOR_DATA = new HashMap<>();

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        Map<String, HolderClass> defaultMobDrops = new HashMap<>();
        Map<String, ModuleData> defaultModuleDrops = new HashMap<>();
        Map<String, FluidReplicatorData> defaultFluidData = new HashMap<>();
        Map<String, BlockReplicatorData> defaultBlockData = new HashMap<>();

        // Load defaults from data packs into temporary maps
        loadDefaultsFromDataPacks(resourceManager, defaultMobDrops, defaultModuleDrops, defaultFluidData, defaultBlockData);
        
        File configFile = FMLPaths.CONFIGDIR.get().resolve("minformax/drops.json").toFile();
        if (!configFile.getParentFile().exists()) {
            configFile.getParentFile().mkdirs();
        }

        if (!configFile.exists()) {
            MinForMax.LOGGER.info("Config file does not exist, creating with defaults");
            createDefaultConfig(configFile, defaultMobDrops, defaultModuleDrops, defaultFluidData, defaultBlockData);
        }
        
        // Now read from config file
        readConfig(configFile);
        
        MinForMax.LOGGER.info("Data loaded on server/client");
        MinForMax.LOGGER.info("Mob Drops: {}", MOB_DROPS.size());
        MinForMax.LOGGER.info("Module Drops: {}", MODULE_DROPS.size());
        MinForMax.LOGGER.info("Fluid Replicator Data: {}", FLUID_REPLICATOR_DATA.size());
        MinForMax.LOGGER.info("Block Replicator Data: {}", BLOCK_REPLICATOR_DATA.size());
    }

    private void loadDefaultsFromDataPacks(ResourceManager resourceManager, 
                                           Map<String, HolderClass> mobDrops,
                                           Map<String, ModuleData> moduleDrops,
                                           Map<String, FluidReplicatorData> fluidData,
                                           Map<String, BlockReplicatorData> blockData) {
        
        // Load default mob_drops
        var mobResources = resourceManager.listResources("mob_drops", id -> id.getPath().endsWith(".json") && id.getNamespace().equals("minformax"));
        mobResources.forEach((location, resource) -> {
            try (Reader reader = resource.openAsReader()) {
                JsonElement json = GSON.fromJson(reader, JsonElement.class);
                if (json != null && json.isJsonObject()) {
                    json.getAsJsonObject().entrySet().forEach(entry -> {
                        HolderClass.CODEC.parse(JsonOps.INSTANCE, entry.getValue())
                                .resultOrPartial(error -> System.err.println("Failed to parse mob drop: " + error))
                                .ifPresent(holderClass -> {
                                    String key = entry.getKey();
                                    mobDrops.put(key, holderClass);
                                });
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Load default module_drops
        var moduleResources = resourceManager.listResources("module_drops", id -> id.getPath().endsWith(".json") && id.getNamespace().equals("minformax"));
        moduleResources.forEach((location, resource) -> {
            try (Reader reader = resource.openAsReader()) {
                JsonElement json = GSON.fromJson(reader, JsonElement.class);
                if (json != null && json.isJsonObject()) {
                    json.getAsJsonObject().entrySet().forEach(entry -> {
                        ModuleData.CODEC.parse(JsonOps.INSTANCE, entry.getValue())
                                .resultOrPartial(error -> System.err.println("Failed to parse module drop: " + error))
                                .ifPresent(moduleData -> {
                                    String key = entry.getKey();
                                    moduleDrops.put(key, moduleData);
                                });
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Load default fluid_replicator_data
        var fluidResources = resourceManager.listResources("fluid_replicator_data", id -> id.getPath().endsWith(".json") && id.getNamespace().equals("minformax"));
        fluidResources.forEach((location, resource) -> {
            try (Reader reader = resource.openAsReader()) {
                JsonElement json = GSON.fromJson(reader, JsonElement.class);
                if (json != null && json.isJsonObject()) {
                    json.getAsJsonObject().entrySet().forEach(entry -> {
                        FluidReplicatorData.CODEC.parse(JsonOps.INSTANCE, entry.getValue())
                                .resultOrPartial(error -> System.err.println("Failed to parse fluid replicator data: " + error))
                                .ifPresent(data -> {
                                    if (data.energyMultiplier() > 10000) {
                                        data = new FluidReplicatorData(data.basicAmountGenerated(), data.duration(), 10000);
                                    }
                                    String key = parseKey(entry.getKey());
                                    fluidData.put(key, data);
                                });
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Load default block_replicator_data
        var blockResources = resourceManager.listResources("block_replicator_data", id -> id.getPath().endsWith(".json") && id.getNamespace().equals("minformax"));
        blockResources.forEach((location, resource) -> {
            try (Reader reader = resource.openAsReader()) {
                JsonElement json = GSON.fromJson(reader, JsonElement.class);
                if (json != null && json.isJsonObject()) {
                    json.getAsJsonObject().entrySet().forEach(entry -> {
                        BlockReplicatorData.CODEC.parse(JsonOps.INSTANCE, entry.getValue())
                                .resultOrPartial(error -> System.err.println("Failed to parse block replicator data: " + error))
                                .ifPresent(data -> {
                                    if (data.energyMultiplier() > 10000) {
                                        data = new BlockReplicatorData(data.duration(), 10000);
                                    }
                                    String key = parseKey(entry.getKey());
                                    blockData.put(key, data);
                                });
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void createDefaultConfig(File configFile, 
                                     Map<String, HolderClass> mobDrops,
                                     Map<String, ModuleData> moduleDrops,
                                     Map<String, FluidReplicatorData> fluidData,
                                     Map<String, BlockReplicatorData> blockData) {
        try (FileWriter writer = new FileWriter(configFile)) {
            JsonObject emptyConfig = new JsonObject();

            JsonObject mobDropsJson = new JsonObject();
            mobDrops.forEach((key, value) -> {
                HolderClass.CODEC.encodeStart(JsonOps.INSTANCE, value)
                        .result()
                        .ifPresent(json -> mobDropsJson.add(key, json));
            });
            emptyConfig.add("mob_drops", mobDropsJson);

            JsonObject moduleDropsJson = new JsonObject();
            moduleDrops.forEach((key, value) -> {
                ModuleData.CODEC.encodeStart(JsonOps.INSTANCE, value)
                        .result()
                        .ifPresent(json -> moduleDropsJson.add(key, json));
            });
            emptyConfig.add("module_drops", moduleDropsJson);

            JsonObject fluidReplicatorJson = new JsonObject();
            fluidData.forEach((key, value) -> {
                FluidReplicatorData.CODEC.encodeStart(JsonOps.INSTANCE, value)
                        .result()
                        .ifPresent(json -> fluidReplicatorJson.add(key, json));
            });
            emptyConfig.add("fluid_replicator_data", fluidReplicatorJson);

            JsonObject blockReplicatorJson = new JsonObject();
            blockData.forEach((key, value) -> {
                BlockReplicatorData.CODEC.encodeStart(JsonOps.INSTANCE, value)
                        .result()
                        .ifPresent(json -> blockReplicatorJson.add(key, json));
            });
            emptyConfig.add("block_replicator_data", blockReplicatorJson);

            Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
            gson.toJson(emptyConfig, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readConfig(File configFile) {
        // Clear existing data before reading config
        MOB_DROPS.clear();
        MODULE_DROPS.clear();
        FLUID_REPLICATOR_DATA.clear();
        BLOCK_REPLICATOR_DATA.clear();
        
        MinForMax.LOGGER.info("Reading config from {}", configFile.getAbsolutePath());
        try (FileReader reader = new FileReader(configFile)) {
            JsonObject json = GSON.fromJson(reader, JsonObject.class);
            if (json != null) {
                if (json.has("mob_drops")) {
                    json.getAsJsonObject("mob_drops").entrySet().forEach(entry -> {
                        HolderClass.CODEC.parse(JsonOps.INSTANCE, entry.getValue())
                                .resultOrPartial(error -> MinForMax.LOGGER.error("Failed to parse custom mob drop: " + error))
                                .ifPresent(holderClass -> {
                                    String key = entry.getKey();
                                    MOB_DROPS.put(key, holderClass);
                                });
                    });
                }
                if (json.has("module_drops")) {
                    json.getAsJsonObject("module_drops").entrySet().forEach(entry -> {
                        ModuleData.CODEC.parse(JsonOps.INSTANCE, entry.getValue())
                                .resultOrPartial(error -> MinForMax.LOGGER.error("Failed to parse custom module drop: " + error))
                                .ifPresent(moduleData -> {
                                    String key = entry.getKey();
                                    MODULE_DROPS.put(key, moduleData);
                                });
                    });
                }
                if (json.has("fluid_replicator_data")) {
                    json.getAsJsonObject("fluid_replicator_data").entrySet().forEach(entry -> {
                        FluidReplicatorData.CODEC.parse(JsonOps.INSTANCE, entry.getValue())
                                .resultOrPartial(error -> MinForMax.LOGGER.error("Failed to parse custom fluid replicator data: " + error))
                                .ifPresent(data -> {
                                    if (data.energyMultiplier() > 10000) {
                                        data = new FluidReplicatorData(data.basicAmountGenerated(), data.duration(), 10000);
                                    }
                                    String key = parseKey(entry.getKey());
                                    FLUID_REPLICATOR_DATA.put(key, data);
                                });
                    });
                }
                if (json.has("block_replicator_data")) {
                    json.getAsJsonObject("block_replicator_data").entrySet().forEach(entry -> {
                        BlockReplicatorData.CODEC.parse(JsonOps.INSTANCE, entry.getValue())
                                .resultOrPartial(error -> MinForMax.LOGGER.error("Failed to parse custom block replicator data: " + error))
                                .ifPresent(data -> {
                                    if (data.energyMultiplier() > 10000) {
                                        data = new BlockReplicatorData(data.duration(), 10000);
                                    }
                                    String key = parseKey(entry.getKey());
                                    BLOCK_REPLICATOR_DATA.put(key, data);
                                });
                    });
                }
            } else {
                MinForMax.LOGGER.warn("Config JSON is null");
            }
        } catch (IOException e) {
            MinForMax.LOGGER.error("Error reading config file", e);
        }
    }

    private String parseKey(String key) {
        if (key.startsWith("#")) {
            ResourceLocation.parse(key.substring(1));
            return key;
        } else {
            return ResourceLocation.parse(key).toString();
        }
    }
}
