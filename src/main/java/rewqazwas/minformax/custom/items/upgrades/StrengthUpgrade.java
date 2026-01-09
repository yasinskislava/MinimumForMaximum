package rewqazwas.minformax.custom.items.upgrades;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class StrengthUpgrade extends UpgradeItem{
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("§aAllows to farm bosses."));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
