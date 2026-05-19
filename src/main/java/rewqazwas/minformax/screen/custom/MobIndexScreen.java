package rewqazwas.minformax.screen.custom;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Quaternionf;
import rewqazwas.minformax.MinForMax;
import rewqazwas.minformax.custom.index.ModDataReloadListener;
import rewqazwas.minformax.custom.utility.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static rewqazwas.minformax.custom.utility.Utils.convertKey;

public class MobIndexScreen extends Screen {
    private static final ResourceLocation OPEN_CARD = ResourceLocation.fromNamespaceAndPath(MinForMax.MOD_ID, "textures/gui/open_card.png");
    private static final ResourceLocation CLOSED_CARD = ResourceLocation.fromNamespaceAndPath(MinForMax.MOD_ID, "textures/gui/closed_card.png");

    private final List<String> scannedMobs;
    private final List<EntityType<?>> allRegisteredMobs;
    private final List<EntityType<?>> filteredMobs;

    private final int cardSize = 64;
    private final int padding = 0;
    private final int columns = 5;
    private int maxVisibleRows = 3;

    private double scrollAmount = 0;
    private int maxScrollRows = 0;

    private EditBox searchBox;

    public MobIndexScreen(List<String> scannedMobs) {
        super(Component.translatable("screen.minformax.index_menu"));
        this.scannedMobs = scannedMobs;
        this.allRegisteredMobs = new ArrayList<>();
        this.filteredMobs = new ArrayList<>();

        for (String mobKey : ModDataReloadListener.MOB_DROPS.keySet()) {
            var key = convertKey(mobKey);
            BuiltInRegistries.ENTITY_TYPE.getOptional(ResourceLocation.tryParse(key)).ifPresent(allRegisteredMobs::add);
        }
        if (allRegisteredMobs.isEmpty()) {
            BuiltInRegistries.ENTITY_TYPE.stream()
                    .filter(type -> type.create(Minecraft.getInstance().level) instanceof LivingEntity)
                    .forEach(allRegisteredMobs::add);
        }

        this.filteredMobs.addAll(this.allRegisteredMobs);
    }

    @Override
    protected void init() {
        super.init();

        this.maxVisibleRows = Math.max(1, (this.height - 60) / (cardSize + padding));
        updateScrollBounds();

        int searchBoxWidth = 150;
        int searchBoxHeight = 20;
        int searchX = (this.width - searchBoxWidth) / 2;
        int searchY = this.height - 20;

        this.searchBox = new EditBox(this.font, searchX, searchY, searchBoxWidth, searchBoxHeight, Component.translatable("gui.minformax.search"));
        this.searchBox.setMaxLength(32);
        this.searchBox.setHint(Component.literal("Search..."));

        this.searchBox.setResponder(this::onSearchTextChanged);

        this.addWidget(this.searchBox);
    }

    private void onSearchTextChanged(String text) {
        this.filteredMobs.clear();
        String query = text.toLowerCase(Locale.ROOT).trim();

        for (EntityType<?> type : this.allRegisteredMobs) {
            String localizedName = type.getDescription().getString().toLowerCase(Locale.ROOT);
            String registryName = BuiltInRegistries.ENTITY_TYPE.getKey(type).getPath().toLowerCase(Locale.ROOT);

            if (localizedName.contains(query) || registryName.contains(query)) {
                this.filteredMobs.add(type);
            }
        }


        this.scrollAmount = 0;
        updateScrollBounds();
    }

    private void updateScrollBounds() {
        int totalRows = (int) Math.ceil((double) filteredMobs.size() / columns);
        this.maxScrollRows = Math.max(0, totalRows - maxVisibleRows);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTicks);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xFFFFFF);

        int gridWidth = (columns * cardSize) + ((columns - 1) * padding);
        int startX = (this.width - gridWidth) / 2;
        int startY = 40;

        int scissorY = startY;
        int scissorHeight = maxVisibleRows * (cardSize + padding);
        guiGraphics.enableScissor(startX - 2, scissorY - 2, startX + gridWidth + 2, scissorY + scissorHeight + 2);

        int currentRowOffset = (int) scrollAmount;

        for (int i = 0; i < filteredMobs.size(); i++) {
            int row = i / columns;
            int col = i % columns;

            int renderRow = row - currentRowOffset;
            if (renderRow < 0 || renderRow >= maxVisibleRows) continue;

            int x = startX + col * (cardSize + padding);
            int y = startY + renderRow * (cardSize + padding);

            EntityType<?> type = filteredMobs.get(i);
            boolean isScanned = scannedMobs.contains(type.toString());

            ResourceLocation cardTexture = isScanned ? OPEN_CARD : CLOSED_CARD;
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            guiGraphics.blit(cardTexture, x, y, 0, 0, cardSize, cardSize, cardSize, cardSize);
            guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.disableBlend();

            Entity entity = type.create(this.minecraft.level);
            if (entity instanceof LivingEntity livingEntity) {
                float maxDimension = Math.max(livingEntity.getBbWidth(), livingEntity.getBbHeight());
                int dynamicScale = (int) (24 / maxDimension);

                dynamicScale = Math.clamp(dynamicScale, 8, 30);
                int yOffset = y + cardSize - 14;

                renderMobModel(guiGraphics, x + (cardSize / 2), yOffset, dynamicScale, livingEntity);
            }

            Component name = type.getDescription();
            int textColor = isScanned ? 0x55FF55 : 0xAA0000;

            Utils.renderScrollingText(guiGraphics, this.font, name, x + 4, y + cardSize - 10, cardSize - 8, textColor, 0.7f);
        }
        guiGraphics.disableScissor();

        this.searchBox.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    private void renderMobModel(GuiGraphics guiGraphics, int x, int y, int scale, LivingEntity entity) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x, y, 50.0);
        guiGraphics.pose().scale((float) (-scale), (float) scale, (float) scale);

        Quaternionf baseRotation = new Quaternionf().rotationZ((float) Math.PI);

        float spinSpeedModifier = 2000.0F;
        float angle = (float) (net.minecraft.Util.getMillis() / spinSpeedModifier % (Math.PI * 2));

        Quaternionf spinRotation = new Quaternionf().rotationY(angle);
        baseRotation.mul(spinRotation);

        guiGraphics.pose().mulPose(baseRotation);

        entity.setYRot(0F);
        entity.setXRot(0F);
        entity.yBodyRot = 0F;
        entity.yHeadRot = 0F;

        EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        entityRenderDispatcher.setRenderShadow(false);

        var bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();

        int cardX = x - (cardSize / 2) + 2;
        int cardY = y - cardSize + 12;
        int cardWidth = cardSize - 4;

        guiGraphics.enableScissor(cardX, cardY, cardX + cardWidth, cardY + cardSize);

        RenderSystem.runAsFancy(() -> {
            entityRenderDispatcher.render(entity, 0.0, 0.0, 0.0, 0.0F, 1.0F, guiGraphics.pose(), bufferSource, 15728880);
        });
        bufferSource.endBatch();

        guiGraphics.disableScissor();

        entityRenderDispatcher.setRenderShadow(true);
        guiGraphics.pose().popPose();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (maxScrollRows > 0) {
            this.scrollAmount = Math.clamp(this.scrollAmount - scrollY, 0, maxScrollRows);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}