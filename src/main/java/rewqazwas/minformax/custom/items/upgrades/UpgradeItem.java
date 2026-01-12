package rewqazwas.minformax.custom.items.upgrades;

import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import rewqazwas.minformax.custom.blocks.BlockReplicatorBlockEntity;
import rewqazwas.minformax.custom.blocks.EternalGeneratorBlockEntity;
import rewqazwas.minformax.custom.blocks.FluidReplicatorBlockEntity;

public class UpgradeItem extends Item {
    public UpgradeItem() {
        super(new Item.Properties().stacksTo(16));
    }

    private boolean isBlockValid(BlockEntity blockEntity) {
        return blockEntity instanceof EternalGeneratorBlockEntity || blockEntity instanceof FluidReplicatorBlockEntity || blockEntity instanceof BlockReplicatorBlockEntity;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var be = context.getLevel().getBlockEntity(context.getClickedPos());
        if(context.getPlayer().isCrouching() && isBlockValid(be)){
            var cap = context.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, context.getClickedPos(), context.getClickedFace());

            if (cap != null) {
                for (int i = 0; i < cap.getSlots(); i++) {
                    var stackInSlot = cap.getStackInSlot(i);
                    if (stackInSlot.getItem().getClass() == context.getItemInHand().getItem().getClass()) {
                        // Found an upgrade of the same type
                        boolean isBetter = false;
                        if (stackInSlot.getItem() instanceof SpeedUpgrade existingSpeed && context.getItemInHand().getItem() instanceof SpeedUpgrade newSpeed) {
                            if (newSpeed.getModifier() > existingSpeed.getModifier()) {
                                isBetter = true;
                            }
                        } else if (stackInSlot.getItem() instanceof StackUpgrade existingStack && context.getItemInHand().getItem() instanceof StackUpgrade newStack) {
                            if (newStack.getMultiplier() > existingStack.getMultiplier()) {
                                isBetter = true;
                            }
                        } else if (stackInSlot.getItem() instanceof ExtraDropUpgrade existingExtra && context.getItemInHand().getItem() instanceof ExtraDropUpgrade newExtra) {
                            if (newExtra.getPercentage() > existingExtra.getPercentage()) {
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
