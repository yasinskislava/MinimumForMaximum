package rewqazwas.minformax.custom.items.upgrades;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;
import java.util.Random;

public class FortuneUpgrade extends UpgradeItem {
    private final int level;

    public FortuneUpgrade(int level) {
        this.level = level;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.minformax.fortune_upgrade", level).withColor(5635925));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    public int getMultiplier() {
        var coef = (float) level / 100;
        Random random = new Random();
        return (int) (Math.min(Math.min((random.nextFloat() + coef), 1) * 10, ((level / 24) + 1) * 2 + 2));
    }

    public int getLevel() {
        return level;
    }
}
