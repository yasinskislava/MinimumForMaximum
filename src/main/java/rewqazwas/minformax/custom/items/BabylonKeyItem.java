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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import rewqazwas.minformax.config.DataConfigs;
import rewqazwas.minformax.custom.ModAttachmentTypes;
import rewqazwas.minformax.custom.blocks.AbstractMultiblockPartBlockEntity;
import rewqazwas.minformax.custom.blocks.ModBlocks;
import rewqazwas.minformax.custom.blocks.Multiblocks.GateOfBabylonBlockEntity;
import rewqazwas.minformax.custom.blocks.SakuraditeMultiblockPartBlock;
import rewqazwas.minformax.custom.utility.MultiblockSavedData;

import java.util.*;

public class BabylonKeyItem extends Item {
    public BabylonKeyItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        BlockState clickedState = level.getBlockState(clickedPos);
        Player player = context.getPlayer();

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (player == null) {
            return InteractionResult.FAIL;
        }

        if (clickedState.getBlock() instanceof SakuraditeMultiblockPartBlock) {
            // 1. Line-scan outward from the clicked block to find initial relative boundaries
            int minX = clickedPos.getX(), maxX = clickedPos.getX();
            int minY = clickedPos.getY(), maxY = clickedPos.getY();
            int minZ = clickedPos.getZ(), maxZ = clickedPos.getZ();

            // Scan X Axis
            while (level.getBlockState(new BlockPos(minX - 1, clickedPos.getY(), clickedPos.getZ())).getBlock() instanceof SakuraditeMultiblockPartBlock) { minX--; }
            while (level.getBlockState(new BlockPos(maxX + 1, clickedPos.getY(), clickedPos.getZ())).getBlock() instanceof SakuraditeMultiblockPartBlock) { maxX++; }

            // Scan Y Axis
            while (level.getBlockState(new BlockPos(clickedPos.getX(), minY - 1, clickedPos.getZ())).getBlock() instanceof SakuraditeMultiblockPartBlock) { minY--; }
            while (level.getBlockState(new BlockPos(clickedPos.getX(), maxY + 1, clickedPos.getZ())).getBlock() instanceof SakuraditeMultiblockPartBlock) { maxY++; }

            // Scan Z Axis
            while (level.getBlockState(new BlockPos(clickedPos.getX(), clickedPos.getY(), minZ - 1)).getBlock() instanceof SakuraditeMultiblockPartBlock) { minZ--; }
            while (level.getBlockState(new BlockPos(clickedPos.getX(), clickedPos.getY(), maxZ + 1)).getBlock() instanceof SakuraditeMultiblockPartBlock) { maxZ++; }

            // 2. Handle the wall face click scenario.
            // If the player clicked a flat wall face, one of the axes will have a thickness of 1.
            // We look down the frame edges to find and slide to the true opposite wall.
            BlockPos minCorner = new BlockPos(minX, minY, minZ);

            // Clicked a flat East/West Wall face
            if (minX == maxX) {
                if (level.getBlockState(minCorner.west()).getBlock() instanceof SakuraditeMultiblockPartBlock) {
                    while (level.getBlockState(new BlockPos(minX - 1, minY, minZ)).getBlock() instanceof SakuraditeMultiblockPartBlock) { minX--; }
                } else {
                    while (level.getBlockState(new BlockPos(maxX + 1, minY, minZ)).getBlock() instanceof SakuraditeMultiblockPartBlock) { maxX++; }
                }
            }

            // Clicked a flat Top/Bottom Floor face
            if (minY == maxY) {
                if (level.getBlockState(minCorner.below()).getBlock() instanceof SakuraditeMultiblockPartBlock) {
                    while (level.getBlockState(new BlockPos(minX, minY - 1, minZ)).getBlock() instanceof SakuraditeMultiblockPartBlock) { minY--; }
                } else {
                    while (level.getBlockState(new BlockPos(minX, maxY + 1, minZ)).getBlock() instanceof SakuraditeMultiblockPartBlock) { maxY++; }
                }
            }

            // Clicked a flat North/South Wall face
            if (minZ == maxZ) {
                if (level.getBlockState(minCorner.north()).getBlock() instanceof SakuraditeMultiblockPartBlock) {
                    while (level.getBlockState(new BlockPos(minX, minY, minZ - 1)).getBlock() instanceof SakuraditeMultiblockPartBlock) { minZ--; }
                } else {
                    while (level.getBlockState(new BlockPos(minX, minY, maxZ + 1)).getBlock() instanceof SakuraditeMultiblockPartBlock) { maxZ++; }
                }
            }

            // 3. Compile the true absolute boundaries
            BlockPos finalMin = new BlockPos(minX, minY, minZ);
            BlockPos finalMax = new BlockPos(maxX, maxY, maxZ);

            int xSize = finalMax.getX() - finalMin.getX() + 1;
            int ySize = finalMax.getY() - finalMin.getY() + 1;
            int zSize = finalMax.getZ() - finalMin.getZ() + 1;

            // Size verification
            int maxSize = DataConfigs.gateOfBabylonSize.get();
            if (xSize < 3 || ySize < 3 || zSize < 3) {
                if (player != null) {
                    player.sendSystemMessage(Component.translatable("message.minformax.babylon.structure_too_small").withStyle(ChatFormatting.RED));
                }
                return InteractionResult.FAIL;
            } else if (xSize > maxSize|| ySize > maxSize || zSize > maxSize) {
                if (player != null) {
                    player.sendSystemMessage(Component.translatable("message.minformax.babylon.structure_too_big", maxSize, maxSize, maxSize).withStyle(ChatFormatting.RED));
                }
                return InteractionResult.FAIL;
            }

            // 4. Run validation over the discovered cuboid space
            if (validateStructure(level, finalMin, finalMax, clickedPos, player)) {
                formStructure(level, finalMin, finalMax, clickedPos);
                if (player != null) {
                    player.sendSystemMessage(Component.translatable("message.minformax.babylon.formed", xSize, ySize, zSize).withStyle(ChatFormatting.GREEN));
                }
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }

    @SuppressWarnings("deprecated")
    public boolean validateStructure(Level level, BlockPos min, BlockPos max, BlockPos clickedPos, Player player) {
        // 1. Chunk Loading Safety Guard
        if (!level.hasChunksAt(min, max)) {
            if (player != null) {
                player.sendSystemMessage(Component.translatable("message.minformax.babylon.unloaded_chunks").withStyle(ChatFormatting.RED));
            }
            return false;
        }

        if (level instanceof ServerLevel serverLevel) {
            MultiblockSavedData savedData = MultiblockSavedData.get(serverLevel);

            // Create an AABB box for the new structure being validated
            AABB newStructureBox = new AABB(
                    min.getX(), min.getY(), min.getZ(),
                    max.getX() + 1, max.getY() + 1, max.getZ() + 1
            );

            for (Map.Entry<UUID, MultiblockSavedData.StructureBounds> entry : savedData.getStructures().entrySet()) {
                MultiblockSavedData.StructureBounds existing = entry.getValue();

                AABB existingStructureBox = new AABB(
                        existing.min().getX(), existing.min().getY(), existing.min().getZ(),
                        existing.max().getX() + 1, existing.max().getY() + 1, existing.max().getZ() + 1
                );

                // If the boxes intersect at all (fully inside, partially clipping, or wrapping around), fail validation!
                if (newStructureBox.intersects(existingStructureBox)) {
                    if (player != null) {
                        player.sendSystemMessage(Component.translatable("message.minformax.babylon.collision").withStyle(ChatFormatting.RED));
                    }
                    return false;
                }
            }
        }

        // 2. Scan Floor (min.getY()) and Roof (max.getY())
        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int z = min.getZ(); z <= max.getZ(); z++) {
                if (!isValidComponent(level, x, min.getY(), z, min, max, player)) {
                    player.sendSystemMessage(Component.translatable("message.minformax.babylon.block_missing", x, min.getY(), z).withStyle(ChatFormatting.RED));
                    return false;
                }
                if (!isValidComponent(level, x, max.getY(), z, min, max, player)) {
                    player.sendSystemMessage(Component.translatable("message.minformax.babylon.block_missing", x, max.getY(), z).withStyle(ChatFormatting.RED));
                    return false;
                }
            }
        }

