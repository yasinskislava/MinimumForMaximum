package rewqazwas.minformax.custom.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;
import rewqazwas.minformax.custom.ModBlockEntities;
import rewqazwas.minformax.custom.ModTags;
import rewqazwas.minformax.custom.items.ModItems;
import rewqazwas.minformax.custom.items.upgrades.FortuneUpgrade;
import rewqazwas.minformax.custom.items.upgrades.ProcessingUpgrade;
import rewqazwas.minformax.custom.items.upgrades.SpeedUpgrade;
import rewqazwas.minformax.custom.items.upgrades.UpgradeItem;
import rewqazwas.minformax.custom.utility.Utils;
import rewqazwas.minformax.screen.custom.FarmerMenu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class FarmerBlockEntity extends MachineBaseEntity implements MenuProvider {
    public final ItemStackHandler upgradeHandler = new ItemStackHandler(4) {
        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stack.getItem() instanceof UpgradeItem
                    && !Utils.canPass(itemHandler, stack)
                    && (stack.is(ModTags.PROCESSING_UPGRADES) && !stack.is(ModItems.ULTIMATE_PROCESSING_UPGRADE)
                    || stack.is(ModTags.SPEED_UPGRADES)
                    || stack.is(ModItems.WATERING_UPGRADE)
                    || stack.is(ModTags.FORTUNE_UPGRADES));
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if(Utils.canPass(this, stack)){
                return stack;
            }
            return super.insertItem(slot, stack, simulate);
        }
    };

    public final ItemStackHandler itemHandler = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if(level != null && !level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            Block cropBlock = stack.getItem() instanceof BlockItem blockItem ? blockItem.getBlock() : null;
            return stack.is(Tags.Items.SEEDS) || stack.is(Tags.Items.CROPS) || stack.is(ModTags.FARMER_SELF_SUSTAINING) || cropBlock instanceof FlowerBlock || cropBlock instanceof SaplingBlock;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        protected int getStackLimit(int slot, ItemStack stack) {
            return 1;
        }
    };

    public final EnergyStorage energyHandler = new EnergyStorage(40_960_000) {
        @Override
        public int receiveEnergy(int toReceive, boolean simulate) {
            setChanged();
            return super.receiveEnergy(toReceive, simulate);
        }

        @Override
        public int extractEnergy(int toExtract, boolean simulate) {
            setChanged();
            return super.extractEnergy(toExtract, simulate);
        }
    };

    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> FarmerBlockEntity.this.process;
                case 1 -> FarmerBlockEntity.this.maxProcess;
                case 2 -> FarmerBlockEntity.this.energyHandler.getEnergyStored();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> FarmerBlockEntity.this.process = value;
                case 1 -> FarmerBlockEntity.this.maxProcess = value;
                case 2 -> FarmerBlockEntity.this.energyHandler.receiveEnergy(value, false);
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    };

    private int process = 0;
    private int maxProcess = 256;
    private int duration = maxProcess;

    public FarmerBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.FARMER_BE.get(), pos, blockState);
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    public void drops() {
        if (this.level == null) return;
        SimpleContainer inv = new SimpleContainer(upgradeHandler.getSlots() + itemHandler.getSlots());
        for(int i = 0; i < upgradeHandler.getSlots(); i++){
            inv.setItem(i, upgradeHandler.getStackInSlot(i));
        }
        for(int i = 0; i < itemHandler.getSlots(); i++){
            inv.setItem(i + upgradeHandler.getSlots(), itemHandler.getStackInSlot(i));
        }
        Containers.dropContents(this.level, this.worldPosition, inv);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", itemHandler.serializeNBT(registries));
        tag.put("upgrades", upgradeHandler.serializeNBT(registries));
        tag.putInt("farmer.process", this.process);
        tag.putInt("farmer.max_process", this.maxProcess);
        tag.putInt("farmer.energy", this.energyHandler.getEnergyStored());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        itemHandler.deserializeNBT(registries, tag.getCompound("inventory"));
        upgradeHandler.deserializeNBT(registries, tag.getCompound("upgrades"));
        this.process = tag.getInt("farmer.process");
        this.maxProcess = tag.getInt("farmer.max_process");
        this.energyHandler.receiveEnergy(tag.getInt("farmer.energy"), false);
    }

    public void tick(Level level, BlockPos blockPos, BlockState blockState, FarmerBlockEntity blockEntity) {
        if(level.isClientSide()) return;

        var sourceStack = itemHandler.getStackInSlot(0);
        if(sourceStack.isEmpty()) {
            process = 0;
            setChanged(level, blockPos, blockState);
            return;
        }

        int speedModifier = 1;
        int stackMultiplier = 1;
        int fortuneMultiplier = 1;
        int wateringMultiplier = 1;

        for(int i = 0; i < upgradeHandler.getSlots(); i++) {
            var stack = upgradeHandler.getStackInSlot(i);
            var upgrade = stack.getItem();
            if(upgrade instanceof SpeedUpgrade speedUpgrade) {
                speedModifier = speedUpgrade.getModifier();
            } else if(upgrade instanceof ProcessingUpgrade processingUpgrade) {
                stackMultiplier = processingUpgrade.getMultiplier();
            } else if(upgrade instanceof FortuneUpgrade fortuneUpgrade) {
                fortuneMultiplier = fortuneUpgrade.getMultiplier();
            } else if(stack.is(ModItems.WATERING_UPGRADE)) {
                wateringMultiplier = wateringChance();
            }
        }

        int energyCost = stackMultiplier;
        if (speedModifier == 9999) {
            energyCost = 0;
        }
        if (energyHandler.getEnergyStored() < energyCost) return;
        var drops = getPotentialDrops(((ServerLevel) level), sourceStack);
        if (!Utils.canInsertAtLeastOneComplex(level, blockPos, drops)) return;
        energyHandler.extractEnergy(energyCost, false);

        if(process == 0){
            maxProcess = Math.max(duration / speedModifier / wateringMultiplier, 1);
        }
        process++;
        if(process >= maxProcess) {
            System.out.println(maxProcess);
            for (ItemStack drop : drops) {
                drop.setCount(drop.getCount() * stackMultiplier * fortuneMultiplier);
                Utils.moveItem(level, blockPos, drop);
            }
            process = 0;
        }
        setChanged(level, blockPos, blockState);
    }

    public static int wateringChance() {
        double chance = ThreadLocalRandom.current().nextDouble(100.0);
        if (chance < 90.0) {
            return 2;
        } else if (chance < 99.0) {
            return 4;
        } else {
            return 8;
        }
    }

    private List<ItemStack> getPotentialDrops(ServerLevel level, ItemStack seedStack) {
        if (!(seedStack.getItem() instanceof BlockItem blockItem)) return Collections.emptyList();
        Block cropBlock = blockItem.getBlock();
        if (seedStack.getItem() == Items.MELON_SEEDS) {
            return List.of(new ItemStack(Items.MELON, 1));
        } else if (seedStack.getItem() == Items.PUMPKIN_SEEDS) {
            return List.of(new ItemStack(Items.PUMPKIN, 1));
        }  else if (seedStack.is(ModTags.FARMER_SELF_SUSTAINING)) {
            return List.of(new ItemStack(seedStack.getItem(), 1));
        } else if (cropBlock instanceof FlowerBlock) {
            return List.of(new ItemStack(seedStack.getItem(), 1));
        } else if (cropBlock instanceof SaplingBlock) {
            var drops = new ArrayList<ItemStack>();
            
            ResourceLocation key = BuiltInRegistries.BLOCK.getKey(cropBlock);
            String path = key.getPath();
            String logPath = path.replace("sapling", "log");
            String leavesPath = path.replace("sapling", "leaves");
            
            if (path.equals("mangrove_propagule")) {
                logPath = "mangrove_log";
                leavesPath = "mangrove_leaves";
            } else if (path.equals("azalea") || path.equals("flowering_azalea")) {
                 logPath = "oak_log";
                 leavesPath = "azalea_leaves";
                 if (path.equals("flowering_azalea")) leavesPath = "flowering_azalea_leaves";
            }

            Block logBlock = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(key.getNamespace(), logPath));
            Block leavesBlock = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(key.getNamespace(), leavesPath));

            if (logBlock != Blocks.AIR) {
                drops.add(new ItemStack(logBlock, 2 + level.random.nextInt(2)));
            }
            if (leavesBlock != Blocks.AIR) {
                drops.add(new ItemStack(leavesBlock, 1));
            }
            
            return drops;
        }

        BlockState fullyGrownState;
        if (cropBlock instanceof CropBlock ageable) {
            fullyGrownState = ageable.getStateForAge(ageable.getMaxAge());
        } else {
            fullyGrownState = cropBlock.defaultBlockState();
            var optAgeProp = cropBlock.getStateDefinition().getProperties().stream()
                    .filter(p -> p instanceof IntegerProperty && p.getName().equals("age"))
                    .map(p -> (IntegerProperty) p)
                    .findFirst();

            if (optAgeProp.isPresent()) {
                var ageProperty = optAgeProp.get();
                int maxAge = Collections.max(ageProperty.getPossibleValues());
                fullyGrownState = fullyGrownState.setValue(ageProperty, maxAge);
            }
        }

        LootParams.Builder builder = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, Vec3.ZERO)
                .withParameter(LootContextParams.TOOL, ItemStack.EMPTY);

        List<ItemStack> drops = new ArrayList<>(fullyGrownState.getDrops(builder));
        if (seedStack.is(ModTags.MYSTICAL_AGRICULTURE_SEEDS)) {
            drops.removeIf(stack -> stack.is(seedStack.getItem()));
        }

        return drops;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.minformax.farmer");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new FarmerMenu(containerId, playerInventory, this, this.data);
    }
}
