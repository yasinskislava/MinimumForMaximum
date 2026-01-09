package rewqazwas.minformax.custom.items;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.ArrayList;
import java.util.List;

public class BossModuleItem extends ModuleItem {
    public BossModuleItem() {
        super();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.minformax.boss_module_item"));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
