package rewqazwas.minformax.custom.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;
import rewqazwas.minformax.custom.ModBlockEntities;
import rewqazwas.minformax.custom.index.BlockReplicatorData;
import rewqazwas.minformax.custom.index.ModDataReloadListener;
import rewqazwas.minformax.custom.items.ModItems;
import rewqazwas.minformax.custom.items.upgrades.SpeedUpgrade;
import rewqazwas.minformax.custom.items.upgrades.ProcessingUpgrade;
import rewqazwas.minformax.custom.utility.Utils;

import java.util.Map;

public class BlockReplicatorBlockEntity extends BlockEntity {
    public final ItemStackHandler upgradeHandler = new ItemStackHandler(2) {
        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stack.getItem() instanceof SpeedUpgrade || (stack.getItem() instanceof ProcessingUpgrade && !(stack.getItem() == ModItems.ULTIMATE_PROCESSING_UPGRADE.get()));
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if(Utils.canInsertUpgrade(this, stack)){
                return stack;
            }
            return super.insertItem(slot, stack, simulate);
        }
    };

    public final ItemStackHandler itemHandler = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if(!level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (!(stack.getItem() instanceof BlockItem)) return false;
            return getData(stack) != null;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        protected int getStackLimit(int slot, ItemStack stack) {
            return 1;
        }
    };

    public final EnergyStorage energyHandler = new EnergyStorage(40_960_000) {
        @Override
        public int receiveEnergy(int toReceive, boolean simulate) {
            setChanged();
            return super.receiveEnergy(toReceive, simulate);
        }

        @Override
        public int extractEnergy(int toExtract, boolean simulate) {
            setChanged();
            return super.extractEnergy(toExtract, simulate);
        }
    };

    private int process = 0;
    private int maxProcess = 256;

    public BlockReplicatorBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.BLOCK_REPLICATOR_BE.get(), pos, blockState);
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    public void drops() {
        SimpleContainer inv = new SimpleContainer(upgradeHandler.getSlots() + itemHandler.getSlots());
        for(int i = 0; i < upgradeHandler.getSlots(); i++){
            inv.setItem(i, upgradeHandler.getStackInSlot(i));
        }
        for(int i = 0; i < itemHandler.getSlots(); i++){
            inv.setItem(i + upgradeHandler.getSlots(), itemHandler.getStackInSlot(i));
        }
        Containers.dropContents(this.level, this.worldPosition, inv);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", itemHandler.serializeNBT(registries));
        tag.put("upgrades", upgradeHandler.serializeNBT(registries));
        tag.putInt("block_replicator.process", this.process);
        tag.putInt("block_replicator.max_process", this.maxProcess);
        tag.putInt("block_replicator.energy", this.energyHandler.getEnergyStored());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        itemHandler.deserializeNBT(registries, tag.getCompound("inventory"));
        upgradeHandler.deserializeNBT(registries, tag.getCompound("upgrades"));
        this.process = tag.getInt("block_replicator.process");
        this.maxProcess = tag.getInt("block_replicator.max_process");
        this.energyHandler.receiveEnergy(tag.getInt("block_replicator.energy"), false);
    }

    private BlockReplicatorData getData(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof BlockItem)) return null;

        // Check for direct block match
        var key = BuiltInRegistries.BLOCK.getKey(((BlockItem) stack.getItem()).getBlock()).toString();
        if (ModDataReloadListener.BLOCK_REPLICATOR_DATA.containsKey(key)) {
            return ModDataReloadListener.BLOCK_REPLICATOR_DATA.get(key);
        }

        // Check for tag match
        for (Map.Entry<String, BlockReplicatorData> entry : ModDataReloadListener.BLOCK_REPLICATOR_DATA.entrySet()) {
            if (entry.getKey().startsWith("#")) {
                var tagKey = TagKey.create(BuiltInRegistries.BLOCK.key(), ResourceLocation.parse(entry.getKey().substring(1)));
                var block = ((BlockItem) stack.getItem()).getBlock();
                var blockState = block.defaultBlockState();
                if (blockState.is(tagKey)) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }

    public void tick(Level level, BlockPos blockPos, BlockState blockState, BlockReplicatorBlockEntity blockEntity) {
        if(level.isClientSide()) return;

        var sourceStack = itemHandler.getStackInSlot(0);
        var data = getData(sourceStack);
        if (data == null) return;

        int speedModifier = 1;
        int stackMultiplier = 1;

        for(int i = 0; i < upgradeHandler.getSlots(); i++) {
            var upgrade = upgradeHandler.getStackInSlot(i).getItem();
            if(upgrade instanceof SpeedUpgrade speedUpgrade) {
                speedModifier = speedUpgrade.getModifier();
            } else if(upgrade instanceof ProcessingUpgrade processingUpgrade) {
                stackMultiplier = processingUpgrade.getMultiplier();
            }
        }

        int effectiveSpeed = Math.min(speedModifier, data.duration());
        int energyCost = data.energyMultiplier() * effectiveSpeed * stackMultiplier;
        if (speedModifier == 9999) {
            energyCost = 0;
        }
        if (energyHandler.getEnergyStored() < energyCost) return;
        if (!Utils.canInsertAtLeastOne(level, blockPos, sourceStack)) return;
        energyHandler.extractEnergy(energyCost, false);

        process++;
        maxProcess = data.duration();

        if(process >= maxProcess / speedModifier) {
            var toFill = new ItemStack(sourceStack.getItem());

            toFill.setCount(stackMultiplier);
            
            Utils.moveItem(level, blockPos, toFill);
            process = 0;
        }
        setChanged(level, blockPos, blockState);
    }
}