        // 3. Scan North Wall (min.getZ()) and South Wall (max.getZ())
        for (int y = min.getY() + 1; y < max.getY(); y++) {
            for (int x = min.getX(); x <= max.getX(); x++) {
                if (!isValidComponent(level, x, y, min.getZ(), min, max, player)) {
                    player.sendSystemMessage(Component.translatable("message.minformax.babylon.block_missing", x, y, min.getZ()).withStyle(ChatFormatting.RED));
                    return false;
                }
                if (!isValidComponent(level, x, y, max.getZ(), min, max, player)) {
                    player.sendSystemMessage(Component.translatable("message.minformax.babylon.block_missing", x, y, max.getZ()).withStyle(ChatFormatting.RED));
                    return false;
                }
            }
        }

        // 4. Scan West Wall (min.getX()) and East Wall (max.getX())
        for (int y = min.getY() + 1; y < max.getY(); y++) {
            for (int z = min.getZ() + 1; z < max.getZ(); z++) {
                if (!isValidComponent(level, min.getX(), y, z, min, max, player)) {
                    player.sendSystemMessage(Component.translatable("message.minformax.babylon.block_missing", min.getX(), y, z).withStyle(ChatFormatting.RED));
                    return false;
                }
                if (!isValidComponent(level, max.getX(), y, z, min, max, player)) {
                    player.sendSystemMessage(Component.translatable("message.minformax.babylon.block_missing", max.getX(), y, z).withStyle(ChatFormatting.RED));
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Validates a single block position based on its location context within the cuboid framework.
     */
    private boolean isValidComponent(Level level, int x, int y, int z, BlockPos min, BlockPos max, Player player) {
        BlockPos targetPos = new BlockPos(x, y, z);
        BlockState state = level.getBlockState(targetPos);

        int boundaryMatches = 0;
        if (x == min.getX() || x == max.getX()) boundaryMatches++;
        if (y == min.getY() || y == max.getY()) boundaryMatches++;
        if (z == min.getZ() || z == max.getZ()) boundaryMatches++;

        if (boundaryMatches >= 2) {
            if (!state.is(ModBlocks.SAKURADITE_CASING.get())) {
                player.sendSystemMessage(Component.translatable("message.minformax.babylon.casing_missing", x, y, z).withStyle(ChatFormatting.RED));
                return false;
            }
        } else {
            if (!(state.getBlock() instanceof SakuraditeMultiblockPartBlock)) return false;
        }

        var chunk = level.getChunkAt(targetPos);
        if (chunk.hasData(ModAttachmentTypes.MULTIBLOCK_PROXY)) {
            Map<BlockPos, BlockPos> proxyMap = chunk.getData(ModAttachmentTypes.MULTIBLOCK_PROXY);
            if (proxyMap != null && proxyMap.containsKey(targetPos)) {
                player.sendSystemMessage(Component.translatable("message.minformax.babylon.belongs_to_other", x, y, z).withStyle(ChatFormatting.RED));
                return false;
            }
        }

        if (level.getBlockEntity(targetPos) instanceof AbstractMultiblockPartBlockEntity partBE && partBE.isFormed()) {
            player.sendSystemMessage(Component.translatable("message.minformax.babylon.belongs_to_other", x, y, z).withStyle(ChatFormatting.RED));
            return false;
        }

        return true;
    }

    private void formStructure(Level level, BlockPos min, BlockPos max, BlockPos clickedPos) {
        UUID structureId = UUID.randomUUID();
        List<BlockPos> slaves = new ArrayList<>();

        if (level instanceof ServerLevel serverLevel) {
            MultiblockSavedData.get(serverLevel).register(structureId, clickedPos, min, max);
        }

        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            boolean isOuterLayer = pos.getX() == min.getX() || pos.getX() == max.getX() ||
                    pos.getY() == min.getY() || pos.getY() == max.getY() ||
                    pos.getZ() == min.getZ() || pos.getZ() == max.getZ();

            if (isOuterLayer && !pos.equals(clickedPos)) {
                BlockPos immutablePos = pos.immutable();
                slaves.add(immutablePos);

                var chunk = level.getChunkAt(immutablePos);
                Map<BlockPos, BlockPos> proxyMap = chunk.getData(ModAttachmentTypes.MULTIBLOCK_PROXY);
                proxyMap.put(immutablePos, clickedPos);
                chunk.setUnsaved(true);
            }
        }

        BlockState originalState = level.getBlockState(clickedPos);

        GateOfBabylonBlockEntity babylonMaster = new GateOfBabylonBlockEntity(clickedPos, originalState);
        level.setBlockEntity(babylonMaster);

        // Initialize and map assigned slaves
        babylonMaster.convertToMaster(structureId, slaves);
        level.sendBlockUpdated(clickedPos, originalState, originalState, 3);

        for (BlockPos slavePos : slaves) {
            BlockState slaveState = level.getBlockState(slavePos);

            if (slaveState.is(ModBlocks.SAKURADITE_INPUT.get()) || slaveState.is(ModBlocks.SAKURADITE_OUTPUT.get())) {
                level.invalidateCapabilities(slavePos);
                level.updateNeighborsAt(slavePos, slaveState.getBlock());
            }
        }

        AABB boundingBox = new AABB(min.getX(), min.getY(), min.getZ(), max.getX() + 1, max.getY() + 1, max.getZ() + 1);
        spawnCenteredBurstParticles(level, boundingBox);
    }

    private void spawnCenteredBurstParticles(Level level, AABB boundingBox) {
        if (level instanceof ServerLevel serverLevel) {
            double centerX = boundingBox.minX + (boundingBox.getXsize() / 2.0);
            double centerY = boundingBox.minY + (boundingBox.getYsize() / 2.0);
            double centerZ = boundingBox.minZ + (boundingBox.getZsize() / 2.0);

            serverLevel.sendParticles(ParticleTypes.DRAGON_BREATH, centerX, centerY, centerZ, 100, 0.5, 0.5, 0.5, 0.1);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.translatable("tooltip.minformax.babylon_key").withStyle(ChatFormatting.GOLD));
    }
}