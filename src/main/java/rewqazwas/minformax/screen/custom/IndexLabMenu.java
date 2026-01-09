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
import rewqazwas.minformax.custom.ModAttachmentTypes;
import rewqazwas.minformax.custom.blocks.IndexLabBlockEntity;
import rewqazwas.minformax.custom.blocks.ModBlocks;
import rewqazwas.minformax.custom.component.ModDataComponents;
import rewqazwas.minformax.custom.index.HolderClass;
import rewqazwas.minformax.custom.items.ModItems;
import rewqazwas.minformax.screen.ModMenuTypes;

import java.util.ArrayList;
import java.util.TreeMap;

public class IndexLabMenu extends AbstractContainerMenu {
    public final IndexLabBlockEntity blockEntity;
    private final Level level;
    public final int containerId;
    public final ContainerData data;
    public ArrayList<String> indexList;
    public ArrayList<HolderClass> holderList;

    private static final int VANILLA_SLOT_COUNT = 36;
    private static final int VANILLA_FIRST_SLOT_INDEX = 0;
    private static final int CUSTOM_INVENTORY_COUNT = 1;
    private static final int CUSTOM_INVENTORY_FIRST_SLOT_INDEX = VANILLA_SLOT_COUNT;

    public IndexLabMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(3));
    }

    public IndexLabMenu(int containerId, Inventory inv, BlockEntity blockEntity, ContainerData data) {
        super(ModMenuTypes.INDEX_LAB_MENU.get(), containerId);
        this.blockEntity = ((IndexLabBlockEntity) blockEntity);
        this.level = inv.player.level();
        this.containerId = containerId;
        this.data = data;
        var tempData = new TreeMap<>(inv.player.getData(ModAttachmentTypes.INDEX_SYNC));
        if(inv.player.level().isClientSide()){
            this.indexList = new ArrayList<>(tempData.keySet());
            this.holderList = new ArrayList<>(tempData.values());
        }

        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        this.addSlot(new MemoryCardSlot(this.blockEntity.itemHandler, 0, 143, 45));
        addDataSlots(data);
    }

    public boolean isProcessing() {
        return data.get(0) > 0;
    }

    public int getProgress() {
        int process = this.data.get(0);
        int maxProcess = 1200;
        int progressBarWidth = 88;
        return Math.round((process / (float)maxProcess) * progressBarWidth);
    }

    public int getKey() {
        return this.data.get(1);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot sourceSlot = slots.get(index);
        if (sourceSlot == null || !sourceSlot.hasItem()) return ItemStack.EMPTY;
        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSourceStack = sourceStack.copy();
        if(index < VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT) {
            if (!this.moveItemStackTo(sourceStack, CUSTOM_INVENTORY_FIRST_SLOT_INDEX, CUSTOM_INVENTORY_FIRST_SLOT_INDEX + CUSTOM_INVENTORY_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else if(index < CUSTOM_INVENTORY_FIRST_SLOT_INDEX + CUSTOM_INVENTORY_COUNT) {
            if (!this.moveItemStackTo(sourceStack, VANILLA_FIRST_SLOT_INDEX, VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            System.out.println("Invalid slotIndex:" + index);
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
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), player, ModBlocks.INDEX_LAB.get());
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        blockEntity.setKey(id);
        return super.clickMenuButton(player, id);
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 108 + l * 18, 84 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 108 + i * 18, 142));
        }
    }


    class MemoryCardSlot extends SlotItemHandler {
        public MemoryCardSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
            super(itemHandler, index, xPosition, yPosition);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.getItem() == ModItems.MEMORY_SHARD.get() && stack.get(ModDataComponents.MOB_INDEX) == null;
        }
    }
}
