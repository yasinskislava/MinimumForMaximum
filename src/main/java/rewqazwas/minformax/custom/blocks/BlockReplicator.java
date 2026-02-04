package rewqazwas.minformax.custom.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;
import rewqazwas.minformax.custom.ModBlockEntities;
import rewqazwas.minformax.custom.index.BlockReplicatorData;
import rewqazwas.minformax.custom.index.ModDataReloadListener;

import java.util.Map;

public class BlockReplicator extends BaseEntityBlock {
    protected BlockReplicator(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(BlockReplicator::new);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockReplicatorBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if(!level.isClientSide()) {
            if (level.getBlockEntity(pos) instanceof BlockReplicatorBlockEntity blockEntity) {
                if (stack.isEmpty()) {
                    // Extract from item handler
                    ItemStack extracted = blockEntity.itemHandler.extractItem(0, 1, false);
                    if (!extracted.isEmpty()) {
                        player.setItemInHand(hand, extracted);
                    }
                } else {
                    // Insert into item handler
                    if(isItemValid(stack)) {
                        ItemStack remainder = ItemHandlerHelper.insertItem(blockEntity.itemHandler, stack, false);
                        if (remainder.getCount() < stack.getCount()) {
                            player.setItemInHand(hand, remainder);
                        }
                    } else {
                        player.sendSystemMessage(Component.translatable("message.minformax.block_support"));
                    }
                }
            }
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide());

    }

    private boolean isItemValid(ItemStack stack) {
        if (!(stack.getItem() instanceof BlockItem)) return false;

        // Check for direct block match
        var key = BuiltInRegistries.BLOCK.getKey(((BlockItem) stack.getItem()).getBlock()).toString();
        if (ModDataReloadListener.BLOCK_REPLICATOR_DATA.containsKey(key)) {
            return true;
        }

        // Check for tag match
        for (Map.Entry<String, BlockReplicatorData> entry : ModDataReloadListener.BLOCK_REPLICATOR_DATA.entrySet()) {
            if (entry.getKey().startsWith("#")) {
                var tagKey = TagKey.create(BuiltInRegistries.BLOCK.key(), ResourceLocation.parse(entry.getKey().substring(1)));
                var block = ((BlockItem) stack.getItem()).getBlock();
                var blockState = block.defaultBlockState();
                if (blockState.is(tagKey)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof BlockReplicatorBlockEntity) {
                ((BlockReplicatorBlockEntity) blockEntity).drops();
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if(level.isClientSide()) {
            return null;
        }
        return createTickerHelper(type, ModBlockEntities.BLOCK_REPLICATOR_BE.get(), (level1, blockPos, blockState, blockEntity) -> blockEntity.tick(level1, blockPos, blockState, blockEntity));
    }
}
