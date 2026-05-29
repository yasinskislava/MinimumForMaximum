package rewqazwas.minformax.custom.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class AbstractMultiblockPartBlockEntity extends BlockEntity implements MenuProvider {
    protected UUID multiblockId = null;
    protected BlockPos masterPos = null;
    protected boolean isMaster = false;
    protected final List<BlockPos> slaveBlocks = new ArrayList<>();
    private boolean isUnforming = false;

    public AbstractMultiblockPartBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static <T extends AbstractMultiblockPartBlockEntity> void tick(Level level, BlockPos pos, BlockState state, T blockEntity) {
        if (blockEntity.isMaster) {
            blockEntity.serverTick(level, pos, state);
        }
    }

    protected abstract void serverTick(Level level, BlockPos pos, BlockState state);

    public boolean isFormed() {
        return masterPos != null || isMaster;
    }

    public boolean isMaster() {
        return isMaster;
    }

    @Nullable
    public BlockPos getMasterPos() {
        return isMaster ? worldPosition : masterPos;
    }

    public void setSlaveOf(UUID multiblockId, BlockPos masterPos) {
        this.multiblockId = multiblockId;
        this.masterPos = masterPos;
        this.isMaster = false;
        this.slaveBlocks.clear();
        setChanged();
        sync();
    }

    public void convertToMaster(UUID multiblockId, List<BlockPos> slaves) {
        this.multiblockId = multiblockId;
        this.masterPos = null;
        this.isMaster = true;
        this.slaveBlocks.clear();
        this.slaveBlocks.addAll(slaves);
        setChanged();
        sync();
    }

    public void unformStructure(Level level) {
        if (isUnforming) return;
        isUnforming = true;

        if (isMaster) {
            for (BlockPos pos : slaveBlocks) {
                if (level.getBlockEntity(pos) instanceof AbstractMultiblockPartBlockEntity part) {
                    part.resetMultiblockState();
                }
            }
        } else if (masterPos != null) {
            if (level.getBlockEntity(masterPos) instanceof AbstractMultiblockPartBlockEntity masterBE) {
                masterBE.unformStructure(level);
            }
        }
        resetMultiblockState();
    }

    // When we break a structure, we swap the block entities back into generic unformed instances
    protected void resetMultiblockState() {
        if (level != null && !level.isClientSide) {
            BlockState state = getBlockState();
            BlockPos pos = getBlockPos();

            // Wipe the block entity entirely and make it a clean, unformed base entity
            level.removeBlockEntity(pos);
            MultiblockPartBlockEntity freshBase = new MultiblockPartBlockEntity(pos, state);
            level.setBlockEntity(freshBase);
            freshBase.setChanged();
            freshBase.sync();
        }
    }

    protected void sync() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public UUID getMultiblockId() {
        return this.multiblockId;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (multiblockId != null) tag.putUUID("multiblock_id", multiblockId);
        if (masterPos != null) tag.putLong("master_pos", masterPos.asLong());
        tag.putBoolean("is_master", isMaster);

        if (isMaster && !slaveBlocks.isEmpty()) {
            long[] positions = new long[slaveBlocks.size()];
            for (int i = 0; i < slaveBlocks.size(); i++) {
                positions[i] = slaveBlocks.get(i).asLong();
            }
            tag.putLongArray("slave_blocks", positions);
        }
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.multiblockId = tag.hasUUID("multiblock_id") ? tag.getUUID("multiblock_id") : null;
        this.masterPos = tag.contains("master_pos") ? BlockPos.of(tag.getLong("master_pos")) : null;
        this.isMaster = tag.getBoolean("is_master");

        this.slaveBlocks.clear();
        if (tag.contains("slave_blocks")) {
            long[] positions = tag.getLongArray("slave_blocks");
            for (long posLong : positions) {
                this.slaveBlocks.add(BlockPos.of(posLong));
            }
        }
    }
}
