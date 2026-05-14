package rewqazwas.minformax.custom.blocks.PandoraBox;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import rewqazwas.minformax.MinForMax;
import rewqazwas.minformax.custom.blocks.ModBlocks;
import rewqazwas.minformax.custom.utility.Utils;

import java.util.List;

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

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip." + MinForMax.MOD_ID + ".pandora_box").withStyle(ChatFormatting.GOLD));

        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            CompoundTag tag = customData.copyTag();

            if (tag.contains("overload")) {
                long overload = tag.getLong("overload");
                tooltipComponents.add(Component.literal("Overload: ")
                        .withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(Utils.formatNumber(overload)).withStyle(ChatFormatting.RED)));
            }

            if (tag.contains("totalXp")) {
                long xp = tag.getLong("totalXp");
                tooltipComponents.add(Component.literal("Experience: ")
                        .withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(Utils.formatNumber(xp)).withStyle(ChatFormatting.GREEN)));
            }
        }

        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
