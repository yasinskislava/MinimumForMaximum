package rewqazwas.minformax.custom.index;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import rewqazwas.minformax.custom.items.ModItems;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ModuleDropsReloadListener {
    private static final Random RANDOM = new Random();

    public static ModuleData rulesForModule(Item item) {
        String key = BuiltInRegistries.ITEM.getKey(item).toString();
        if (ModDataReloadListener.MODULE_DROPS.containsKey(key)) {
            return ModDataReloadListener.MODULE_DROPS.get(key);
        }
        return new ModuleData(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), 0);
    }

    public static List<ItemStack> mainDropsFromModule(Item item) {
        ModuleData data = rulesForModule(item);
        List<ItemStack> potentialDrops = new ArrayList<>();

        for (String itemId : data.allowedItems()) {
            ResourceLocation location = ResourceLocation.parse(itemId);
            Item resultItem = BuiltInRegistries.ITEM.get(location);
            if (resultItem != Items.AIR) {
                potentialDrops.add(new ItemStack(resultItem));
            }
        }

        for (String tagId : data.allowedTags()) {
            TagKey<Item> tagKey = TagKey.create(BuiltInRegistries.ITEM.key(), ResourceLocation.parse(tagId));
            for (var holder : BuiltInRegistries.ITEM.getTagOrEmpty(tagKey)) {
                potentialDrops.add(new ItemStack(holder.value()));
            }
        }
        
        // Filter prohibited items
        potentialDrops.removeIf(stack -> {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
            if (data.prohibitedItems().contains(id.toString())) return true;
            
            for (String prohibitedTag : data.prohibitedTags()) {
                TagKey<Item> tagKey = TagKey.create(BuiltInRegistries.ITEM.key(), ResourceLocation.parse(prohibitedTag));
                if (stack.is(tagKey)) return true;
            }
            return false;
        });

        List<ItemStack> drops = new ArrayList<>();
        if (!potentialDrops.isEmpty()) {
            drops.add(potentialDrops.get(RANDOM.nextInt(potentialDrops.size())).copy());
        }
        
        return drops;
    }
}
