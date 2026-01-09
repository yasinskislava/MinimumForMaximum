package rewqazwas.minformax.screen.custom;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import rewqazwas.minformax.MinForMax;
import rewqazwas.minformax.custom.utility.Utils;


public class EternalGeneratorScreen extends AbstractContainerScreen<EternalGeneratorMenu> {
    private static final ResourceLocation GUI_TEXTURE = ResourceLocation.fromNamespaceAndPath(MinForMax.MOD_ID, "textures/gui/eternal_generator/eternal_generator.png");
    private static final ResourceLocation PROGRESS_BAR = ResourceLocation.fromNamespaceAndPath(MinForMax.MOD_ID, "textures/gui/progress_bar.png");
    private static final ResourceLocation ENERGY_BAR = ResourceLocation.fromNamespaceAndPath(MinForMax.MOD_ID, "textures/gui/energy_bar.png");
    private static final ResourceLocation UPGRADES_TEXTURE = ResourceLocation.fromNamespaceAndPath(MinForMax.MOD_ID, "textures/gui/upgrades_bar.png");
    private static final ResourceLocation SLOT = ResourceLocation.fromNamespaceAndPath(MinForMax.MOD_ID, "textures/gui/slot.png");
    private static final ResourceLocation HIGHLIGHTED_SLOT = ResourceLocation.fromNamespaceAndPath(MinForMax.MOD_ID, "textures/gui/highlighted_slot.png");
    private static final ResourceLocation XP_MENU = ResourceLocation.fromNamespaceAndPath(MinForMax.MOD_ID, "textures/gui/xp_menu.png");
    private static final ResourceLocation XP_BAR = ResourceLocation.fromNamespaceAndPath(MinForMax.MOD_ID, "textures/gui/xp_bar.png");
    private static final ResourceLocation OVERLOAD_BAR = ResourceLocation.fromNamespaceAndPath(MinForMax.MOD_ID, "textures/gui/overload_bar.png");

