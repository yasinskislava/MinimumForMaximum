package rewqazwas.minformax.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
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
        basicItem(ModItems.SPEED_UPGRADE_TIER1.get());
        basicItem(ModItems.SPEED_UPGRADE_TIER2.get());
        basicItem(ModItems.SPEED_UPGRADE_TIER3.get());
        basicItem(ModItems.SPEED_UPGRADE_TIER4.get());
        basicItem(ModItems.CREATIVE_SPEED_UPGRADE.get());
        basicItem(ModItems.STACK_UPGRADE_TIER1.get());
        basicItem(ModItems.STACK_UPGRADE_TIER2.get());
        basicItem(ModItems.STACK_UPGRADE_TIER3.get());
        basicItem(ModItems.STACK_UPGRADE_TIER4.get());
        basicItem(ModItems.EXTRA_DROP_UPGRADE_TIER1.get());
        basicItem(ModItems.EXTRA_DROP_UPGRADE_TIER2.get());
        basicItem(ModItems.EXTRA_DROP_UPGRADE_TIER3.get());
        basicItem(ModItems.EXTRA_DROP_UPGRADE_TIER4.get());
        basicItem(ModItems.INVERTED_UPGRADE.get());
        basicItem(ModItems.STRENGTH_UPGRADE.get());
        basicItem(ModItems.BUTCHER_MODULE.get());
        basicItem(ModItems.CHEF_MODULE.get());
        basicItem(ModItems.FARMER_MODULE.get());
        basicItem(ModItems.FISHER_MODULE.get());
        basicItem(ModItems.GATHERER_MODULE.get());
        basicItem(ModItems.ENDER_DRAGON_MODULE.get());
        basicItem(ModItems.WITHER_MODULE.get());
        basicItem(ModItems.WARDEN_MODULE.get());
        basicItem(ModItems.QUANTUM_FOAM.get());
        basicItem(ModItems.QUANTUM_INGOT.get());
    }


    private ItemModelBuilder handheldItem(DeferredItem<?> item) {
        return withExistingParent(item.getId().getPath(),
                ResourceLocation.parse("item/handheld")).texture("layer0",
                ResourceLocation.fromNamespaceAndPath(MinForMax.MOD_ID,"item/" + item.getId().getPath()));
    }
}
