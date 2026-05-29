package rewqazwas.minformax.custom.blocks.PandoraBox;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import rewqazwas.minformax.custom.ModBlockEntities;
import rewqazwas.minformax.custom.blocks.EternalGeneratorBlockEntity;
import rewqazwas.minformax.custom.blocks.ModBlocks;

import java.util.List;

public class PandoraBoxCore extends Block implements EntityBlock {
    public static final MapCodec<PandoraBoxCore> CODEC = simpleCodec(PandoraBoxCore::new);

    public static final List<BlockPos> STRUCTURE_OFFSETS = List.of(
            new BlockPos(0, -1, 0),   // hatch
            new BlockPos(1, -1, 0),   // dummy
            new BlockPos(-1, -1, 0),  // dummy
            new BlockPos(0, -1, 1),   // dummy
            new BlockPos(0, -1, -1),  // dummy

            new BlockPos(1, 0, 0),    // dummy
            new BlockPos(-1, 0, 0),   // dummy
            new BlockPos(0, 0, 1),    // dummy
            new BlockPos(0, 0, -1),   // dummy

            new BlockPos(0, 1, 0),    // hatch
            new BlockPos(1, 1, 0),    // dummy
            new BlockPos(-1, 1, 0),   // dummy
            new BlockPos(0, 1, 1),    // dummy
            new BlockPos(0, 1, -1)    // dummy
    );

    public PandoraBoxCore(Properties properties) {
        super(properties);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        // --- NEW: Restore data from item ---
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof PandoraBoxCoreBlockEntity core) {
            CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
            if (customData != null) {
                CompoundTag tag = customData.copyTag();
                if (tag.contains("overload")) core.addOverload(tag.getLong("overload"));
                if (tag.contains("totalXp")) core.addXp(tag.getLong("totalXp"));
            }
        }
        // ------------------------------------

        super.setPlacedBy(level, pos, state, placer, stack);
        for (BlockPos offset : STRUCTURE_OFFSETS) {
            BlockPos targetPos = pos.offset(offset);
            BlockState blockState;
            if (offset.getX() == 0 && offset.getZ() == 0) {
                blockState = ModBlocks.PANDORA_BOX_HATCH.get().defaultBlockState();
            } else {
                blockState = ModBlocks.PANDORA_BOX_DUMMY.get().defaultBlockState();
            }

            level.setBlock(targetPos, blockState, 3);
            BlockEntity dummyBeRaw = level.getBlockEntity(targetPos);
            if (dummyBeRaw instanceof PandoraBoxDummyBlockEntity dummyBe) {
                dummyBe.setCorePos(pos);
            }
        }
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        List<ItemStack> drops = super.getDrops(state, builder);
        BlockEntity be = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);

        if (be instanceof PandoraBoxCoreBlockEntity core) {
            for (ItemStack stack : drops) {
                // Check if the dropped item is the core itself
                if (stack.is(this.asItem())) {
                    CompoundTag tag = new CompoundTag();
                    tag.putLong("overload", core.getOverload());
                    tag.putLong("totalXp", core.getTotalXp());
                    // Store the NBT into the item's Custom Data component
                    stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                }
            }
        }
        return drops;
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
        ItemStack stack = new ItemStack(ModBlocks.PANDORA_BOX_CORE.get());
        BlockEntity be = level.getBlockEntity(pos);

        if (be instanceof PandoraBoxCoreBlockEntity core) {
            CompoundTag tag = new CompoundTag();
            tag.putLong("overload", core.getOverload());
            tag.putLong("totalXp", core.getTotalXp());
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }

        return stack;
    }
    
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if(level.getBlockEntity(pos) instanceof PandoraBoxCoreBlockEntity core) {
                core.drops();
                level.updateNeighbourForOutputSignal(pos, this);
            }
            for (BlockPos offset : STRUCTURE_OFFSETS) {
                BlockPos targetPos = pos.offset(offset);
                if (level.getBlockState(targetPos).getBlock() instanceof PandoraBoxDummy) {
                    level.setBlock(targetPos, Blocks.AIR.defaultBlockState(), 35);
                }
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShapeForOffset(BlockPos.ZERO);
    }

    public static VoxelShape getShapeForOffset(BlockPos offset) {
        int x = offset.getX();
        int y = offset.getY();
        int z = offset.getZ();

        if (x == 0 && y == 0 && z == 0) return Shapes.empty();

        // Top layer (y = 1)
        if (x == 0 && y == 1 && z == 0) return Block.box(0, 0, 0, 16, 16, 16);
        if (x == -1 && y == 1 && z == 0) return Block.box(0, 4, 0, 16, 16, 16);
        if (x == 1 && y == 1 && z == 0) return Block.box(0, 4, 0, 16, 16, 16);
        if (x == 0 && y == 1 && z == -1) return Block.box(0, 4, 0, 16, 16, 16);
        if (x == 0 && y == 1 && z == 1) return Block.box(0, 4, 0, 16, 16, 16);

        // Middle layer (y = 0)
        if (x == 0 && y == 0 && z == 1) return Block.box(0, 0, 10, 16, 16, 16);
        if (x == 0 && y == 0 && z == -1) return Block.box(0, 0, 0, 16, 16, 4);
        if (x == -1 && y == 0 && z == 0) return Block.box(0, 0, 0, 4, 16, 16);
        if (x == 1 && y == 0 && z == 0) return Block.box(12, 0, 0, 16, 16, 16);

        // Bottom layer (y = -1)
        if (x == 0 && y == -1 && z == 0) return Block.box(0, 0, 0, 16, 16, 16);
        if (x == -1 && y == -1 && z == 0) return Block.box(0, 0, 0, 16, 12, 16);
        if (x == 1 && y == -1 && z == 0) return Block.box(0, 0, 0, 16, 12, 16);
        if (x == 0 && y == -1 && z == -1) return Block.box(0, 0, 0, 16, 12, 16);
        if (x == 0 && y == -1 && z == 1) return Block.box(0, 0, 0, 16, 12, 16);

        return Shapes.block();
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new PandoraBoxCoreBlockEntity(blockPos, blockState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        if (type == ModBlockEntities.PANDORA_BOX_CORE_BE.get()) {
            return (lvl, pos, st, be) -> {
                if (be instanceof PandoraBoxCoreBlockEntity pandora) {
                    pandora.tick(lvl, pos, st);
                }
            };
        }
        return null;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof PandoraBoxCoreBlockEntity) {
                player.openMenu((PandoraBoxCoreBlockEntity) be, pos);
            }
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }
}