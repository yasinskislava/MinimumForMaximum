package rewqazwas.minformax.custom.items;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.common.util.FakePlayer;
import rewqazwas.minformax.custom.blocks.MachineBaseEntity;
import rewqazwas.minformax.custom.component.ModDataComponents;

import java.util.List;

public class ConfigurationToolItem extends Item {
    public ConfigurationToolItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        var customData = stack.get(DataComponents.CUSTOM_DATA);

        if (customData != null && customData.copyTag().contains("side_config")) {
            int[] savedSides = customData.copyTag().getIntArray("side_config");

            if (savedSides.length == 6) {
                tooltipComponents.add(Component.literal("Copied Configuration:").withStyle(ChatFormatting.GOLD));

                for (int i = 0; i < 6; i++) {
                    Direction dir = Direction.from3DDataValue(i);
                    boolean enabled = savedSides[i] == 1;

                    ChatFormatting color = enabled ? ChatFormatting.GREEN : ChatFormatting.RED;
                    String status = enabled ? "✔ ON" : "❌ OFF";

                    tooltipComponents.add(Component.literal("  " + dir.name().toUpperCase() + ": ")
                            .withStyle(ChatFormatting.GRAY)
                            .append(Component.literal(status).withStyle(color)));
                }
            }
        } else {
            tooltipComponents.add(Component.literal("No configuration stored!").withStyle(ChatFormatting.DARK_GRAY));
        }

        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();

        if (player == null || player instanceof FakePlayer) return InteractionResult.PASS;

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof MachineBaseEntity machine) {
            if (!level.isClientSide) {
                if (player.isShiftKeyDown()) {
                    boolean[] sides = machine.getEnabledSides();
                    int[] data = new int[6];
                    for (int i = 0; i < 6; i++) data[i] = sides[i] ? 1 : 0;

                    CompoundTag tag = new CompoundTag();
                    tag.putIntArray("side_config", data);
                    stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

                    player.displayClientMessage(Component.translatable("message.minformax.config_tool.copied").withStyle(ChatFormatting.GREEN), true);
                } else {
                    var customData = stack.get(DataComponents.CUSTOM_DATA);
                    if (customData != null && customData.copyTag().contains("side_config")) {
                        int[] savedSides = customData.copyTag().getIntArray("side_config");
                        boolean[] newSides = new boolean[6];
                        for (int i = 0; i < 6; i++) newSides[i] = savedSides[i] == 1;

                        machine.setEnabledSides(newSides);
                        player.displayClientMessage(Component.translatable("message.minformax.config_tool.pasted").withStyle(ChatFormatting.GREEN), true);
                    } else {
                        player.displayClientMessage(Component.translatable("message.minformax.config_tool.no_config").withStyle(ChatFormatting.RED), true);
                    }
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return InteractionResult.PASS;
    }
}