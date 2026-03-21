package rewqazwas.minformax;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
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
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
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
import rewqazwas.minformax.custom.blocks.PandoraBox.PandoraBoxCoreBlockEntity;
import rewqazwas.minformax.custom.command.IndexCommand;
import rewqazwas.minformax.custom.component.ModDataComponents;
import rewqazwas.minformax.custom.index.ModDataReloadListener;
import rewqazwas.minformax.custom.index.PlayerIndex;
import rewqazwas.minformax.custom.items.LinkerItem;
import rewqazwas.minformax.custom.items.ModItems;
import rewqazwas.minformax.custom.items.upgrades.UpgradeItem;
import rewqazwas.minformax.custom.utility.UpgradeHud;
import rewqazwas.minformax.network.SyncJeiDataPacket;
import rewqazwas.minformax.renderer.BlockReplicatorRenderer;
import rewqazwas.minformax.renderer.FluidReplicatorRenderer;
import rewqazwas.minformax.renderer.PandoraBoxRenderer;
import rewqazwas.minformax.screen.ModMenuTypes;
import rewqazwas.minformax.screen.custom.*;

import java.util.List;

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
                ItemProperties.register(ModItems.LINKER.get(), ResourceLocation.fromNamespaceAndPath(MinForMax.MOD_ID, "storage"),
                        ((stack, level, entity, seed) -> stack.get(ModDataComponents.LINKED_POS) != null && !stack.get(ModDataComponents.LINKED_POS).isEmpty() ? 1.0f : 0.0f));
            });
        }

        @SubscribeEvent
        public static void onRenderLevelStage(RenderLevelStageEvent event) {
            if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;

            Minecraft mc = Minecraft.getInstance();
            LocalPlayer player = mc.player;
            if (player == null) return;

            ItemStack stack = player.getMainHandItem();
            if (!(stack.getItem() instanceof LinkerItem)) {
                stack = player.getOffhandItem();
            }

            if (stack.getItem() instanceof LinkerItem) {
                List<BlockPos> positions = stack.get(ModDataComponents.LINKED_POS);
                if (positions == null || positions.isEmpty()) return;

                PoseStack poseStack = event.getPoseStack();
                Vec3 camera = mc.gameRenderer.getMainCamera().getPosition();

                VertexConsumer buffer = mc.renderBuffers().bufferSource().getBuffer(RenderType.debugFilledBox());

                for (BlockPos pos : positions) {
                    poseStack.pushPose();

                    float pulse = (float) (Math.sin(System.currentTimeMillis() / 500.0) * 0.1 + 0.3);
                    poseStack.translate(pos.getX() - camera.x, pos.getY() - camera.y, pos.getZ() - camera.z);

                    LevelRenderer.addChainedFilledBoxVertices(
                            poseStack,
                            buffer,
                            0.0, 0.0, 0.0,
                            1.01, 1.01, 1.01,
                            1.0f, 1.0f, 1.0f, pulse
                    );

                    poseStack.popPose();
                }

                mc.renderBuffers().bufferSource().endBatch(RenderType.debugFilledBox());
            }
        }

        @SubscribeEvent
        public static void registerScreens(RegisterMenuScreensEvent event) {
            event.register(ModMenuTypes.INDEX_LAB_MENU.get(), IndexLabScreen::new);
            event.register(ModMenuTypes.ETERNAL_GENERATOR_MENU.get(), EternalGeneratorScreen::new);
            event.register(ModMenuTypes.ORE_COALESCER_MENU.get(), OreCoalescerScreen::new);
            event.register(ModMenuTypes.FARMER_MENU.get(), FarmerScreen::new);
            event.register(ModMenuTypes.PANDORA_MENU.get(), PandoraBoxScreen::new);
        }

        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(ModBlockEntities.BLOCK_REPLICATOR_BE.get(), BlockReplicatorRenderer::new);
            event.registerBlockEntityRenderer(ModBlockEntities.FLUID_REPLICATOR_BE.get(), FluidReplicatorRenderer::new);
            event.registerBlockEntityRenderer(ModBlockEntities.PANDORA_BOX_CORE_BE.get(), PandoraBoxRenderer::new);
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
            MinForMax.LOGGER.info("Data sizes - Mob: {}, Module: {}, Fluid: {}, Block: {}, Farmer: {}",
                ModDataReloadListener.MOB_DROPS.size(),
                ModDataReloadListener.MODULE_DROPS.size(),
                ModDataReloadListener.FLUID_REPLICATOR_DATA.size(),
                ModDataReloadListener.BLOCK_REPLICATOR_DATA.size(), ModDataReloadListener.FARMER_DATA.size()
            );

            PacketDistributor.sendToPlayer((ServerPlayer) player, new SyncJeiDataPacket(
                    ModDataReloadListener.MOB_DROPS,
                    ModDataReloadListener.MODULE_DROPS,
                    ModDataReloadListener.FLUID_REPLICATOR_DATA,
                    ModDataReloadListener.BLOCK_REPLICATOR_DATA,
                    ModDataReloadListener.FARMER_DATA
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
            event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ModBlockEntities.PANDORA_BOX_CORE_BE.get(), (PandoraBoxCoreBlockEntity be, Direction context) -> be.energyHandler);
            event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK,
                    ModBlockEntities.PANDORA_BOX_DUMMY_BE.get(), (be, side) -> {
                        if (be.getBlockState().is(ModBlocks.PANDORA_BOX_HATCH.get())) {
                            BlockPos corePos = be.getCorePos();
                            if (corePos != null) {
                                BlockEntity coreBe = be.getLevel().getBlockEntity(corePos);
                                if (coreBe instanceof PandoraBoxCoreBlockEntity core) {
                                    return core.energyHandler;
                                }
                            }
                        }
                        return null;
                    });
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
//Textures/GUI revamp
//Config revamp