    public EternalGeneratorScreen(EternalGeneratorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        this.addRenderableWidget(new CollectButton(x + 135, y - 15, 0, (button) -> {
            if (button instanceof CollectButton) {
                this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 0);
            }
        }));
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, GUI_TEXTURE);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        guiGraphics.blit(GUI_TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
        guiGraphics.blit(UPGRADES_TEXTURE, x - 23, y + 3, 0, 0, 23, 78, 23, 78);
        guiGraphics.blit(XP_MENU, x, y - 21, 0, 0, 176, 24, 176, 24);
        renderSlots(guiGraphics, x, y);
        var barHeight = 55;
        var energyLevel = this.menu.getEnergyLevel();
        var energyDiff = barHeight - energyLevel;
        guiGraphics.blit(ENERGY_BAR, x + 163, y + 13 + energyDiff, 0, energyDiff, 7, energyLevel, 7, barHeight);
        var overloadLevel = this.menu.getOverloadLevel();
        var overloadDiff = barHeight - overloadLevel;
        guiGraphics.blit(OVERLOAD_BAR, x + 6, y + 13 + overloadDiff, 0, overloadDiff, 7, overloadLevel, 7, barHeight);
        guiGraphics.drawString(this.font, "?", x + 7, y + 71, 16777215, true);
        guiGraphics.blit(XP_BAR, x + 31, y - 15, 0, 0, this.menu.getXPProgress(), 12, 102, 12);
        guiGraphics.blit(PROGRESS_BAR, x + 44, y + 70, 0, 0, this.menu.getProgress(), 6, 88, 6);
        var lvl = this.menu.getXPLevel();
        var pos = (lvl < 100) ? 11 : (lvl < 1000) ? 8 : (lvl < 10000) ? 5 : 16;
        guiGraphics.drawString(this.font, lvl + "", x + pos, y - 13, 16777215, true);
    }

    @Override
    protected void renderSlotHighlight(GuiGraphics guiGraphics, Slot slot, int mouseX, int mouseY, float partialTick) {
        if(slot.isHighlightable()){
            guiGraphics.blit(HIGHLIGHTED_SLOT, slot.x - 1, slot.y - 1, 0, 0, 18, 18, 18, 18);
        }
    }

    public void renderSlots(GuiGraphics guiGraphics, int x, int y) {
        int centerX = 80;
        int centerY = 31;
        int[] xOffsets;
        int[] yOffsets;

        int tier = this.menu.tier;

        switch (tier) {
            case 1 -> { xOffsets = new int[]{0};               yOffsets = new int[]{0}; }
            case 2 -> { xOffsets = new int[]{-18, 18};         yOffsets = new int[]{0}; }
            case 3 -> { xOffsets = new int[]{-54, -18, 18, 54}; yOffsets = new int[]{0}; }
            case 4 -> { xOffsets = new int[]{-54, -18, 18, 54}; yOffsets = new int[]{-18, 18}; }
            default -> { xOffsets = new int[0]; yOffsets = new int[0]; }
        }

        int index = 0;
        for (int yOff : yOffsets) {
            for (int xOff : xOffsets) {
                int yAdjust = 0;
                if (tier == 4) {
                    yAdjust = (index < 4) ? 1 : -1;
                }
                int drawX = x + centerX - 1 + xOff;
                int drawY = y + centerY - 1 + yOff + yAdjust;
                guiGraphics.blit(SLOT, drawX, drawY, 0, 0, 18, 18, 18, 18);
                index++;
            }
        }
    }


    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int x, int y) {
        super.renderTooltip(guiGraphics, x, y);
        int posX = (width - imageWidth) / 2;
        int posY = (height - imageHeight) / 2;
        if(Math.clamp(x , posX + 44, posX + 132) == x && Math.clamp(y, posY + 70, posY + 76) == y) {
            guiGraphics.renderTooltip(this.font, Component.literal(this.menu.getPercentage() + "%").withColor(5635925), x, y);
        }
        if(Math.clamp(x , posX + 163, posX + 169) == x && Math.clamp(y, posY + 13, posY + 68) == y) {
            int energy = this.menu.data.get(2);
            guiGraphics.renderTooltip(this.font, Component.literal(Utils.simpleEnergyDisplay(energy, this.menu.blockEntity.energyHandler.getMaxEnergyStored())), x, y);
        }
        if(Math.clamp(x , posX + 6, posX + 12) == x && Math.clamp(y, posY + 13, posY + 68) == y) {
            int overload = this.menu.data.get(4);
            guiGraphics.renderTooltip(this.font, Component.literal(overload + "/5120"), x, y);
        }
        if(Math.clamp(x , posX + 6, posX + 12) == x && Math.clamp(y, posY + 70, posY + 80) == y) {
            guiGraphics.renderTooltip(this.font, Component.translatable("tooltip.minformax.quantum_foam"), x, y);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {}


    public static class CollectButton extends Button {
        protected static final ResourceLocation BUTTON_OFF = ResourceLocation.fromNamespaceAndPath(MinForMax.MOD_ID, "widget/collect_button_off");
        protected static final ResourceLocation BUTTON_ON = ResourceLocation.fromNamespaceAndPath(MinForMax.MOD_ID, "widget/collect_button_on");
        protected static final WidgetSprites SPRITES = new WidgetSprites(
                BUTTON_OFF,
                BUTTON_OFF,
                BUTTON_ON
        );

        public CollectButton(int x, int y, int index, OnPress onPress) {
            super(x, y, 35, 12, Component.literal("+"), onPress, DEFAULT_NARRATION);
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            Minecraft minecraft = Minecraft.getInstance();
            guiGraphics.setColor(1.0F, 1.0F, 1.0F, this.alpha);
            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();
            var highlight = mouseX >= this.getX() && mouseY >= this.getY() && mouseX < this.getX() + this.width && mouseY < this.getY() + this.height;
            guiGraphics.blitSprite(SPRITES.get(highlight, this.isHoveredOrFocused()), this.getX(), this.getY(), this.getWidth(), this.getHeight());
            guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            int i = getFGColor();
            this.renderString(guiGraphics, minecraft.font, i | Mth.ceil(this.alpha * 255.0F) << 24);
        }
    }
}
