package rewqazwas.minformax.screen.custom;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import rewqazwas.minformax.MinForMax;
import rewqazwas.minformax.custom.ModTags;
import rewqazwas.minformax.custom.blocks.EternalGeneratorBlockEntity;
import rewqazwas.minformax.custom.blocks.FarmerBlockEntity;
import rewqazwas.minformax.custom.blocks.ModBlocks;
import rewqazwas.minformax.custom.component.ModDataComponents;
import rewqazwas.minformax.custom.index.FarmerData;
import rewqazwas.minformax.custom.index.ModDataReloadListener;
import rewqazwas.minformax.custom.items.AccShard;
import rewqazwas.minformax.custom.items.ModItems;
import rewqazwas.minformax.custom.items.upgrades.FortuneUpgrade;
import rewqazwas.minformax.custom.items.upgrades.UpgradeItem;
import rewqazwas.minformax.custom.utility.Utils;
import rewqazwas.minformax.screen.ModMenuTypes;

public class FarmerMenu extends AbstractContainerMenu {
    public final FarmerBlockEntity blockEntity;
    private final Level level;
    public final int containerId;
    public final ContainerData data;
    public final Player player;

    private static final int VANILLA_SLOT_COUNT = 36;
    private static final int VANILLA_FIRST_SLOT_INDEX = 0;
    private static int CUSTOM_INVENTORY_COUNT = 5;
    private static final int CUSTOM_INVENTORY_FIRST_SLOT_INDEX = VANILLA_SLOT_COUNT;

    public FarmerMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(4));
    }

    public FarmerMenu(int containerId, Inventory inv, BlockEntity blockEntity, ContainerData data) {
        super(ModMenuTypes.FARMER_MENU.get(), containerId);
        this.blockEntity = ((FarmerBlockEntity) blockEntity);
        this.player = inv.player;
        this.level = player.level();
        this.containerId = containerId;
        this.data = data;

        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        this.addSlot(new SupplierSlot(this.blockEntity.itemHandler, 0, 80, 31));

        for (int i = 0; i < 4; i++) {
            this.addSlot(new UpgradeSlot(this.blockEntity.upgradeHandler, i, -19, i * 18 + 8));
        }
        addDataSlots(data);
    }

    public int getProgress() {
        int process = this.data.get(0);
        int maxProcess = this.data.get(1);
        int progressBarWidth = 88;
        return Math.round((process / (float)maxProcess) * progressBarWidth);
    }

    public int getEnergyLevel() {
        int energy = this.data.get(2);
        int maxEnergy = this.blockEntity.energyHandler.getMaxEnergyStored();
        return Math.round((energy / (float)maxEnergy) * 55);
    }

    public int getPercentage() {
        int process = this.data.get(0);
        int maxProcess = this.data.get(1);
        return Math.round((process / (float)maxProcess) * 100);
    }

    public int getConsumptionRate() {
        return this.data.get(3);
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
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), player, ModBlocks.FARMER.get());
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
            String itemString = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();

            for (FarmerData data : ModDataReloadListener.FARMER_DATA.values()) {
                // Prohibited check takes priority
                if (data.prohibitedItems().contains(itemString)) return false;
                for (String tag : data.prohibitedTags()) {
                    if (stack.is(TagKey.create(BuiltInRegistries.ITEM.key(), ResourceLocation.parse(tag)))) return false;
                }

                // Allowed check
                if (data.allowedItems().contains(itemString)) return true;
                for (String tag : data.allowedTags()) {
                    if (stack.is(TagKey.create(BuiltInRegistries.ITEM.key(), ResourceLocation.parse(tag)))) return true;
                }
            }
            return false; // Only allow items found in config
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
                    && (stack.is(ModTags.FORTUNE_UPGRADES)
                    || stack.is(ModItems.WATERING_UPGRADE)
                    || stack.is(ModTags.SPEED_UPGRADES)
                    || stack.is(ModItems.COMPRESSING_UPGRADE)
                    || (stack.is(ModTags.PROCESSING_UPGRADES) && !(stack.getItem() == ModItems.ULTIMATE_PROCESSING_UPGRADE.get())));
        }
    }
}
