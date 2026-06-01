package rewqazwas.minformax.screen.custom;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.network.PacketDistributor;
import rewqazwas.minformax.MinForMax;
import rewqazwas.minformax.custom.utility.UpgradeHud;
import rewqazwas.minformax.custom.utility.Utils;
import rewqazwas.minformax.network.packet.SideConfigPayload;

public class OreCoalescerScreen extends AbstractContainerScreen<OreCoalescerMenu> {
    private static final ResourceLocation GUI_TEXTURE = ResourceLocation.fromNamespaceAndPath(MinForMax.MOD_ID, "textures/gui/ore_coalescer.png");
    private static final ResourceLocation HIGHLIGHTED_SLOT = ResourceLocation.fromNamespaceAndPath(MinForMax.MOD_ID, "textures/gui/highlighted_slot.png");
    private final Utils.SideConfigComponent sideConfig = new Utils.SideConfigComponent();

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
        Utils.drawIOTab(guiGraphics, this.font, x, y, mouseX, mouseY);
        Utils.drawInfoTab(guiGraphics, this.font, x, y, mouseX, mouseY, this.menu.getErrorMask());
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
        sideConfig.render(guiGraphics, this.menu.blockEntity);
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int x, int y) {
        super.renderTooltip(guiGraphics, x, y);
        int posX = (width - imageWidth) / 2;
        int posY = (height - imageHeight) / 2;

        Utils.renderEnergyTooltip(guiGraphics, this.font, x, y, posX, posY, this.menu.data.get(2), this.menu.blockEntity.energyHandler.getMaxEnergyStored());
        Utils.renderPercentageTooltip(guiGraphics, this.font, x, y, posX, posY, this.menu.getPercentage());

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
}
