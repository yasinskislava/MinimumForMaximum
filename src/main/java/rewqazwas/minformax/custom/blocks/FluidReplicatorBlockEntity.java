package rewqazwas.minformax.custom.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Nullable;
import rewqazwas.minformax.custom.ModBlockEntities;
import rewqazwas.minformax.custom.index.ModDataReloadListener;
import rewqazwas.minformax.custom.items.upgrades.SpeedUpgrade;
import rewqazwas.minformax.custom.items.upgrades.ProcessingUpgrade;
import rewqazwas.minformax.custom.utility.Utils;


public class FluidReplicatorBlockEntity extends MachineBaseEntity {
    public FluidReplicatorBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.FLUID_REPLICATOR_BE.get(), pos, blockState);
    }
    //Handlers

    public final FluidTank fluidHandler = new FluidTank(1000) {
        @Override
        protected void onContentsChanged() {
            setChanged();
            if(!level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }

        @Override
        public boolean isFluidValid(FluidStack stack) {
            var key = BuiltInRegistries.FLUID.getKey(stack.getFluid()).toString();
            return ModDataReloadListener.FLUID_REPLICATOR_DATA.containsKey(key);
        }
    };

    public final Utils.UpgradeItemHandler upgradeHandler = new Utils.UpgradeItemHandler(2);

    public final EnergyStorage energyHandler = new EnergyStorage(40_960_000);

    //Variables
    private int process = 0;
    private int maxProcess = 256;

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
        SimpleContainer inv = new SimpleContainer(upgradeHandler.getSlots());
        for(int i = 0; i < upgradeHandler.getSlots(); i++){
            inv.setItem(i, upgradeHandler.getStackInSlot(i));
        }
        Containers.dropContents(this.level, this.worldPosition, inv);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("fluid_tank", fluidHandler.writeToNBT(registries, new CompoundTag()));
        tag.put("upgrades", upgradeHandler.serializeNBT(registries));
        tag.putInt("fluid_replicator.process", this.process);
        tag.putInt("fluid_replicator.max_process", this.maxProcess);
        tag.putInt("fluid_replicator.energy", this.energyHandler.getEnergyStored());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        fluidHandler.readFromNBT(registries, tag.getCompound("fluid_tank"));
        upgradeHandler.deserializeNBT(registries, tag.getCompound("upgrades"));
        this.process = tag.getInt("fluid_replicator.process");
        this.maxProcess = tag.getInt("fluid_replicator.max_process");
        this.energyHandler.receiveEnergy(tag.getInt("fluid_replicator.energy"), false);
    }

    //Main
    public void tick(Level level, BlockPos blockPos, BlockState blockState, FluidReplicatorBlockEntity blockEntity) {
        if(level.isClientSide()) return;

        var sourceStack = fluidHandler.getFluid();
        if (sourceStack.isEmpty()) return;

        if (sourceStack.getAmount() < fluidHandler.getCapacity()) return;

        var key = BuiltInRegistries.FLUID.getKey(sourceStack.getFluid()).toString();
        if (!ModDataReloadListener.FLUID_REPLICATOR_DATA.containsKey(key)) return;
        var data = ModDataReloadListener.FLUID_REPLICATOR_DATA.get(key);

        int speedModifier = 1;
        int stackMultiplier = 1;

        for(int i = 0; i < upgradeHandler.getSlots(); i++) {
            var upgrade = upgradeHandler.getStackInSlot(i).getItem();
            if(upgrade instanceof SpeedUpgrade speedUpgrade) {
                speedModifier = speedUpgrade.getModifier();
            } else if(upgrade instanceof ProcessingUpgrade processingUpgrade) {
                stackMultiplier = processingUpgrade.getMultiplier();
            }
        }

        int effectiveSpeed = Math.min(speedModifier, data.duration());
        int energyCost = data.energyMultiplier() * effectiveSpeed * stackMultiplier;
        if (speedModifier == 9999) {
            energyCost = 0;
        }
        if (energyHandler.getEnergyStored() < energyCost) return;
        if (!Utils.canInsertAtLeastOne(level, blockPos, sourceStack)) return;

        energyHandler.extractEnergy(energyCost, false);

        process++;
        maxProcess = data.duration();

        if (process >= maxProcess / speedModifier) {
            int totalToGenerate = data.basicAmountGenerated() * stackMultiplier;
            int leftovers = Utils.moveFluid(level, blockPos, sourceStack, totalToGenerate);
            process = 0;
        }
        setChanged(level, blockPos, blockState);
    }
}
