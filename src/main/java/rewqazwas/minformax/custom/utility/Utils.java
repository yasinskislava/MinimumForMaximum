package rewqazwas.minformax.custom.utility;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import rewqazwas.minformax.MinForMax;
import rewqazwas.minformax.custom.ModTags;
import rewqazwas.minformax.custom.blocks.MachineBaseEntity;
import rewqazwas.minformax.custom.index.HolderClass;
import rewqazwas.minformax.custom.index.ModDataReloadListener;
import rewqazwas.minformax.custom.items.ModItems;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.*;
import java.util.function.BiConsumer;

public class Utils {

    private static final String MOD_ID = MinForMax.MOD_ID;

    public static final ResourceLocation ENERGY_BAR = ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/gui/energy_bar.png");
    public static final ResourceLocation EMPTY_BAR = ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/gui/empty_bar.png");
    public static final ResourceLocation OVERLOAD_BAR = ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/gui/overload_bar.png");
    public static final ResourceLocation PROGRESS_BAR = ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/gui/progress_bar.png");
    public static final ResourceLocation UPGRADES_BAR = ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/gui/upgrades_bar.png");
    public static final ResourceLocation SLOT_TEXTURE = ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/gui/slot.png");
    public static final ResourceLocation GUI_TEXTURE = ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/gui/template.png");
    public static final ResourceLocation SIDE = ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/gui/side.png");
    public static final ResourceLocation CONSUMPTION = ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/gui/energy.png");
    public static final ResourceLocation SIDE_CONFIG = ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/gui/circuit.png");
    private static final ResourceLocation INFO = ResourceLocation.fromNamespaceAndPath(MOD_ID,"textures/item/analyzer.png");

    // Standard Sizes
    public static final int V_BAR_HEIGHT = 55;
    public static final int V_BAR_WIDTH = 7;
    public static final int H_BAR_WIDTH = 88;
    public static final int H_BAR_HEIGHT = 6;

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

    public static <T> void forEachNeighborCapability(BlockCapability<T, Direction> cap, Level level, BlockPos pos, BiConsumer<T, Direction> action) {
        for (Direction side : Direction.values()) {
            T handler = level.getCapability(cap, pos.relative(side), side.getOpposite());
            if (handler != null) {
                action.accept(handler, side);
            }
        }
    }

    /**
     * Initiates a network item transfer from a starting block position.
     */
    public static ItemStack moveItem(Level level, BlockPos pos, ItemStack stack, boolean[] enabledSides) {
        if (stack.isEmpty()) return stack;

        Set<BlockPos> visited = new HashSet<>();
        visited.add(pos);

        return routeItemThroughNetwork(level, pos, stack, enabledSides, visited);
    }

    /**
     * Core recursive method that handles network traversal and inventory insertion.
     */
    private static ItemStack routeItemThroughNetwork(Level level, BlockPos currentPos, ItemStack stack, boolean[] enabledSides, Set<BlockPos> visited) {
        ItemStack remaining = stack.copy();

        for (Direction side : Direction.values()) {
            if (!enabledSides[side.get3DDataValue()]) {
                continue;
            }

            BlockPos targetPos = currentPos.relative(side);
            if (visited.contains(targetPos)) {
                continue;
            }

            IItemHandler targetHandler = level.getCapability(Capabilities.ItemHandler.BLOCK, targetPos, side.getOpposite());
            if (targetHandler == null) {
                continue;
            }

            BlockEntity targetBE = level.getBlockEntity(targetPos);
            if (targetBE instanceof MachineBaseEntity machineBE) {
                visited.add(targetPos);

                boolean[] targetSides = machineBE.getEnabledSides();

                remaining = routeItemThroughNetwork(level, targetPos, remaining, targetSides, visited);
            } else {
                remaining = ItemHandlerHelper.insertItemStacked(targetHandler, remaining, false);
            }

            if (remaining.isEmpty()) {
                return ItemStack.EMPTY;
            }
        }

        return remaining;
    }

    /**
     * Initiates a network fluid transfer from a starting block position.
     * Returns the amount of fluid that COULD NOT be accepted anywhere.
     */
    public static int moveFluidNetwork(Level level, BlockPos pos, FluidStack fluidStack, int amount, boolean[] enabledSides) {
        if (fluidStack.isEmpty() || amount <= 0) return amount;

        Set<BlockPos> visited = new HashSet<>();
        visited.add(pos);

        return routeFluidThroughNetwork(level, pos, fluidStack, amount, enabledSides, visited);
    }

