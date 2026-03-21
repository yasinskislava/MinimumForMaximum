package rewqazwas.minformax.custom.items;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import rewqazwas.minformax.MinForMax;
import rewqazwas.minformax.custom.items.upgrades.*;

import java.util.List;


public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MinForMax.MOD_ID);

    public static final DeferredItem<Item> QUANTUM_FOAM = ITEMS.register("quantum_foam", QuantumFoamItem::new);
    public static final DeferredItem<Item> QUANTUM_INGOT = ITEMS.register("quantum_ingot", QuantumIngotItem::new);
    public static final DeferredItem<Item> FROZEN_CRYSTAL = ITEMS.register("frozen_crystal", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> FROZEN_CORE = ITEMS.register("frozen_core", () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> LINKER = ITEMS.register("linker", () -> new LinkerItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> SCANNER = ITEMS.register("scanner", () -> new ScannerItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> ANALYZER = ITEMS.register("analyzer", () -> new AnalyzerItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> MEMORY_SHARD = ITEMS.register("memory_shard", () -> new AccShard(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> CHAOS_SHARD = ITEMS.register("chaos_shard", () -> new AccShard(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> SPEED_UPGRADE_TIER1 = ITEMS.register("speed_upgrade_tier1", () -> new SpeedUpgrade(2));
    public static final DeferredItem<Item> SPEED_UPGRADE_TIER2 = ITEMS.register("speed_upgrade_tier2", () -> new SpeedUpgrade(4));
    public static final DeferredItem<Item> SPEED_UPGRADE_TIER3 = ITEMS.register("speed_upgrade_tier3", () -> new SpeedUpgrade(8));
    public static final DeferredItem<Item> SPEED_UPGRADE_TIER4 = ITEMS.register("speed_upgrade_tier4", () -> new SpeedUpgrade(16));
    public static final DeferredItem<Item> ULTIMATE_SPEED_UPGRADE = ITEMS.register("ultimate_speed_upgrade", () -> new SpeedUpgrade(9999) {
        @Override
        public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
            super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            tooltipComponents.add(Component.translatable("tooltip.minformax.ultimate_speed_upgrade"));
        }
    });
    public static final DeferredItem<Item> PROCESSING_UPGRADE_TIER1 = ITEMS.register("processing_upgrade_tier1", () -> new ProcessingUpgrade(4));
    public static final DeferredItem<Item> PROCESSING_UPGRADE_TIER2 = ITEMS.register("processing_upgrade_tier2", () -> new ProcessingUpgrade(16));
    public static final DeferredItem<Item> PROCESSING_UPGRADE_TIER3 = ITEMS.register("processing_upgrade_tier3", () -> new ProcessingUpgrade(64));
    public static final DeferredItem<Item> PROCESSING_UPGRADE_TIER4 = ITEMS.register("processing_upgrade_tier4", () -> new ProcessingUpgrade(256));
    public static final DeferredItem<Item> ULTIMATE_PROCESSING_UPGRADE = ITEMS.register("ultimate_processing_upgrade", () -> new ProcessingUpgrade(9999) {
        @Override
        public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
            super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            tooltipComponents.add(Component.translatable("tooltip.minformax.ultimate_processing_upgrade"));
        }
    });
    public static final DeferredItem<Item> EXTRA_DROP_UPGRADE_TIER1 = ITEMS.register("extra_drop_upgrade_tier1", () -> new ExtraDropUpgrade(25));
    public static final DeferredItem<Item> EXTRA_DROP_UPGRADE_TIER2 = ITEMS.register("extra_drop_upgrade_tier2", () -> new ExtraDropUpgrade(50));
    public static final DeferredItem<Item> EXTRA_DROP_UPGRADE_TIER3 = ITEMS.register("extra_drop_upgrade_tier3", () -> new ExtraDropUpgrade(75));
    public static final DeferredItem<Item> EXTRA_DROP_UPGRADE_TIER4 = ITEMS.register("extra_drop_upgrade_tier4", () -> new ExtraDropUpgrade(100));
    public static final DeferredItem<Item> INVERTED_UPGRADE = registerUpgradeWithTooltip("inverted_upgrade", "tooltip.minformax.inverted_upgrade");
    public static final DeferredItem<Item> FORTUNE_UPGRADE_TIER1 = ITEMS.register("fortune_upgrade_tier1", () -> new FortuneUpgrade(10));
    public static final DeferredItem<Item> FORTUNE_UPGRADE_TIER2 = ITEMS.register("fortune_upgrade_tier2", () -> new FortuneUpgrade(25));
    public static final DeferredItem<Item> FORTUNE_UPGRADE_TIER3 = ITEMS.register("fortune_upgrade_tier3", () -> new FortuneUpgrade(50));
    public static final DeferredItem<Item> FORTUNE_UPGRADE_TIER4 = ITEMS.register("fortune_upgrade_tier4", () -> new FortuneUpgrade(100));
    public static final DeferredItem<Item> AUTO_SMELTING_UPGRADE = registerUpgradeWithTooltip("auto_smelting_upgrade", "tooltip.minformax.auto_smelting_upgrade");
    public static final DeferredItem<Item> WATERING_UPGRADE = registerUpgradeWithTooltip("watering_upgrade", "tooltip.minformax.watering_upgrade");


    public static final DeferredItem<Item> CHEF_MODULE = ITEMS.register("chef_module", ModuleItem::new);
    public static final DeferredItem<Item> FARMER_MODULE = ITEMS.register("farmer_module", ModuleItem::new);
    public static final DeferredItem<Item> GATHERER_MODULE = ITEMS.register("gatherer_module", ModuleItem::new);
    public static final DeferredItem<Item> FISHER_MODULE = ITEMS.register("fisher_module", ModuleItem::new);
    public static final DeferredItem<Item> BUTCHER_MODULE = ITEMS.register("butcher_module", ModuleItem::new);

    public static final DeferredItem<Item> BLANK_MODULE_1 = ITEMS.register("blank_module_1", ModuleItem::new);
    public static final DeferredItem<Item> BLANK_MODULE_2 = ITEMS.register("blank_module_2", ModuleItem::new);
    public static final DeferredItem<Item> BLANK_MODULE_3 = ITEMS.register("blank_module_3", ModuleItem::new);
    public static final DeferredItem<Item> BLANK_MODULE_4 = ITEMS.register("blank_module_4", ModuleItem::new);
    public static final DeferredItem<Item> BLANK_MODULE_5 = ITEMS.register("blank_module_5", ModuleItem::new);
    public static final DeferredItem<Item> BLANK_MODULE_6 = ITEMS.register("blank_module_6", ModuleItem::new);
    public static final DeferredItem<Item> BLANK_MODULE_7 = ITEMS.register("blank_module_7", ModuleItem::new);
    public static final DeferredItem<Item> BLANK_MODULE_8 = ITEMS.register("blank_module_8", ModuleItem::new);


    private static DeferredItem<Item> registerUpgradeWithTooltip(String name, String tooltipKey) {
        return ITEMS.register(name, () -> new UpgradeItem() {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                tooltipComponents.add(Component.translatable(tooltipKey));
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    }
}
