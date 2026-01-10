package rewqazwas.minformax.custom.items;


import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import rewqazwas.minformax.custom.ModAttachmentTypes;
import rewqazwas.minformax.custom.index.ModDataReloadListener;
import rewqazwas.minformax.custom.index.PlayerIndex;
import rewqazwas.minformax.custom.utility.Raycaster;
import rewqazwas.minformax.custom.utility.Utils;

import java.util.List;

public class ScannerItem extends Item {
    public ScannerItem(Properties properties) {
        super(properties);
    }

    public UseAnim getUseAnimation(ItemStack stack) {return UseAnim.CUSTOM;}

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.minformax.scanner"));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            List<EntityHitResult> result = Raycaster.INSTANCE.raycast(level, ((Entity) player), 10D, true, false);
            if(!result.isEmpty()) {
                Entity target = result.get(0).getEntity();

                if (target instanceof Mob && !level.isClientSide()) {
                    String key = target.getType().toString();
                    level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_STEP, SoundSource.PLAYERS,1f, 1f);
                    if(ModDataReloadListener.MOB_DROPS.containsKey(key)) {
                        if (!PlayerIndex.contains(((ServerPlayer) player), key)) {
                            PlayerIndex.add(((ServerPlayer) player), key);
                            player.setData(ModAttachmentTypes.INDEX_SYNC, Utils.clearContent(PlayerIndex.getLocalIndex((ServerPlayer) player), level));
                            player.sendSystemMessage(Component.literal("§aNew entry added to your index: §e" + Utils.prettyName(key)));
                        } else {
                            player.sendSystemMessage(Component.literal("§eEntry already exists in your index: §e" + Utils.prettyName(key)));
                        }
                    } else {
                        player.sendSystemMessage(Component.literal("§cThis entity cannot be scanned: §e" + Utils.prettyName(key)));
                    }
                }
            }
        }
        return InteractionResultHolder.consume(itemstack);
    }


}
