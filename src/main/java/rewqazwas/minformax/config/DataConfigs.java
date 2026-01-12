package rewqazwas.minformax.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;
import rewqazwas.minformax.custom.index.HolderClass;

import java.util.List;

public class DataConfigs {
    public static final ModConfigSpec COMMON;

    public static final  ModConfigSpec.BooleanValue shouldRender;

    static {
        final var common = new ModConfigSpec.Builder();

        common.push("general");
        shouldRender = common
                .comment("Should replicators render block inside")
                .define("should_render", true);
        common.pop();

        COMMON= common.build();
    }
}
