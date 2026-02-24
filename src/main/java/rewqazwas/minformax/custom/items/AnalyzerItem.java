package rewqazwas.minformax.custom.items;


import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import rewqazwas.minformax.custom.utility.Raycaster;

import java.util.List;

public class AnalyzerItem extends Item {
    public AnalyzerItem(Properties properties) {
        super(properties);
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
                    player.sendSystemMessage(Component.literal(key));
                }
            }
        }
        return InteractionResultHolder.consume(itemstack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.minformax.analyzer"));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
