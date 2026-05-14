package rewqazwas.minformax.custom.utility;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.IItemHandler;
import rewqazwas.minformax.custom.blocks.MachineBaseEntity;
import rewqazwas.minformax.custom.items.ModItems;

import java.util.ArrayList;
import java.util.List;

public class UpgradeHud {
    public static void render(GuiGraphics gui, Minecraft mc, BlockEntity be) {
        if (!(be instanceof MachineBaseEntity machine)) {
            return;
        }

        var upgradeHandler = machine.getUpgradeHandler();
        if (upgradeHandler == null) {
            return;
        }

        List<List<ItemStack>> displayGroups = new ArrayList<>();

        // Speed Upgrades
        List<ItemStack> speedGroup = new ArrayList<>();
        if (isSupported(upgradeHandler, new ItemStack(ModItems.SPEED_UPGRADE_TIER1.get()))) {
            speedGroup.add(new ItemStack(ModItems.SPEED_UPGRADE_TIER1.get()));
            speedGroup.add(new ItemStack(ModItems.SPEED_UPGRADE_TIER2.get()));
            speedGroup.add(new ItemStack(ModItems.SPEED_UPGRADE_TIER3.get()));
            speedGroup.add(new ItemStack(ModItems.SPEED_UPGRADE_TIER4.get()));
            if (isSupported(upgradeHandler, new ItemStack(ModItems.ULTIMATE_SPEED_UPGRADE.get()))) {
                speedGroup.add(new ItemStack(ModItems.ULTIMATE_SPEED_UPGRADE.get()));
            }
            displayGroups.add(speedGroup);
        }

        // Processing Upgrades
        List<ItemStack> processingGroup = new ArrayList<>();
        if (isSupported(upgradeHandler, new ItemStack(ModItems.PROCESSING_UPGRADE_TIER1.get()))) {
            processingGroup.add(new ItemStack(ModItems.PROCESSING_UPGRADE_TIER1.get()));
            processingGroup.add(new ItemStack(ModItems.PROCESSING_UPGRADE_TIER2.get()));
            processingGroup.add(new ItemStack(ModItems.PROCESSING_UPGRADE_TIER3.get()));
            processingGroup.add(new ItemStack(ModItems.PROCESSING_UPGRADE_TIER4.get()));
            if (isSupported(upgradeHandler, new ItemStack(ModItems.ULTIMATE_PROCESSING_UPGRADE.get()))) {
                processingGroup.add(new ItemStack(ModItems.ULTIMATE_PROCESSING_UPGRADE.get()));
            }
            displayGroups.add(processingGroup);
        }

        // Extra Drop Upgrades
        List<ItemStack> extraDropGroup = new ArrayList<>();
        if (isSupported(upgradeHandler, new ItemStack(ModItems.EXTRA_DROP_UPGRADE_TIER1.get()))) {
            extraDropGroup.add(new ItemStack(ModItems.EXTRA_DROP_UPGRADE_TIER1.get()));
            extraDropGroup.add(new ItemStack(ModItems.EXTRA_DROP_UPGRADE_TIER2.get()));
            extraDropGroup.add(new ItemStack(ModItems.EXTRA_DROP_UPGRADE_TIER3.get()));
            extraDropGroup.add(new ItemStack(ModItems.EXTRA_DROP_UPGRADE_TIER4.get()));
            displayGroups.add(extraDropGroup);
        }

        // Fortune Upgrades
        List<ItemStack> fortuneGroup = new ArrayList<>();
        if (isSupported(upgradeHandler, new ItemStack(ModItems.FORTUNE_UPGRADE_TIER1.get()))) {
            fortuneGroup.add(new ItemStack(ModItems.FORTUNE_UPGRADE_TIER1.get()));
            fortuneGroup.add(new ItemStack(ModItems.FORTUNE_UPGRADE_TIER2.get()));
            fortuneGroup.add(new ItemStack(ModItems.FORTUNE_UPGRADE_TIER3.get()));
            fortuneGroup.add(new ItemStack(ModItems.FORTUNE_UPGRADE_TIER4.get()));
            displayGroups.add(fortuneGroup);
        }

        // Single Upgrades
        if (isSupported(upgradeHandler, new ItemStack(ModItems.INVERTED_UPGRADE.get()))) {
            displayGroups.add(List.of(new ItemStack(ModItems.INVERTED_UPGRADE.get())));
        }
        if (isSupported(upgradeHandler, new ItemStack(ModItems.AUTO_SMELTING_UPGRADE.get()))) {
            displayGroups.add(List.of(new ItemStack(ModItems.AUTO_SMELTING_UPGRADE.get())));
        }
        if (isSupported(upgradeHandler, new ItemStack(ModItems.WATERING_UPGRADE.get()))) {
            displayGroups.add(List.of(new ItemStack(ModItems.WATERING_UPGRADE.get())));
        }
        if (isSupported(upgradeHandler, new ItemStack(ModItems.COMPRESSING_UPGRADE.get()))) {
            displayGroups.add(List.of(new ItemStack(ModItems.COMPRESSING_UPGRADE.get())));
        }


        int xc = mc.getWindow().getGuiScaledWidth() / 2;
        int yc = mc.getWindow().getGuiScaledHeight() / 2;
        int yOffset = 20;
        
        // Adjust position to left side
        xc -= 230;
        yc -= 20;

        if (!displayGroups.isEmpty()) {
            gui.drawString(mc.font, "Supported Upgrades:", xc, yc + 4, 0xFFFFFF, true);

            long time = System.currentTimeMillis() / 1000;

            for (List<ItemStack> group : displayGroups) {
                if (group.isEmpty()) continue;
                int index = (int) (time % group.size());
                ItemStack stack = group.get(index);

                gui.renderFakeItem(stack, xc - 8, yc + yOffset);
                gui.drawString(mc.font, adaptName(stack.getHoverName().getString()), xc + 10, yc + yOffset + 4, 0xFFFFFF, true);
                yOffset += 20;
            }
        }
    }

    private static boolean isSupported(IItemHandler handler, ItemStack stack) {
        for (int i = 0; i < handler.getSlots(); i++) {
            if (handler.isItemValid(i, stack)) {
                return true;
            }
        }
        return false;
    }

    private static String adaptName(String name) {
        return name.replace("Upgrade ", "").replace("Upgrade", "").trim();
    }

    public static boolean isBlockValid(BlockEntity blockEntity) {
        return blockEntity instanceof MachineBaseEntity;
    }
}
