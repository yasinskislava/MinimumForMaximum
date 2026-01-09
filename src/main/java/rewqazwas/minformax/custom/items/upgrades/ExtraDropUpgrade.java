package rewqazwas.minformax.custom.items.upgrades;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class ExtraDropUpgrade extends UpgradeItem{
    private int percentage;

    public ExtraDropUpgrade(int percentage) {
        this.percentage = percentage;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("§a" + percentage + "% chance for extra drop"));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    public int getPercentage() {
        return percentage;
    }
}
