package rewqazwas.minformax.custom.items.upgrades;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class StackUpgrade extends UpgradeItem{
    private int multiplier;

    public StackUpgrade(int multiplier) {
        this.multiplier = multiplier;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("§a" + multiplier + "x output"));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    public int getMultiplier() {
        return multiplier;
    }
}
