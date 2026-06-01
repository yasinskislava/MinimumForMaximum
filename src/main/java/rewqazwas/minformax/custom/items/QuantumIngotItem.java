package rewqazwas.minformax.custom.items;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.neoforged.neoforge.common.util.FakePlayer;
import rewqazwas.minformax.custom.blocks.EternalGeneratorBlockEntity;

import java.util.List;

public class QuantumIngotItem extends Item {
    public QuantumIngotItem() {
        super(new Properties());
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if(context.getPlayer().isCrouching()){
            var target = context.getLevel().getBlockEntity(context.getClickedPos());
            if(target instanceof EternalGeneratorBlockEntity generator && generator.canConsumeOverload()) {
                generator.consumeOverload(5120);
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


}
