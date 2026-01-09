package rewqazwas.minformax.custom.items;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.neoforged.fml.common.Mod;
import rewqazwas.minformax.custom.blocks.EternalGeneratorBlockEntity;
import rewqazwas.minformax.custom.index.MasterIndex;

import java.util.List;

public class QuantumIngotItem extends Item {
    public QuantumIngotItem() {
        super(new Properties());
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if(!context.getLevel().isClientSide()) {
            MasterIndex masterIndex = MasterIndex.get(context.getLevel().getServer().overworld());
            System.out.println(masterIndex.getIndex());
        }
        if(context.getPlayer().isCrouching()){
            var target = context.getLevel().getBlockEntity(context.getClickedPos());
            if(target instanceof EternalGeneratorBlockEntity generator && generator.canConsumeOverload()) {
                generator.consumeOverload();
                var itemStack = context.getItemInHand();
                itemStack.shrink(1);
                var inv = context.getPlayer().getInventory();
                var QUANTUM_FOAM = ModItems.QUANTUM_FOAM.get().getDefaultInstance();
                if(!inv.add(QUANTUM_FOAM)){
                    context.getPlayer().drop(QUANTUM_FOAM, false);
                }
            }
        }

        return super.useOn(context);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.minformax.quantum_ingot"));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
