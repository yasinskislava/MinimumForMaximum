package rewqazwas.minformax.screen;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import rewqazwas.minformax.MinForMax;
import rewqazwas.minformax.screen.custom.EternalGeneratorMenu;
import rewqazwas.minformax.screen.custom.IndexLabMenu;


public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, MinForMax.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<IndexLabMenu>> INDEX_LAB_MENU = registerMenuType("index_lab_menu", IndexLabMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<EternalGeneratorMenu>> ETERNAL_GENERATOR_MENU = registerMenuType("eternal_generator_menu", EternalGeneratorMenu::new);


    private static <T extends AbstractContainerMenu>DeferredHolder<MenuType<?>, MenuType<T>> registerMenuType(String name, IContainerFactory<T> factory) {
        return MENUS.register(name, () -> IMenuTypeExtension.create(factory));
    }
}
