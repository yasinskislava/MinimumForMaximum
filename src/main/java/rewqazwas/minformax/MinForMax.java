package rewqazwas.minformax;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import rewqazwas.minformax.custom.CreativeTabs;
import rewqazwas.minformax.custom.ModAttachmentTypes;
import rewqazwas.minformax.custom.ModBlockEntities;
import rewqazwas.minformax.custom.blocks.CreativeEnergyBlockEntity;
import rewqazwas.minformax.custom.blocks.EternalGeneratorBlockEntity;
import rewqazwas.minformax.custom.blocks.ModBlocks;
import rewqazwas.minformax.custom.component.ModDataComponents;
import rewqazwas.minformax.custom.index.MasterIndex;
import rewqazwas.minformax.custom.index.PlayerIndex;
import rewqazwas.minformax.custom.index.reload.MobDropsReloadListener;
import rewqazwas.minformax.custom.index.reload.ModuleDropsReloadListener;
import rewqazwas.minformax.custom.items.ModItems;
import rewqazwas.minformax.screen.ModMenuTypes;
import rewqazwas.minformax.screen.custom.EternalGeneratorScreen;
import rewqazwas.minformax.screen.custom.IndexLabScreen;

import static rewqazwas.minformax.custom.utility.Utils.clearContent;

@Mod(MinForMax.MOD_ID)
public class MinForMax {
    public static final String MOD_ID = "minformax";


    public MinForMax(IEventBus eventBus, ModContainer modContainer) {
        ModItems.ITEMS.register(eventBus);
        ModBlocks.BLOCKS.register(eventBus);
        CreativeTabs.TABS.register(eventBus);
        ModBlockEntities.BLOCK_ENTITY_TYPES.register(eventBus);
        ModMenuTypes.MENUS.register(eventBus);
        ModDataComponents.DATA_COMPONENT_TYPES.register(eventBus);
        ModAttachmentTypes.ATTACHMENT_TYPES.register(eventBus);
    }


    @EventBusSubscriber(Dist.CLIENT)
    public static class ClientSetup {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                ItemProperties.register(ModItems.MEMORY_SHARD.get(), ResourceLocation.fromNamespaceAndPath(MinForMax.MOD_ID, "storage"),
                        ((stack, level, entity, seed) -> stack.get(ModDataComponents.MOB_INDEX) != null ? 1.0f : 0.0f));
            });
        }

        @SubscribeEvent
        public static void registerScreens(RegisterMenuScreensEvent event) {
            event.register(ModMenuTypes.INDEX_LAB_MENU.get(), IndexLabScreen::new);
            event.register(ModMenuTypes.ETERNAL_GENERATOR_MENU.get(), EternalGeneratorScreen::new);
        }
    }


    @EventBusSubscriber
    public static class ClientServerSetup {
        @SubscribeEvent
        public static void onServerReady(ServerStartedEvent event) {
            MobDropsReloadListener.syncMobDropsIntoAllLevels(event.getServer().overworld());
        }

        @SubscribeEvent
        public static void playerJoins(PlayerEvent.PlayerLoggedInEvent event) {
            var player = event.getEntity();
            player.setData(ModAttachmentTypes.INDEX_SYNC, clearContent(PlayerIndex.getLocalIndex((ServerPlayer) player), event.getEntity().level()));
        }

        @SubscribeEvent
        public static void registerCapabilities(RegisterCapabilitiesEvent event) {
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.ETERNAL_GENERATOR_BE.get(), (EternalGeneratorBlockEntity be, Direction context) -> be.upgradeHandler);
            event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ModBlockEntities.ETERNAL_GENERATOR_BE.get(), (EternalGeneratorBlockEntity be, Direction context) -> be.energyHandler);
            event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ModBlockEntities.CREATIVE_ENERGY_BE.get(), (CreativeEnergyBlockEntity be, Direction context) -> be.energyHandler);
        }

        @SubscribeEvent
        public static void onAddReloadListeners(AddReloadListenerEvent event) {
            event.addListener(new MobDropsReloadListener());
            event.addListener(new ModuleDropsReloadListener());
        }
    }
}
