package rewqazwas.minformax.custom.items;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

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

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        Level level = entity.level();

        if (!level.isClientSide) {
            double y = entity.getY();
            int amount = stack.getCount();
            ResourceKey<Level> dimension = level.dimension();

            if (dimension == Level.OVERWORLD && y > 1000) {
                transform(entity, ModItems.AETHER_INGOT.get(), amount);
                return true;
            } else if (dimension == Level.END && y < 0) {
                transform(entity, ModItems.VOID_INGOT.get(), amount);
                return true;
            }
        }
        return false;
    }

    private void transform(ItemEntity oldEntity, Item resultItem, int amount) {
        ItemEntity newEntity = new ItemEntity(oldEntity.level(),
                oldEntity.getX(), oldEntity.getY(), oldEntity.getZ(),
                new ItemStack(resultItem, amount));

        newEntity.setNoGravity(true);
        newEntity.setDeltaMovement(0, 0, 0);
        newEntity.setPickUpDelay(20);

        oldEntity.level().addFreshEntity(newEntity);
        oldEntity.discard();
    }
}