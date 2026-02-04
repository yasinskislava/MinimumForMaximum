package rewqazwas.minformax.custom.items;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;


public class QuantumFoamItem extends Item {
    public QuantumFoamItem() {
        super(new Item.Properties());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.minformax.quantum_foam"));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
