package rewqazwas.minformax.custom.utility;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.SaplingBlock;
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
import rewqazwas.minformax.custom.ModTags;
import rewqazwas.minformax.custom.index.HolderClass;
import rewqazwas.minformax.custom.index.ModDataReloadListener;
import rewqazwas.minformax.custom.items.ModItems;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.BiConsumer;

public class Utils {
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

    public static ItemStack moveItem(Level level, BlockPos pos, ItemStack stack) {
        final ItemStack[] currentStack = {stack.copy()};

        forEachNeighborCapability(Capabilities.ItemHandler.BLOCK, level, pos, (handler, side) -> {
            if (!currentStack[0].isEmpty()) {
                currentStack[0] = ItemHandlerHelper.insertItemStacked(handler, currentStack[0], false);
            }
        });

        return currentStack[0];
    }

    public static int moveFluid(Level level, BlockPos pos, FluidStack sourceStack, int amount) {
        final int[] remaining = {amount};

        forEachNeighborCapability(Capabilities.FluidHandler.BLOCK, level, pos, (handler, side) -> {
            if (remaining[0] > 0) {
                FluidStack toFill = sourceStack.copy();

                // Set the amount to the smaller of:
                // 1. What we have left to give
                // 2. The total amount available in the source stack
                toFill.setAmount(Math.min(remaining[0], sourceStack.getAmount()));

                int filled = handler.fill(toFill, IFluidHandler.FluidAction.EXECUTE);
                remaining[0] -= filled;
            }
        });

        return remaining[0];
    }

    public static boolean canInsertAtLeastOne(Level level, BlockPos pos, ItemStack stack) {
        ItemStack toInsert = stack.copy();
        toInsert.setCount(1);

        boolean[] canInsert = {false};

        forEachNeighborCapability(Capabilities.ItemHandler.BLOCK, level, pos, (handler, side) -> {
            if (!canInsert[0]) {
                if (ItemHandlerHelper.insertItemStacked(handler, toInsert, true).isEmpty()) {
                    canInsert[0] = true;
                }
            }
        });

        return canInsert[0];
    }

    public static boolean canInsertAtLeastOne(Level level, BlockPos pos, FluidStack stack) {
        FluidStack toInsert = stack.copy();
        toInsert.setAmount(1);

        boolean[] canInsert = {false};

        forEachNeighborCapability(Capabilities.FluidHandler.BLOCK, level, pos, (handler, side) -> {
            if (!canInsert[0]) {
                if (handler.fill(toInsert, IFluidHandler.FluidAction.SIMULATE) > 0) {
                    canInsert[0] = true;
                }
            }
        });

        return canInsert[0];
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

    public static String simpleEnergyDisplay(int energy) {
        return getDelimeter(energy) + "FE";
    }

    public static String simpleEnergyDisplay(long energy) {
        return getDelimeter(energy) + "FE";
    }

    public static String simpleEnergyDisplay(int energy, int maxEnergy) {
        return getDelimeter(energy) + "FE / " + getDelimeter(maxEnergy) + "FE";
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
        public EnergyGenStorage(int capacity) {
            super(capacity, 0, capacity);
        }

        @Override
        public int receiveEnergy(int toReceive, boolean simulate) {
            return 0;
        }

        public void setEnergy(int value) {
            this.energy = Math.max(0, Math.min(getMaxEnergyStored(), value));
        }

        @Override
        public boolean canExtract() {
            return true;
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
}
