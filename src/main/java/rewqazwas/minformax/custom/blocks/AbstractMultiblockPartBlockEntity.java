package rewqazwas.minformax.custom.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import rewqazwas.minformax.custom.ModAttachmentTypes;
import rewqazwas.minformax.custom.utility.MultiblockSavedData;

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
        if (!level.isClientSide && blockEntity.isMaster) {
            blockEntity.serverTick(level, pos, state);
        }
    }

    protected abstract void serverTick(Level level, BlockPos pos, BlockState state);

    public void convertToMaster(UUID structureId, List<BlockPos> structuralBlocks) {
        this.multiblockId = structureId;
        this.masterPos = this.worldPosition;
        this.isMaster = true;
        this.slaveBlocks.clear();
        this.slaveBlocks.addAll(structuralBlocks);
        setChanged();
    }

    public void unformStructure(Level level) {
        if (this.isUnforming) return;
        this.isUnforming = true;

        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            // Create detached local copies to prevent concurrent modification issues
            List<BlockPos> slavesToClear = new ArrayList<>(this.slaveBlocks);
            UUID idToRemove = this.multiblockId;
            BlockPos masterPosition = this.worldPosition;

            // 1. Purge Proxy Index from Chunks and update blocks
            for (BlockPos p : slavesToClear) {
                var chunk = serverLevel.getChunkAt(p);
                if (chunk.hasData(ModAttachmentTypes.MULTIBLOCK_PROXY)) {
                    chunk.getData(ModAttachmentTypes.MULTIBLOCK_PROXY).remove(p);
                    chunk.setUnsaved(true);
                }
                BlockState slaveState = level.getBlockState(p);
                level.sendBlockUpdated(p, slaveState, slaveState, 3);
            }

            // 2. Remove from global storage ledger
            if (idToRemove != null) {
                MultiblockSavedData.get(serverLevel).remove(idToRemove);
            }

            // 3. Strip the BlockEntity instance entirely from the world.
            level.removeBlockEntity(masterPosition);

            BlockState masterState = level.getBlockState(masterPosition);
            level.sendBlockUpdated(masterPosition, masterState, masterState, 3);
        }
    }

    public void resetMultiblockState() {
        this.multiblockId = null;
        this.masterPos = null;
        this.isMaster = false;
        this.slaveBlocks.clear();
        this.isUnforming = false;
        setChanged();
    }

    public boolean isFormed() {
        return this.multiblockId != null;
    }

    public boolean isMaster() {
        return isMaster;
    }

    public BlockPos getMasterPos() {
        return masterPos != null ? masterPos : this.worldPosition;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (multiblockId != null) tag.putUUID("multiblock_id", multiblockId);
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
        this.isMaster = tag.getBoolean("is_master");
        this.masterPos = isMaster ? this.worldPosition : null;

        this.slaveBlocks.clear();
        if (isMaster && tag.contains("slave_blocks")) {
            long[] positions = tag.getLongArray("slave_blocks");
            for (long p : positions) {
                slaveBlocks.add(BlockPos.of(p));
            }
        }
    }
}