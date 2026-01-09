package rewqazwas.minformax.custom.items;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import rewqazwas.minformax.MinForMax;
import rewqazwas.minformax.custom.items.upgrades.*;

import java.util.ArrayList;
import java.util.List;


public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MinForMax.MOD_ID);

    public static final DeferredItem<Item> QUANTUM_FOAM = ITEMS.register("quantum_foam", QuantumFoamItem::new);
    public static final DeferredItem<Item> QUANTUM_INGOT = ITEMS.register("quantum_ingot", QuantumIngotItem::new);

    public static final DeferredItem<Item> SCANNER = ITEMS.register("scanner", () -> new ScannerItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> MEMORY_SHARD = ITEMS.register("memory_shard", () -> new MemoryShard(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> SPEED_UPGRADE_TIER1 = ITEMS.register("speed_upgrade_tier1", () -> new SpeedUpgrade(2));
    public static final DeferredItem<Item> SPEED_UPGRADE_TIER2 = ITEMS.register("speed_upgrade_tier2", () -> new SpeedUpgrade(4));
    public static final DeferredItem<Item> SPEED_UPGRADE_TIER3 = ITEMS.register("speed_upgrade_tier3", () -> new SpeedUpgrade(8));
    public static final DeferredItem<Item> SPEED_UPGRADE_TIER4 = ITEMS.register("speed_upgrade_tier4", () -> new SpeedUpgrade(16));
    public static final DeferredItem<Item> STACK_UPGRADE_TIER1 = ITEMS.register("stack_upgrade_tier1", () -> new StackUpgrade(4));
    public static final DeferredItem<Item> STACK_UPGRADE_TIER2 = ITEMS.register("stack_upgrade_tier2", () -> new StackUpgrade(16));
    public static final DeferredItem<Item> STACK_UPGRADE_TIER3 = ITEMS.register("stack_upgrade_tier3", () -> new StackUpgrade(64));
    public static final DeferredItem<Item> STACK_UPGRADE_TIER4 = ITEMS.register("stack_upgrade_tier4", () -> new StackUpgrade(256));
    public static final DeferredItem<Item> EXTRA_DROP_UPGRADE_TIER1 = ITEMS.register("extra_drop_upgrade_tier1", () -> new ExtraDropUpgrade(25));
    public static final DeferredItem<Item> EXTRA_DROP_UPGRADE_TIER2 = ITEMS.register("extra_drop_upgrade_tier2", () -> new ExtraDropUpgrade(50));
    public static final DeferredItem<Item> EXTRA_DROP_UPGRADE_TIER3 = ITEMS.register("extra_drop_upgrade_tier3", () -> new ExtraDropUpgrade(75));
    public static final DeferredItem<Item> EXTRA_DROP_UPGRADE_TIER4 = ITEMS.register("extra_drop_upgrade_tier4", () -> new ExtraDropUpgrade(100));
    public static final DeferredItem<Item> INVERTED_UPGRADE = ITEMS.register("inverted_upgrade", InvertedUpgrade::new);
    public static final DeferredItem<Item> STRENGTH_UPGRADE = ITEMS.register("strength_upgrade", StrengthUpgrade::new);

    public static final DeferredItem<Item> CHEF_MODULE = ITEMS.register("chef_module", ModuleItem::new);
    public static final DeferredItem<Item> FARMER_MODULE = ITEMS.register("farmer_module", ModuleItem::new);
    public static final DeferredItem<Item> GATHERER_MODULE = ITEMS.register("gatherer_module", ModuleItem::new);
    public static final DeferredItem<Item> FISHER_MODULE = ITEMS.register("fisher_module", ModuleItem::new);
    public static final DeferredItem<Item> BUTCHER_MODULE = ITEMS.register("butcher_module", ModuleItem::new);

    public static final DeferredItem<Item> ENDER_DRAGON_MODULE = ITEMS.register("ender_dragon_module", BossModuleItem::new);
    public static final DeferredItem<Item> WITHER_MODULE = ITEMS.register("wither_module", BossModuleItem::new);
    public static final DeferredItem<Item> WARDEN_MODULE = ITEMS.register("warden_module", BossModuleItem::new);


}
