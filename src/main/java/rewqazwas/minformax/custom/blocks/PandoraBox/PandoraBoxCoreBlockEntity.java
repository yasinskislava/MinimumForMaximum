package rewqazwas.minformax.custom.blocks.PandoraBox;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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

    public final Utils.EnergyGenStorage energyHandler = new Utils.EnergyGenStorage(Long.MAX_VALUE);

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
                case 0 -> (int) (PandoraBoxCoreBlockEntity.this.energyHandler.getLongEnergyStored() & 0xFFFFFFFFL); // Lower 32 bits of current energy
                case 1 -> (int) ((PandoraBoxCoreBlockEntity.this.energyHandler.getLongEnergyStored() >>> 32) & 0xFFFFFFFFL); // Upper 32 bits of current energy
                case 2 -> (int) (PandoraBoxCoreBlockEntity.this.energyHandler.getMaxCapacityLong() & 0xFFFFFFFFL); // Lower 32 bits of max energy
                case 3 -> (int) ((PandoraBoxCoreBlockEntity.this.energyHandler.getMaxCapacityLong() >>> 32) & 0xFFFFFFFFL); // Upper 32 bits of max energy
                case 4 -> (int) (PandoraBoxCoreBlockEntity.this.overload & 0xFFFFFFFFL); // Lower 32 bits of overload
                case 5 -> (int) ((PandoraBoxCoreBlockEntity.this.overload >>> 32) & 0xFFFFFFFFL); // Upper 32 bits of overload
                case 6 -> (int) (PandoraBoxCoreBlockEntity.this.totalXp & 0xFFFFFFFFL); // Lower 32 bits of totalXp
                case 7 -> (int) ((PandoraBoxCoreBlockEntity.this.totalXp >>> 32) & 0xFFFFFFFFL); // Upper 32 bits of totalXp
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {

        }

        @Override
        public int getCount() {
            return 8; // 2 for energy, 2 for max energy, 2 for overload, 2 for totalXp
        }
    };

    private long overload = 0;
    private long totalXp = 0;
    private int cooldown = 0;

    public long getOverload() {
        return this.overload;
    }

    public long getTotalXp() {
        return this.totalXp;
    }

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
            long energyBeforeAdd = energyHandler.getLongEnergyStored();
            energyHandler.addEnergy(production);
            if (energyHandler.getLongEnergyStored() != energyBeforeAdd) {
                changed = true;
            }
        }
        
        // Energy Distribution Loop
        if (this.energyHandler.getLongEnergyStored() > 0) {
            boolean energyTransferredInPass;
            do {
                energyTransferredInPass = false;
                for (BlockPos offset : this.hatchOffsets) {
                    BlockPos hatchPos = pos.offset(offset);

                    // Use a local variable to track energy pushed to this specific neighbor in this pass
                    final long[] energyPushedToNeighbor = {0};
                    Utils.forEachNeighborCapability(Capabilities.EnergyStorage.BLOCK, level, hatchPos, (targetHandler, side) -> {
                        if (targetHandler.canReceive()) {
                            long remainingEnergyInBox = this.energyHandler.getLongEnergyStored();
                            while (remainingEnergyInBox > 0) {
                                // Neoforge's receiveEnergy takes an int, so we push in chunks of Integer.MAX_VALUE
                                int amountToPush = (int) Math.min(remainingEnergyInBox, Integer.MAX_VALUE);
                                int accepted = targetHandler.receiveEnergy(amountToPush, false);
                                if (accepted > 0) {
                                    this.energyHandler.extractEnergy(accepted, false);
                                    energyPushedToNeighbor[0] += accepted;
                                    remainingEnergyInBox -= accepted; // Update remaining for next iteration
                                    // Mark changed only if energy was actually moved
                                    if (accepted > 0) {
                                        setChanged();
                                    }
                                } else {
                                    // Neighbor can't accept more, break inner loop for this neighbor
                                    break;
                                }
                            }
                        }
                    });
                    if (energyPushedToNeighbor[0] > 0) {
                        energyTransferredInPass = true; // Mark that some energy was transferred in this pass
                        changed = true; // Mark block entity as changed
                    }
                    if (this.energyHandler.getLongEnergyStored() <= 0) {
                        break; // If box is empty, stop distributing to other neighbors in this pass
                    }
                }
            } while (energyTransferredInPass && this.energyHandler.getLongEnergyStored() > 0); // Repeat pass if energy was transferred and box not empty
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
        tag.putLong("energy", this.energyHandler.getLongEnergyStored());
        tag.put("inventory", itemHandler.serializeNBT(registries));
        tag.putLong("overload", this.overload);
        tag.putLong("totalXp", this.totalXp);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.energyHandler.setEnergy(tag.getLong("energy"));
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
