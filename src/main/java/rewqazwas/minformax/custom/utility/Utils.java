package rewqazwas.minformax.custom.utility;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import rewqazwas.minformax.custom.blocks.MachineBaseEntity;
import rewqazwas.minformax.custom.index.HolderClass;
import rewqazwas.minformax.custom.index.ModDataReloadListener;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

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

    public static ItemStack moveItem(Level level, BlockPos pos, ItemStack stack) {
        return moveItem(getItemHandlers(level, pos), stack);
    }

    public static ItemStack moveItem(IItemHandler[] handlers, ItemStack stack) {
        for (IItemHandler handler : handlers) {
            if (handler == null) continue;
            stack = ItemHandlerHelper.insertItemStacked(handler, stack, false);
            if (stack.isEmpty()) return ItemStack.EMPTY;
        }
        return stack;
    }

    public static boolean canInsertAtLeastOne(Level level, BlockPos pos, ItemStack stack) {
        var toInsert = stack.copy();
        toInsert.setCount(1);
        for (IItemHandler handler : getItemHandlers(level, pos)) {
            if (handler == null) continue;
            if (ItemHandlerHelper.insertItemStacked(handler, toInsert, true).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public static boolean canInsertAtLeastOne(Level level, BlockPos pos, FluidStack stack) {
        var toInsert = stack.copy();
        toInsert.setAmount(1);
        for (IFluidHandler handler : getFluidHandlers(level, pos)) {
            if (handler == null) continue;
            if (handler.fill(toInsert, IFluidHandler.FluidAction.SIMULATE) > 0) {
                return true;
            }
        }
        return false;
    }

    public static boolean canInsertAtLeastOneComplex(Level level, BlockPos pos, List<ItemStack> stacks) {
        for (ItemStack stack : stacks) {
            if (canInsertAtLeastOne(level, pos, stack)) {
                return true;
            }
        }
        return false;
    }

    public static String simpleFluidDisplay(int amount) {
        if (amount < 1000) {
            return amount + "mb";
        }
        double amountInB = amount / 1000.0;
        return String.format(Locale.US, "%.1fB", amountInB);
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

    public static Class<?> getOriginalClass(Object obj) {
        Class<?> cls = obj.getClass();
        return cls.isAnonymousClass()
                ? cls.getInterfaces().length == 0 ? cls.getSuperclass() : cls.getInterfaces()[0]
                : cls;
    }

    public static boolean canPass(IItemHandler itemHandler, ItemStack stack) {
        return canPass(itemHandler, stack, itemHandler.getSlots());
    }

    public static boolean canPass(IItemHandler itemHandler, ItemStack stack, int size) {
        for(int i = 0; i < size; i++) {
            var currentStack = itemHandler.getStackInSlot(i);
            if (currentStack.isEmpty()) continue;
            if (Utils.getOriginalClass(stack.getItem()) == Utils.getOriginalClass(currentStack.getItem())) {
                return true;
            }
        }
        return false;
    }
}
