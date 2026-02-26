package rewqazwas.minformax.compat;

import net.minecraft.resources.ResourceLocation;
import rewqazwas.minformax.custom.index.HolderClass;

public class JeiIndexInscriberRecipe {
    private final ResourceLocation mobId;
    private final HolderClass data;

    public JeiIndexInscriberRecipe(ResourceLocation mobId, HolderClass data) {
        this.mobId = mobId;
        this.data = data;
    }

    public ResourceLocation getMobId() {
        return mobId;
    }

    public HolderClass getData() {
        return data;
    }
}
