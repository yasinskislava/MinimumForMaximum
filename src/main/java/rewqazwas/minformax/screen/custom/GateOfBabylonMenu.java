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
import rewqazwas.minformax.MinForMax;
import rewqazwas.minformax.custom.blocks.ModBlocks;
import rewqazwas.minformax.custom.blocks.Multiblocks.GateOfBabylonBlockEntity;
import rewqazwas.minformax.custom.blocks.PandoraBox.PandoraBoxCoreBlockEntity;
import rewqazwas.minformax.custom.items.ModItems;
import rewqazwas.minformax.custom.utility.Utils;
import rewqazwas.minformax.screen.ModMenuTypes;

public class GateOfBabylonMenu extends AbstractContainerMenu {
    public final GateOfBabylonBlockEntity blockEntity;
    private final Level level;
    public final int containerId;
    public final ContainerData data;
    public final Player player;

    private static final int VANILLA_SLOT_COUNT = 36;

    public GateOfBabylonMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(4));
    }

    public GateOfBabylonMenu(int containerId, Inventory inv, BlockEntity blockEntity, ContainerData data) {
        super(ModMenuTypes.GATE_OF_BABYLON_MENU.get(), containerId);
        this.player = inv.player;
        this.level = player.level();
        this.containerId = containerId;
        this.data = data;

        if (blockEntity instanceof GateOfBabylonBlockEntity gateBE) {
            this.blockEntity = gateBE;
        } else {
            this.blockEntity = null;
        }

        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        addDataSlots(data);
    }

    public long getCurrentEnergy() {
        int lower = this.data.get(0);
        int upper = this.data.get(1);
        return ((long) upper << 32) | (lower & 0xFFFFFFFFL);
    }

    public long getMaxEnergy() {
        int lower = this.data.get(2);
        int upper = this.data.get(3);
        return ((long) upper << 32) | (lower & 0xFFFFFFFFL);
    }

    public int getEnergyLevel() {
        long currentEnergy = getCurrentEnergy();
        long maxEnergy = getMaxEnergy();

        if (currentEnergy <= 0 || maxEnergy <= 0) return 0;

        double ratio = Math.log1p(currentEnergy) / Math.log1p(maxEnergy);

        return (int) Math.min(Math.round(ratio * 55), 55);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index < 27) {
                if (!this.moveItemStackTo(itemstack1, 27, 36, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index < 36) {
                if (!this.moveItemStackTo(itemstack1, 0, 27, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        return Utils.stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), player,
                ModBlocks.SAKURADITE_CASING.get(),
                ModBlocks.SAKURADITE_PANEL.get(),
                ModBlocks.SAKURADITE_INPUT.get(),
                ModBlocks.SAKURADITE_OUTPUT.get()
        );
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
            return stack.is(ModItems.LINKER);
        }
    }
}