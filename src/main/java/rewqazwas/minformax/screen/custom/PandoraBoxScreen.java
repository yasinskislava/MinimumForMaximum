package rewqazwas.minformax.screen.custom;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import rewqazwas.minformax.MinForMax;
import rewqazwas.minformax.custom.utility.Utils;


public class PandoraBoxScreen extends AbstractContainerScreen<PandoraBoxMenu> {
    private static final ResourceLocation GUI_TEXTURE = ResourceLocation.fromNamespaceAndPath(MinForMax.MOD_ID, "textures/gui/template.png");
    private static final ResourceLocation HIGHLIGHTED_SLOT = ResourceLocation.fromNamespaceAndPath(MinForMax.MOD_ID, "textures/gui/highlighted_slot.png");
    private static final ResourceLocation XP_MENU = ResourceLocation.fromNamespaceAndPath(MinForMax.MOD_ID, "textures/gui/xp_menu.png");
    private static final ResourceLocation XP_BAR = ResourceLocation.fromNamespaceAndPath(MinForMax.MOD_ID, "textures/gui/xp_bar.png");

    public PandoraBoxScreen(PandoraBoxMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        this.addRenderableWidget(new ActionButton(x + 135, y - 15, "-", (button) -> {
            if (this.minecraft != null && this.minecraft.gameMode != null) {
                this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 0);
            }
        }));
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        Utils.drawTemplate(guiGraphics, x, y, imageWidth, imageHeight, GUI_TEXTURE, this.menu.getEnergyLevel());
        Utils.drawOverload(guiGraphics, x, y, this.menu.getOverloadLevel());
        Utils.drawSlot(guiGraphics, x + 79, y + 30);

        guiGraphics.blit(XP_MENU, x, y - 21, 0, 0, 176, 24, 176, 24);
        guiGraphics.blit(XP_BAR, x + 32, y - 15, 0, 0, this.menu.getXPProgress(), 12, 102, 12);
        var lvl = this.menu.getXPLevel();
        var pos = 5;
        if(lvl <= 9){
            pos = 13;
        } else if(lvl <= 99) {
            pos = 11;
        } else if(lvl <= 1000) {
            pos = 7;
        }
        guiGraphics.drawString(this.font, Utils.getDelimeter(lvl), x + pos, y - 13, 16777215, true);

        long overload = this.menu.getOverload();
        long xpLevel = this.menu.getXPLevel();
        double multiplier = Math.log10(xpLevel + 10);
        long production = (long) (overload * multiplier);

        // Multiplier text
        String multText = String.format("x%.2f", multiplier);
        int multWidth = this.font.width(multText);
        guiGraphics.drawString(this.font, Component.literal(multText), x + 88 - (multWidth / 2), y + 18, 16777215, false);

        // Energy generation text
        String genText = Utils.simpleEnergyDisplay(production) + "/t";
        int textWidth = this.font.width(genText);
        guiGraphics.drawString(this.font, Component.literal(genText), x + 89 - (textWidth / 2), y + 55, 16777215, false);
    }

    @Override
    protected void renderSlotHighlight(GuiGraphics guiGraphics, Slot slot, int mouseX, int mouseY, float partialTick) {
        if(slot.isHighlightable()){
            guiGraphics.blit(HIGHLIGHTED_SLOT, slot.x - 1, slot.y - 1, 0, 0, 18, 18, 18, 18);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {}

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int x, int y) {
        super.renderTooltip(guiGraphics, x, y);
        int posX = (width - imageWidth) / 2;
        int posY = (height - imageHeight) / 2;
        if(Math.clamp(x , posX + 163, posX + 169) == x && Math.clamp(y, posY + 13, posY + 68) == y) {
            long currentEnergy = this.menu.getCurrentEnergy();
            guiGraphics.renderTooltip(this.font, Component.literal(Utils.simpleEnergyDisplay(currentEnergy)), x, y);
        }
        if(Math.clamp(x , posX + 6, posX + 12) == x && Math.clamp(y, posY + 13, posY + 68) == y) {
            guiGraphics.renderTooltip(this.font, Component.literal(Utils.getDelimeter(this.menu.getOverload()) + " Overload"), x, y);
        }
    }

    public static class ActionButton extends Button {
        protected static final ResourceLocation BUTTON_OFF = ResourceLocation.fromNamespaceAndPath(MinForMax.MOD_ID, "widget/collect_button_off");
        protected static final ResourceLocation BUTTON_ON = ResourceLocation.fromNamespaceAndPath(MinForMax.MOD_ID, "widget/collect_button_on");
        protected static final WidgetSprites SPRITES = new WidgetSprites(
                BUTTON_OFF,
                BUTTON_OFF,
                BUTTON_ON
        );

        public ActionButton(int x, int y, String message, OnPress onPress) {
            super(x, y, 35, 12, Component.literal(message), onPress, DEFAULT_NARRATION);
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
