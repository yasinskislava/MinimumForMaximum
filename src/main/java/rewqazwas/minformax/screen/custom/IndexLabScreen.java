package rewqazwas.minformax.screen.custom;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import rewqazwas.minformax.MinForMax;
import rewqazwas.minformax.custom.index.HolderClass;
import rewqazwas.minformax.custom.utility.Utils;

import java.util.List;


public class IndexLabScreen extends AbstractContainerScreen<IndexLabMenu> {
    private static final ResourceLocation GUI_TEXTURE = ResourceLocation.fromNamespaceAndPath(MinForMax.MOD_ID, "textures/gui/index_lab/index_lab.png");
    private static final ResourceLocation SCROLLER_SPRITE = ResourceLocation.fromNamespaceAndPath(MinForMax.MOD_ID,"widget/scroller");
    private static final ResourceLocation PROGRESS_BAR = ResourceLocation.fromNamespaceAndPath(MinForMax.MOD_ID, "textures/gui/progress_bar.png");
    private static final ResourceLocation HIGHLIGHTED_SLOT = ResourceLocation.fromNamespaceAndPath(MinForMax.MOD_ID, "textures/gui/highlighted_slot.png");
    private static final ResourceLocation SUB_BACKGROUND = ResourceLocation.fromNamespaceAndPath(MinForMax.MOD_ID, "widget/collect_button_off");
    private final IndexButton[] indexButtons = new IndexButton[7];
    int scrollOff;
    private HolderClass mobData;
    private int mobIndex;


