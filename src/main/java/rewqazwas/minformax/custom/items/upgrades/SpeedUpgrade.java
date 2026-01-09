package rewqazwas.minformax.custom.items.upgrades;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class SpeedUpgrade extends UpgradeItem{
    private int modifier;
    public SpeedUpgrade(int modifier) {
        this.modifier = modifier;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("§a" + modifier + "x"));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    public int getModifier() {
        return modifier;
    }
}
