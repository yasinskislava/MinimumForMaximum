package rewqazwas.minformax.screen.custom;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.network.PacketDistributor;
import rewqazwas.minformax.MinForMax;
import rewqazwas.minformax.custom.utility.UpgradeHud;
import rewqazwas.minformax.custom.utility.Utils;
import rewqazwas.minformax.network.packet.SideConfigPayload;


public class EternalGeneratorScreen extends AbstractContainerScreen<EternalGeneratorMenu> {
    private static final ResourceLocation GUI_TEXTURE = ResourceLocation.fromNamespaceAndPath(MinForMax.MOD_ID, "textures/gui/template.png");
    private static final ResourceLocation SLOT = ResourceLocation.fromNamespaceAndPath(MinForMax.MOD_ID, "textures/gui/slot.png");
    private static final ResourceLocation HIGHLIGHTED_SLOT = ResourceLocation.fromNamespaceAndPath(MinForMax.MOD_ID, "textures/gui/highlighted_slot.png");
    private static final ResourceLocation XP_MENU = ResourceLocation.fromNamespaceAndPath(MinForMax.MOD_ID, "textures/gui/xp_menu.png");
    private static final ResourceLocation XP_BAR = ResourceLocation.fromNamespaceAndPath(MinForMax.MOD_ID, "textures/gui/xp_bar.png");
    private final Utils.SideConfigComponent sideConfig = new Utils.SideConfigComponent();

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
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        Utils.drawTemplate(guiGraphics, x, y, imageWidth, imageHeight, GUI_TEXTURE, this.menu.getEnergyLevel());
        Utils.drawUpgradeTab(guiGraphics, x, y - 2);
        Utils.drawProgressBar(guiGraphics, x, y, this.menu.getProgress());
        Utils.drawConsumptionTab(guiGraphics, this.font, x, y, mouseX, mouseY, this.menu.getConsumptionRate());
        Utils.drawIOTab(guiGraphics, this.font, x, y, mouseX, mouseY);
        Utils.drawInfoTab(guiGraphics, this.font, x, y, mouseX, mouseY, this.menu.getErrorMask());
        Utils.drawUpgradeInfoTab(guiGraphics, x, y - 2);
        guiGraphics.blit(XP_MENU, x, y - 21, 0, 0, 176, 24, 176, 24);
        renderSlots(guiGraphics, x, y);
        Utils.drawOverload(guiGraphics, x, y, this.menu.getOverloadLevel());
        guiGraphics.drawString(this.font, "?", x + 7, y + 71, 16777215, true);
        guiGraphics.blit(XP_BAR, x + 31, y - 15, 0, 0, this.menu.getXPProgress(), 12, 102, 12);
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
        int drawX = x + centerX - 1;
        int drawY = y + centerY - 1;
        guiGraphics.blit(SLOT, drawX, drawY, 0, 0, 18, 18, 18, 18);
    }


    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
        int posX = (width - imageWidth) / 2;
        int posY = (height - imageHeight) / 2;
        if (Utils.isMouseOver(mouseX, mouseY, posX - 24, posY - 21, 24, 24)) {
            UpgradeHud.render(guiGraphics, this.minecraft, this.menu.blockEntity);
        }
        sideConfig.render(guiGraphics, this.menu.blockEntity);
    }


    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int x, int y) {
        super.renderTooltip(guiGraphics, x, y);
        int posX = (width - imageWidth) / 2;
        int posY = (height - imageHeight) / 2;

        Utils.renderPercentageTooltip(guiGraphics, this.font, x, y, posX, posY, this.menu.getPercentage());

        Utils.renderEnergyTooltip(guiGraphics, this.font, x, y, posX, posY, this.menu.data.get(2), this.menu.blockEntity.energyHandler.getMaxEnergyStored());

        if (Utils.isMouseOver(x, y, posX + 6, posY + 13, 6, 55)) {
            int overload = this.menu.data.get(4);
            guiGraphics.renderTooltip(this.font, Component.literal(String.valueOf(overload)), x, y);
        }
        if (Utils.isMouseOver(x, y, posX + 6, posY + 70, 6, 10)) {
            guiGraphics.renderTooltip(this.font, Component.translatable("tooltip.minformax.overload"), x, y);
        }
        if (Utils.isMouseOver(x, y, posX + 135, posY - 15, 35, 12)) {
            guiGraphics.renderTooltip(this.font, Component.translatable("tooltip.minformax.collect_xp"), x, y);
        }

        sideConfig.renderTooltips(guiGraphics, this.font, x, y);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {}


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (Utils.isMouseOver(mouseX, mouseY, leftPos - 24, topPos + 86, 24, 24)) {
            sideConfig.showSideConfig = !sideConfig.showSideConfig;
            return true;
        }
        if (sideConfig.mouseClicked(mouseX, mouseY, button, this.menu.blockEntity.getBlockPos())) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (sideConfig.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        sideConfig.mouseReleased(mouseX, mouseY, button);
        return super.mouseReleased(mouseX, mouseY, button);
    }

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
