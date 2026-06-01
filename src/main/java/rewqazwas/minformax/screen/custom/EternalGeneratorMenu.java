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
    private final Block correctBlock;

    private static final int VANILLA_SLOT_COUNT = 36;
    private static final int VANILLA_FIRST_SLOT_INDEX = 0;
    private static final int CUSTOM_INVENTORY_COUNT = 1;
    private static final int UPGRADE_SLOT_COUNT = 4;
    private static final int CUSTOM_INVENTORY_FIRST_SLOT_INDEX = VANILLA_SLOT_COUNT;
    private static final int UPGRADE_FIRST_SLOT_INDEX = CUSTOM_INVENTORY_FIRST_SLOT_INDEX + CUSTOM_INVENTORY_COUNT;

    public EternalGeneratorMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(8));
    }

    public EternalGeneratorMenu(int containerId, Inventory inv, BlockEntity blockEntity, ContainerData data) {
        super(ModMenuTypes.ETERNAL_GENERATOR_MENU.get(), containerId);
        this.blockEntity = ((EternalGeneratorBlockEntity) blockEntity);
        this.player = inv.player;
        this.level = player.level();
        this.containerId = containerId;
        this.data = data;
        this.correctBlock = ModBlocks.ETERNAL_GENERATOR.get();

        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        addSlots();

        for (int i = 0; i < 4; i++) {
            this.addSlot(new UpgradeSlot(this.blockEntity.upgradeHandler, i, -19, i * 18 + 6));
        }
        addDataSlots(data);
    }

    private void addSlots() {
        int centerX = 80;
        int centerY = 31;
        this.addSlot(new SupplierSlot(this.blockEntity.itemHandler, 0, centerX, centerY));
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

    public int getErrorMask() {
        return this.data.get(7);
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

    public int getConsumptionRate() {
        return this.data.get(6);
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

        // 1. If the item is in the Player Inventory (0-35)
        if (index < VANILLA_SLOT_COUNT) {
            // Try to move to upgrades first if it's an upgrade
            if (isUpgrade(sourceStack)) {
                if (!this.moveItemStackTo(sourceStack, UPGRADE_FIRST_SLOT_INDEX, UPGRADE_FIRST_SLOT_INDEX + UPGRADE_SLOT_COUNT, false)) {
                    // If it fails to move to upgrades, try moving to the supplier slot
                    if (!this.moveItemStackTo(sourceStack, CUSTOM_INVENTORY_FIRST_SLOT_INDEX, CUSTOM_INVENTORY_FIRST_SLOT_INDEX + CUSTOM_INVENTORY_COUNT, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            }
            // If not an upgrade, try moving to supplier slot
            else if (!this.moveItemStackTo(sourceStack, CUSTOM_INVENTORY_FIRST_SLOT_INDEX, CUSTOM_INVENTORY_FIRST_SLOT_INDEX + CUSTOM_INVENTORY_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        }
        // 2. If the item is in the Supplier Slot or Upgrade Slots
        else if (index < UPGRADE_FIRST_SLOT_INDEX + UPGRADE_SLOT_COUNT) {
            if (!this.moveItemStackTo(sourceStack, VANILLA_FIRST_SLOT_INDEX, VANILLA_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }

        if (sourceStack.getCount() == 0) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }
        sourceSlot.onTake(player, sourceStack);
        return copyOfSourceStack;
    }

    // Helper method to keep quickMoveStack clean
    private boolean isUpgrade(ItemStack stack) {
        return stack.is(ModTags.EXTRA_DROP_UPGRADES)
                || stack.is(ModItems.INVERTED_UPGRADE)
                || stack.is(ModTags.SPEED_UPGRADES)
                || (stack.is(ModTags.PROCESSING_UPGRADES) && !(stack.getItem() == ModItems.ULTIMATE_PROCESSING_UPGRADE.get()));
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
