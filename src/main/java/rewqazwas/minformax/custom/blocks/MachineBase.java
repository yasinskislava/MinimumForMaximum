package rewqazwas.minformax.custom.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public class MachineBase extends BaseEntityBlock {
    protected MachineBase(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return null;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return null;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide() && player.isCrouching() && stack.isEmpty()) {
            if (level.getBlockEntity(pos) instanceof MachineBaseEntity mbe) {
                ItemStackHandler upgradeHandler = mbe.getUpgradeHandler();
                if (upgradeHandler != null) {
                    for (int i = upgradeHandler.getSlots() - 1; i >= 0; i--) {
                        if (!upgradeHandler.getStackInSlot(i).isEmpty()) {
                            ItemHandlerHelper.giveItemToPlayer(player, upgradeHandler.extractItem(i, 1, false));
                            return ItemInteractionResult.SUCCESS;
                        }
                    }
                }
            }
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }
}
