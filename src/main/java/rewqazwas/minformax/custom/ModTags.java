package rewqazwas.minformax.custom;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import rewqazwas.minformax.MinForMax;

public class ModTags {
    public static final TagKey<Item> SUPPLIER_ITEMS = createTag("supplier_items");

    private static TagKey<Item> createTag(String name) {
        return ItemTags.create(ResourceLocation.fromNamespaceAndPath(MinForMax.MOD_ID, name));
    }
}
