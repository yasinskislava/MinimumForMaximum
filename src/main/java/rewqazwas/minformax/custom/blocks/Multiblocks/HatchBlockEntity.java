package rewqazwas.minformax.custom.blocks.Multiblocks;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import rewqazwas.minformax.custom.ModAttachmentTypes;
import rewqazwas.minformax.custom.ModBlockEntities;
import rewqazwas.minformax.custom.blocks.AbstractMultiblockPartBlockEntity;

public class HatchBlockEntity extends AbstractMultiblockPartBlockEntity {
    public HatchBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GATE_OF_BABYLON_HATCH_BE.get(), pos, state);
    }

    @Override
    protected void serverTick(Level level, BlockPos pos, BlockState state) { }

    /**
     * Resolves the master core BlockEntity dynamically using the chunk multiblock proxy data.
     */
    @Nullable
    public GateOfBabylonBlockEntity getMasterBlockEntity() {
        if (this.level == null) return null;
        var chunk = this.level.getChunkAt(this.worldPosition);
        if (chunk.hasData(ModAttachmentTypes.MULTIBLOCK_PROXY)) {
            BlockPos masterPosition = chunk.getData(ModAttachmentTypes.MULTIBLOCK_PROXY).get(this.worldPosition);
            if (masterPosition != null && this.level.getBlockEntity(masterPosition) instanceof GateOfBabylonBlockEntity master) {
                return master;
            }
        }
        return null;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        GateOfBabylonBlockEntity master = getMasterBlockEntity();
        return master != null ? master.createMenu(containerId, inventory, player) : null;
    }

    @Override
    public Component getDisplayName() {
        GateOfBabylonBlockEntity master = getMasterBlockEntity();
        return master != null ? master.getDisplayName() : Component.literal("");
    }
}