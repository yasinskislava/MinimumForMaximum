package rewqazwas.minformax.compat;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import rewqazwas.minformax.custom.blocks.IndexLabBlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum OwnerComponentProvider implements IBlockComponentProvider {
    INSTANCE;

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        tooltip.add(Component.translatable("tooltip.minformax.owner", ((IndexLabBlockEntity) accessor.getBlockEntity()).owner).withColor(16733525));
    }

    @Override
    public ResourceLocation getUid() {
        return IndexPlugin.OWNER;
    }

}
