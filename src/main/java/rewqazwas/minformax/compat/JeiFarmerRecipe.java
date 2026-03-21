package rewqazwas.minformax.compat;

import net.minecraft.world.item.ItemStack;
import rewqazwas.minformax.custom.index.FarmerData;
import java.util.List;

public record JeiFarmerRecipe(ItemStack input, List<ItemStack> outputs, FarmerData data) {}