    public IndexLabScreen(IndexLabMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 276;
    }


    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, GUI_TEXTURE);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        guiGraphics.blit(GUI_TEXTURE, x, y, 0, 0, imageWidth, imageHeight, 512, 256);
        this.mobIndex = menu.getKey();
        if(mobIndex >= 0){
            postButtonClick();
        }
        this.renderProgressBar(guiGraphics, x, y);
    }

    private void renderScroller(GuiGraphics guiGraphics, int posX, int posY, int size) {
        int i = size + 1 - 7;
        if (i > 1) {
            int j = 139 - (27 + (i - 1) * 139 / i);
            int k = 1 + j / i + 139 / i;
            int i1 = Math.min(113, this.scrollOff * k);
            if (this.scrollOff == i - 1) {
                i1 = 113;
            }

            guiGraphics.blitSprite(SCROLLER_SPRITE, posX + 94, posY + 9 + i1, 0, 6, 27);
        } else {
            guiGraphics.blitSprite(SCROLLER_SPRITE, posX + 94, posY + 9, 0, 6, 27);
        }

    }

    @Override
    protected void init() {
        super.init();
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        int k = j + 9;

        for(int l = 0; l < 7; ++l) {
            this.indexButtons[l] = (IndexButton) this.addRenderableWidget(new IndexButton(i + 5, j + 9 + l * 20, l, (button) -> {
                if (button instanceof IndexButton) {
                    this.mobIndex = ((IndexButton)button).getIndex() + this.scrollOff;
                    this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, this.mobIndex);
                    this.postButtonClick();
                }

            }));
            k += 20;
        }
    }

    private void postButtonClick() {
        this.mobData = this.menu.holderList.get(this.mobIndex);
    }

    private void renderData(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (this.mobData != null) {
            int i = (this.width - this.imageWidth) / 2;
            int j = (this.height - this.imageHeight) / 2;
            int k = 0;
            ItemStack mainDrop = this.mobData.mainDrop();
            List<ItemStack> extraDrops = this.mobData.additionalDrop();

            guiGraphics.drawString(this.font, "Duration: " + this.mobData.duration(), i + 106, j + 10, 16777215, false);
            guiGraphics.drawString(this.font, "FE/Cycle: " + Utils.simpleEnergyDisplay(this.mobData.duration() * 4), i + 106, j + 28, 16777215, false);

            guiGraphics.drawString(this.font, "Main Drop", i + 208, j + 10, 16777215, false);
            guiGraphics.blitSprite(SUB_BACKGROUND, i + 202, j + 21, 0, 61, 20);
            guiGraphics.renderFakeItem(mainDrop, i + 205, j + 23);
            if(mouseX >= i + 205 && mouseX <= i + 205 + 16 && mouseY >= j + 23 && mouseY <= j + 23 + 16) {guiGraphics.renderTooltip(this.font, mainDrop, mouseX, mouseY);}
            guiGraphics.drawString(this.font, "+", i + 225, j + 29, 16777215, false);
            guiGraphics.renderFakeItem(Items.EXPERIENCE_BOTTLE.getDefaultInstance(), i + 233, j + 23);
            guiGraphics.drawString(this.font, "" + this.mobData.xp(), i + 249, j + 31, 16777215, false);

            guiGraphics.drawString(this.font, "Extra Drop", i + 205, j + 45, 16777215, false);
            guiGraphics.blitSprite(SUB_BACKGROUND, i + 202, j + 59, 0, 61, 20);//▼
            if(extraDrops.isEmpty()) {
                guiGraphics.drawString(this.font, "None", i + 220, j + 66, 16777215, false);
            } else {
                for (ItemStack item : extraDrops) {
                    guiGraphics.renderFakeItem(item, i + 205 + k, j + 61);
                    if(mouseX >= i + 205 + k && mouseX <= i + 205 + k + 16 && mouseY >= j + 61 && mouseY <= j + 61 + 16) {
                        guiGraphics.renderTooltip(this.font, item, mouseX, mouseY);
                    }
                    k += 19;
                }
            }
        }
    }

    private void renderProgressBar(GuiGraphics guiGraphics, int posX, int posY) {
        if(this.menu.isProcessing()) {
            guiGraphics.blit(PROGRESS_BAR, posX + 107, posY + 71, 0, 0, this.menu.getProgress(), 6, 88, 6);
        }
    }



    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        int k = j + 16 + 1;
        int size = this.menu.indexList.size();
        this.renderScroller(guiGraphics, i, j, size);

        int i1 = 0;

        for(Object key : this.menu.indexList) {
            if (!this.canScroll(size) || i1 >= this.scrollOff && i1 < 7 + this.scrollOff) {
                int topPadding = (20 - this.font.lineHeight) / 2;
                guiGraphics.drawString(this.font, Utils.prettyName((String) key), i + 10, k + topPadding - 7, 16777215, false);
                k += 20;
            }
            ++i1;
        }

        for(IndexButton IndexLabScreen$IndexButton : this.indexButtons) {
            if(IndexLabScreen$IndexButton.isHoveredOrFocused()) {
                IndexLabScreen$IndexButton.renderToolTip(guiGraphics, mouseX, mouseY);
            }
            IndexLabScreen$IndexButton.visible = IndexLabScreen$IndexButton.index < size;
        }

        this.renderData(guiGraphics, mouseX, mouseY);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    private boolean canScroll(int numOffers) {
        return numOffers > 7;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int i = this.menu.indexList.size(); //Entries
        if (this.canScroll(i)) {
            int j = i - 7;
            this.scrollOff = Mth.clamp((int)((double)this.scrollOff - scrollY), 0, j);
        }

        return true;
    }

    class IndexButton extends Button {
        protected static final ResourceLocation BUTTON_OFF = ResourceLocation.fromNamespaceAndPath(MinForMax.MOD_ID, "widget/collect_button_off");
        protected static final ResourceLocation BUTTON_ON = ResourceLocation.fromNamespaceAndPath(MinForMax.MOD_ID, "widget/collect_button_on");
        protected static final WidgetSprites SPRITES = new WidgetSprites(
                BUTTON_OFF,
                BUTTON_OFF,
                BUTTON_ON
        );
        final int index;

        public IndexButton(int x, int y, int index, OnPress onPress) {
            super(x, y, 88, 20, CommonComponents.EMPTY, onPress, DEFAULT_NARRATION);
            this.index = index;
            this.visible = false;
        }

        public int getIndex() {
            return this.index;
        }

        public void renderToolTip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
            if (this.isHovered) {
                //Model
            }
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

    @Override
    protected void renderSlotHighlight(GuiGraphics guiGraphics, Slot slot, int mouseX, int mouseY, float partialTick) {
        if(slot.isHighlightable()){
            guiGraphics.blit(HIGHLIGHTED_SLOT, slot.x - 1, slot.y - 1, 0, 0, 18, 18, 18, 18);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {}
}
