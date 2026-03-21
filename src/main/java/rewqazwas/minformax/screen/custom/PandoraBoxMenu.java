package rewqazwas.minformax.screen.custom;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import rewqazwas.minformax.MinForMax;
import rewqazwas.minformax.custom.blocks.ModBlocks;
import rewqazwas.minformax.custom.blocks.PandoraBox.PandoraBoxCoreBlockEntity;
import rewqazwas.minformax.custom.items.ModItems;
import rewqazwas.minformax.custom.utility.Utils;
import rewqazwas.minformax.screen.ModMenuTypes;

public class PandoraBoxMenu extends AbstractContainerMenu {
    public final PandoraBoxCoreBlockEntity blockEntity;
    private final Level level;
    public final int containerId;
    public final ContainerData data;
    public final Player player;

    private static final int VANILLA_SLOT_COUNT = 36;
    private static final int VANILLA_FIRST_SLOT_INDEX = 0;
    private static int CUSTOM_INVENTORY_COUNT = 1;
    private static final int CUSTOM_INVENTORY_FIRST_SLOT_INDEX = VANILLA_SLOT_COUNT;

    public PandoraBoxMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(5));
    }

    public PandoraBoxMenu(int containerId, Inventory inv, BlockEntity blockEntity, ContainerData data) {
        super(ModMenuTypes.PANDORA_MENU.get(), containerId);
        this.blockEntity = ((PandoraBoxCoreBlockEntity) blockEntity);
        this.player = inv.player;
        this.level = player.level();
        this.containerId = containerId;
        this.data = data;

        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        this.addSlot(new SupplierSlot(this.blockEntity.itemHandler, 0, 80, 31));

        addDataSlots(data);
    }

    public int getEnergyLevel() {
        int energy = this.data.get(0);
        int maxEnergy = this.blockEntity.energyHandler.getMaxEnergyStored();
        return Math.round((energy / (float)maxEnergy) * 55);
    }

    public long getOverload() {
        return (long)this.data.get(1) + (long)this.data.get(2) * Integer.MAX_VALUE;
    }

    public int getOverloadLevel() {
        long overload = getOverload();
        return Math.min(Math.round(((float) overload / Integer.MAX_VALUE) * 55), 55);
    }

    public long getTotalXp() {
        return (long)this.data.get(3) + (long)this.data.get(4) * Integer.MAX_VALUE;
    }

    public int getXPProgress() {
        long xp = getTotalXp();
        var res = Utils.calculateLevel(xp);
        return Math.round((res.currentXp() / (float)res.xpForNext()) * 102);
    }

    public int getXPLevel() {
        long xp = getTotalXp();
        var res = Utils.calculateLevel(xp);
        return res.level();
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == 0) {
            long totalXp = getPlayerXPAsLong(player);
            if (totalXp > 0) {
                int overflow = Math.toIntExact(totalXp / Integer.MAX_VALUE);
                int actualAmount = Math.toIntExact(totalXp % Integer.MAX_VALUE);
                for(int i = 0; i < overflow; i++){
                    this.blockEntity.addXp(Integer.MAX_VALUE);
                }
                this.blockEntity.addXp(actualAmount);
                player.experienceLevel = 0;
                player.experienceProgress = 0;
                player.totalExperience = 0;
            }
            return true;
        }
        return super.clickMenuButton(player, id);
    }

    private long getPlayerXPAsLong(Player player) {
        long total = 0;
        for (int i = 0; i < player.experienceLevel; i++) {
            total += getXpForLevel(i);
        }
        total += Math.round(player.experienceProgress * player.getXpNeededForNextLevel());
        return total;
    }

    private long getXpForLevel(int level) {
        if (level >= 31) return 9L * level - 158;
        if (level >= 16) return 5L * level - 38;
        return 2L * level + 7;
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
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), player, ModBlocks.PANDORA_BOX_CORE.get());
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
