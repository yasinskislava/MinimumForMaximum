package rewqazwas.minformax.custom.utility;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import rewqazwas.minformax.custom.blocks.MachineBaseEntity;

public class SideConfigHud {
    private static final ResourceLocation SIDE_CONFIG_TEX = ResourceLocation.fromNamespaceAndPath("minformax", "textures/gui/side_config.png");
    private static final ResourceLocation SELECT_TEXTURE = ResourceLocation.fromNamespaceAndPath("minformax", "textures/gui/select.png");

    private record ConfigBox(int x, int y, int w, int h) {}

    private static final ConfigBox[] BOXES = {
            new ConfigBox(5, 5, 18, 18),    // Up
            new ConfigBox(25, 5, 18, 18),   // Forward
            new ConfigBox(5, 25, 18, 18),   // Left
            new ConfigBox(45, 25, 18, 18),  // Right
            new ConfigBox(25, 45, 18, 18),  // Back
            new ConfigBox(45, 45, 18, 18)   // Down
    };

    private static Direction getDirectionFromBox(Minecraft minecraft, int boxIndex) {
        if (minecraft.player == null) return Direction.NORTH;

        Direction facing = minecraft.player.getDirection();
        return switch (boxIndex) {
            case 0 -> Direction.UP;
            case 5 -> Direction.DOWN;
            case 1 -> facing; // Forward
            case 2 -> facing.getCounterClockWise(); // Left
            case 3 -> facing.getClockWise(); // Right
            case 4 -> facing.getOpposite(); // Back
            default -> Direction.NORTH;
        };
    }

    public static void render(GuiGraphics guiGraphics, Minecraft mc, BlockEntity be) {
        if (!(be instanceof MachineBaseEntity machine)) {
            return;
        }

        int xc = mc.getWindow().getGuiScaledWidth() / 2;
        int yc = mc.getWindow().getGuiScaledHeight() / 2;

        int configX = xc - 100;
        int configY = yc - 34;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 300);

        guiGraphics.blit(SIDE_CONFIG_TEX, configX, configY, 0, 0, 68, 68, 68, 68);
        for (int i = 0; i < BOXES.length; i++) {
            Direction dir = getDirectionFromBox(mc, i);

            if (machine.isSideEnabled(dir)) {
                int drawX = configX + BOXES[i].x;
                int drawY = configY + BOXES[i].y;
                guiGraphics.blit(SELECT_TEXTURE, drawX, drawY, 0, 0, 18, 18, 18, 18);
            }
        }

        guiGraphics.pose().popPose();
    }

    public static boolean isBlockValid(BlockEntity blockEntity) {
        return blockEntity instanceof MachineBaseEntity;
    }
}