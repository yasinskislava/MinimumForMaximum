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


public class FarmerScreen extends AbstractContainerScreen<FarmerMenu> {
    private static final ResourceLocation GUI_TEXTURE = ResourceLocation.fromNamespaceAndPath(MinForMax.MOD_ID, "textures/gui/template.png");
    private static final ResourceLocation PROGRESS_BAR = ResourceLocation.fromNamespaceAndPath(MinForMax.MOD_ID, "textures/gui/progress_bar.png");
    private static final ResourceLocation ENERGY_BAR = ResourceLocation.fromNamespaceAndPath(MinForMax.MOD_ID, "textures/gui/energy_bar.png");
    private static final ResourceLocation UPGRADES_BAR = ResourceLocation.fromNamespaceAndPath(MinForMax.MOD_ID, "textures/gui/upgrades_bar.png");
    private static final ResourceLocation SLOT = ResourceLocation.fromNamespaceAndPath(MinForMax.MOD_ID, "textures/gui/slot.png");
    private static final ResourceLocation HIGHLIGHTED_SLOT = ResourceLocation.fromNamespaceAndPath(MinForMax.MOD_ID, "textures/gui/highlighted_slot.png");

    public FarmerScreen(FarmerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, GUI_TEXTURE);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        guiGraphics.blit(GUI_TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
        guiGraphics.blit(UPGRADES_BAR, x - 23, y + 3, 0, 0, 23, 78, 23, 78);
        guiGraphics.blit(SLOT, x + 80 - 1, y + 31 - 1, 0, 0, 18, 18, 18, 18);
        var barHeight = 55;
        var energyLevel = this.menu.getEnergyLevel();
        var energyDiff = barHeight - energyLevel;
        guiGraphics.blit(ENERGY_BAR, x + 163, y + 13 + energyDiff, 0, energyDiff, 7, energyLevel, 7, barHeight);
        guiGraphics.blit(PROGRESS_BAR, x + 44, y + 70, 0, 0, this.menu.getProgress(), 6, 88, 6);
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
        if(Math.clamp(x , posX + 44, posX + 132) == x && Math.clamp(y, posY + 70, posY + 76) == y) {
            guiGraphics.renderTooltip(this.font, Component.literal(this.menu.getPercentage() + "%").withColor(5635925), x, y);
        }
        if(Math.clamp(x , posX + 163, posX + 169) == x && Math.clamp(y, posY + 13, posY + 68) == y) {
            int energy = this.menu.data.get(2);
            guiGraphics.renderTooltip(this.font, Component.literal(Utils.simpleEnergyDisplay(energy, this.menu.blockEntity.energyHandler.getMaxEnergyStored())), x, y);
        }
    }
}
