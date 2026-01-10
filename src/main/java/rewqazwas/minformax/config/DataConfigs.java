package rewqazwas.minformax.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;
import rewqazwas.minformax.custom.index.HolderClass;

import java.util.List;

public class DataConfigs {
    public static final ModConfigSpec COMMON;

    public static final  ModConfigSpec.IntValue TEST;

    static {
        final var common = new ModConfigSpec.Builder();

        common.push("general");
        TEST = common
                .comment("TEST value")
                .defineInRange("test_data", 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
        common.pop();

        COMMON= common.build();
    }
}
