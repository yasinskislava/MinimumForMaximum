package rewqazwas.minformax.custom.index;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;

public class PlayerIndex {
    private static final String TAG_KEY = "minformax_player_index";
    private static final Codec<List<String>> LIST_CODEC = Codec.list(Codec.STRING);

    public static List<String> load(ServerPlayer player) {
        CompoundTag pd = player.getPersistentData();
        Tag tag = pd.get(TAG_KEY);
        if (tag == null) return new ArrayList<>();
        DataResult<List<String>> result = LIST_CODEC.parse(NbtOps.INSTANCE, tag);
        return result.result().orElseGet(ArrayList::new);
    }

    public static void save(ServerPlayer player, List<String> list) {
        DataResult<Tag> encoded = LIST_CODEC.encodeStart(NbtOps.INSTANCE, list);
        Optional<Tag> opt = encoded.result();
        if (opt.isPresent()) {
            player.getPersistentData().put(TAG_KEY, opt.get());
        }
    }

    public static boolean contains(ServerPlayer player, String key) {
        return load(player).contains(key);
    }

    public static List<String> getLocalIndex(ServerPlayer player) { return Collections.unmodifiableList(load(player)); }

    public static void add(ServerPlayer player, String key) {
        List<String> indexList = new ArrayList<>(getLocalIndex(player));
        indexList.add(key);
        save(player, indexList);
    }
}