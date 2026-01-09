package rewqazwas.minformax.compat;

import net.minecraft.resources.ResourceLocation;
import rewqazwas.minformax.MinForMax;
import rewqazwas.minformax.custom.blocks.IndexLabBlock;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class IndexPlugin implements IWailaPlugin {
    public static final ResourceLocation OWNER = ResourceLocation.fromNamespaceAndPath(MinForMax.MOD_ID,"owner");


    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(OwnerComponentProvider.INSTANCE, IndexLabBlock.class);
    }
}
