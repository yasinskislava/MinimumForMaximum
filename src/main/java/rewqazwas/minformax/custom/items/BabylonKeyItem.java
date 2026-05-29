package rewqazwas.minformax.custom.items;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import rewqazwas.minformax.custom.blocks.AbstractMultiblockPartBlockEntity;
import rewqazwas.minformax.custom.blocks.ModBlocks;
import rewqazwas.minformax.custom.blocks.Multiblocks.GateOfBabylonBlockEntity;
import rewqazwas.minformax.custom.blocks.SakuraditeMultiblockPartBlock;

import java.util.*;

public class BabylonKeyItem extends Item {
    public BabylonKeyItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();

        if (!level.isClientSide) {
            if (isSakuraditeBlock(level, clickedPos)) {
                if (checkAndFormStructure(level, clickedPos, context.getPlayer())) {
                    context.getItemInHand().shrink(1);
                } else {
                    context.getPlayer().sendSystemMessage(Component.literal("Structure is not appropriate."));
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.translatable("tooltip.minformax.babylon_key").withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC));
    }

    private boolean checkAndFormStructure(Level level, BlockPos clickedPos, Player player) {
        AABB boundingBox = findStructureBoundingBox(level, clickedPos);
        if (boundingBox == null) {
            player.sendSystemMessage(Component.literal("Could not determine a valid structure bounding box."));
            return false;
        }

        if (!validateStructure(level, boundingBox)) {
            player.sendSystemMessage(Component.literal("Structure validation failed. Check corners, walls, and interior."));
            return false;
        }

        formStructure(level, boundingBox, clickedPos);
        spawnCenteredBurstParticles(level, boundingBox);
        player.sendSystemMessage(Component.literal("Gate of Babylon formed successfully!"));
        return true;
    }

    private AABB findStructureBoundingBox(Level level, BlockPos clickedPos) {
        Set<BlockPos> visited = new HashSet<>();
        Set<BlockPos> structureCandidateBlocks = new HashSet<>();
        Queue<BlockPos> queue = new LinkedList<>();

        queue.add(clickedPos);
        visited.add(clickedPos);
        structureCandidateBlocks.add(clickedPos);

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();

            for (Direction dir : Direction.values()) {
                BlockPos neighbor = current.relative(dir);
                if (!visited.contains(neighbor) && isSakuraditeBlock(level, neighbor)) {
                    if (level.getBlockEntity(neighbor) instanceof AbstractMultiblockPartBlockEntity part && part.isFormed()) {
                        continue;
                    }
                    visited.add(neighbor);
                    queue.add(neighbor);
                    structureCandidateBlocks.add(neighbor);
                }
            }
        }

        if (structureCandidateBlocks.isEmpty()) return null;

        int minX = Integer.MAX_VALUE; int minY = Integer.MAX_VALUE; int minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE; int maxY = Integer.MIN_VALUE; int maxZ = Integer.MIN_VALUE;

        for (BlockPos pos : structureCandidateBlocks) {
            minX = Math.min(minX, pos.getX());
            minY = Math.min(minY, pos.getY());
            minZ = Math.min(minZ, pos.getZ());
            maxX = Math.max(maxX, pos.getX());
            maxY = Math.max(maxY, pos.getY());
            maxZ = Math.max(maxZ, pos.getZ());
        }

        if (maxX - minX < 2 || maxY - minY < 2 || maxZ - minZ < 2) return null;

        return new AABB(minX, minY, minZ, maxX + 1, maxY + 1, maxZ + 1);
    }

    private boolean isSakuraditeBlock(Level level, BlockPos pos) {
        Block block = level.getBlockState(pos).getBlock();
        return block instanceof SakuraditeMultiblockPartBlock;
    }

    private boolean validateStructure(Level level, AABB boundingBox) {
        BlockPos min = BlockPos.containing(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
        BlockPos max = BlockPos.containing(boundingBox.maxX - 1, boundingBox.maxY - 1, boundingBox.maxZ - 1);

        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            int boundaryMatchCount = 0;
            if (pos.getX() == min.getX() || pos.getX() == max.getX()) boundaryMatchCount++;
            if (pos.getY() == min.getY() || pos.getY() == max.getY()) boundaryMatchCount++;
            if (pos.getZ() == min.getZ() || pos.getZ() == max.getZ()) boundaryMatchCount++;

            BlockState state = level.getBlockState(pos);

            if(boundaryMatchCount == 0){
                continue;
            }
            // 1. Borders (Edges and Corners) -> Must be strictly Casings
            if (boundaryMatchCount >= 2) {
                if (!state.is(ModBlocks.SAKURADITE_CASING.get())) return false;

                // Ensure it isn't already part of another formed multiblock
                if (level.getBlockEntity(pos) instanceof AbstractMultiblockPartBlockEntity part && part.isFormed()) {
                    return false;
                }
            }
            // 2. Flat wall faces
            else {
                if (!isSakuraditeBlock(level, pos)) return false;

                if (level.getBlockEntity(pos) instanceof AbstractMultiblockPartBlockEntity part && part.isFormed()) {
                    return false;
                }
            }
        }
        return true;
    }

    private void formStructure(Level level, AABB boundingBox, BlockPos clickedPos) {
        BlockPos min = BlockPos.containing(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
        BlockPos max = BlockPos.containing(boundingBox.maxX - 1, boundingBox.maxY - 1, boundingBox.maxZ - 1);

        UUID structureId = UUID.randomUUID();
        List<BlockPos> slaves = new ArrayList<>();

        BlockState masterState = level.getBlockState(clickedPos);
        level.removeBlockEntity(clickedPos);
        level.setBlock(clickedPos, masterState, 3);

        GateOfBabylonBlockEntity babylonMaster = new GateOfBabylonBlockEntity(clickedPos, masterState);
        level.setBlockEntity(babylonMaster);

        // Assign Slaves
        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            boolean isOuterLayer = pos.getX() == min.getX() || pos.getX() == max.getX() ||
                    pos.getY() == min.getY() || pos.getY() == max.getY() ||
                    pos.getZ() == min.getZ() || pos.getZ() == max.getZ();

            if (isOuterLayer && !pos.equals(clickedPos)) {
                if (level.getBlockEntity(pos) instanceof AbstractMultiblockPartBlockEntity part) {
                    part.setSlaveOf(structureId, clickedPos);
                    slaves.add(pos.immutable());
                }
            }
        }

        // Populate structure coordinates into the ready master
        babylonMaster.convertToMaster(structureId, slaves);

        level.sendBlockUpdated(clickedPos, masterState, masterState, 3);
    }

    private void spawnCenteredBurstParticles(Level level, AABB boundingBox) {
        if (level instanceof ServerLevel serverLevel) {
            double centerX = boundingBox.minX + (boundingBox.getXsize() / 2.0);
            double centerY = boundingBox.minY + (boundingBox.getYsize() / 2.0);
            double centerZ = boundingBox.minZ + (boundingBox.getZsize() / 2.0);

            serverLevel.sendParticles(ParticleTypes.DRAGON_BREATH, centerX, centerY, centerZ, 50, boundingBox.getXsize() / 3, boundingBox.getYsize() / 3, boundingBox.getZsize() / 3, 0.2);
        }
    }
}