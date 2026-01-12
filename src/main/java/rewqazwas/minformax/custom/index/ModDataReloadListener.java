package rewqazwas.minformax.custom.index;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.fml.loading.FMLPaths;

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
        MOB_DROPS.clear();
        MODULE_DROPS.clear();
        FLUID_REPLICATOR_DATA.clear();
        BLOCK_REPLICATOR_DATA.clear();

        // Load default mob_drops from data packs (specifically minformax namespace)
        var mobResources = resourceManager.listResources("mob_drops", id -> id.getPath().endsWith(".json") && id.getNamespace().equals("minformax"));
        mobResources.forEach((location, resource) -> {
            try (Reader reader = resource.openAsReader()) {
                JsonElement json = GSON.fromJson(reader, JsonElement.class);
                if (json != null && json.isJsonObject()) {
                    json.getAsJsonObject().entrySet().forEach(entry -> {
                        HolderClass.CODEC.parse(JsonOps.INSTANCE, entry.getValue())
                                .resultOrPartial(error -> System.err.println("Failed to parse mob drop: " + error))
                                .ifPresent(holderClass -> MOB_DROPS.put(entry.getKey(), holderClass));
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Load default module_drops from data packs (specifically minformax namespace)
        var moduleResources = resourceManager.listResources("module_drops", id -> id.getPath().endsWith(".json") && id.getNamespace().equals("minformax"));
        moduleResources.forEach((location, resource) -> {
            try (Reader reader = resource.openAsReader()) {
                JsonElement json = GSON.fromJson(reader, JsonElement.class);
                if (json != null && json.isJsonObject()) {
                    json.getAsJsonObject().entrySet().forEach(entry -> {
                        ModuleData.CODEC.parse(JsonOps.INSTANCE, entry.getValue())
                                .resultOrPartial(error -> System.err.println("Failed to parse module drop: " + error))
                                .ifPresent(moduleData -> MODULE_DROPS.put(entry.getKey(), moduleData));
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Load default fluid_replicator_data from data packs (specifically minformax namespace)
        var fluidResources = resourceManager.listResources("fluid_replicator_data", id -> id.getPath().endsWith(".json") && id.getNamespace().equals("minformax"));
        fluidResources.forEach((location, resource) -> {
            try (Reader reader = resource.openAsReader()) {
                JsonElement json = GSON.fromJson(reader, JsonElement.class);
                if (json != null && json.isJsonObject()) {
                    json.getAsJsonObject().entrySet().forEach(entry -> {
                        FluidReplicatorData.CODEC.parse(JsonOps.INSTANCE, entry.getValue())
                                .resultOrPartial(error -> System.err.println("Failed to parse fluid replicator data: " + error))
                                .ifPresent(data -> FLUID_REPLICATOR_DATA.put(entry.getKey(), data));
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Load default block_replicator_data from data packs (specifically minformax namespace)
        var blockResources = resourceManager.listResources("block_replicator_data", id -> id.getPath().endsWith(".json") && id.getNamespace().equals("minformax"));
        blockResources.forEach((location, resource) -> {
            try (Reader reader = resource.openAsReader()) {
                JsonElement json = GSON.fromJson(reader, JsonElement.class);
                if (json != null && json.isJsonObject()) {
                    json.getAsJsonObject().entrySet().forEach(entry -> {
                        BlockReplicatorData.CODEC.parse(JsonOps.INSTANCE, entry.getValue())
                                .resultOrPartial(error -> System.err.println("Failed to parse block replicator data: " + error))
                                .ifPresent(data -> BLOCK_REPLICATOR_DATA.put(entry.getKey(), data));
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        loadConfig();
    }

    private void loadConfig() {
        File configFile = FMLPaths.CONFIGDIR.get().resolve("minformax/drops.json").toFile();
        if (!configFile.getParentFile().exists()) {
            configFile.getParentFile().mkdirs();
        }

        if (!configFile.exists()) {
            try (FileWriter writer = new FileWriter(configFile)) {
                JsonObject emptyConfig = new JsonObject();
                
                JsonObject mobDropsJson = new JsonObject();
                MOB_DROPS.forEach((key, value) -> {
                    HolderClass.CODEC.encodeStart(JsonOps.INSTANCE, value)
                            .result()
                            .ifPresent(json -> mobDropsJson.add(key, json));
                });
                emptyConfig.add("mob_drops", mobDropsJson);

                JsonObject moduleDropsJson = new JsonObject();
                MODULE_DROPS.forEach((key, value) -> {
                    ModuleData.CODEC.encodeStart(JsonOps.INSTANCE, value)
                            .result()
                            .ifPresent(json -> moduleDropsJson.add(key, json));
                });
                emptyConfig.add("module_drops", moduleDropsJson);

                JsonObject fluidReplicatorJson = new JsonObject();
                FLUID_REPLICATOR_DATA.forEach((key, value) -> {
                    FluidReplicatorData.CODEC.encodeStart(JsonOps.INSTANCE, value)
                            .result()
                            .ifPresent(json -> fluidReplicatorJson.add(key, json));
                });
                emptyConfig.add("fluid_replicator_data", fluidReplicatorJson);

                JsonObject blockReplicatorJson = new JsonObject();
                BLOCK_REPLICATOR_DATA.forEach((key, value) -> {
                    BlockReplicatorData.CODEC.encodeStart(JsonOps.INSTANCE, value)
                            .result()
                            .ifPresent(json -> blockReplicatorJson.add(key, json));
                });
                emptyConfig.add("block_replicator_data", blockReplicatorJson);

                GSON.toJson(emptyConfig, writer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try (FileReader reader = new FileReader(configFile)) {
                JsonObject json = GSON.fromJson(reader, JsonObject.class);
                if (json != null) {
                    if (json.has("mob_drops")) {
                        json.getAsJsonObject("mob_drops").entrySet().forEach(entry -> {
                            HolderClass.CODEC.parse(JsonOps.INSTANCE, entry.getValue())
                                    .resultOrPartial(error -> System.err.println("Failed to parse custom mob drop: " + error))
                                    .ifPresent(holderClass -> MOB_DROPS.put(entry.getKey(), holderClass));
                        });
                    }
                    if (json.has("module_drops")) {
                        json.getAsJsonObject("module_drops").entrySet().forEach(entry -> {
                            ModuleData.CODEC.parse(JsonOps.INSTANCE, entry.getValue())
                                    .resultOrPartial(error -> System.err.println("Failed to parse custom module drop: " + error))
                                    .ifPresent(moduleData -> MODULE_DROPS.put(entry.getKey(), moduleData));
                        });
                    }
                    if (json.has("fluid_replicator_data")) {
                        json.getAsJsonObject("fluid_replicator_data").entrySet().forEach(entry -> {
                            FluidReplicatorData.CODEC.parse(JsonOps.INSTANCE, entry.getValue())
                                    .resultOrPartial(error -> System.err.println("Failed to parse custom fluid replicator data: " + error))
                                    .ifPresent(data -> FLUID_REPLICATOR_DATA.put(entry.getKey(), data));
                        });
                    }
                    if (json.has("block_replicator_data")) {
                        json.getAsJsonObject("block_replicator_data").entrySet().forEach(entry -> {
                            BlockReplicatorData.CODEC.parse(JsonOps.INSTANCE, entry.getValue())
                                    .resultOrPartial(error -> System.err.println("Failed to parse custom block replicator data: " + error))
                                    .ifPresent(data -> BLOCK_REPLICATOR_DATA.put(entry.getKey(), data));
                        });
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
