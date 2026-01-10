package rewqazwas.minformax.custom.index;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

public class ModuleDropsReloadListener {

    public static ModuleData rulesForModule(Item item) {
        String key = BuiltInRegistries.ITEM.getKey(item).toString();
        if (ModDataReloadListener.MODULE_DROPS.containsKey(key)) {
            return ModDataReloadListener.MODULE_DROPS.get(key);
        }
        return new ModuleData(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), 0);
    }

    public static List<ItemStack> mainDropsFromModule(Item item) {
        ModuleData data = rulesForModule(item);
        List<ItemStack> drops = new ArrayList<>();

        for (String itemId : data.allowedItems()) {
            ResourceLocation location = ResourceLocation.parse(itemId);
            Item resultItem = BuiltInRegistries.ITEM.get(location);
            if (resultItem != Items.AIR) {
                drops.add(new ItemStack(resultItem));
            }
        }

        for (String tagId : data.allowedTags()) {
            TagKey<Item> tagKey = TagKey.create(BuiltInRegistries.ITEM.key(), ResourceLocation.parse(tagId));
            for (var holder : BuiltInRegistries.ITEM.getTagOrEmpty(tagKey)) {
                drops.add(new ItemStack(holder.value()));
            }
        }
        
        // Filter prohibited items
        drops.removeIf(stack -> {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
            if (data.prohibitedItems().contains(id.toString())) return true;
            
            for (String prohibitedTag : data.prohibitedTags()) {
                TagKey<Item> tagKey = TagKey.create(BuiltInRegistries.ITEM.key(), ResourceLocation.parse(prohibitedTag));
                if (stack.is(tagKey)) return true;
            }
            return false;
        });

        return drops;
    }
}
