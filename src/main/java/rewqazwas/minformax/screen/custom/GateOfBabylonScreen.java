package rewqazwas.minformax.screen.custom;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import rewqazwas.minformax.MinForMax;
import rewqazwas.minformax.custom.utility.Utils;

public class GateOfBabylonScreen extends AbstractContainerScreen<GateOfBabylonMenu> {
    private static final ResourceLocation GUI_TEXTURE = ResourceLocation.fromNamespaceAndPath(MinForMax.MOD_ID, "textures/gui/template.png");
    private static final ResourceLocation HIGHLIGHTED_SLOT = ResourceLocation.fromNamespaceAndPath(MinForMax.MOD_ID, "textures/gui/highlighted_slot.png");

    public GateOfBabylonScreen(GateOfBabylonMenu menu, Inventory playerInventory, Component title) {
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
    }
}