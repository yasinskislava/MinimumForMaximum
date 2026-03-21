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

public class BlockReplicatorBlockEntity extends MachineBaseEntity {
    public BlockReplicatorBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.BLOCK_REPLICATOR_BE.get(), pos, blockState);
    }
    //Handlers

    public final Utils.UpgradeItemHandler upgradeHandler = new Utils.UpgradeItemHandler(2);

    public final Utils.SingleItemHandler itemHandler = new Utils.SingleItemHandler(1) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (!(stack.getItem() instanceof BlockItem)) return false;
            return getData(stack) != null;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    };

    public final EnergyStorage energyHandler = new EnergyStorage(40_960_000);

    //Variables
    private int process = 0;
    private int maxProcess = 256;

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

    //Utility
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

    //Main
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
