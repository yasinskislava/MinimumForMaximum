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
                    output.accept(ModBlocks.INDEX_LAB);
                    output.accept(ModBlocks.ETERNAL_GENERATOR_TIER1);
                    output.accept(ModBlocks.ETERNAL_GENERATOR_TIER2);
                    output.accept(ModBlocks.ETERNAL_GENERATOR_TIER3);
                    output.accept(ModBlocks.ETERNAL_GENERATOR_TIER4);
                    output.accept(ModItems.MEMORY_SHARD);
                    output.accept(ModItems.QUANTUM_FOAM);
                    output.accept(ModItems.QUANTUM_INGOT);
                    output.accept(ModItems.INVERTED_UPGRADE);
                    output.accept(ModItems.STRENGTH_UPGRADE);
                    output.accept(ModItems.SPEED_UPGRADE_TIER1);
                    output.accept(ModItems.SPEED_UPGRADE_TIER2);
                    output.accept(ModItems.SPEED_UPGRADE_TIER3);
                    output.accept(ModItems.SPEED_UPGRADE_TIER4);
                    output.accept(ModItems.STACK_UPGRADE_TIER1);
                    output.accept(ModItems.STACK_UPGRADE_TIER2);
                    output.accept(ModItems.STACK_UPGRADE_TIER3);
                    output.accept(ModItems.STACK_UPGRADE_TIER4);
                    output.accept(ModItems.EXTRA_DROP_UPGRADE_TIER1);
                    output.accept(ModItems.EXTRA_DROP_UPGRADE_TIER2);
                    output.accept(ModItems.EXTRA_DROP_UPGRADE_TIER3);
                    output.accept(ModItems.EXTRA_DROP_UPGRADE_TIER4);
                    output.accept(ModItems.CHEF_MODULE);
                    output.accept(ModItems.FARMER_MODULE);
                    output.accept(ModItems.GATHERER_MODULE);
                    output.accept(ModItems.FISHER_MODULE);
                    output.accept(ModItems.BUTCHER_MODULE);
                    output.accept(ModItems.ENDER_DRAGON_MODULE);
                    output.accept(ModItems.WITHER_MODULE);
                    output.accept(ModItems.WARDEN_MODULE);



                })
                .build();
    });
}
