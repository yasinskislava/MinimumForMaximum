package rewqazwas.minformax.custom.utility;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import rewqazwas.minformax.custom.index.HolderClass;
import rewqazwas.minformax.custom.index.ModDataReloadListener;

import java.util.HashMap;
import java.util.List;

public class Utils {
    public static void warn(String text) {
        System.out.println("\u001B[33m" + text + "\u001B[0m");
    }
    public static void common(String text) {
        System.out.println("\u001B[32m" + text + "\u001B[0m");
    }

    public static String prettyName(String input) {
        if (input == null || input.isEmpty()) return "";
        int idx = input.lastIndexOf('.');
        String core = idx >= 0 ? input.substring(idx + 1) : input;
        core = core.replace('_', ' ').trim();
        if (core.isEmpty()) return "";

        StringBuilder out = new StringBuilder();
        String[] words = core.split("\\s+");
        for (int i = 0; i < words.length; i++) {
            String w = words[i];
            if (w.isEmpty()) continue;
            out.append(Character.toUpperCase(w.charAt(0)));
            if (w.length() > 1) out.append(w.substring(1).toLowerCase());
            if (i < words.length - 1) out.append(' ');
        }
        return out.toString();
    }

    public static HashMap<String, HolderClass> clearContent(List<String> keys, Level level) {
        var map = new HashMap<String, HolderClass>();
        if(!level.isClientSide()) {
            for (String key : keys) {
                if (ModDataReloadListener.MOB_DROPS.containsKey(key)) {
                    var value = ModDataReloadListener.MOB_DROPS.get(key);
                    map.put(key, value);
                }
            }
        }
        return map;
    }

    public static IItemHandler[] getItemHandlers(Level level, BlockPos blockPos) {
        return new IItemHandler[]{
                level.getCapability(Capabilities.ItemHandler.BLOCK, blockPos.above(), Direction.DOWN),
                level.getCapability(Capabilities.ItemHandler.BLOCK, blockPos.below(), Direction.UP),
                level.getCapability(Capabilities.ItemHandler.BLOCK, blockPos.north(), Direction.SOUTH),
                level.getCapability(Capabilities.ItemHandler.BLOCK, blockPos.south(), Direction.NORTH),
                level.getCapability(Capabilities.ItemHandler.BLOCK, blockPos.east(), Direction.WEST),
                level.getCapability(Capabilities.ItemHandler.BLOCK, blockPos.west(), Direction.EAST)
        };
    }

    public static IFluidHandler[] getFluidHandlers(Level level, BlockPos blockPos) {
        return new IFluidHandler[]{
                level.getCapability(Capabilities.FluidHandler.BLOCK, blockPos.above(), Direction.DOWN),
                level.getCapability(Capabilities.FluidHandler.BLOCK, blockPos.below(), Direction.UP),
                level.getCapability(Capabilities.FluidHandler.BLOCK, blockPos.north(), Direction.SOUTH),
                level.getCapability(Capabilities.FluidHandler.BLOCK, blockPos.south(), Direction.NORTH),
                level.getCapability(Capabilities.FluidHandler.BLOCK, blockPos.east(), Direction.WEST),
                level.getCapability(Capabilities.FluidHandler.BLOCK, blockPos.west(), Direction.EAST)
        };
    }

    public static IEnergyStorage[] getEnergyHandlers(Level level, BlockPos blockPos) {
        return new IEnergyStorage[]{
                level.getCapability(Capabilities.EnergyStorage.BLOCK, blockPos.above(), Direction.DOWN),
                level.getCapability(Capabilities.EnergyStorage.BLOCK, blockPos.below(), Direction.UP),
                level.getCapability(Capabilities.EnergyStorage.BLOCK, blockPos.north(), Direction.SOUTH),
                level.getCapability(Capabilities.EnergyStorage.BLOCK, blockPos.south(), Direction.NORTH),
                level.getCapability(Capabilities.EnergyStorage.BLOCK, blockPos.east(), Direction.WEST),
                level.getCapability(Capabilities.EnergyStorage.BLOCK, blockPos.west(), Direction.EAST)
        };
    }

    public static String simpleEnergyDisplay(int energy, int maxEnergy) {
        return getDelimeter(energy) + "FE" + "/" + getDelimeter(maxEnergy) + "FE";
    }

    public static String simpleEnergyDisplay(int energy) {
        return getDelimeter(energy) + "FE";
    }

    private static String getDelimeter(int energy) {
        if (energy >= 1_000_000_000) {
            return energy / 1_000_000_000 + "G";
        } else if (energy >= 1_000_000) {
            return energy / 1_000_000 + "M";
        } else if (energy >= 1_000) {
            return energy / 1_000 + "K";
        } else {
            return energy + "";
        }
    }

    public static int[] calculateLevel(int totalXP) {
        int level = 0;
        int currentXP = totalXP;
        int requiredXPForNextLevel = 0;

        while (currentXP >= getXPForLevel(level)) {
            currentXP -= getXPForLevel(level);
            level++;
        }

        requiredXPForNextLevel = getXPForLevel(level);

        return new int[]{level, currentXP, requiredXPForNextLevel};
    }

    private static int getXPForLevel(int level) {
        if (level < 16) {
            return 2 * level + 7;
        } else if (level < 31) {
            return 5 * level - 38;
        } else {
            return 9 * level - 158;
        }
    }
}
