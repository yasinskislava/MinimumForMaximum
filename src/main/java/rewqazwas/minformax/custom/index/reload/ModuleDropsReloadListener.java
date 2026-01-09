package rewqazwas.minformax.custom.index.reload;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import rewqazwas.minformax.custom.index.DropRegistry;

import java.util.*;

public final class ModuleDropsReloadListener extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public ModuleDropsReloadListener() {
        super(GSON, "module_drops");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsons, ResourceManager rm, ProfilerFiller profiler) {
        Map<ResourceLocation, DropRegistry.ModuleDropRules> out = new HashMap<>();

        for (Map.Entry<ResourceLocation, JsonElement> file : jsons.entrySet()) {
            JsonElement el = file.getValue();
            if (el == null || !el.isJsonObject()) continue;

            JsonObject root = GsonHelper.convertToJsonObject(el, "root");

            for (Map.Entry<String, JsonElement> e : root.entrySet()) {
                if (e.getValue() == null || !e.getValue().isJsonObject()) continue;

                ResourceLocation moduleItemId = ResourceLocation.parse(e.getKey());
                JsonObject obj = GsonHelper.convertToJsonObject(e.getValue(), "entry");

                DropRegistry.ModuleDropRules rules = DropRegistry.ModuleDropRules.fromJson(obj);
                out.put(moduleItemId, rules);
            }
        }

        DropRegistry.setModuleRules(out);
    }

    public static DropRegistry.ModuleDropRules rulesForModule(Item moduleStack) {
        ResourceLocation moduleId = BuiltInRegistries.ITEM.getKey(moduleStack);
        return DropRegistry.getModuleRules(moduleId);
    }

    public static List<ItemStack> mainDropsFromModule(Item loaderItem) {
        DropRegistry.ModuleDropRules rules = ModuleDropsReloadListener.rulesForModule(loaderItem);
        if (rules == null) return List.of();


        Set<ResourceLocation> allowed = expandAllowed(rules.allowedItems(), rules.allowedTags());
        allowed = removeProhibited(allowed, rules.prohibitedItems(), rules.prohibitedTags());

        List<ItemStack> out = new ArrayList<>(allowed.size());
        for (ResourceLocation id : allowed) {
            Item item = BuiltInRegistries.ITEM.get(id);
            if (item != null) out.add(new ItemStack(item));
        }
        System.out.println(out);
        return out;
    }

    public static Set<ResourceLocation> expandAllowed(Set<ResourceLocation> allowedItems, Set<ResourceLocation> allowedTags) {
        Set<ResourceLocation> out = new HashSet<>(allowedItems);

        for (ResourceLocation tagId : allowedTags) {
            TagKey<Item> tagKey = TagKey.create(BuiltInRegistries.ITEM.key(), tagId);
            BuiltInRegistries.ITEM.getTag(tagKey).ifPresent(named -> {
                named.forEach(holder -> out.add(BuiltInRegistries.ITEM.getKey(holder.value())));
            });
        }

        return out;
    }

    public static Set<ResourceLocation> removeProhibited(Set<ResourceLocation> allowed,
                                         Set<ResourceLocation> prohibitedItems,
                                         Set<ResourceLocation> prohibitedTags) {
        allowed.removeAll(prohibitedItems);

        for (ResourceLocation tagId : prohibitedTags) {
            TagKey<Item> tagKey = TagKey.create(BuiltInRegistries.ITEM.key(), tagId);
            BuiltInRegistries.ITEM.getTag(tagKey).ifPresent(named -> {
                named.forEach(holder -> allowed.remove(BuiltInRegistries.ITEM.getKey(holder.value())));
            });
        }
        return allowed;
    }
}
