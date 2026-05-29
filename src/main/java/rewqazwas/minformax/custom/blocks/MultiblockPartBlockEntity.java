package rewqazwas.minformax.custom.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import rewqazwas.minformax.custom.ModBlockEntities;

public class MultiblockPartBlockEntity extends AbstractMultiblockPartBlockEntity {

    public MultiblockPartBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MULTIBLOCK_PART_BE.get(), pos, state);
    }

    @Override
    protected void serverTick(Level level, BlockPos pos, BlockState state) {
        // Base components do not tick until a master transforms them
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Unformed Structure");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return null;
    }


}