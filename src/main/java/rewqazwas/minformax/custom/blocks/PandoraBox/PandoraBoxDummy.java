package rewqazwas.minformax.custom.blocks.PandoraBox;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import rewqazwas.minformax.custom.blocks.ModBlocks;

public class PandoraBoxDummy extends Block implements EntityBlock {

    public PandoraBoxDummy(Properties properties) {
        super(properties);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof PandoraBoxDummyBlockEntity dummyBe) {
            BlockPos corePos = dummyBe.getCorePos();
            if (corePos != null) {
                return PandoraBoxCore.getShapeForOffset(pos.subtract(corePos));
            }
        }
        return Shapes.block();
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof PandoraBoxDummyBlockEntity dummyBe) {
            BlockPos corePos = dummyBe.getCorePos();
            if (corePos != null) {
                BlockState coreState = level.getBlockState(corePos);
                // Correctly delegate using the core's state and position
                return coreState.useItemOn(stack, level, player, hand, hitResult.withPosition(corePos));
            }
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof PandoraBoxDummyBlockEntity dummyBe) {
                BlockPos corePos = dummyBe.getCorePos();
                if (corePos != null && level.getBlockState(corePos).getBlock() instanceof PandoraBoxCore) {
                    level.destroyBlock(corePos, true, player);
                }
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, net.minecraft.world.entity.player.Player player) {
        return new ItemStack(ModBlocks.PANDORA_BOX_CORE.get());
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PandoraBoxDummyBlockEntity(pos, state);
    }


}
