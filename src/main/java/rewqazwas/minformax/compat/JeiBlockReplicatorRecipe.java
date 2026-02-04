package rewqazwas.minformax.compat;

import net.minecraft.world.item.ItemStack;
import rewqazwas.minformax.custom.index.BlockReplicatorData;

import java.util.List;

public record JeiBlockReplicatorRecipe(List<ItemStack> items, BlockReplicatorData data) {
}
