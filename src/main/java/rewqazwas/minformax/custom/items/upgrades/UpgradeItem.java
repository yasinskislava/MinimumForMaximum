package rewqazwas.minformax.custom.items.upgrades;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import rewqazwas.minformax.custom.blocks.MachineBaseEntity;
import rewqazwas.minformax.custom.utility.Utils;

import java.util.List;

public class UpgradeItem extends Item {
    public UpgradeItem() {
        super(new Item.Properties().stacksTo(16));
    }

    private boolean isBlockValid(BlockEntity blockEntity) {
        return blockEntity instanceof MachineBaseEntity;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, context, tooltipComponents, isAdvanced);
        tooltipComponents.add(Component.translatable("tooltip.minformax.upgrade_item").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var be = context.getLevel().getBlockEntity(context.getClickedPos());
        if(context.getPlayer().isCrouching() && isBlockValid(be)){
            var cap = context.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, context.getClickedPos(), context.getClickedFace());

            if (cap != null) {
                for (int i = 0; i < cap.getSlots(); i++) {
                    var stackInSlot = cap.getStackInSlot(i);
                    var stackItem = stackInSlot.getItem();
                    var contextItem = context.getItemInHand().getItem();
                    if (Utils.getOriginalClass(stackItem) == Utils.getOriginalClass(contextItem)) {
                        // Found an upgrade of the same type
                        boolean isBetter = false;
                        if (stackInSlot.getItem() instanceof SpeedUpgrade existingSpeed && contextItem instanceof SpeedUpgrade newSpeed) {
                            if (newSpeed.getModifier() > existingSpeed.getModifier()) {
                                isBetter = true;
                            }
                        } else if (stackInSlot.getItem() instanceof ProcessingUpgrade existingStack && context.getItemInHand().getItem() instanceof ProcessingUpgrade newStack) {
                            if (newStack.getMultiplier() > existingStack.getMultiplier()) {
                                isBetter = true;
                            }
                        } else if (stackInSlot.getItem() instanceof ExtraDropUpgrade existingExtra && context.getItemInHand().getItem() instanceof ExtraDropUpgrade newExtra) {
                            if (newExtra.getPercentage() > existingExtra.getPercentage()) {
                                isBetter = true;
                            }
                        } else if (stackInSlot.getItem() instanceof FortuneUpgrade existingFortune && context.getItemInHand().getItem() instanceof FortuneUpgrade newFortune) {
                            if (newFortune.getLevel() > existingFortune.getLevel()) {
                                isBetter = true;
                            }
                        }

                        if (isBetter) {
                            // Eject the old upgrade
                            Containers.dropItemStack(context.getLevel(), context.getClickedPos().getX(), context.getClickedPos().getY() + 1, context.getClickedPos().getZ(), stackInSlot.copy());
                            
                            // Extract the old item from the slot
                            cap.extractItem(i, stackInSlot.getCount(), false);
                            
                            // Insert the new item
                            var remainder = cap.insertItem(i, context.getItemInHand().copy(), false);
                            if (remainder.isEmpty()) {
                                context.getItemInHand().setCount(0);
                            } else {
                                context.getItemInHand().setCount(remainder.getCount());
                            }
                        }
                        return InteractionResult.sidedSuccess(context.getLevel().isClientSide());
                    }
                }

                // If no matching upgrade found, try to insert normally
                var remainder = ItemHandlerHelper.insertItemStacked(cap, context.getItemInHand().copy(), false);
                if (remainder.getCount() != context.getItemInHand().getCount()) {
                    context.getItemInHand().setCount(remainder.getCount());
                }
            }
        }
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide());
    }

}
