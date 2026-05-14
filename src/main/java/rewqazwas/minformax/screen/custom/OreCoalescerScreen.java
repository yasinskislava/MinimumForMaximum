package rewqazwas.minformax.screen.custom;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.network.PacketDistributor;
import rewqazwas.minformax.MinForMax;
import rewqazwas.minformax.custom.utility.UpgradeHud;
import rewqazwas.minformax.custom.utility.Utils;
import rewqazwas.minformax.network.SideConfigPayload;

public class OreCoalescerScreen extends AbstractContainerScreen<OreCoalescerMenu> {
    private static final ResourceLocation GUI_TEXTURE = ResourceLocation.fromNamespaceAndPath(MinForMax.MOD_ID, "textures/gui/ore_coalescer.png");
    private static final ResourceLocation HIGHLIGHTED_SLOT = ResourceLocation.fromNamespaceAndPath(MinForMax.MOD_ID, "textures/gui/highlighted_slot.png");
    private static final ResourceLocation SELECT_TEXTURE = ResourceLocation.fromNamespaceAndPath(MinForMax.MOD_ID, "textures/gui/select.png");

    public OreCoalescerScreen(OreCoalescerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        Utils.drawTemplate(guiGraphics, x, y, imageWidth, imageHeight, GUI_TEXTURE, this.menu.getEnergyLevel());
        Utils.drawUpgradeTab(guiGraphics, x, y);
        Utils.drawProgressBar(guiGraphics, x, y, this.menu.getProgress());
        Utils.drawIOTab(guiGraphics, x, y);
        Utils.drawInfoTab(guiGraphics, x, y);
        Utils.drawUpgradeInfoTab(guiGraphics, x, y);
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
        int posX = (width - imageWidth) / 2;
        int posY = (height - imageHeight) / 2;
        if (Utils.isMouseOver(mouseX, mouseY, posX - 24, posY - 19, 24, 24)) {
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
        
        if(Math.clamp(x , posX + 163, posX + 169) == x && Math.clamp(y, posY + 13, posY + 68) == y) {
            int energy = this.menu.data.get(2);
            guiGraphics.renderTooltip(this.font, Component.literal(Utils.simpleEnergyDisplay(energy, this.menu.blockEntity.energyHandler.getMaxEnergyStored())), x, y);
        }
        
        if(Math.clamp(x , posX + 44, posX + 132) == x && Math.clamp(y, posY + 70, posY + 76) == y) {
            guiGraphics.renderTooltip(this.font, Component.literal(this.menu.getPercentage() + "%").withColor(5635925), x, y);
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
}
