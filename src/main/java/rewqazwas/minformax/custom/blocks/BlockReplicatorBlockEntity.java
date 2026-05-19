package rewqazwas.minformax.custom.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;
import rewqazwas.minformax.custom.ModBlockEntities;
import rewqazwas.minformax.custom.index.BlockReplicatorData;
import rewqazwas.minformax.custom.index.ModDataReloadListener;
import rewqazwas.minformax.custom.items.upgrades.ProcessingUpgrade;
import rewqazwas.minformax.custom.items.upgrades.SpeedUpgrade;
import rewqazwas.minformax.custom.utility.Utils;

import java.util.List;
import java.util.Map;

public class BlockReplicatorBlockEntity extends MachineBaseEntity {
    public BlockReplicatorBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.BLOCK_REPLICATOR_BE.get(), pos, blockState);

        this.enabledSides[Direction.DOWN.get3DDataValue()] = true;
        this.enabledSides[Direction.UP.get3DDataValue()] = true;
        this.enabledSides[Direction.NORTH.get3DDataValue()] = true;
        this.enabledSides[Direction.SOUTH.get3DDataValue()] = true;
        this.enabledSides[Direction.EAST.get3DDataValue()] = true;
        this.enabledSides[Direction.WEST.get3DDataValue()] = true;

    }
    //Handlers

    public final Utils.UpgradeItemHandler upgradeHandler = new Utils.UpgradeItemHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            cacheDirty = true;
            setChanged();
        }
    };

    public final Utils.SingleItemHandler itemHandler = new Utils.SingleItemHandler(1) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (!(stack.getItem() instanceof BlockItem)) return false;
            return findData(stack) != null;
        }

        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            cacheDirty = true;
            setChanged();
            if (level != null) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    };

    public final EnergyStorage energyHandler = new EnergyStorage(40_960_000);

    //Variables
    private int process = 0;
    private int maxProcess = 256;
    
    // Cache
    private boolean cacheDirty = true;
    private BlockReplicatorData cachedData;
    private int cachedSpeedModifier = 1;
    private int cachedStackMultiplier = 1;

    //Extra
    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    protected List<IItemHandler> getDroppableHandlers() {
        return List.of(itemHandler, upgradeHandler);
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
        this.cacheDirty = true;
    }

    //Utility
    private BlockReplicatorData findData(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof BlockItem blockItem)) return null;

        ResourceLocation keyRL = BuiltInRegistries.BLOCK.getKey(blockItem.getBlock());
        String key = keyRL.toString();
        
        // Check for direct block match
        if (ModDataReloadListener.BLOCK_REPLICATOR_DATA.containsKey(key)) {
            return ModDataReloadListener.BLOCK_REPLICATOR_DATA.get(key);
        }

        // Check for tag match
        for (Map.Entry<String, BlockReplicatorData> entry : ModDataReloadListener.BLOCK_REPLICATOR_DATA.entrySet()) {
            String entryKey = entry.getKey();
            if (entryKey.startsWith("#")) {
                try {
                    var tagKey = TagKey.create(BuiltInRegistries.BLOCK.key(), ResourceLocation.parse(entryKey.substring(1)));
                    if (blockItem.getBlock().defaultBlockState().is(tagKey)) {
                        return entry.getValue();
                    }
                } catch (Exception ignored) {}
            }
        }
        return null;
    }

    private void recalculateCache() {
        var sourceStack = itemHandler.getStackInSlot(0);
        this.cachedData = findData(sourceStack);
        
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
        this.cachedSpeedModifier = speedModifier;
        this.cachedStackMultiplier = stackMultiplier;
        
        this.cacheDirty = false;
    }

    //Main
    public void tick(Level level, BlockPos blockPos, BlockState blockState, BlockReplicatorBlockEntity blockEntity) {
        if(level.isClientSide()) return;
        
        if (this.cacheDirty) {
            recalculateCache();
        }

        if (this.cachedData == null) {
            if (process != 0) {
                process = 0;
                setChanged(level, blockPos, blockState);
            }
            return;
        }

        var sourceStack = itemHandler.getStackInSlot(0);

        if (sourceStack.isEmpty()) {
             process = 0;
             setChanged(level, blockPos, blockState);
             return;
        }

        boolean dirty = false;
        
        int speedModifier = this.cachedSpeedModifier;
        int stackMultiplier = this.cachedStackMultiplier;
        int effectiveSpeed = Math.min(speedModifier, this.cachedData.duration());
        int energyCost = this.cachedData.energyMultiplier() * effectiveSpeed * stackMultiplier;
        
        if (speedModifier == 9999) {
            energyCost = 0;
        }
        
        int currentEnergy = energyHandler.getEnergyStored();
        
        if (currentEnergy < energyCost) return;
        
        // Check output space before extracting energy
        if (!Utils.canInsertAtLeastOneNetwork(level, blockPos, sourceStack, this.enabledSides)) return;
        
        energyHandler.extractEnergy(energyCost, false);
        if (energyHandler.getEnergyStored() != currentEnergy) {
            dirty = true;
        }

        process++;
        maxProcess = this.cachedData.duration();
        dirty = true; // Process changed

        // Adjust logic: original code did process >= maxProcess / speedModifier
        if(process >= Math.max(1, maxProcess / speedModifier)) {
            var toFill = new ItemStack(sourceStack.getItem());
            toFill.setCount(stackMultiplier);
            
            Utils.moveItem(level, blockPos, toFill, this.enabledSides);
            process = 0;
        }
        
        if (dirty) {
            setChanged(level, blockPos, blockState);
        }
    }
}
