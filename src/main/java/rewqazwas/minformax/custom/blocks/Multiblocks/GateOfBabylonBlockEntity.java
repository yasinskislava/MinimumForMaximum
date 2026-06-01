package rewqazwas.minformax.custom.blocks.Multiblocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import rewqazwas.minformax.config.DataConfigs;
import rewqazwas.minformax.custom.ModBlockEntities;
import rewqazwas.minformax.custom.blocks.AbstractMultiblockPartBlockEntity;
import rewqazwas.minformax.custom.index.ModDataReloadListener;
import rewqazwas.minformax.custom.utility.MultiblockSavedData;
import rewqazwas.minformax.custom.utility.Utils;
import rewqazwas.minformax.screen.custom.GateOfBabylonMenu;

import java.util.ArrayList;
import java.util.List;

public class GateOfBabylonBlockEntity extends AbstractMultiblockPartBlockEntity {
    public GateOfBabylonBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GATE_OF_BABYLON_BE.get(), pos, state);
    }

    private final List<BlockEntity> cachedMachines = new ArrayList<>();
    private boolean requiresRescan = true;
    private long lastTickTime = -1L;
    private int tickRate = 2;
    private long consumptionRate = 0;
    private int errorMask = 0;

    public void flagForRescan() {
        this.requiresRescan = true;
    }

    public final Utils.UniversalEnergyStorage energyHandler = new Utils.UniversalEnergyStorage(Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE);

    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> (int) (GateOfBabylonBlockEntity.this.energyHandler.getLongEnergyStored() & 0xFFFFFFFFL);
                case 1 -> (int) ((GateOfBabylonBlockEntity.this.energyHandler.getLongEnergyStored() >>> 32) & 0xFFFFFFFFL);
                case 2 -> (int) (GateOfBabylonBlockEntity.this.energyHandler.getMaxCapacityLong() & 0xFFFFFFFFL);
                case 3 -> (int) ((GateOfBabylonBlockEntity.this.energyHandler.getMaxCapacityLong() >>> 32) & 0xFFFFFFFFL);
                case 4 -> GateOfBabylonBlockEntity.this.tickRate;
                case 5 -> GateOfBabylonBlockEntity.this.cachedMachines.size();
                case 6 -> GateOfBabylonBlockEntity.this.errorMask;
                case 7 -> (int) (GateOfBabylonBlockEntity.this.consumptionRate & 0xFFFFFFFFL);
                case 8 -> (int) ((GateOfBabylonBlockEntity.this.consumptionRate >>> 32) & 0xFFFFFFFFL);
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            if (index == 4) {
                GateOfBabylonBlockEntity.this.tickRate = value;
                GateOfBabylonBlockEntity.this.setChanged();
            }
        }

        @Override
        public int getCount() {
            return 9;
        }
    };

    private void rescanInterior(Level level, BlockPos pos) {
        this.cachedMachines.clear();

        if (this.slaveBlocks.isEmpty()) return;

        int minX = pos.getX(), minY = pos.getY(), minZ = pos.getZ();
        int maxX = pos.getX(), maxY = pos.getY(), maxZ = pos.getZ();
        for (BlockPos slavePos : this.slaveBlocks) {
            minX = Math.min(minX, slavePos.getX()); minY = Math.min(minY, slavePos.getY()); minZ = Math.min(minZ, slavePos.getZ());
            maxX = Math.max(maxX, slavePos.getX()); maxY = Math.max(maxY, slavePos.getY()); maxZ = Math.max(maxZ, slavePos.getZ());
        }

        int innerMinX = minX + 1; int innerMinY = minY + 1; int innerMinZ = minZ + 1;
        int innerMaxX = maxX - 1; int innerMaxY = maxY - 1; int innerMaxZ = maxZ - 1;

        if (innerMinX <= innerMaxX && innerMinY <= innerMaxY && innerMinZ <= innerMaxZ) {
            for (int x = innerMinX; x <= innerMaxX; x++) {
                for (int y = innerMinY; y <= innerMaxY; y++) {
                    for (int z = innerMinZ; z <= innerMaxZ; z++) {
                        BlockPos internalPos = new BlockPos(x, y, z);
                        BlockEntity machine = level.getBlockEntity(internalPos);

                        if (machine != null) {
                            if (machine instanceof AbstractMultiblockPartBlockEntity part && part.isFormed()) {
                                BlockPos masterPosition = part.getMasterPos();

                                if (masterPosition != null) {
                                    BlockEntity masterBE = level.getBlockEntity(masterPosition);

                                    if (masterBE instanceof GateOfBabylonBlockEntity nestedGate) {
                                        if (!this.multiblockId.equals(nestedGate.multiblockId)) {
                                            this.cachedMachines.clear();
                                            this.requiresRescan = false;
                                            return;
                                        }
                                    }
                                }
                            }

                            String blockId = machine.getBlockState().getBlock().builtInRegistryHolder().key().location().toString();
                            if (!(machine instanceof GateOfBabylonBlockEntity) && !ModDataReloadListener.GATE_OF_BABYLON_BLACKLIST.contains(blockId)) {
                                this.cachedMachines.add(machine);
                            }
                        }
                    }
                }
            }
        }
        this.requiresRescan = false;
    }

    @Override
    protected void serverTick(Level level, BlockPos pos, BlockState state) {
        long currentTick = level.getGameTime();
        if (this.lastTickTime == currentTick) {
            return;
        }
        this.lastTickTime = currentTick;

        if (this.requiresRescan) {
            rescanInterior(level, pos);
        }

        int currentErrors = 0;
        if(this.cachedMachines.isEmpty()){
            currentErrors |= 1;
            this.consumptionRate = 0;
        }

        int power = (int) (Math.log(this.tickRate) / Math.log(2) - 1);
        long cost = (long) (this.cachedMachines.size() * DataConfigs.basicConsumption.get() * Math.pow(DataConfigs.exponentialGrowth.get(), power));
        long requiredEnergy = (long) (DataConfigs.basicConsumption.get() * Math.pow(DataConfigs.exponentialGrowth.get(), power));

        if(this.energyHandler.getLongEnergyStored() >= cost){
            for (int i = 0; i < this.cachedMachines.size(); i++) {
                BlockEntity machine = this.cachedMachines.get(i);

                if (machine.isRemoved()) {
                    this.requiresRescan = true;
                    continue;
                }

                BlockPos mPos = machine.getBlockPos();
                BlockState mState = machine.getBlockState();
                var rawTicker = mState.getTicker(level, machine.getType());

                if (rawTicker != null) {
                    @SuppressWarnings("unchecked")
                    BlockEntityTicker<BlockEntity> ticker = (BlockEntityTicker<BlockEntity>) rawTicker;

                    for (int t = 0; t < this.tickRate; t++) {
                        if (machine.isRemoved()) {
                            this.requiresRescan = true;
                            break;
                        }
                        ticker.tick(level, mPos, mState, machine);
                    }
                    this.energyHandler.consumeEnergy(requiredEnergy);
                    this.consumptionRate = requiredEnergy;
                    setChanged();
                }
            }
        } else {
            currentErrors |= 2;
            this.consumptionRate = 0;
        }

        if(this.errorMask != currentErrors){
            this.errorMask = currentErrors;
            setChanged();
        }
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new GateOfBabylonMenu(containerId, inventory, this, this.data);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.minformax.gate_of_babylon");
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putLong("energy", this.energyHandler.getLongEnergyStored());
        tag.putInt("tickRate", this.tickRate);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.energyHandler.setEnergy(tag.getLong("energy"));
        if (tag.contains("tickRate")) {
            this.tickRate = tag.getInt("tickRate");
        } else {
            this.tickRate = 2;
        }
    }
}