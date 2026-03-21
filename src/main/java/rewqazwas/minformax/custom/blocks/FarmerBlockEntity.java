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
import net.minecraft.tags.TagKey;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
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
import rewqazwas.minformax.config.DataConfigs;
import rewqazwas.minformax.custom.ModBlockEntities;
import rewqazwas.minformax.custom.ModTags;
import rewqazwas.minformax.custom.index.FarmerData;
import rewqazwas.minformax.custom.index.ModDataReloadListener;
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
    public FarmerBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.FARMER_BE.get(), pos, blockState);
    }
    //Handlers

    public final Utils.UpgradeItemHandler upgradeHandler = new Utils.UpgradeItemHandler(4) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stack.is(ModItems.WATERING_UPGRADE)
                    || stack.is(ModTags.FORTUNE_UPGRADES)
                    || super.isItemValid(slot, stack);
        }
    };

    public final Utils.SingleItemHandler itemHandler = new Utils.SingleItemHandler(1) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            String itemString = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();

            for (FarmerData data : ModDataReloadListener.FARMER_DATA.values()) {
                // Prohibited check takes priority
                if (data.prohibitedItems().contains(itemString)) return false;
                for (String tag : data.prohibitedTags()) {
                    if (stack.is(TagKey.create(BuiltInRegistries.ITEM.key(), ResourceLocation.parse(tag)))) return false;
                }

                // Allowed check
                if (data.allowedItems().contains(itemString)) return true;
                for (String tag : data.allowedTags()) {
                    if (stack.is(TagKey.create(BuiltInRegistries.ITEM.key(), ResourceLocation.parse(tag)))) return true;
                }
            }
            return false; // Only allow items found in config
        }
    };

    public final EnergyStorage energyHandler = new EnergyStorage(40_960_000);

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

    //Variables
    private int process = 0;
    private int maxProcess = 256;
    private int duration = maxProcess;

    //Extra
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

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.minformax.farmer");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new FarmerMenu(containerId, playerInventory, this, this.data);
    }

    //Utility
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
        return Utils.getFarmerDrops(level, seedStack);
    }

    //Main
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

        int energyCost = stackMultiplier * speedModifier * DataConfigs.farmCoefficient.get();
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
            for (ItemStack drop : drops) {
                drop.setCount(drop.getCount() * stackMultiplier * fortuneMultiplier);
                Utils.moveItem(level, blockPos, drop);
            }
            process = 0;
        }
        setChanged(level, blockPos, blockState);
    }
}
