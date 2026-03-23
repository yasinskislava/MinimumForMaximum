package rewqazwas.minformax.custom.blocks.PandoraBox;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import org.jetbrains.annotations.Nullable;
import rewqazwas.minformax.MinForMax;
import rewqazwas.minformax.custom.ModBlockEntities;
import rewqazwas.minformax.custom.blocks.EternalGeneratorBlockEntity;
import rewqazwas.minformax.custom.component.ModDataComponents;
import rewqazwas.minformax.custom.items.ModItems;
import rewqazwas.minformax.custom.utility.Utils;
import rewqazwas.minformax.screen.custom.PandoraBoxMenu;

import java.util.ArrayList;
import java.util.List;

public class PandoraBoxCoreBlockEntity extends BlockEntity implements MenuProvider {
    public PandoraBoxCoreBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PANDORA_BOX_CORE_BE.get(), pos, state);
    }
    //Handlers

    public final Utils.EnergyGenStorage energyHandler = new Utils.EnergyGenStorage(Integer.MAX_VALUE);

    public final Utils.SingleItemHandler itemHandler = new Utils.SingleItemHandler(1) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stack.is(ModItems.LINKER);
        }
    };

    //Variables
    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> PandoraBoxCoreBlockEntity.this.energyHandler.getEnergyStored();
                case 1 -> (int) (PandoraBoxCoreBlockEntity.this.overload % Integer.MAX_VALUE);
                case 2 -> (int) (PandoraBoxCoreBlockEntity.this.overload / Integer.MAX_VALUE);
                case 3 -> (int) (PandoraBoxCoreBlockEntity.this.totalXp % Integer.MAX_VALUE);
                case 4 -> (int) (PandoraBoxCoreBlockEntity.this.totalXp / Integer.MAX_VALUE);
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> PandoraBoxCoreBlockEntity.this.energyHandler.setEnergy(value);
            }
        }

        @Override
        public int getCount() {
            return 5;
        }
    };

    private long overload = 0;
    private long totalXp = 0;
    private int cooldown = 0;
    
    // Cache
    private long cachedProduction = 0;
    private boolean cacheDirty = true;
    private final List<BlockPos> hatchOffsets = new ArrayList<>();
    private boolean hatchesInitialized = false;


    //Main
    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) return;
        
        if (cacheDirty) {
            recalculateCache();
        }
        
        if (!hatchesInitialized) {
            initializeHatches();
        }

        cooldown++;
        
        boolean changed = false;
        long production = this.cachedProduction;
        
        if (production > 0) {
            int currentEnergy = energyHandler.getEnergyStored();
            int maxEnergy = energyHandler.getMaxEnergyStored();
            if (currentEnergy < maxEnergy) {
                long newEnergy = currentEnergy + production;
                energyHandler.setEnergy((int) Math.min(newEnergy, maxEnergy));
                changed = true;
            }
        }
        
        if (this.energyHandler.getEnergyStored() > 0) {
            for (BlockPos offset : this.hatchOffsets) {
                BlockPos hatchPos = pos.offset(offset);

                Utils.forEachNeighborCapability(Capabilities.EnergyStorage.BLOCK, level, hatchPos, (targetHandler, side) -> {
                    int energyToPush = this.energyHandler.getEnergyStored();
                    if (energyToPush > 0 && targetHandler.canReceive()) {
                        int accepted = targetHandler.receiveEnergy(energyToPush, false);
                        if (accepted > 0) {
                            this.energyHandler.setEnergy(this.energyHandler.getEnergyStored() - accepted);
                            setChanged();
                        }
                    }
                });

                if (this.energyHandler.getEnergyStored() <= 0) break;
            }
        }

        if(cooldown >= 1200){
            cooldown = 0;
            ItemStack stack = itemHandler.getStackInSlot(0);
            if (!stack.isEmpty()) {
                List<BlockPos> storage = stack.getOrDefault(ModDataComponents.LINKED_POS, List.of());
                if (!storage.isEmpty()) {
                    for (BlockPos blockPos : storage) {
                        if (level.getBlockEntity(blockPos) instanceof EternalGeneratorBlockEntity generator) {
                            var overloadVal = generator.getOverload();
                            if (overloadVal > 0) {
                                generator.consumeOverload(overloadVal);
                                this.overload += overloadVal;
                                cacheDirty = true;
                                changed = true;
                            }
                        }
                    }
                }
            }
        }
        if (changed) {
            setChanged(level, pos, state);
        }
    }
    
    private void recalculateCache() {
        long xpLevel = Utils.calculateLevel(totalXp).level();
        double multiplier = Math.log10(xpLevel + 10);
        this.cachedProduction = (long) (overload * multiplier);
        this.cacheDirty = false;
    }
    
    private void initializeHatches() {
        hatchOffsets.clear();
        for (BlockPos offset : PandoraBoxCore.STRUCTURE_OFFSETS) {
            if (offset.getX() == 0 && offset.getZ() == 0) {
                hatchOffsets.add(offset);
            }
        }
        hatchesInitialized = true;
    }

    //Extra
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("energy", this.energyHandler.getEnergyStored());
        tag.put("inventory", itemHandler.serializeNBT(registries));
        tag.putLong("overload", this.overload);
        tag.putLong("totalXp", this.totalXp);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.energyHandler.setEnergy(tag.getInt("energy"));
        itemHandler.deserializeNBT(registries, tag.getCompound("inventory"));
        this.overload = tag.getLong("overload");
        this.totalXp = tag.getLong("totalXp");
        this.cacheDirty = true;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(MinForMax.MOD_ID, "block.minformax.pandora_box_core");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new PandoraBoxMenu(i, inventory, this, this.data);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public void addOverload(long value) {
        this.overload += value;
        this.cacheDirty = true;
        setChanged();
    }

    public void addXp(long value) {
        this.totalXp += value;
        this.cacheDirty = true;
        setChanged();
    }

    public void drops() {
        SimpleContainer inv = new SimpleContainer(itemHandler.getSlots());
        for(int i = 0; i < itemHandler.getSlots(); i++){
            inv.setItem(i, itemHandler.getStackInSlot(i));
        }
        Containers.dropContents(this.level, this.worldPosition, inv);
    }
}
