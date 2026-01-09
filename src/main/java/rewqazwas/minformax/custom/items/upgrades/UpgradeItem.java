package rewqazwas.minformax.custom.items.upgrades;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import rewqazwas.minformax.custom.blocks.EternalGeneratorBlockEntity;

public class UpgradeItem extends Item {
    public UpgradeItem() {
        super(new Item.Properties().stacksTo(16));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var be = context.getLevel().getBlockEntity(context.getClickedPos());
        if(context.getPlayer().isCrouching() && be instanceof EternalGeneratorBlockEntity){
            var cap = context.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, context.getClickedPos(), context.getClickedFace());

            var pass = false;
            for (int i = 0; i < cap.getSlots(); i++) {
                var stackInSlot = cap.getStackInSlot(i);
                pass = stackInSlot.getItem().getClass() == context.getItemInHand().getItem().getClass() || pass;
            }

            if(!pass) {
                var remainder = ItemHandlerHelper.insertItemStacked(cap, context.getItemInHand().copy(), false);
                if (remainder.getCount() != context.getItemInHand().getCount()) {
                    context.getItemInHand().setCount(remainder.getCount());
                }
            }
        }
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide());
    }

}
