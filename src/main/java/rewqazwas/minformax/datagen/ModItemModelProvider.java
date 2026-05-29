package rewqazwas.minformax.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredItem;
import rewqazwas.minformax.MinForMax;
import rewqazwas.minformax.custom.items.ModItems;

public class ModItemModelProvider extends ItemModelProvider {

    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, MinForMax.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        // Tiered Upgrades
        tieredUpgrade(ModItems.SPEED_UPGRADE_TIER1, 1, "speed");
        tieredUpgrade(ModItems.SPEED_UPGRADE_TIER2, 2, "speed");
        tieredUpgrade(ModItems.SPEED_UPGRADE_TIER3, 3, "speed");
        tieredUpgrade(ModItems.SPEED_UPGRADE_TIER4, 4, "speed");

        tieredUpgrade(ModItems.PROCESSING_UPGRADE_TIER1, 1, "processing");
        tieredUpgrade(ModItems.PROCESSING_UPGRADE_TIER2, 2, "processing");
        tieredUpgrade(ModItems.PROCESSING_UPGRADE_TIER3, 3, "processing");
        tieredUpgrade(ModItems.PROCESSING_UPGRADE_TIER4, 4, "processing");

        tieredUpgrade(ModItems.EXTRA_DROP_UPGRADE_TIER1, 1, "extra_drop");
        tieredUpgrade(ModItems.EXTRA_DROP_UPGRADE_TIER2, 2, "extra_drop");
        tieredUpgrade(ModItems.EXTRA_DROP_UPGRADE_TIER3, 3, "extra_drop");
        tieredUpgrade(ModItems.EXTRA_DROP_UPGRADE_TIER4, 4, "extra_drop");

        tieredUpgrade(ModItems.FORTUNE_UPGRADE_TIER1, 1, "fortune");
        tieredUpgrade(ModItems.FORTUNE_UPGRADE_TIER2, 2, "fortune");
        tieredUpgrade(ModItems.FORTUNE_UPGRADE_TIER3, 3, "fortune");
        tieredUpgrade(ModItems.FORTUNE_UPGRADE_TIER4, 4, "fortune");

        // Ultimate/Non-Tiered Upgrades (Using ultimate_upgrade_base)
        ultimateUpgrade(ModItems.ULTIMATE_SPEED_UPGRADE, "speed");
        ultimateUpgrade(ModItems.ULTIMATE_PROCESSING_UPGRADE, "processing");
        ultimateUpgrade(ModItems.AUTO_SMELTING_UPGRADE, "auto_smelting");
        ultimateUpgrade(ModItems.WATERING_UPGRADE, "watering");
        ultimateUpgrade(ModItems.INVERTED_UPGRADE, "inverted");
        ultimateUpgrade(ModItems.COMPRESSING_UPGRADE, "compressing");


        // Standard Items (Single Layer)
        basicItem(ModItems.QUANTUM_FOAM.get());
        basicItem(ModItems.QUANTUM_INGOT.get());
        basicItem(ModItems.ANALYZER.get());
        basicItem(ModItems.FROZEN_CORE.get());
        basicItem(ModItems.FROZEN_CRYSTAL.get());
        basicItem(ModItems.AETHER_INGOT.get());
        basicItem(ModItems.AMANITA_INGOT.get());
        basicItem(ModItems.ULTIMATE_INGOT.get());
        basicItem(ModItems.HELIODOR_INGOT.get());
        basicItem(ModItems.YINMIN_INGOT.get());
        basicItem(ModItems.VOID_INGOT.get());
        basicItem(ModItems.BABYLON_KEY.get());
    }

    /**
     * Helper to create a model with a tiered base (layer0) and a type overlay (layer1)
     */
    private void tieredUpgrade(DeferredItem<Item> item, int tier, String overlayType) {
        withExistingParent(item.getId().getPath(), "item/generated")
                .texture("layer0", modLoc("item/upgrade_base_tier_" + tier))
                .texture("layer1", modLoc("item/" + overlayType));
    }

    /**
     * Helper to create a model with the ultimate base (layer0) and a type overlay (layer1)
     */
    private void ultimateUpgrade(DeferredItem<Item> item, String overlayType) {
        withExistingParent(item.getId().getPath(), "item/generated")
                .texture("layer0", modLoc("item/ultimate_upgrade_base"))
                .texture("layer1", modLoc("item/" + overlayType));
    }
}