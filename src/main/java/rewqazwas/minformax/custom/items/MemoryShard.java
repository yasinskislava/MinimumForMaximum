package rewqazwas.minformax.custom.items;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import rewqazwas.minformax.custom.component.ModDataComponents;
import rewqazwas.minformax.custom.utility.Utils;

import java.util.List;

public class MemoryShard extends Item {
    public MemoryShard(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        var rawName = stack.get(ModDataComponents.MOB_INDEX);
        Component nameComponent;
        if (rawName == null) {
            nameComponent = Component.translatable("tooltip.minformax.empty");
        } else {
            nameComponent = Component.literal(Utils.prettyName(rawName));
        }

        tooltipComponents.add(Component.literal("§2").append(nameComponent).append("§r"));
        if(rawName != null) {
            tooltipComponents.add(Component.translatable("tooltip.minformax.memory_shard"));
        }
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        var stack = player.getItemInHand(usedHand);
        if(player.isCrouching() && stack.get(ModDataComponents.MOB_INDEX) != null) {
            stack.set(ModDataComponents.MOB_INDEX, null);
        }
        return super.use(level, player, usedHand);
    }
}
