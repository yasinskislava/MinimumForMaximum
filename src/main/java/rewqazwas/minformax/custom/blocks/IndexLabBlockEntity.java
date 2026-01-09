package rewqazwas.minformax.custom.blocks;

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
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;
import rewqazwas.minformax.custom.ModAttachmentTypes;
import rewqazwas.minformax.custom.ModBlockEntities;
import rewqazwas.minformax.custom.component.ModDataComponents;
import rewqazwas.minformax.custom.items.ModItems;
import rewqazwas.minformax.screen.custom.IndexLabMenu;

import java.util.TreeMap;

public class IndexLabBlockEntity extends BlockEntity implements MenuProvider {
    public final ItemStackHandler itemHandler = new ItemStackHandler(1) {
        @Override
        protected int getStackLimit(int slot, ItemStack stack) { return 1; }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if(!level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    };
    protected final ContainerData data;
    private int mobKey = -1;
    private int process = 0;
    public String owner;


    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) { return saveWithoutMetadata(registries); }

    public IndexLabBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.INDEX_LAB_BE.get(), pos, blockState);
        data = new ContainerData() {
            @Override
            public int get(int i) {
                return switch (i) {
                    case 0 -> IndexLabBlockEntity.this.process;
                    case 1 -> IndexLabBlockEntity.this.mobKey;
                    default -> 0;
                };
            }

            @Override
            public void set(int i, int value) {
                switch (i) {
                    case 0: IndexLabBlockEntity.this.process = value;
                    case 1: IndexLabBlockEntity.this.mobKey = value;
                }
            }

            @Override
            public int getCount() {
                return 2;
            }
        };
    }


    public void drops() {
        SimpleContainer inv = new SimpleContainer(itemHandler.getSlots());
        for(int i = 0; i < itemHandler.getSlots(); i++){
            inv.setItem(i, itemHandler.getStackInSlot(i));
        }

        Containers.dropContents(this.level, this.worldPosition, inv);
    }



    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        itemHandler.deserializeNBT(registries, tag.getCompound("inventory"));
        this.process = tag.getInt("index_lab.process");
        this.mobKey = tag.getInt("index_lab.mob_key");
        this.owner = tag.getString("index_lab.owner");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        tag.put("inventory", itemHandler.serializeNBT(registries));
        tag.putInt("index_lab.process", this.process);
        tag.putInt("index_lab.mob_key", this.mobKey);
        tag.putString("index_lab.owner", this.owner);
        super.saveAdditional(tag, registries);
    }


    @Override
    public Component getDisplayName() {
        return Component.translatable("block.minformax.index_lab");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        if(owner != null && !owner.equals(player.getScoreboardName())) {
            player.sendSystemMessage(Component.literal("§cYou are not the owner of this Index Installer."));
            return null;
        }
        return new IndexLabMenu(containerId, playerInventory, this, this.data);
    }

    public void setKey(int key) {
        if(this.mobKey == key) return;
        resetProcess();
        this.mobKey = key;
        setChanged();
    }

    private void resetProcess() {
        this.process = 0;
        setChanged();
    }

    public void tick(Level level, BlockPos blockPos, BlockState blockState, IndexLabBlockEntity blockEntity) {
        ItemStack loader = blockEntity.itemHandler.getStackInSlot(0);
        if (!loader.isEmpty() && loader.getItem() == ModItems.MEMORY_SHARD.get() && loader.get(ModDataComponents.MOB_INDEX) == null) {
            if(mobKey > -1) {
                process++;
                setChanged(level, blockPos, blockState);
                if (process >= 1200) {
                    String keyName = null;
                    var playerList = level.players();
                    for(Player player : playerList) {
                        if(player.getScoreboardName().equals(owner) && !level.isClientSide()){
                            keyName = new TreeMap<>(player.getData(ModAttachmentTypes.INDEX_SYNC)).keySet().toArray(new String[0])[mobKey];
                            break;
                        }
                    }
                    loader.set(ModDataComponents.MOB_INDEX, keyName);
                    resetProcess();
                    this.mobKey = -1;
                    setChanged(level, blockPos, blockState);
                }
            }
        } else {
            resetProcess();
        }
    }

}


