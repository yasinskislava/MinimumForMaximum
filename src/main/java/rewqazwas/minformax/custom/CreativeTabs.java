package rewqazwas.minformax.custom;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredRegister;
import rewqazwas.minformax.MinForMax;
import rewqazwas.minformax.custom.blocks.ModBlocks;
import rewqazwas.minformax.custom.items.ModItems;

public class CreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MinForMax.MOD_ID);

    public static final Holder<CreativeModeTab> MFM_ITEMS = TABS.register("mfm_items", () -> {
        return CreativeModeTab.builder()
                .title(Component.translatable("tab.minformax.items"))
                .icon(() -> new ItemStack(ModItems.SCANNER.get()))
                .displayItems((params, output) -> {
                    output.accept(ModItems.SCANNER);
                    output.accept(ModBlocks.INDEX_INSCRIBER);
                    output.accept(ModBlocks.ETERNAL_GENERATOR);
                    output.accept(ModBlocks.FARMER);
                    output.accept(ModBlocks.FLUID_REPLICATOR);
                    output.accept(ModBlocks.BLOCK_REPLICATOR);
                    output.accept(ModBlocks.ORE_COALESCER);
                    output.accept(ModBlocks.PANDORA_BOX_CORE);
                    output.accept(ModItems.LINKER);
                    output.accept(ModItems.QUANTUM_INGOT);
                    output.accept(ModItems.YINMIN_INGOT);
                    output.accept(ModItems.AMANITA_INGOT);
                    output.accept(ModItems.HELIODOR_INGOT);
                    output.accept(ModItems.ULTIMATE_INGOT);
                    output.accept(ModItems.VOID_INGOT);
                    output.accept(ModItems.AETHER_INGOT);
                    output.accept(ModItems.MEMORY_SHARD);
                    output.accept(ModItems.CHAOS_SHARD);
                    output.accept(ModItems.SPEED_UPGRADE_TIER1);
                    output.accept(ModItems.SPEED_UPGRADE_TIER2);
                    output.accept(ModItems.SPEED_UPGRADE_TIER3);
                    output.accept(ModItems.SPEED_UPGRADE_TIER4);
                    output.accept(ModItems.ULTIMATE_SPEED_UPGRADE);
                    output.accept(ModItems.EXTRA_DROP_UPGRADE_TIER1);
                    output.accept(ModItems.EXTRA_DROP_UPGRADE_TIER2);
                    output.accept(ModItems.EXTRA_DROP_UPGRADE_TIER3);
                    output.accept(ModItems.EXTRA_DROP_UPGRADE_TIER4);
                    output.accept(ModItems.PROCESSING_UPGRADE_TIER1);
                    output.accept(ModItems.PROCESSING_UPGRADE_TIER2);
                    output.accept(ModItems.PROCESSING_UPGRADE_TIER3);
                    output.accept(ModItems.PROCESSING_UPGRADE_TIER4);
                    output.accept(ModItems.ULTIMATE_PROCESSING_UPGRADE);
                    output.accept(ModItems.FORTUNE_UPGRADE_TIER1);
                    output.accept(ModItems.FORTUNE_UPGRADE_TIER2);
                    output.accept(ModItems.FORTUNE_UPGRADE_TIER3);
                    output.accept(ModItems.FORTUNE_UPGRADE_TIER4);
                    output.accept(ModItems.INVERTED_UPGRADE);
                    output.accept(ModItems.AUTO_SMELTING_UPGRADE);
                    output.accept(ModItems.WATERING_UPGRADE);
                    output.accept(ModItems.COMPRESSING_UPGRADE);
                    output.accept(ModItems.QUANTUM_FOAM);
                    output.accept(ModItems.FROZEN_CRYSTAL);
                    output.accept(ModItems.FROZEN_CORE);
                    output.accept(ModItems.ANALYZER);
                })
                .build();
    });
}
