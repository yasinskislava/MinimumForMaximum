package rewqazwas.minformax.custom.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;
import rewqazwas.minformax.custom.ModAttachmentTypes;
import rewqazwas.minformax.custom.blocks.Multiblocks.GateOfBabylonBlockEntity;
import rewqazwas.minformax.custom.blocks.Multiblocks.HatchBlockEntity;
import rewqazwas.minformax.custom.utility.MultiblockSavedData;

import java.util.List;

public class SakuraditeMultiblockPartBlock extends BaseEntityBlock {
    public static final MapCodec<SakuraditeMultiblockPartBlock> CODEC = simpleCodec(SakuraditeMultiblockPartBlock::new);

    public SakuraditeMultiblockPartBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if (state.is(ModBlocks.SAKURADITE_INPUT.get()) || state.is(ModBlocks.SAKURADITE_OUTPUT.get())) {
            return new HatchBlockEntity(pos, state);
        }

        if (FMLEnvironment.dist.isDedicatedServer() ||
                Minecraft.getInstance().getSingleplayerServer() != null) {

            var server = ServerLifecycleHooks.getCurrentServer();
            if (server != null) {
                var data = MultiblockSavedData.get(server.overworld());
                if(data.isMaster(pos)){
                    return new GateOfBabylonBlockEntity(pos, state);
                }
            }
        }
        return null;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            BlockPos masterPos = null;

            var chunk = level.getChunkAt(pos);
            if (chunk.hasData(ModAttachmentTypes.MULTIBLOCK_PROXY)) {
                masterPos = chunk.getData(ModAttachmentTypes.MULTIBLOCK_PROXY).get(pos);
            }

            if (masterPos == null && level.getBlockEntity(pos) instanceof AbstractMultiblockPartBlockEntity be && be.isMaster()) {
                masterPos = pos;
            }

            if (masterPos != null && level.getBlockEntity(masterPos) instanceof AbstractMultiblockPartBlockEntity masterBE) {
                player.openMenu(masterBE, masterPos);
                return ItemInteractionResult.SUCCESS;
            }
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && !level.isClientSide) {
            BlockPos masterPos = null;
            var chunk = level.getChunkAt(pos);
            if (chunk.hasData(ModAttachmentTypes.MULTIBLOCK_PROXY)) {
                masterPos = chunk.getData(ModAttachmentTypes.MULTIBLOCK_PROXY).get(pos);
            }

            if (masterPos != null && level.getBlockEntity(masterPos) instanceof AbstractMultiblockPartBlockEntity masterBE) {
                masterBE.unformStructure(level);
            } else if (level.getBlockEntity(pos) instanceof AbstractMultiblockPartBlockEntity directBE && directBE.isMaster()) {
                directBE.unformStructure(level);
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return (lvl, pos, st, be) -> {
            if (be instanceof AbstractMultiblockPartBlockEntity part) {
                AbstractMultiblockPartBlockEntity.tick(lvl, pos, st, part);
            }
        };
    }

    @Override
    protected boolean skipRendering(BlockState state, BlockState adjacentBlockState, net.minecraft.core.Direction side) {
        if (adjacentBlockState.is(state.getBlock())) {
            return true;
        }
        return super.skipRendering(state, adjacentBlockState, side);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.translatable("tooltip.minformax.sakuradite_multiblock_part"));
        tooltipComponents.add(Component.translatable("tooltip.minformax.phantom"));
    }
}