package rewqazwas.minformax.custom.index;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;

import java.util.*;

public final class DropRegistry {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static volatile Map<String, HolderClass> MOB_DROPS = Map.of();
    private static volatile Map<ResourceLocation, ModuleDropRules> MODULE_RULES = Map.of();

    private DropRegistry() {}

    public static void setMobDrops(Map<String, HolderClass> drops) {
        MOB_DROPS = Collections.unmodifiableMap(new HashMap<>(drops));
        LOGGER.info("Loaded {} mob drop entries", MOB_DROPS.size());
    }

    public static Map<String, HolderClass> getAllMobDrops() {
        return MOB_DROPS;
    }

    public static void setModuleRules(Map<ResourceLocation, ModuleDropRules> rules) {
        MODULE_RULES = Collections.unmodifiableMap(new HashMap<>(rules));
        LOGGER.info("Loaded {} module rule entries", MODULE_RULES.size());
    }

    public static HolderClass getMobDrop(String key) {
        return MOB_DROPS.get(key);
    }

    public static boolean hasMobDrop(String key) {
        return MOB_DROPS.containsKey(key);
    }

    public static ModuleDropRules getModuleRules(ResourceLocation moduleId) {
        return MODULE_RULES.get(moduleId);
    }

    public record ModuleDropRules(
            Set<ResourceLocation> allowedItems,
            Set<ResourceLocation> prohibitedItems,
            Set<ResourceLocation> allowedTags,
            Set<ResourceLocation> prohibitedTags,
            int xp
    ) {
        public boolean allows(ItemStack stack) {
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());

            if (prohibitedItems.contains(itemId)) return false;

            for (ResourceLocation tagId : prohibitedTags) {
                TagKey<Item> tagKey = TagKey.create(BuiltInRegistries.ITEM.key(), tagId);
                if (stack.is(tagKey)) return false;
            }

            boolean hasAnyAllow =
                    !allowedItems.isEmpty() || !allowedTags.isEmpty();

            if (!hasAnyAllow) {
                return true;
            }

            if (allowedItems.contains(itemId)) return true;

            for (ResourceLocation tagId : allowedTags) {
                TagKey<Item> tagKey = TagKey.create(BuiltInRegistries.ITEM.key(), tagId);
                if (stack.is(tagKey)) return true;
            }

            return false;
        }

        public static ModuleDropRules fromJson(JsonObject obj) {
            return new ModuleDropRules(
                    parseIdSet(obj, "allowed_items"),
                    parseIdSet(obj, "prohibited_items"),
                    parseIdSet(obj, "allowed_tags"),
                    parseIdSet(obj, "prohibited_tags"),
                    GsonHelper.getAsInt(obj, "xp", 0)
            );
        }

        private static Set<ResourceLocation> parseIdSet(JsonObject obj, String key) {
            if (!obj.has(key)) return Set.of();
            JsonArray arr = GsonHelper.getAsJsonArray(obj, key);
            Set<ResourceLocation> out = new HashSet<>();
            for (JsonElement el : arr) {
                out.add(ResourceLocation.parse(el.getAsString()));
            }
            return out;
        }
    }
}
