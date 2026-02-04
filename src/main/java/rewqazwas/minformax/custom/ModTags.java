package rewqazwas.minformax.custom;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import rewqazwas.minformax.MinForMax;

public class ModTags {
    public static final TagKey<Item> SUPPLIER_ITEMS = createTag("supplier_items");
    
    public static final TagKey<Item> SPEED_UPGRADES = createTag("speed_upgrades");
    public static final TagKey<Item> PROCESSING_UPGRADES = createTag("processing_upgrades");
    public static final TagKey<Item> EXTRA_DROP_UPGRADES = createTag("extra_drop_upgrades");
    public static final TagKey<Item> FORTUNE_UPGRADES = createTag("fortune_upgrades");

    private static TagKey<Item> createTag(String name) {
        return ItemTags.create(ResourceLocation.fromNamespaceAndPath(MinForMax.MOD_ID, name));
    }
}
