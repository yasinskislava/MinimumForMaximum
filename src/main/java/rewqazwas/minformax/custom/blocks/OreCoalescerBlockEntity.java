package rewqazwas.minformax.custom.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;
import net.neoforged.neoforge.items.wrapper.RangedWrapper;
import rewqazwas.minformax.custom.ModBlockEntities;
import rewqazwas.minformax.custom.ModTags;
import rewqazwas.minformax.custom.items.ModItems;
import rewqazwas.minformax.custom.items.upgrades.*;
import rewqazwas.minformax.custom.utility.Utils;
import rewqazwas.minformax.screen.custom.OreCoalescerMenu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class OreCoalescerBlockEntity extends MachineBaseEntity implements MenuProvider {
    public OreCoalescerBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.ORE_COALESCER_BE.get(), pos, blockState);

        this.enabledSides[Direction.DOWN.get3DDataValue()] = true;
        this.enabledSides[Direction.UP.get3DDataValue()] = true;
    }
    //Handlers

    public final ItemStackHandler inventoryHandler = new ItemStackHandler(12) {
        private final int[] bigStackCounts = new int[8]; // For slots 4-11

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (slot < 4) {
                updateUpgrades();
            }
            if (!isProcessing && level != null && !level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot < 4 ? 1 : 512;
        }

        @Override
        protected int getStackLimit(int slot, ItemStack stack) {
            return getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (slot < 4) {
                return stack.is(ModTags.PROCESSING_UPGRADES) ||
                        stack.is(ModTags.SPEED_UPGRADES) ||
                        stack.is(ModTags.FORTUNE_UPGRADES) ||
                        stack.is(ModItems.AUTO_SMELTING_UPGRADE);
            }
            return stack.is(Tags.Items.ORES);
        }

        @Override
        public void setStackInSlot(int slot, ItemStack stack) {
            if (slot < 4) {
                super.setStackInSlot(slot, stack);
                return;
            }
            int bigSlot = slot - 4;
            if (stack.isEmpty()) {
                this.bigStackCounts[bigSlot] = 0;
                super.setStackInSlot(slot, ItemStack.EMPTY);
            } else {
                this.bigStackCounts[bigSlot] = stack.getCount();
                super.setStackInSlot(slot, stack.copyWithCount(1));
            }
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            if (slot < 4) {
                return super.getStackInSlot(slot);
            }
            ItemStack stack = super.getStackInSlot(slot);
            if (stack.isEmpty()) {
                return ItemStack.EMPTY;
            }
            return stack.copyWithCount(this.bigStackCounts[slot - 4]);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (slot < 4) {
                if(Utils.canPass(this, stack)) {
                    return stack;
                }
                return super.insertItem(slot, stack, simulate);
            }

            if (stack.isEmpty() || !isItemValid(slot, stack)) {
                return stack;
            }

            int bigSlot = slot - 4;
            ItemStack existing = super.getStackInSlot(slot);
            int limit = getSlotLimit(slot);

            if (!existing.isEmpty() && !ItemStack.isSameItemSameComponents(existing, stack)) {
                return stack;
            }

            int currentCount = existing.isEmpty() ? 0 : this.bigStackCounts[bigSlot];
            int space = limit - currentCount;
            if (space <= 0) {
                return stack;
            }

            int toInsert = Math.min(stack.getCount(), space);
            if (toInsert <= 0) {
                return stack;
            }

            if (!simulate) {
                if (existing.isEmpty()) {
                    super.setStackInSlot(slot, stack.copyWithCount(1));
                }
                this.bigStackCounts[bigSlot] += toInsert;
                onContentsChanged(slot);
            }

            ItemStack remainder = stack.copy();
            remainder.shrink(toInsert);
            return remainder;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot < 4) {
                return super.extractItem(slot, amount, simulate);
            }

            if (amount == 0) return ItemStack.EMPTY;

            int bigSlot = slot - 4;
            ItemStack existing = super.getStackInSlot(slot);
            if (existing.isEmpty()) return ItemStack.EMPTY;

            int currentCount = this.bigStackCounts[bigSlot];
            int toExtract = Math.min(amount, currentCount);

            if (toExtract <= 0) return ItemStack.EMPTY;

            if (!simulate) {
                this.bigStackCounts[bigSlot] -= toExtract;
                if (this.bigStackCounts[bigSlot] == 0) {
                    super.setStackInSlot(slot, ItemStack.EMPTY);
                }
                onContentsChanged(slot);
            }

            return existing.copyWithCount(toExtract);
        }

        @Override
        public CompoundTag serializeNBT(HolderLookup.Provider registries) {
            CompoundTag nbt = super.serializeNBT(registries);
            nbt.putIntArray("BigStackCounts", this.bigStackCounts);
            return nbt;
        }

        @Override
        public void deserializeNBT(HolderLookup.Provider registries, CompoundTag nbt) {
            super.deserializeNBT(registries, nbt);
            if (nbt.contains("BigStackCounts", CompoundTag.TAG_INT_ARRAY)) {
                int[] loadedCounts = nbt.getIntArray("BigStackCounts");
                if (loadedCounts.length == this.bigStackCounts.length) {
                    System.arraycopy(loadedCounts, 0, this.bigStackCounts, 0, loadedCounts.length);
                }
            }
            OreCoalescerBlockEntity.this.updateUpgrades();
        }
    };

    public final ItemStackHandler outputHandler = new ItemStackHandler(8) {
        private final int[] counts = new int[8];

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (!isProcessing && level != null && !level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }

        @Override
        public int getSlotLimit(int slot) {
            return 5120;
        }

        @Override
        protected int getStackLimit(int slot, ItemStack stack) {
            return getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return false;
        }

        @Override
        public void setStackInSlot(int slot, ItemStack stack) {
            if (stack.isEmpty()) {
                this.counts[slot] = 0;
                super.setStackInSlot(slot, ItemStack.EMPTY);
            } else {
                this.counts[slot] = stack.getCount();
                super.setStackInSlot(slot, stack.copyWithCount(1));
            }
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            ItemStack stack = super.getStackInSlot(slot);
            if (stack.isEmpty()) {
                return ItemStack.EMPTY;
            }
            return stack.copyWithCount(this.counts[slot]);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (stack.isEmpty()) return stack;

            ItemStack existing = super.getStackInSlot(slot);
            int limit = getSlotLimit(slot);

            if (!existing.isEmpty() && !ItemStack.isSameItemSameComponents(existing, stack)) {
                return stack;
            }

            int currentCount = existing.isEmpty() ? 0 : this.counts[slot];
            int space = limit - currentCount;
            if (space <= 0) return stack;

            int toInsert = Math.min(stack.getCount(), space);
            if (toInsert <= 0) return stack;

            if (!simulate) {
                if (existing.isEmpty()) {
                    super.setStackInSlot(slot, stack.copyWithCount(1));
                }
                this.counts[slot] += toInsert;
                onContentsChanged(slot);
            }

            ItemStack remainder = stack.copy();
            remainder.shrink(toInsert);
            return remainder;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (amount == 0) return ItemStack.EMPTY;

            ItemStack existing = super.getStackInSlot(slot);
            if (existing.isEmpty()) return ItemStack.EMPTY;

            int currentCount = this.counts[slot];
            int toExtract = Math.min(amount, currentCount);

            if (toExtract <= 0) return ItemStack.EMPTY;

            if (!simulate) {
                this.counts[slot] -= toExtract;
                if (this.counts[slot] == 0) {
                    super.setStackInSlot(slot, ItemStack.EMPTY);
                }
                onContentsChanged(slot);
            }

            return existing.copyWithCount(toExtract);
        }

        @Override
        public CompoundTag serializeNBT(HolderLookup.Provider registries) {
            CompoundTag nbt = super.serializeNBT(registries);
            nbt.putIntArray("RealCounts", this.counts);
            return nbt;
        }

        @Override
        public void deserializeNBT(HolderLookup.Provider registries, CompoundTag nbt) {
            super.deserializeNBT(registries, nbt);
            if (nbt.contains("RealCounts", CompoundTag.TAG_INT_ARRAY)) {
                int[] loadedCounts = nbt.getIntArray("RealCounts");
                if (loadedCounts.length == this.counts.length) {
                    System.arraycopy(loadedCounts, 0, this.counts, 0, loadedCounts.length);
                }
            }
        }
    };

    public final IItemHandler automationHandler = new CombinedInvWrapper(
            new RangedWrapper(inventoryHandler, 0, 12) {
                @Override
                public ItemStack extractItem(int slot, int amount, boolean simulate) {
                    if(slot < 4) {
                        return super.extractItem(slot, amount, simulate);
                    }
                    return ItemStack.EMPTY;
                }
            },
            new RangedWrapper(outputHandler, 0, 8) {
                @Override
                public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
                    return stack;
                }
            }
    );

    public final EnergyStorage energyHandler = new EnergyStorage(5000000) {
        @Override
        public int receiveEnergy(int toReceive, boolean simulate) {
            int received = super.receiveEnergy(toReceive, simulate);
            if (received > 0 && !simulate) setChanged();
            return received;
        }

        @Override
        public int extractEnergy(int toExtract, boolean simulate) {
            int extracted = super.extractEnergy(toExtract, simulate);
            if (extracted > 0 && !simulate) setChanged();
            return extracted;
        }
    };

    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> OreCoalescerBlockEntity.this.process;
                case 1 -> OreCoalescerBlockEntity.this.maxProcess;
                case 2 -> OreCoalescerBlockEntity.this.energyHandler.getEnergyStored();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> OreCoalescerBlockEntity.this.process = value;
                case 1 -> OreCoalescerBlockEntity.this.maxProcess = value;
                case 2 -> OreCoalescerBlockEntity.this.energyHandler.receiveEnergy(value, false);
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    };

    //Variables
    private int process = 0;
    private int maxProcess = 512;
    private boolean isProcessing = false;

    // Cache
    private int speedMultiplier = 1;
    private int processingMultiplier = 1;
    private int fortuneMultiplier = 1;
    private boolean autoSmelt = false;
    
    // Result Cache
    private final Map<Item, List<ItemStack>> outputCache = new HashMap<>();

    //Extra
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.minformax.ore_coalescer");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new OreCoalescerMenu(containerId, playerInventory, this, this.data);
    }

    @Override
    protected List<IItemHandler> getDroppableHandlers() {
        return List.of(inventoryHandler, outputHandler);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        tag.put("inventory", inventoryHandler.serializeNBT(registries));
        tag.put("output", outputHandler.serializeNBT(registries));
        tag.putInt("ore_coalescer.process", this.process);
        tag.putInt("ore_coalescer.max_process", this.maxProcess);
        tag.putInt("ore_coalescer.energy", this.energyHandler.getEnergyStored());
        int[] toSave = new int[6];
        for (int i = 0; i < 6; i++) {
            toSave[i] = enabledSides[i] ? 1 : 0;
        }
        tag.putIntArray("ore_coalescer.enabled_sides", toSave);
        super.saveAdditional(tag, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventoryHandler.deserializeNBT(registries, tag.getCompound("inventory"));
        outputHandler.deserializeNBT(registries, tag.getCompound("output"));
        this.process = tag.getInt("ore_coalescer.process");
        this.maxProcess = tag.getInt("ore_coalescer.max_process");
        this.energyHandler.receiveEnergy(tag.getInt("ore_coalescer.energy"), false);
        int[] savedSides = tag.getIntArray("ore_coalescer.enabled_sides");
        for (int i = 0; i < Math.min(savedSides.length, 6); i++) {
            enabledSides[i] = savedSides[i] == 1;
        }
        this.outputCache.clear();
    }

    //Utility
    public int getTotalItemCount() {
        int count = 0;
        for (int i = 0; i < inventoryHandler.getSlots(); i++) {
            count += inventoryHandler.getStackInSlot(i).getCount();
        }
        for (int i = 0; i < outputHandler.getSlots(); i++) {
            count += outputHandler.getStackInSlot(i).getCount();
        }
        return count;
    }

    public void toggleSide(Direction dir) {
        enabledSides[dir.get3DDataValue()] = !enabledSides[dir.get3DDataValue()];
        this.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    public boolean isSideEnabled(Direction dir) {
        return enabledSides[dir.get3DDataValue()];
    }

    private ItemStack forceInsertItemStacked(ItemStackHandler handler, ItemStack stack) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack toInsert = stack.copy();

        // First pass: merge with existing stacks
        for (int i = 0; i < handler.getSlots(); i++) {
            if (toInsert.isEmpty()) {
                break;
            }

            ItemStack existing = handler.getStackInSlot(i);
            if (!existing.isEmpty() && ItemStack.isSameItemSameComponents(existing, toInsert)) {
                int space = handler.getSlotLimit(i) - existing.getCount();
                if (space > 0) {
                    int transfer = Math.min(toInsert.getCount(), space);

                    ItemStack newExisting = existing.copy();
                    newExisting.grow(transfer);
                    handler.setStackInSlot(i, newExisting);

                    toInsert.shrink(transfer);
                }
            }
        }

        // Second pass: fill empty slots
        if (!toInsert.isEmpty()) {
            for (int i = 0; i < handler.getSlots(); i++) {
                if (toInsert.isEmpty()) {
                    break;
                }

                if (handler.getStackInSlot(i).isEmpty()) {
                    int transfer = Math.min(toInsert.getCount(), handler.getSlotLimit(i));

                    ItemStack newStack = toInsert.copy();
                    newStack.setCount(transfer);
                    handler.setStackInSlot(i, newStack);

                    toInsert.shrink(transfer);
                }
            }
        }

        return toInsert;
    }

    private void autoExportItems() {
        if (level == null || level.isClientSide()) {
            return;
        }

        // Iterate through all slots in the output handler
        for (int i = 0; i < outputHandler.getSlots(); i++) {
            ItemStack stackInSlot = outputHandler.getStackInSlot(i);

            if (!stackInSlot.isEmpty()) {
                ItemStack remainder = Utils.moveItem(level, getBlockPos(), stackInSlot.copy(), enabledSides);

                if (remainder.getCount() != stackInSlot.getCount()) {
                    outputHandler.setStackInSlot(i, remainder);
                    setChanged();
                }
            }
        }
    }

    private void updateUpgrades() {
        int speed = 1;
        int processing = 1;
        int fortune = 1;
        boolean smelt = false;

        for (int i = 0; i < 4; i++) {
            ItemStack stack = inventoryHandler.getStackInSlot(i);
            if (stack.isEmpty()) continue;
            var item = stack.getItem();
            if (item instanceof SpeedUpgrade speedUpgrade) {
                speed = speedUpgrade.getModifier();
            } else if (item instanceof ProcessingUpgrade processingUpgrade) {
                processing = processingUpgrade.getMultiplier();
            } else if (item instanceof FortuneUpgrade fortuneUpgrade) {
                fortune = fortuneUpgrade.getMultiplier();
            } else if (stack.is(ModItems.AUTO_SMELTING_UPGRADE)) {
                smelt = true;
            }
        }

        if (this.fortuneMultiplier != fortune || this.autoSmelt != smelt) {
            this.outputCache.clear();
        }

        this.speedMultiplier = speed;
        this.processingMultiplier = processing;
        this.fortuneMultiplier = fortune;
        this.autoSmelt = smelt;
    }

    private int calculateMaxInsertion(List<ItemStack> outputs, int maxToProcess) {
        if (maxToProcess <= 0) return 0;
        
        int low = 1;
        int high = maxToProcess;
        int validAmount = 0;

        while (low <= high) {
            int mid = low + (high - low) / 2;
            if (canInsertAllExact(outputs, mid)) {
                validAmount = mid;
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        return validAmount;
    }

    private boolean canInsertAllExact(List<ItemStack> outputs, int multiplier) {
        ItemStack[] tempSlots = new ItemStack[outputHandler.getSlots()];
        for (int i = 0; i < tempSlots.length; i++) {
            ItemStack original = outputHandler.getStackInSlot(i);
            tempSlots[i] = original.isEmpty() ? ItemStack.EMPTY : original.copy();
        }

        for (ItemStack output : outputs) {
            ItemStack remaining = output.copy();
            remaining.setCount(output.getCount() * multiplier);

            // 1. Merge
            for (int i = 0; i < tempSlots.length; i++) {
                if (remaining.isEmpty()) break;
                if (!tempSlots[i].isEmpty() && ItemStack.isSameItemSameComponents(tempSlots[i], remaining)) {
                    int limit = outputHandler.getSlotLimit(i);
                    int space = limit - tempSlots[i].getCount();
                    if (space > 0) {
                        int toAdd = Math.min(remaining.getCount(), space);
                        tempSlots[i].grow(toAdd);
                        remaining.shrink(toAdd);
                    }
                }
            }
            // 2. Fill empty
            if (!remaining.isEmpty()) {
                for (int i = 0; i < tempSlots.length; i++) {
                    if (remaining.isEmpty()) break;
                    if (tempSlots[i].isEmpty()) {
                        int limit = outputHandler.getSlotLimit(i);
                        int toAdd = Math.min(remaining.getCount(), limit);
                        tempSlots[i] = remaining.copyWithCount(toAdd);
                        remaining.shrink(toAdd);
                    }
                }
            }

            if (!remaining.isEmpty()) return false;
        }
        return true;
    }

    private boolean hasInput() {
        for (int i = 4; i < 12; i++) {
            if (!inventoryHandler.getStackInSlot(i).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private boolean isOutputFull() {
        for (int i = 0; i < outputHandler.getSlots(); i++) {
            ItemStack stack = outputHandler.getStackInSlot(i);
            if (stack.isEmpty() || stack.getCount() < outputHandler.getSlotLimit(i)) {
                return false;
            }
        }
        return true;
    }
    
    private List<ItemStack> getProcessedOutput(Item inputItem) {
        if (outputCache.containsKey(inputItem)) {
            return outputCache.get(inputItem);
        }

        ArrayList<ItemStack> baseOutputs = new ArrayList<>();
        Block block = Block.byItem(inputItem);
        
        if (this.level instanceof ServerLevel serverLevel) {
            List<ItemStack> drops = block.defaultBlockState().getDrops(new LootParams.Builder(serverLevel)
                    .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(this.worldPosition))
                    .withParameter(LootContextParams.TOOL, ItemStack.EMPTY));

            if (drops.isEmpty()) {
                drops.add(new ItemStack(inputItem));
            }

            for (ItemStack drop : drops) {
                ItemStack baseItem = drop.copy();
                int baseCount = drop.getCount();

                if (autoSmelt) {
                    var recipe = level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(baseItem), level);
                    if (recipe.isPresent()) {
                        baseItem = recipe.get().value().getResultItem(level.registryAccess()).copy();
                        baseCount = drop.getCount() * baseItem.getCount();
                    }
                }

                baseItem.setCount(baseCount * fortuneMultiplier);
                baseOutputs.add(baseItem);
            }
        }
        
        boolean isSameItem = false;
        for (ItemStack output : baseOutputs) {
            if (output.getItem() == inputItem) {
                isSameItem = true;
                break;
            }
        }
        
        if (isSameItem) {
             baseOutputs.clear();
        }

        outputCache.put(inputItem, baseOutputs);
        return baseOutputs;
    }

    //Main
    public void tick(Level level, BlockPos blockPos, BlockState blockState, OreCoalescerBlockEntity blockEntity) {
        if(level.isClientSide()) return;

        autoExportItems();

        if (!hasInput() || isOutputFull()) {
            if (process > 0) {
                process = 0;
                setChanged(level, blockPos, blockState);
            }
            return;
        }

        process++;
        maxProcess = Math.max((512 / speedMultiplier), 1);

        boolean changed = false;

        if (process >= maxProcess) {
            process = 0;
            int remainingProcessing = processingMultiplier;

            isProcessing = true;
            try {
                for (int i = 4; i < 12; i++) {
                    if (remainingProcessing <= 0) break;

                    ItemStack inputStack = inventoryHandler.getStackInSlot(i);
                    if (inputStack.isEmpty()) continue;
                    
                    List<ItemStack> baseOutputs = getProcessedOutput(inputStack.getItem());
                    if (baseOutputs.isEmpty()) continue;

                    int amountToProcess = Math.min(inputStack.getCount(), remainingProcessing);

                    int maxAffordable;
                    if (speedMultiplier == 9999) {
                        maxAffordable = amountToProcess;
                    } else {
                        maxAffordable = energyHandler.getEnergyStored() / 1024;
                    }
                    amountToProcess = Math.min(amountToProcess, maxAffordable);

                    if (amountToProcess <= 0) continue;

                    int validAmount = calculateMaxInsertion(baseOutputs, amountToProcess);

                    if (validAmount > 0) {
                        int energyRequired = validAmount * 1024;
                        if(speedMultiplier == 9999) energyRequired = 0;
                        energyHandler.extractEnergy(energyRequired, false);

                        for (ItemStack baseOutput : baseOutputs) {
                            ItemStack toInsert = baseOutput.copy();
                            toInsert.setCount(baseOutput.getCount() * validAmount);
                            forceInsertItemStacked(outputHandler, toInsert);
                        }

                        inventoryHandler.extractItem(i, validAmount, false);
                        remainingProcessing -= validAmount;
                        changed = true;
                    }
                }
            } finally {
                isProcessing = false;
            }
        }

        if (changed) {
            setChanged(level, blockPos, blockState);
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        } else {
             if (process > 0) setChanged(level, blockPos, blockState);
        }
    }
}