    /**
     * Core recursive method that handles fluid network traversal and fluid handler filling.
     */
    private static int routeFluidThroughNetwork(Level level, BlockPos currentPos, FluidStack fluidStack, int amount, boolean[] enabledSides, Set<BlockPos> visited) {
        int remaining = amount;

        for (Direction side : Direction.values()) {
            if (!enabledSides[side.get3DDataValue()]) {
                continue;
            }

            BlockPos targetPos = currentPos.relative(side);
            if (visited.contains(targetPos)) {
                continue;
            }

            IFluidHandler targetHandler = level.getCapability(Capabilities.FluidHandler.BLOCK, targetPos, side.getOpposite());
            if (targetHandler == null) {
                continue;
            }

            BlockEntity targetBE = level.getBlockEntity(targetPos);
            if (targetBE instanceof MachineBaseEntity machineBE) {
                visited.add(targetPos);
                boolean[] targetSides = machineBE.getEnabledSides();

                remaining = routeFluidThroughNetwork(level, targetPos, fluidStack, remaining, targetSides, visited);
            } else {
                FluidStack toFill = fluidStack.copy();
                toFill.setAmount(remaining);
                int filled = targetHandler.fill(toFill, IFluidHandler.FluidAction.EXECUTE);
                remaining -= filled;
            }

            if (remaining <= 0) {
                return 0;
            }
        }

        return remaining;
    }

    /**
     * Network-aware pre-production capability check for fluids.
     */
    public static boolean canInsertAtLeastOneNetwork(Level level, BlockPos pos, FluidStack stack, boolean[] enabledSides) {
        if (stack.isEmpty()) return false;
        FluidStack testStack = stack.copy();
        testStack.setAmount(1); // Simulate with 1mB

        Set<BlockPos> visited = new HashSet<>();
        visited.add(pos);

        return simulateFluidRouteThroughNetwork(level, pos, testStack, enabledSides, visited);
    }

