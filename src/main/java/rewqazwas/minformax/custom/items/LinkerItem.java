package rewqazwas.minformax.custom.items;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import rewqazwas.minformax.custom.blocks.EternalGeneratorBlockEntity;
import rewqazwas.minformax.custom.component.ModDataComponents;
import rewqazwas.minformax.custom.utility.Utils;

import java.util.ArrayList;
import java.util.List;

public class LinkerItem extends Item {
    public LinkerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        BlockEntity be = level.getBlockEntity(pos);

        if (be instanceof EternalGeneratorBlockEntity) {
            List<BlockPos> storedPos = new ArrayList<>(stack.getOrDefault(ModDataComponents.LINKED_POS, List.of()));

            if (storedPos.contains(pos)) {
                if(player.isCrouching()){
                    List<BlockPos> connectedGens = Utils.findConnectedGenerators(level, pos);
                    int removedCount = 0;

                    for (BlockPos genPos : connectedGens) {
                        if (storedPos.contains(genPos)) {
                            storedPos.remove(genPos);
                            removedCount++;
                        }
                    }

                    if (removedCount > 0) {
                        stack.set(ModDataComponents.LINKED_POS, storedPos);
                        player.displayClientMessage(Component.translatable("message.minformax.linker.mass", "un", removedCount).withStyle(ChatFormatting.GREEN), true);
                    } else {
                        player.displayClientMessage(Component.translatable("message.minformax.linker.already", "un").withStyle(ChatFormatting.YELLOW), true);
                    }

                } else {
                    storedPos.remove(pos);
                    stack.set(ModDataComponents.LINKED_POS, storedPos);
                    player.displayClientMessage(Component.translatable("message.minformax.linker.cleared", pos.toShortString()), true);
                }
            } else {
                if(player.isCrouching()){
                    List<BlockPos> connectedGens = Utils.findConnectedGenerators(level, pos);
                    int addedCount = 0;

                    for (BlockPos genPos : connectedGens) {
                        if (!storedPos.contains(genPos)) {
                            storedPos.add(genPos);
                            addedCount++;
                        }
                    }

                    if (addedCount > 0) {
                        stack.set(ModDataComponents.LINKED_POS, storedPos);
                        player.displayClientMessage(Component.translatable("message.minformax.linker.mass", "", addedCount).withStyle(ChatFormatting.GREEN), true);
                    } else {
                        player.displayClientMessage(Component.translatable("message.minformax.linker.already", "").withStyle(ChatFormatting.YELLOW), true);
                    }
                } else {
                    storedPos.add(pos);
                    stack.set(ModDataComponents.LINKED_POS, storedPos);
                    player.displayClientMessage(Component.translatable("message.minformax.linker.linked", pos.toShortString()), true);
                }
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.minformax.linker"));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
