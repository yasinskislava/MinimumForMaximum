package rewqazwas.minformax.screen.custom;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import rewqazwas.minformax.MinForMax;
import rewqazwas.minformax.custom.ModTags;
import rewqazwas.minformax.custom.blocks.EternalGeneratorBlockEntity;
import rewqazwas.minformax.custom.blocks.ModBlocks;
import rewqazwas.minformax.custom.component.ModDataComponents;
import rewqazwas.minformax.custom.items.AccShard;
import rewqazwas.minformax.custom.items.ModItems;
import rewqazwas.minformax.custom.utility.Utils;
import rewqazwas.minformax.screen.ModMenuTypes;

public class EternalGeneratorMenu extends AbstractContainerMenu {
    public final EternalGeneratorBlockEntity blockEntity;
    private final Level level;
    public final int containerId;
    public final ContainerData data;
    public final Player player;
    public final int tier;
    private final Block correctBlock;

    private static final int VANILLA_SLOT_COUNT = 36;
    private static final int VANILLA_FIRST_SLOT_INDEX = 0;
    private static int CUSTOM_INVENTORY_COUNT = 4;
    private static final int CUSTOM_INVENTORY_FIRST_SLOT_INDEX = VANILLA_SLOT_COUNT;

    public EternalGeneratorMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(6));
    }

    public EternalGeneratorMenu(int containerId, Inventory inv, BlockEntity blockEntity, ContainerData data) {
        super(ModMenuTypes.ETERNAL_GENERATOR_MENU.get(), containerId);
        this.blockEntity = ((EternalGeneratorBlockEntity) blockEntity);
        this.player = inv.player;
        this.level = player.level();
        this.containerId = containerId;
        this.data = data;
        this.tier = this.blockEntity.tier;
        CUSTOM_INVENTORY_COUNT = 4 + (int) Math.pow(2, this.tier - 1);
        this.correctBlock = switch (this.tier) {
            case 1 -> ModBlocks.ETERNAL_GENERATOR_TIER1.get();
            case 2 -> ModBlocks.ETERNAL_GENERATOR_TIER2.get();
            case 3 -> ModBlocks.ETERNAL_GENERATOR_TIER3.get();
            case 4 -> ModBlocks.ETERNAL_GENERATOR_TIER4.get();
            default -> null;
        };

        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        addSlots();

        for (int i = 0; i < 4; i++) {
            this.addSlot(new UpgradeSlot(this.blockEntity.upgradeHandler, i, -19, i * 18 + 7));
        }
        addDataSlots(data);
    }

    private void addSlots() {
        int centerX = 80;
        int centerY = 31;
        int[] xOffsets;
        int[] yOffsets;

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
                this.addSlot(new SupplierSlot(this.blockEntity.itemHandler, index++, centerX + xOff, centerY + yOff + yAdjust));
            }
        }
    }

    public int getProgress() {
        int process = this.data.get(0);
        int maxProcess = this.data.get(1);
        int progressBarWidth = 88;
        return Math.round((process / (float)maxProcess) * progressBarWidth);
    }

    public int getXPProgress() {
        long xp = (long)this.data.get(3) + (long)this.data.get(5) * Integer.MAX_VALUE;
        var res = Utils.calculateLevel(xp);
        return Math.round((res.currentXp() / (float)res.xpForNext()) * 102);
    }

    public int getXPLevel() {
        long xp = (long)this.data.get(3) + (long)this.data.get(5) * Integer.MAX_VALUE;
        var res = Utils.calculateLevel(xp);
        return res.level();
    }

    public int getEnergyLevel() {
        int energy = this.data.get(2);
        int maxEnergy = this.blockEntity.energyHandler.getMaxEnergyStored();
        return Math.round((energy / (float)maxEnergy) * 55);
    }

    public int getOverloadLevel() {
        int overload = this.data.get(4);
        return Math.min(Math.round((overload / (float)5120) * 55), 55);
    }

    public int getPercentage() {
        int process = this.data.get(0);
        int maxProcess = this.data.get(1);
        return Math.round((process / (float)maxProcess) * 100);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        long totalXp = (long)this.data.get(3) + (long)this.data.get(5) * Integer.MAX_VALUE;
        int xpToGive = (int)Math.min(totalXp, Integer.MAX_VALUE);

        player.giveExperiencePoints(xpToGive);

        long remainingXp = totalXp - xpToGive;
        this.data.set(3, (int)(remainingXp % Integer.MAX_VALUE));
        this.data.set(5, (int)(remainingXp / Integer.MAX_VALUE));
        return super.clickMenuButton(player, id);
    }


    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot sourceSlot = slots.get(index);
        if (sourceSlot == null || !sourceSlot.hasItem()) return ItemStack.EMPTY;
        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSourceStack = sourceStack.copy();
        if(index < VANILLA_SLOT_COUNT) {
            if (!this.moveItemStackTo(sourceStack, CUSTOM_INVENTORY_FIRST_SLOT_INDEX, CUSTOM_INVENTORY_FIRST_SLOT_INDEX + CUSTOM_INVENTORY_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else if(index < CUSTOM_INVENTORY_FIRST_SLOT_INDEX + CUSTOM_INVENTORY_COUNT) {
            if (!this.moveItemStackTo(sourceStack, VANILLA_FIRST_SLOT_INDEX, VANILLA_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            MinForMax.LOGGER.error("Invalid slotIndex:" + index);
            return ItemStack.EMPTY;
        }
        if(sourceStack.getCount() == 0) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }
        sourceSlot.onTake(player, sourceStack);
        return copyOfSourceStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), player, correctBlock);
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 84 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

    class SupplierSlot extends SlotItemHandler {
        public SupplierSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
            super(itemHandler, index, xPosition, yPosition);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            var pass = !(stack.getItem() instanceof AccShard) || stack.get(ModDataComponents.MOB_INDEX) != null;
            return stack.is(ModTags.SUPPLIER_ITEMS) && pass;
        }
    }

    class UpgradeSlot extends SlotItemHandler {
        public UpgradeSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
            super(itemHandler, index, xPosition, yPosition);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return !Utils.canPass(this.getItemHandler(), stack)
                    && (stack.is(ModTags.EXTRA_DROP_UPGRADES)
                    || stack.is(ModItems.INVERTED_UPGRADE)
                    || stack.is(ModTags.SPEED_UPGRADES)
                    || (stack.is(ModTags.PROCESSING_UPGRADES) && !(stack.getItem() == ModItems.ULTIMATE_PROCESSING_UPGRADE.get())));
        }
    }
}
