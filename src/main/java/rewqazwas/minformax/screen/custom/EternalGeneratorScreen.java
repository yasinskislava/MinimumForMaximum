package rewqazwas.minformax.screen.custom;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
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
import rewqazwas.minformax.network.SideConfigPayload;


public class EternalGeneratorScreen extends AbstractContainerScreen<EternalGeneratorMenu> {
    private static final ResourceLocation GUI_TEXTURE = ResourceLocation.fromNamespaceAndPath(MinForMax.MOD_ID, "textures/gui/template.png");
    private static final ResourceLocation SLOT = ResourceLocation.fromNamespaceAndPath(MinForMax.MOD_ID, "textures/gui/slot.png");
    private static final ResourceLocation HIGHLIGHTED_SLOT = ResourceLocation.fromNamespaceAndPath(MinForMax.MOD_ID, "textures/gui/highlighted_slot.png");
    private static final ResourceLocation XP_MENU = ResourceLocation.fromNamespaceAndPath(MinForMax.MOD_ID, "textures/gui/xp_menu.png");
    private static final ResourceLocation XP_BAR = ResourceLocation.fromNamespaceAndPath(MinForMax.MOD_ID, "textures/gui/xp_bar.png");
    private static final ResourceLocation SELECT_TEXTURE = ResourceLocation.fromNamespaceAndPath(MinForMax.MOD_ID, "textures/gui/select.png");

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
        Utils.drawConsumptionTab(guiGraphics, x, y);
        Utils.drawIOTab(guiGraphics, x, y);
        Utils.drawInfoTab(guiGraphics, x, y);
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
        if(showSideConfig){
            renderSideConfig(guiGraphics);
        }
    }

    private void renderSideConfig(GuiGraphics guiGraphics) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 300);
        guiGraphics.blit(SIDE_CONFIG_TEX, configX, configY, 0, 0, 68, 68, 68, 68);
        for (int i = 0; i < boxes.length; i++) {
            Direction dir = getDirectionFromBox(i);

            if (this.menu.blockEntity.isSideEnabled(dir)) {
                int drawX = configX + boxes[i].x;
                int drawY = configY + boxes[i].y;

                guiGraphics.blit(SELECT_TEXTURE, drawX, drawY, 0, 0, 18, 18, 18, 18);
            }
        }
        guiGraphics.pose().popPose();
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
            guiGraphics.renderTooltip(this.font, Component.literal(overload + ""), x, y);
        }
        if(Math.clamp(x , posX + 6, posX + 12) == x && Math.clamp(y, posY + 70, posY + 80) == y) {
            guiGraphics.renderTooltip(this.font, Component.translatable("tooltip.minformax.overload"), x, y);
        }
        if(Math.clamp(x , posX + 135, posX + 170) == x && Math.clamp(y, posY - 15, posY - 3) == y) {
            guiGraphics.renderTooltip(this.font, Component.translatable("tooltip.minformax.collect_xp"), x, y);
        }
        if (Utils.isMouseOver(x, y, posX - 24, posY + 142, 24, 24)) {
            int consumptionRate = this.menu.getConsumptionRate();
            guiGraphics.renderTooltip(this.font,
                    Component.literal(Utils.simpleEnergyDisplay(consumptionRate) + "/t")
                            .withColor(0xFF5555), x, y);
        }
        if (Utils.isMouseOver(x, y, posX - 24, posY + 86, 24, 24)) {
            guiGraphics.renderTooltip(this.font, Component.translatable("tooltip.minformax.side_config"), x, y);
        }

        if (Utils.isMouseOver(x, y, posX - 24, posY + 114, 24, 24)) {
            guiGraphics.renderTooltip(this.font, Component.translatable("tooltip.minformax.under_construction"), x, y);
        }

        if (showSideConfig) {
            for (int i = 0; i < boxes.length; i++) {
                ConfigBox box = boxes[i];
                int boxX = configX + box.x;
                int boxY = configY + box.y;
                if (Utils.isMouseOver(x, y, boxX, boxY, box.w, box.h)) {
                    Direction dir = getDirectionFromBox(i);
                    guiGraphics.renderTooltip(this.font, Component.literal(dir.name()), x, y);
                }
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {}

    private static final ResourceLocation SIDE_CONFIG_TEX = ResourceLocation.fromNamespaceAndPath(MinForMax.MOD_ID, "textures/gui/side_config.png");
    private int configX = 58, configY = 132; // Position on screen
    private boolean isDragging = false;
    private double dragOffsetX, dragOffsetY;
    private boolean showSideConfig = false;

    private record ConfigBox(int x, int y, int w, int h, String label) {}
    private final ConfigBox[] boxes = {
            new ConfigBox(5, 5, 18, 18, "top_left"),    // Up
            new ConfigBox(25, 5, 18, 18, "top_mid"),    // Forward
            new ConfigBox(5, 25, 18, 18, "mid_left"),   // Left
            new ConfigBox(45, 25, 18, 18, "mid_right"), // Right
            new ConfigBox(25, 45, 18, 18, "bot_mid"),   // Back
            new ConfigBox(45, 45, 18, 18, "bot_right")  // Down
    };

    private Direction getDirectionFromBox(int boxIndex) {
        Direction facing = this.minecraft.player.getDirection();
        return switch (boxIndex) {
            case 0 -> Direction.UP;
            case 5 -> Direction.DOWN;
            case 1 -> facing; // Forward
            case 2 -> facing.getCounterClockWise(); // Left
            case 3 -> facing.getClockWise(); // Right
            case 4 -> facing.getOpposite(); // Back
            default -> Direction.NORTH;
        };
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (Utils.isMouseOver(mouseX, mouseY, leftPos - 24, topPos + 86, 24, 24)) { // Updated coordinates for IO tab
            this.showSideConfig = !this.showSideConfig;
            return true;
        }
        if(showSideConfig){
            for (int i = 0; i < 6; i++) {
                if (mouseX >= configX + boxes[i].x && mouseX < configX + boxes[i].x + boxes[i].w &&
                        mouseY >= configY + boxes[i].y && mouseY < configY + boxes[i].y + boxes[i].h) {

                    Direction dir = getDirectionFromBox(i);
                    PacketDistributor.sendToServer(new SideConfigPayload(this.menu.blockEntity.getBlockPos(), dir.get3DDataValue()));
                    return true;
                }
            }
            // Check if clicking background to drag
            if (mouseX >= configX && mouseX < configX + 68 && mouseY >= configY && mouseY < configY + 68) {
                isDragging = true;
                dragOffsetX = mouseX - configX;
                dragOffsetY = mouseY - configY;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (isDragging && showSideConfig) {
            configX = (int) (mouseX - dragOffsetX);
            configY = (int) (mouseY - dragOffsetY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        isDragging = false;
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
