package rewqazwas.minformax.datagen;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import rewqazwas.minformax.MinForMax;
import rewqazwas.minformax.custom.items.ModItems;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class ModAdvancementProvider extends AdvancementProvider {
    public ModAdvancementProvider(net.minecraft.data.PackOutput output, CompletableFuture<HolderLookup.Provider> registries, ExistingFileHelper existingFileHelper) {
        super(output, registries, existingFileHelper, java.util.List.of(new Advancements()));
    }

    public static class Advancements implements AdvancementGenerator {
        @Override
        public void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> saver, ExistingFileHelper existingFileHelper) {
            AdvancementHolder root = Advancement.Builder.advancement()
                    .display(
                            ModItems.SCANNER.get(),
                            Component.translatable("advancements.minformax.root.title"),
                            Component.translatable("advancements.minformax.root.description"),
                            ResourceLocation.withDefaultNamespace("textures/block/white_wool.png"),
                            AdvancementType.TASK, true, true, false
                    )
                    .addCriterion("has_item", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.SCANNER.get()))
                    .save(saver, MinForMax.MOD_ID + ":root");
        }
    }
}