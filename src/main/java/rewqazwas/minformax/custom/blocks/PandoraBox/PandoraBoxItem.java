package rewqazwas.minformax.custom.blocks.PandoraBox;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import rewqazwas.minformax.custom.blocks.ModBlocks;

public class PandoraBoxItem extends BlockItem {

    public PandoraBoxItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        BlockPos centerPos = context.getClickedPos().above();
        Level level = context.getLevel();
        BlockState coreState = level.getBlockState(centerPos);
        if (!coreState.canBeReplaced()) {
            return InteractionResult.FAIL;
        }

        // Check if the area is clear for the structure
        for (BlockPos offset : PandoraBoxCore.STRUCTURE_OFFSETS) {
            BlockPos targetPos = centerPos.offset(offset);
            BlockState state = level.getBlockState(targetPos);
            if (!state.canBeReplaced()) {
                return InteractionResult.FAIL;
            }
        }

        BlockPlaceContext newContext = BlockPlaceContext.at(
                context,
                centerPos,
                context.getClickedFace()
        );
        return super.place(newContext);
    }
}
