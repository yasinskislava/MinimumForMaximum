package rewqazwas.minformax.custom.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.List;

public abstract class MachineBaseEntity extends BlockEntity {
    public MachineBaseEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }
    //Utility

    public ItemStackHandler getUpgradeHandler() {
        return switch (this) {
            case EternalGeneratorBlockEntity be -> be.upgradeHandler;
            case BlockReplicatorBlockEntity be -> be.upgradeHandler;
            case FluidReplicatorBlockEntity be -> be.upgradeHandler;
            case OreCoalescerBlockEntity be -> be.inventoryHandler;
            case FarmerBlockEntity be -> be.upgradeHandler;
            default -> null;
        };
    }

    public void drops() {
        if (this.level == null) return;
        List<IItemHandler> handlers = getDroppableHandlers();
        int totalSlots = handlers.stream().mapToInt(IItemHandler::getSlots).sum();
        if (totalSlots == 0) return;

        SimpleContainer inv = new SimpleContainer(totalSlots);
        int slot = 0;
        for (IItemHandler handler : handlers) {
            for (int i = 0; i < handler.getSlots(); i++) {
                inv.setItem(slot++, handler.getStackInSlot(i));
            }
        }
        Containers.dropContents(this.level, this.worldPosition, inv);
    }

    protected boolean[] enabledSides = new boolean[6];
    public boolean[] getEnabledSides() {
        return this.enabledSides;
    }

    public boolean isSideEnabled(Direction dir) {
        return this.enabledSides[dir.get3DDataValue()];
    }

    public void setEnabledSides(boolean[] sides) {
        if (sides.length == 6) {
            System.arraycopy(sides, 0, this.enabledSides, 0, 6);
            this.setChanged();
            if (this.level != null) {
                this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
            }
        }
    }

    protected abstract List<IItemHandler> getDroppableHandlers();
}
