// src/main/java/rewqazwas/minformax/custom/index/reload/MobDropsReloadListener.java
package rewqazwas.minformax.custom.index.reload;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import rewqazwas.minformax.custom.index.DropRegistry;
import rewqazwas.minformax.custom.index.HolderClass;
import rewqazwas.minformax.custom.index.MasterIndex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MobDropsReloadListener extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public MobDropsReloadListener() {
        super(GSON, "mob_drops");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsons, ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<String, HolderClass> rebuilt = new HashMap<>();

        for (Map.Entry<ResourceLocation, JsonElement> fileEntry : jsons.entrySet()) {
            JsonElement el = fileEntry.getValue();
            if (el == null || !el.isJsonObject()) continue;

            JsonObject root = GsonHelper.convertToJsonObject(el, "root");

            for (Map.Entry<String, JsonElement> e : root.entrySet()) {
                if (e.getValue() == null || !e.getValue().isJsonObject()) continue;

                String key = e.getKey();
                JsonObject obj = GsonHelper.convertToJsonObject(e.getValue(), "entry");

                ItemStack mainDrop = ItemStack.EMPTY;
                if (obj.has("main_drop")) {
                    mainDrop = parseItemStackOne(obj.get("main_drop"));
                }

                List<ItemStack> additional = new ArrayList<>();
                JsonArray addArr = GsonHelper.getAsJsonArray(obj, "additional_drop", new JsonArray());
                for (JsonElement dropEl : addArr) {
                    ItemStack parsed = parseItemStackOne(dropEl);
                    if (!parsed.isEmpty()) additional.add(parsed);
                }

                int xp = GsonHelper.getAsInt(obj, "xp", 0);
                int duration = GsonHelper.getAsInt(obj, "duration", 0);

                rebuilt.put(key, new HolderClass(mainDrop, additional, xp, duration));
            }
        }

        DropRegistry.setMobDrops(rebuilt);
    }

    private static ItemStack parseItemStackOne(JsonElement el) {
        if (el == null || !el.isJsonPrimitive()) return ItemStack.EMPTY;

        String id = GsonHelper.convertToString(el, "item");
        ResourceLocation rl = ResourceLocation.parse(id);

        Item item = BuiltInRegistries.ITEM.get(rl);
        if (item == null) return ItemStack.EMPTY;

        return new ItemStack(item, 1);
    }

    public static void syncMobDropsIntoAllLevels(ServerLevel level) {
        Map<String, HolderClass> drops = DropRegistry.getAllMobDrops();

        MasterIndex index = MasterIndex.get(level);

        index.getIndex().clear();
        index.getIndex().putAll(drops);
        index.setDirty();
    }
}