    private static boolean simulateFluidRouteThroughNetwork(Level level, BlockPos currentPos, FluidStack stack, boolean[] enabledSides, Set<BlockPos> visited) {
        for (Direction side : Direction.values()) {
            if (!enabledSides[side.get3DDataValue()]) {
                continue;
            }

            BlockPos targetPos = currentPos.relative(side);
            if (visited.contains(targetPos)) {
                continue;
            }

            IFluidHandler targetHandler = level.getCapability(Capabilities.FluidHandler.BLOCK, targetPos, side.getOpposite());
            if (targetHandler == null) {
                continue;
            }

            BlockEntity targetBE = level.getBlockEntity(targetPos);
            if (targetBE instanceof MachineBaseEntity machineBE) {
                visited.add(targetPos);
                boolean[] targetSides = machineBE.getEnabledSides();

                if (simulateFluidRouteThroughNetwork(level, targetPos, stack, targetSides, visited)) {
                    return true;
                }
            } else {
                // It's an external tank/pipe: Simulate filling 1mB
                if (targetHandler.fill(stack, IFluidHandler.FluidAction.SIMULATE) > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Network-aware check to see if an item stack can be inserted anywhere in the network.
     */
    public static boolean canInsertAtLeastOneNetwork(Level level, BlockPos pos, ItemStack stack, boolean[] enabledSides) {
        if (stack.isEmpty()) return false;
        ItemStack testStack = stack.copy();
        testStack.setCount(1);

        Set<BlockPos> visited = new HashSet<>();
        visited.add(pos);

        return simulateRouteThroughNetwork(level, pos, testStack, enabledSides, visited);
    }

    private static boolean simulateRouteThroughNetwork(Level level, BlockPos currentPos, ItemStack stack, boolean[] enabledSides, Set<BlockPos> visited) {
        for (Direction side : Direction.values()) {
            if (!enabledSides[side.get3DDataValue()]) {
                continue;
            }

            BlockPos targetPos = currentPos.relative(side);
            if (visited.contains(targetPos)) {
                continue;
            }

            IItemHandler targetHandler = level.getCapability(Capabilities.ItemHandler.BLOCK, targetPos, side.getOpposite());
            if (targetHandler == null) {
                continue;
            }

            BlockEntity targetBE = level.getBlockEntity(targetPos);
            if (targetBE instanceof MachineBaseEntity machineBE) {
                visited.add(targetPos);
                boolean[] targetSides = machineBE.getEnabledSides();

                if (simulateRouteThroughNetwork(level, targetPos, stack, targetSides, visited)) {
                    return true;
                }
            } else {
                if (ItemHandlerHelper.insertItemStacked(targetHandler, stack, true).isEmpty()) {
                    return true;
                }
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

    public static String simpleEnergyDisplay(int energy) {
        return getDelimeter(energy) + "FE";
    }

    public static String simpleEnergyDisplay(long energy) {
        return getDelimeter(energy) + "FE";
    }

    public static String simpleEnergyDisplay(int energy, int maxEnergy) {
        return getDelimeter(energy) + "FE / " + getDelimeter(maxEnergy) + "FE";
    }

    public static String simpleEnergyDisplay(long energy, long maxEnergy) {
        return getDelimeter(energy) + "FE / " + getDelimeter(maxEnergy) + "FE";
    }

    private static final String[] SUFFIXES = {"", "k", "M", "G", "T", "P", "E"};

    public static String formatNumber(long number) {
        if (number < 1000) return String.valueOf(number);

        int exp = (int) (Math.log(number) / Math.log(1000));
        exp = Math.min(exp, SUFFIXES.length - 1);

        double value = number / Math.pow(1000, exp);

        DecimalFormat df = new DecimalFormat("#.#");
        return df.format(value) + SUFFIXES[exp];
    }

    public static String getDelimeter(int value) {
        if (value >= 1_000_000_000) {
            return formatToThreeDigits(value / 1_000_000_000.0, "G");
        } else if (value >= 1_000_000) {
            return formatToThreeDigits(value / 1_000_000.0, "M");
        } else if (value >= 1_000) {
            return formatToThreeDigits(value / 1_000.0, "K");
        } else {
            return String.valueOf(value);
        }
    }

    public static String getDelimeter(long value) {
        if (value >= 1_000_000_000_000_000_000L) {
            return formatToThreeDigits(value / 1_000_000_000_000_000_000.0, "E");
        } else if (value >= 1_000_000_000_000_000L) {
            return formatToThreeDigits(value / 1_000_000_000_000_000.0, "P");
        } else if (value >= 1_000_000_000_000L) {
            return formatToThreeDigits(value / 1_000_000_000_000.0, "T");
        } else if (value >= 1_000_000_000L) {
            return formatToThreeDigits(value / 1_000_000_000.0, "G");
        } else if (value >= 1_000_000L) {
            return formatToThreeDigits(value / 1_000_000.0, "M");
        } else if (value >= 1_000L) {
            return formatToThreeDigits(value / 1_000.0, "K");
        } else {
            return String.valueOf(value);
        }
    }

    private static String formatToThreeDigits(double value, String unit) {
        if (value >= 100) return String.format("%.0f%s", value, unit);
        if (value >= 10)  return String.format("%.1f%s", value, unit);
        return String.format("%.2f%s", value, unit);
    }

    public record XpInfo(int level, long currentXp, long xpForNext) {}

    public static XpInfo calculateLevel(long totalXP) {
        double level;

        if (totalXP <= 352) { // Level 0-16
            level = Math.sqrt(totalXP + 9) - 3;
        } else if (totalXP <= 1507) { // Level 16-31
            level = (81 + Math.sqrt(40 * totalXP - 7839)) / 10.0;
        } else { // Level 31+
            level = (325 + Math.sqrt(72 * totalXP - 54215)) / 18.0;
        }

        int intLevel = (int) Math.floor(level);

        long xpAtStartOfLevel = getTotalXPForLevel(intLevel);
        long currentXPInLevel = totalXP - xpAtStartOfLevel;
        long requiredForNext = getXPForLevel(intLevel);

        return new XpInfo(intLevel, currentXPInLevel, requiredForNext);
    }

    /**
     * Returns the total accumulated XP required to reach a specific level from 0.
     * This is the integral of the linear getXPForLevel functions.
     */
    private static long getTotalXPForLevel(int level) {
        if (level <= 16) {
            return (long) level * level + 6L * level;
        } else if (level <= 31) {
            // 2.5L^2 - 40.5L + 360
            return (long) (2.5 * level * level - 40.5 * level + 360);
        } else {
            // 4.5L^2 - 162.5L + 2220
            return (long) (4.5 * level * level - 162.5 * level + 2220);
        }
    }

    private static long getXPForLevel(int level) {
        if (level < 16) {
            return 2L * level + 7;
        } else if (level < 31) {
            return 5L * level - 38;
        } else {
            return 9L * level - 158;
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

    public static class EnergyGenStorage extends EnergyStorage {
        private long currentEnergy;
        private long maxCapacity;

        public EnergyGenStorage(long capacity) {
            super((int) Math.min(capacity, Integer.MAX_VALUE), 0, (int) Math.min(capacity, Integer.MAX_VALUE));
            this.maxCapacity = capacity;
            this.currentEnergy = 0;
        }

        @Override
        public int getEnergyStored() {
            return (int) Math.min(currentEnergy, Integer.MAX_VALUE);
        }

        public long getLongEnergyStored() {
            return currentEnergy;
        }

        @Override
        public int getMaxEnergyStored() {
            return (int) Math.min(maxCapacity, Integer.MAX_VALUE);
        }

        public long getMaxCapacityLong() {
            return maxCapacity;
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            // Generators do not receive energy from external sources
            return 0;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            long energyExtracted = Math.min(currentEnergy, maxExtract);
            if (!simulate) {
                currentEnergy -= energyExtracted;
            }
            return (int) Math.min(energyExtracted, Integer.MAX_VALUE);
        }

        public void setEnergy(long value) {
            this.currentEnergy = Math.max(0, Math.min(maxCapacity, value));
        }

        public void addEnergy(long energy) {
            this.currentEnergy = Math.min(this.currentEnergy + energy, maxCapacity);
        }

        @Override
        public boolean canExtract() {
            return true;
        }

        @Override
        public boolean canReceive() {
            return false; // Generators do not receive energy
        }
    }

    public static class SingleItemHandler extends ItemStackHandler {
        public SingleItemHandler(int size) {
            super(size);
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        protected int getStackLimit(int slot, ItemStack stack) {
            return 1;
        }
    }

    public static class UpgradeItemHandler extends SingleItemHandler {
        public UpgradeItemHandler(int size) {
            super(size);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if(Utils.canPass(this, stack)){
                return stack;
            }
            return super.insertItem(slot, stack, simulate);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stack.is(ModTags.SPEED_UPGRADES)
                    || stack.is(ModTags.PROCESSING_UPGRADES) && !(stack.getItem() == ModItems.ULTIMATE_PROCESSING_UPGRADE.get());
        }
    }

    public static List<ItemStack> getFarmerDrops(Level level, ItemStack seedStack) {
        Item seedItem = seedStack.getItem();
        ResourceLocation registryName = BuiltInRegistries.ITEM.getKey(seedItem);
        String path = registryName.getPath();

        // 1. Mystical Agriculture: Always produce ONLY Essence
        if (registryName.getNamespace().equals("mysticalagriculture") && path.endsWith("_seeds")) {
            Item essence = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(
                    "mysticalagriculture",
                    path.replace("_seeds", "_essence")
            ));
            if (essence != Items.AIR) return List.of(new ItemStack(essence));
        }

        // 2. Special Exceptions: Pumpkin and Melon
        if (seedItem == Items.PUMPKIN_SEEDS) return List.of(new ItemStack(Items.PUMPKIN));
        if (seedItem == Items.MELON_SEEDS) return List.of(new ItemStack(Items.MELON));

        // 3. Handle Saplings (Tree logic)
        if (seedItem instanceof BlockItem blockItem && blockItem.getBlock() instanceof SaplingBlock) {
            return getSaplingDrops(level, seedItem);
        }

        // 4. Croptopia tea seeds special handling
        if (registryName.getNamespace().equals("croptopia") && path.equals("tea_seeds")) {
             Item teaLeaves = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("croptopia", "tea_leaves"));
             if (teaLeaves != Items.AIR) return List.of(new ItemStack(teaLeaves), new ItemStack(seedItem));
        }

        // 5. Generic Fallback: Try to get drops via Loot Tables
        if (seedItem instanceof BlockItem blockItem) {
            Block block = blockItem.getBlock();
            BlockState fullyGrown = getFullyGrownState(block);

            if (fullyGrown != null && level instanceof ServerLevel serverLevel) {
                LootParams.Builder builder = new LootParams.Builder(serverLevel)
                        .withParameter(LootContextParams.ORIGIN, Vec3.ZERO)
                        .withParameter(LootContextParams.TOOL, ItemStack.EMPTY);

                List<ItemStack> drops = fullyGrown.getDrops(builder);
                if (!drops.isEmpty()) return drops;
            }
        }

        // 6. Name Guessing (Client/Fallback) for JEI
        Set<String> candidates = new LinkedHashSet<>();
        if (path.endsWith("_seeds")) candidates.add(path.replace("_seeds", ""));
        else if (path.endsWith("_seed")) candidates.add(path.replace("_seed", ""));
        else if (path.startsWith("seeds_")) candidates.add(path.replace("seeds_", ""));
        else if (path.startsWith("seed_")) candidates.add(path.replace("seed_", ""));
        
        if (path.contains("seeds")) candidates.add(path.replace("seeds", "").replace("__", "_"));
        if (path.contains("seed")) candidates.add(path.replace("seed", "").replace("__", "_"));

        for (String cropName : candidates) {
            if (cropName.startsWith("_")) cropName = cropName.substring(1);
            if (cropName.endsWith("_")) cropName = cropName.substring(0, cropName.length() - 1);
            if (cropName.isEmpty()) continue;

            Item cropItem = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(registryName.getNamespace(), cropName));
            if (cropItem != Items.AIR && cropItem != seedItem) {
                return List.of(new ItemStack(cropItem), new ItemStack(seedItem));
            }
        }
        
        // 7. Last resort: Fuzzy search in namespace
        for (String cropName : candidates) {
             String cleanName = cropName;
             if (cleanName.startsWith("_")) cleanName = cleanName.substring(1);
             if (cleanName.endsWith("_")) cleanName = cleanName.substring(0, cleanName.length() - 1);
             if (cleanName.isEmpty()) continue;

             for(Item item : BuiltInRegistries.ITEM) {
                 ResourceLocation r = BuiltInRegistries.ITEM.getKey(item);
                 if (r.getNamespace().equals(registryName.getNamespace()) && item != seedItem) {
                     String p = r.getPath();
                     if (p.equals(cleanName) || p.equals(cleanName + "_item") || p.equals(cleanName + "_fruit") || p.equals(cleanName + "_crop")) {
                          return List.of(new ItemStack(item), new ItemStack(seedItem));
                     }
                 }
             }
        }

        // Default: Return the seed itself if no other logic found
        return List.of(new ItemStack(seedItem));
    }

    public record EssenceRecipeInfo(int inputCount, ItemStack output) {}

    public static boolean isCompressibleEssence(Item item) {
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(item);
        return key.getNamespace().equals("mysticalagriculture") && key.getPath().endsWith("_essence");
    }

    /**
     * Optimized compression that uses a pre-calculated cache.
     */
    public static List<ItemStack> applyCompression(List<ItemStack> drops, Map<Item, EssenceRecipeInfo> recipeCache) {
        List<ItemStack> resultDrops = new ArrayList<>();
        for (ItemStack stack : drops) {
            Item item = stack.getItem();

            if (isCompressibleEssence(item) && recipeCache.containsKey(item)) {
                EssenceRecipeInfo recipe = recipeCache.get(item);
                if (!recipe.output().is(Items.AIR)) {
                    int totalEssence = stack.getCount();
                    int craftCount = totalEssence / recipe.inputCount();
                    int leftover = totalEssence % recipe.inputCount();

                    if (craftCount > 0) {
                        ItemStack output = recipe.output().copy();
                        output.setCount(output.getCount() * craftCount);
                        resultDrops.add(output);
                    }

                    if (leftover > 0) {
                        ItemStack remainder = stack.copy();
                        remainder.setCount(leftover);
                        resultDrops.add(remainder);
                    }
                    continue;
                }
            }
            resultDrops.add(stack);
        }
        return resultDrops;
    }

    public static EssenceRecipeInfo getEssenceRecipe(ServerLevel level, Item essence) {
        ItemStack essenceStack = new ItemStack(essence);
        for (RecipeHolder<CraftingRecipe> holder : level.getRecipeManager().getAllRecipesFor(RecipeType.CRAFTING)) {
            CraftingRecipe recipe = holder.value();
            List<Ingredient> ingredients = recipe.getIngredients();
            if (ingredients.isEmpty()) continue;

            int inputCount = 0;
            boolean allIngredientsMatch = true;
            for (Ingredient ing : ingredients) {
                if (ing.isEmpty()) continue;
                if (ing.test(essenceStack)) {
                    inputCount++;
                } else {
                    allIngredientsMatch = false;
                    break;
                }
            }
            if (allIngredientsMatch && inputCount > 0) {
                return new EssenceRecipeInfo(inputCount, recipe.getResultItem(level.registryAccess()));
            }
        }
        return null;
    }

    public static List<ItemStack> applyCompression(Level level, List<ItemStack> drops) {
        if (!(level instanceof ServerLevel serverLevel)) return drops;

        // This is the "expensive" way, preserved for generic calls
        Map<Item, EssenceRecipeInfo> temporaryCache = new HashMap<>();
        for(ItemStack stack : drops) {
            Item item = stack.getItem();
            if(isCompressibleEssence(item)) {
                EssenceRecipeInfo info = getEssenceRecipe(serverLevel, item);
                if(info != null) temporaryCache.put(item, info);
            }
        }
        return applyCompression(drops, temporaryCache);
    }

    /**
     * Finds the max-age BlockState for a crop.
     */
    public static BlockState getFullyGrownState(Block block) {
        if (block instanceof CropBlock crop) return crop.getStateForAge(crop.getMaxAge());

        return block.getStateDefinition().getProperties().stream()
                .filter(p -> p instanceof IntegerProperty && p.getName().equals("age"))
                .map(p -> (IntegerProperty) p)
                .findFirst()
                .map(ageProp -> block.defaultBlockState().setValue(ageProp, Collections.max(ageProp.getPossibleValues())))
                .orElse(null);
    }

    public static List<ItemStack> getSaplingDrops(Item sapling) {
        return getSaplingDrops(null, sapling);
    }

    /**
     * Simulates log and leaf drops for a sapling based on its registry name.
     */
    public static List<ItemStack> getSaplingDrops(Level level, Item sapling) {
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(sapling);
        String path = key.getPath();
        List<ItemStack> drops = new ArrayList<>();

        // Randomize log count: 2 or 3
        int logCount = java.util.concurrent.ThreadLocalRandom.current().nextInt(2, 4);

        // Special Case: Mangrove (Propagule)
        if (path.contains("mangrove_propagule")) {
            drops.add(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:mangrove_log")), logCount));
            drops.add(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:mangrove_leaves")), 1));
            return drops;
        }

        // Special Case: Azalea
        if (path.contains("azalea")) {
            // Azaleas produce Oak logs in vanilla
            drops.add(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:oak_log")), logCount));
            drops.add(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:azalea_leaves")), 1));
            return drops;
        }

        if (key.getNamespace().equals("croptopia") && path.endsWith("_sapling")) {
            String fruitName = path.replace("_sapling", "");
            Item fruit = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("croptopia", fruitName));

            if (fruit != Items.AIR) {
                drops.add(new ItemStack(fruit, 1));
            }

            drops.add(new ItemStack(Items.OAK_LOG, logCount));
            drops.add(new ItemStack(Items.OAK_LEAVES, 1));

            return drops;
        }

        // Try getting configured feature from TreeGrower (advanced reflection)
        if (level != null && sapling instanceof BlockItem blockItem && blockItem.getBlock() instanceof SaplingBlock saplingBlock) {
            try {
                Field growerField = SaplingBlock.class.getDeclaredField("treeGrower");
                growerField.setAccessible(true);
                TreeGrower grower = (TreeGrower) growerField.get(saplingBlock);
                
                if(grower != null) {
                    java.lang.reflect.Method getFeatureMethod = TreeGrower.class.getDeclaredMethod("getConfiguredFeature", RandomSource.class, boolean.class);
                    getFeatureMethod.setAccessible(true);
                    Optional<ResourceKey<ConfiguredFeature<?, ?>>> featureKeyOpt = (Optional<ResourceKey<ConfiguredFeature<?, ?>>>) getFeatureMethod.invoke(grower, RandomSource.create(), false);

                    if (featureKeyOpt.isPresent()) {
                        ResourceKey<ConfiguredFeature<?, ?>> featureKey = featureKeyOpt.get();
                        Optional<ConfiguredFeature<?, ?>> featureOpt = level.registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE).getOptional(featureKey);

                        if (featureOpt.isPresent()) {
                            ConfiguredFeature<?, ?> feature = featureOpt.get();
                            if (feature.config() instanceof TreeConfiguration treeConfig) {
                                BlockState logState = treeConfig.trunkProvider.getState(RandomSource.create(), BlockPos.ZERO);
                                BlockState leafState = treeConfig.foliageProvider.getState(RandomSource.create(), BlockPos.ZERO);

                                if (!logState.isAir()) drops.add(new ItemStack(logState.getBlock().asItem(), logCount));
                                if (!leafState.isAir()) drops.add(new ItemStack(leafState.getBlock().asItem(), 1));

                                if (!drops.isEmpty()) return drops;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                // Ignore reflection errors and continue to fallback
            }
        }

        // Fallback Name Substitution Logic
        String baseName = path.replace("_sapling", "");
        if (baseName.endsWith("_tree")) baseName = baseName.replace("_tree", "");
        
        List<String> logSuffixes = List.of("_log", "_wood", "_stem", "_hyphae");
        List<String> leafSuffixes = List.of("_leaves", "_wart_block");
        
        // 1. Try exact reconstruction
        for (String suffix : logSuffixes) {
            Item log = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(key.getNamespace(), baseName + suffix));
            if (log != Items.AIR) {
                 drops.add(new ItemStack(log, logCount));
                 break;
            }
        }
        
        for (String suffix : leafSuffixes) {
            Item leaf = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(key.getNamespace(), baseName + suffix));
            if (leaf != Items.AIR) {
                drops.add(new ItemStack(leaf, 1));
                break;
            }
        }

        boolean hasLog = drops.stream().anyMatch(d -> d.is(ItemTags.LOGS));
        boolean hasLeaf = drops.stream().anyMatch(d -> d.is(ItemTags.LEAVES));

        if (hasLog && hasLeaf) return drops;

        // 2. Fallback: Check if we missed logs but have leaves or vice versa, or missed both.
        // Scan namespace for items containing baseName and having tags.
        if (!hasLog || !hasLeaf) {
            for (Item item : BuiltInRegistries.ITEM) {
                ResourceLocation itemKey = BuiltInRegistries.ITEM.getKey(item);
                if (itemKey.getNamespace().equals(key.getNamespace()) && itemKey.getPath().contains(baseName)) {
                     ItemStack stack = new ItemStack(item);
                     
                     // Add log if missing
                     if (!hasLog && stack.is(ItemTags.LOGS)) {
                          drops.add(new ItemStack(item, logCount));
                          hasLog = true;
                     }
                     // Add leaf if missing
                     if (!hasLeaf && stack.is(ItemTags.LEAVES)) {
                          drops.add(new ItemStack(item, 1));
                          hasLeaf = true;
                     }
                     if (hasLog && hasLeaf) break;
                }
            }
        }

        return drops.isEmpty() ? List.of(new ItemStack(sapling)) : drops;
    }

    public void drawMirroredX(GuiGraphics guiGraphics, ResourceLocation texture, int x, int y, int width, int height) {
        guiGraphics.pose().pushPose();

        // Move to the right edge of the intended area
        guiGraphics.pose().translate(x + width, y, 0);

        // Flip the X coordinates
        guiGraphics.pose().scale(-1.0F, 1.0F, 1.0F);

        // Draw at 0,0 relative to the new flipped origin
        guiGraphics.blit(texture, 0, 0, 0, 0, width, height, width, height);

        guiGraphics.pose().popPose();
    }

    public void drawMirroredY(GuiGraphics guiGraphics, ResourceLocation texture, int x, int y, int width, int height) {
        guiGraphics.pose().pushPose();

        // Move to the bottom edge of the intended area
        guiGraphics.pose().translate(x, y + height, 0);

        // Flip the Y coordinates
        guiGraphics.pose().scale(1.0F, -1.0F, 1.0F);

        // Draw at 0,0 relative to the new flipped origin
        guiGraphics.blit(texture, 0, 0, 0, 0, width, height, width, height);

        guiGraphics.pose().popPose();
    }

    /**
     * Draws the main background and the standard energy bar on the right.
     */
    public static void drawTemplate(GuiGraphics guiGraphics, int x, int y, int imageWidth, int imageHeight, ResourceLocation guiTexture, int energyLevel) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        // 1. Main Background
        guiGraphics.blit(guiTexture, x, y, 0, 0, imageWidth, imageHeight);

        // 2. Standard Energy Bar (Right side)
        drawVerticalBar(guiGraphics, x + 163, y + 13, ENERGY_BAR, energyLevel);
    }

    /**
     * Internal helper to draw vertical bars (Energy, Overload).
     */
    public static void drawVerticalBar(GuiGraphics guiGraphics, int x, int y, ResourceLocation fillTexture, int level) {
        // Draw the background empty bar
        guiGraphics.blit(EMPTY_BAR, x, y, 0, 0, V_BAR_WIDTH, V_BAR_HEIGHT, V_BAR_WIDTH, V_BAR_HEIGHT);

        // Draw the fill level
        int diff = V_BAR_HEIGHT - level;
        guiGraphics.blit(fillTexture, x, y + diff, 0, diff, V_BAR_WIDTH, level, V_BAR_WIDTH, V_BAR_HEIGHT);
    }

    /**
     * Draws the overload bar on the left.
     */
    public static void drawOverload(GuiGraphics guiGraphics, int x, int y, int overloadLevel) {
        drawVerticalBar(guiGraphics, x + 6, y + 13, OVERLOAD_BAR, overloadLevel);
    }

    /**
     * Draws the horizontal progress bar at the bottom center.
     */
    public static void drawProgressBar(GuiGraphics guiGraphics, int x, int y, int progress) {
        guiGraphics.blit(EMPTY_BAR, x + 44, y + 70, 0, 0, H_BAR_WIDTH, H_BAR_HEIGHT, H_BAR_WIDTH, H_BAR_HEIGHT);
        guiGraphics.blit(PROGRESS_BAR, x + 44, y + 70, 0, 0, progress, H_BAR_HEIGHT, H_BAR_WIDTH, H_BAR_HEIGHT);
    }

    /**
     * Draws the upgrade tab extension.
     */
    public static void drawUpgradeTab(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.blit(UPGRADES_BAR, x - 24, y + 3, 0, 0, 24, 80, 24, 80);
    }

    /**
     * Draws the side extension.
     */
    public static void drawSideTab(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.blit(SIDE, x - 24, y, 0, 0, 26, 24, 26, 24);
    }

    /**
     * Draws the energy consumption side tab extension.
     */
    public static void drawConsumptionTab(GuiGraphics guiGraphics, int x, int y) {
        drawSideTab(guiGraphics, x, y + 142);
        guiGraphics.blit(CONSUMPTION, x - 19, y + 146, 0, 0, 16, 16, 16, 16);
    }

    /**
     * Draws the side configuration tab extension.
     */
    public static void drawIOTab(GuiGraphics guiGraphics, int x, int y) {
        drawSideTab(guiGraphics, x, y + 142 - 56);
        guiGraphics.blit(SIDE_CONFIG, x - 19, y + 146 - 56, 0, 0, 16, 16, 16, 16);
    }

    /**
     * Draws the information side tab extension.
     */
    public static void drawInfoTab(GuiGraphics guiGraphics, int x, int y) {
        drawSideTab(guiGraphics, x, y + 142 - 28);
        //guiGraphics.blit(CONSUMPTION, x - 19, y + 146 - 28, 0, 0, 16, 16, 16, 16);
    }

    /**
     * Draws the upgrade info side tab extension.
     */
    public static void drawUpgradeInfoTab(GuiGraphics guiGraphics, int x, int y) {
        drawSideTab(guiGraphics, x, y - 19);
        guiGraphics.blit(INFO, x - 19, y - 15, 0, 0, 16, 16, 16, 16);
    }

    /**
     * Draws a single item slot background.
     */
    public static void drawSlot(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.blit(SLOT_TEXTURE, x, y, 0, 0, 18, 18, 18, 18);
    }

    public static boolean isMouseOver(double mouseX, double mouseY, int x, int y, int sizeX, int sizeY) {
        return mouseX >= x && mouseX <= x + sizeX && mouseY >= y && mouseY <= y + sizeY;
    }

    public static String convertKey(String rawName) {
        String trimmed = rawName.substring(7);
        trimmed = trimmed.replaceFirst("\\.", ":");
        return trimmed;
    }

    /**
     * Renders a component bounded to a specific width. If the text width exceeds maxWidth,
     * it scrolls continuously inside a scissored boundary box.
     */
    public static void renderScrollingText(GuiGraphics guiGraphics, net.minecraft.client.gui.Font font, Component text, int x, int y, int maxWidth, int textColor, float scale) {
        int textWidth = font.width(text);
        int maxScaledWidth = (int) (maxWidth / scale);

        if (textWidth <= maxScaledWidth) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(x, y, 100);
            guiGraphics.pose().scale(scale, scale, 1.0f);
            guiGraphics.drawCenteredString(font, text, maxScaledWidth / 2, 0, textColor);
            guiGraphics.pose().popPose();
            return;
        }

        guiGraphics.enableScissor(x, y, x + maxWidth, y + (int)(font.lineHeight * scale));

        int speedModifier = 30;
        int gap = 30;
        int totalLoopLength = textWidth + gap;

        long time = net.minecraft.Util.getMillis() / speedModifier;
        int offset = (int) (time % totalLoopLength);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x, y, 100);
        guiGraphics.pose().scale(scale, scale, 1.0f);
        guiGraphics.drawString(font, text, -offset, 0, textColor);
        guiGraphics.drawString(font, text, -offset + totalLoopLength, 0, textColor);

        guiGraphics.pose().popPose();

        guiGraphics.disableScissor();
    }
}
