package rewqazwas.minformax.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;
import rewqazwas.minformax.MinForMax;
import rewqazwas.minformax.custom.ModTags;
import rewqazwas.minformax.custom.items.ModItems;


import java.util.concurrent.CompletableFuture;

public class ModItemTagProvider extends ItemTagsProvider {
    public ModItemTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, CompletableFuture<TagLookup<Block>> blockTags, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTags, MinForMax.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(ModTags.SUPPLIER_ITEMS)
                .add(ModItems.MEMORY_SHARD.get())
                .add(ModItems.FISHER_MODULE.get())
                .add(ModItems.FARMER_MODULE.get())
                .add(ModItems.GATHERER_MODULE.get())
                .add(ModItems.CHEF_MODULE.get())
                .add(ModItems.BUTCHER_MODULE.get())
                .add(ModItems.ENDER_DRAGON_MODULE.get())
                .add(ModItems.WITHER_MODULE.get())
                .add(ModItems.WARDEN_MODULE.get());

        tag(ModTags.SPEED_UPGRADES)
                .add(ModItems.SPEED_UPGRADE_TIER1.get())
                .add(ModItems.SPEED_UPGRADE_TIER2.get())
                .add(ModItems.SPEED_UPGRADE_TIER3.get())
                .add(ModItems.SPEED_UPGRADE_TIER4.get())
                .add(ModItems.ULTIMATE_SPEED_UPGRADE.get());

        tag(ModTags.PROCESSING_UPGRADES)
                .add(ModItems.PROCESSING_UPGRADE_TIER1.get())
                .add(ModItems.PROCESSING_UPGRADE_TIER2.get())
                .add(ModItems.PROCESSING_UPGRADE_TIER3.get())
                .add(ModItems.PROCESSING_UPGRADE_TIER4.get())
                .add(ModItems.ULTIMATE_PROCESSING_UPGRADE.get());

        tag(ModTags.EXTRA_DROP_UPGRADES)
                .add(ModItems.EXTRA_DROP_UPGRADE_TIER1.get())
                .add(ModItems.EXTRA_DROP_UPGRADE_TIER2.get())
                .add(ModItems.EXTRA_DROP_UPGRADE_TIER3.get())
                .add(ModItems.EXTRA_DROP_UPGRADE_TIER4.get());

        tag(ModTags.FORTUNE_UPGRADES)
                .add(ModItems.FORTUNE_UPGRADE_TIER1.get())
                .add(ModItems.FORTUNE_UPGRADE_TIER2.get())
                .add(ModItems.FORTUNE_UPGRADE_TIER3.get())
                .add(ModItems.FORTUNE_UPGRADE_TIER4.get());
    }
}
