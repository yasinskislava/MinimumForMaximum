package rewqazwas.minformax;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rewqazwas.minformax.config.DataConfigs;
import rewqazwas.minformax.custom.CreativeTabs;
import rewqazwas.minformax.custom.ModAttachmentTypes;
import rewqazwas.minformax.custom.ModBlockEntities;
import rewqazwas.minformax.custom.blocks.*;
import rewqazwas.minformax.custom.command.IndexCommand;
import rewqazwas.minformax.custom.component.ModDataComponents;
import rewqazwas.minformax.custom.index.ModDataReloadListener;
import rewqazwas.minformax.custom.index.PlayerIndex;
import rewqazwas.minformax.custom.items.ModItems;
import rewqazwas.minformax.custom.items.upgrades.UpgradeItem;
import rewqazwas.minformax.custom.utility.UpgradeHud;
import rewqazwas.minformax.network.SyncJeiDataPacket;
import rewqazwas.minformax.renderer.BlockReplicatorRenderer;
import rewqazwas.minformax.renderer.FluidReplicatorRenderer;
import rewqazwas.minformax.screen.ModMenuTypes;
import rewqazwas.minformax.screen.custom.EternalGeneratorScreen;
import rewqazwas.minformax.screen.custom.FarmerScreen;
import rewqazwas.minformax.screen.custom.IndexLabScreen;
import rewqazwas.minformax.screen.custom.OreCoalescerScreen;

import static rewqazwas.minformax.custom.utility.Utils.clearContent;

@Mod(MinForMax.MOD_ID)
public class MinForMax {
    public static final String MOD_ID = "minformax";
    public static final Logger LOGGER = LogManager.getLogger();


    public MinForMax(IEventBus eventBus, ModContainer modContainer) {
        ModItems.ITEMS.register(eventBus);
        ModBlocks.BLOCKS.register(eventBus);
        CreativeTabs.TABS.register(eventBus);
        ModBlockEntities.BLOCK_ENTITY_TYPES.register(eventBus);
        ModMenuTypes.MENUS.register(eventBus);
        ModDataComponents.DATA_COMPONENT_TYPES.register(eventBus);
        ModAttachmentTypes.ATTACHMENT_TYPES.register(eventBus);
        modContainer.registerConfig(ModConfig.Type.COMMON, DataConfigs.COMMON);
    }


