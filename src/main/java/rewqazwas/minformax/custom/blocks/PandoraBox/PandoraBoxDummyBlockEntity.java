package rewqazwas.minformax.custom.blocks.PandoraBox;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import rewqazwas.minformax.custom.ModBlockEntities;

public class PandoraBoxDummyBlockEntity extends BlockEntity {
    public PandoraBoxDummyBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PANDORA_BOX_DUMMY_BE.get(), pos, state);
    }
    //Variables

    private BlockPos corePos;

    //Utility
    public void setCorePos(BlockPos corePos) {
        this.corePos = corePos;
        this.setChanged();
        if(level != null){
            level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
        }
    }

    public BlockPos getCorePos() {
        return corePos;
    }

    //Extra
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (corePos != null) {
            tag.putLong("CorePos", corePos.asLong());
        }
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("CorePos")) {
            setCorePos(BlockPos.of(tag.getLong("CorePos")));
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }
}
