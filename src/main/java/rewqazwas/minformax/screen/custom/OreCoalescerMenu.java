package rewqazwas.minformax.screen.custom;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import rewqazwas.minformax.custom.blocks.ModBlocks;
import rewqazwas.minformax.custom.blocks.OreCoalescerBlockEntity;
import rewqazwas.minformax.custom.items.upgrades.UpgradeItem;
import rewqazwas.minformax.custom.utility.Utils;
import rewqazwas.minformax.screen.ModMenuTypes;

public class OreCoalescerMenu extends AbstractContainerMenu {
    public final OreCoalescerBlockEntity blockEntity;
    private final Level level;
    public final int containerId;
    public final ContainerData data;
    public final Player player;

    private static final int UPGRADE_SLOTS = 4;
    private static final int INPUT_SLOTS = 8;
    private static final int OUTPUT_SLOTS = 8;

    private static final int VANILLA_SLOT_COUNT = 36;
    private static final int VANILLA_FIRST_SLOT_INDEX = 0;
    private static final int TE_INVENTORY_FIRST_SLOT_INDEX = VANILLA_SLOT_COUNT;
    private static final int TE_INVENTORY_SLOT_COUNT = UPGRADE_SLOTS + INPUT_SLOTS + OUTPUT_SLOTS;

    public OreCoalescerMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(3));
    }

    public OreCoalescerMenu(int containerId, Inventory inv, BlockEntity blockEntity, ContainerData data) {
        super(ModMenuTypes.ORE_COALESCER_MENU.get(), containerId);
        this.blockEntity = ((OreCoalescerBlockEntity) blockEntity);
        this.player = inv.player;
        this.level = player.level();
        this.containerId = containerId;
        this.data = data;

        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        // Upgrades (Slots 0-3 of inventoryHandler)
        for (int i = 0; i < UPGRADE_SLOTS; i++) {
            this.addSlot(new UpgradeSlot(this.blockEntity.inventoryHandler, i, -19, i * 18 + 7));
        }

        for (int i = 0; i < INPUT_SLOTS; i++) {
            this.addSlot(new OversizedSlot(this.blockEntity.inventoryHandler, UPGRADE_SLOTS + i, 11 + i * 18, 12));
        }

        for (int i = 0; i < OUTPUT_SLOTS; i++) {
            this.addSlot(new OversizedSlot(this.blockEntity.outputHandler, i, 11 + i * 18, 47));
        }

        addDataSlots(data);
    }

    public int getProgress() {
        int process = this.data.get(0);
        int maxProcess = this.data.get(1);
        int progressBarWidth = 88;
        return Math.round((process / (float)maxProcess) * progressBarWidth);
    }

    public int getPercentage() {
        int process = this.data.get(0);
        int maxProcess = this.data.get(1);
        return maxProcess != 0 ? Math.round((process / (float)maxProcess) * 100) : 0;
    }

    public int getEnergyLevel() {
        int energy = this.data.get(2);
        int maxEnergy = this.blockEntity.energyHandler.getMaxEnergyStored();
        return Math.round((energy / (float)maxEnergy) * 55);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot sourceSlot = slots.get(index);
        if (sourceSlot == null || !sourceSlot.hasItem()) return ItemStack.EMPTY;
        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSourceStack = sourceStack.copy();

        if (index < VANILLA_SLOT_COUNT) {
            if (!this.moveItemStackTo(sourceStack, TE_INVENTORY_FIRST_SLOT_INDEX, TE_INVENTORY_FIRST_SLOT_INDEX + UPGRADE_SLOTS + INPUT_SLOTS, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index < TE_INVENTORY_FIRST_SLOT_INDEX + TE_INVENTORY_SLOT_COUNT) {
            if (!this.moveItemStackTo(sourceStack, VANILLA_FIRST_SLOT_INDEX, VANILLA_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (sourceStack.getCount() == copyOfSourceStack.getCount()) {
            return ItemStack.EMPTY;
        }

        if (sourceStack.isEmpty()) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.set(sourceStack);
        }
        sourceSlot.onTake(player, sourceStack);

        if (copyOfSourceStack.getCount() > 64) {
            copyOfSourceStack.setCount(64);
        }

        return copyOfSourceStack;
    }

    @Override
    protected boolean moveItemStackTo(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection) {
        boolean flag = false;
        int i = startIndex;
        if (reverseDirection) {
            i = endIndex - 1;
        }

        if (stack.isStackable()) {
            while (!stack.isEmpty() && (reverseDirection ? i >= startIndex : i < endIndex)) {
                Slot slot = this.slots.get(i);
                ItemStack itemstack = slot.getItem();
                if (!itemstack.isEmpty() && ItemStack.isSameItemSameComponents(stack, itemstack)) {
                    int j = itemstack.getCount() + stack.getCount();
                    int k = slot.getMaxStackSize(itemstack);
                    if (j <= k) {
                        stack.setCount(0);
                        ItemStack newStack = itemstack.copy();
                        newStack.setCount(j);
                        slot.set(newStack);
                        flag = true;
                    } else if (itemstack.getCount() < k) {
                        stack.shrink(k - itemstack.getCount());
                        ItemStack newStack = itemstack.copy();
                        newStack.setCount(k);
                        slot.set(newStack);
                        flag = true;
                    }
                }

                if (reverseDirection) {
                    i--;
                } else {
                    i++;
                }
            }
        }

        if (!stack.isEmpty()) {
            if (reverseDirection) {
                i = endIndex - 1;
            } else {
                i = startIndex;
            }

            while (reverseDirection ? i >= startIndex : i < endIndex) {
                Slot slot1 = this.slots.get(i);
                ItemStack itemstack1 = slot1.getItem();
                if (itemstack1.isEmpty() && slot1.mayPlace(stack)) {
                    int l = slot1.getMaxStackSize(stack);
                    slot1.set(stack.split(Math.min(stack.getCount(), l)));
                    flag = true;
                    break;
                }

                if (reverseDirection) {
                    i--;
                } else {
                    i++;
                }
            }
        }

        return flag;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), player, ModBlocks.ORE_COALESCER.get());
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

    class UpgradeSlot extends SlotItemHandler {
        private final IItemHandler itemHandler;
        public UpgradeSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
            super(itemHandler, index, xPosition, yPosition);
            this.itemHandler = itemHandler;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.getItem() instanceof UpgradeItem && !Utils.canPass(itemHandler, stack, UPGRADE_SLOTS);
        }

        @Override
        public int getMaxStackSize(ItemStack stack) {
            return 1;
        }
    }

    public class OversizedSlot extends SlotItemHandler {
        private final int index;
        public OversizedSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
            super(itemHandler, index, xPosition, yPosition);
            this.index = index;
        }

        @Override
        public int getMaxStackSize() {
            return this.getItemHandler().getSlotLimit(this.index);
        }

        @Override
        public int getMaxStackSize(ItemStack stack) {
            return this.getItemHandler().getSlotLimit(this.index);
        }

        @Override
        public ItemStack remove(int amount) {
            amount = Math.min(amount, 64);
            return this.getItemHandler().extractItem(index, amount, false);
        }
    }
}