    @EventBusSubscriber(Dist.CLIENT)
    public static class ClientSetup {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                ItemProperties.register(ModItems.MEMORY_SHARD.get(), ResourceLocation.fromNamespaceAndPath(MinForMax.MOD_ID, "storage"),
                        ((stack, level, entity, seed) -> stack.get(ModDataComponents.MOB_INDEX) != null ? 1.0f : 0.0f));
                ItemProperties.register(ModItems.CHAOS_SHARD.get(), ResourceLocation.fromNamespaceAndPath(MinForMax.MOD_ID, "storage"),
                        ((stack, level, entity, seed) -> stack.get(ModDataComponents.MOB_INDEX) != null ? 1.0f : 0.0f));
            });
        }

        @SubscribeEvent
        public static void registerScreens(RegisterMenuScreensEvent event) {
            event.register(ModMenuTypes.INDEX_LAB_MENU.get(), IndexLabScreen::new);
            event.register(ModMenuTypes.ETERNAL_GENERATOR_MENU.get(), EternalGeneratorScreen::new);
            event.register(ModMenuTypes.ORE_COALESCER_MENU.get(), OreCoalescerScreen::new);
            event.register(ModMenuTypes.FARMER_MENU.get(), FarmerScreen::new);
        }

        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(ModBlockEntities.BLOCK_REPLICATOR_BE.get(), BlockReplicatorRenderer::new);
            event.registerBlockEntityRenderer(ModBlockEntities.FLUID_REPLICATOR_BE.get(), FluidReplicatorRenderer::new);
        }

        @SubscribeEvent
        public static void registerGuiLayers(RegisterGuiLayersEvent event) {
            event.registerAbove(VanillaGuiLayers.CROSSHAIR, ResourceLocation.fromNamespaceAndPath(MinForMax.MOD_ID, "upgrade_hud"), (guiGraphics, deltaTracker) -> {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player == null || mc.level == null) return;

                if (mc.player.getMainHandItem().getItem() instanceof UpgradeItem || mc.player.getOffhandItem().getItem() instanceof UpgradeItem) {
                    HitResult hitResult = mc.hitResult;
                    if (hitResult instanceof BlockHitResult blockHitResult) {
                        BlockEntity be = mc.level.getBlockEntity(blockHitResult.getBlockPos());
                        if (be != null && UpgradeHud.isBlockValid(be)) {
                            UpgradeHud.render(guiGraphics, mc, be);
                        }
                    }
                }
            });
        }

    }

    @EventBusSubscriber
    public static class ClientServerSetup {

        @SubscribeEvent
        public static void playerJoins(PlayerEvent.PlayerLoggedInEvent event) {
            var player = event.getEntity();
            player.setData(ModAttachmentTypes.INDEX_SYNC, clearContent(PlayerIndex.getLocalIndex((ServerPlayer) player), event.getEntity().level()));
            
            MinForMax.LOGGER.info("Sending JEI data packet to player {}", player.getName().getString());
            MinForMax.LOGGER.info("Data sizes - Mob: {}, Module: {}, Fluid: {}, Block: {}",
                ModDataReloadListener.MOB_DROPS.size(),
                ModDataReloadListener.MODULE_DROPS.size(),
                ModDataReloadListener.FLUID_REPLICATOR_DATA.size(),
                ModDataReloadListener.BLOCK_REPLICATOR_DATA.size()
            );

            PacketDistributor.sendToPlayer((ServerPlayer) player, new SyncJeiDataPacket(
                    ModDataReloadListener.MOB_DROPS,
                    ModDataReloadListener.MODULE_DROPS,
                    ModDataReloadListener.FLUID_REPLICATOR_DATA,
                    ModDataReloadListener.BLOCK_REPLICATOR_DATA
            ));
        }

        @SubscribeEvent
        public static void playerClone(PlayerEvent.Clone event) {
            if (event.isWasDeath()) {
                var original = event.getOriginal();
                var player = event.getEntity();
                var originalIndex = PlayerIndex.getLocalIndex((ServerPlayer) original);
                PlayerIndex.save((ServerPlayer) player, originalIndex);
                player.setData(ModAttachmentTypes.INDEX_SYNC, clearContent(originalIndex, event.getEntity().level()));
            }
        }

        @SubscribeEvent
        public static void registerCapabilities(RegisterCapabilitiesEvent event) {
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.ETERNAL_GENERATOR_BE.get(), (EternalGeneratorBlockEntity be, Direction context) -> be.upgradeHandler);
            event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ModBlockEntities.ETERNAL_GENERATOR_BE.get(), (EternalGeneratorBlockEntity be, Direction context) -> be.energyHandler);
            event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ModBlockEntities.CREATIVE_ENERGY_BE.get(), (CreativeEnergyBlockEntity be, Direction context) -> be.energyHandler);
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.FLUID_REPLICATOR_BE.get(), (FluidReplicatorBlockEntity be, Direction context) -> be.upgradeHandler);
            event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.FLUID_REPLICATOR_BE.get(), (FluidReplicatorBlockEntity be, Direction context) -> be.fluidHandler);
            event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ModBlockEntities.FLUID_REPLICATOR_BE.get(), (FluidReplicatorBlockEntity be, Direction context) -> be.energyHandler);
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.BLOCK_REPLICATOR_BE.get(), (BlockReplicatorBlockEntity be, Direction context) -> be.upgradeHandler);
            event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ModBlockEntities.BLOCK_REPLICATOR_BE.get(), (BlockReplicatorBlockEntity be, Direction context) -> be.energyHandler);
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.FARMER_BE.get(), (FarmerBlockEntity be, Direction context) -> be.upgradeHandler);
            event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ModBlockEntities.FARMER_BE.get(), (FarmerBlockEntity be, Direction context) -> be.energyHandler);
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.ORE_COALESCER_BE.get(), (OreCoalescerBlockEntity be, Direction context) -> be.automationHandler);
            event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ModBlockEntities.ORE_COALESCER_BE.get(), (OreCoalescerBlockEntity be, Direction context) -> be.energyHandler);
         }

        @SubscribeEvent
        public static void onAddReloadListener(AddReloadListenerEvent event) {
            event.addListener(new ModDataReloadListener());
        }

        @SubscribeEvent
        public static void registerCommands(RegisterCommandsEvent event) {
            IndexCommand.register(event.getDispatcher());
        }
    }
}

//TODO
//Fisher
//Revamp module system
//Void tower

//Upgrade