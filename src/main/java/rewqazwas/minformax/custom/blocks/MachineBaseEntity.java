package rewqazwas.minformax.custom.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

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
}
