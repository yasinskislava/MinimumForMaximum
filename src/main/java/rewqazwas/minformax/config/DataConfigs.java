package rewqazwas.minformax.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;
import rewqazwas.minformax.custom.index.HolderClass;

import java.util.List;

public class DataConfigs {
    public static final ModConfigSpec COMMON;

    public static final ModConfigSpec.BooleanValue shouldRender;
    public static final ModConfigSpec.IntValue mobCoefficient;
    public static final ModConfigSpec.IntValue farmCoefficient;
    public static final ModConfigSpec.LongValue basicConsumption;
    public static final ModConfigSpec.IntValue exponentialGrowth;
    public static final ModConfigSpec.IntValue gateOfBabylonSize;

    static {
        final var common = new ModConfigSpec.Builder();

        common.push("general");
        shouldRender = common
                .comment("Should replicators render block inside")
                .define("should_render", true);
        mobCoefficient = common
                .comment("Energy multiplier for mob production")
                .defineInRange("mob_coefficient", 8, 1, Integer.MAX_VALUE);
        farmCoefficient = common
                .comment("Energy multiplier for farm production")
                .defineInRange("farm_coefficient", 1024, 1, Integer.MAX_VALUE);
        basicConsumption = common
                .comment("Energy required for 1 machine at basic speed")
                .defineInRange("basic_consumption", 1_000_000_000, 0, Long.MAX_VALUE);
        exponentialGrowth = common
                .comment("Multiplier applied as speed increases")
                .defineInRange("exponential_growth", 10, 0, Integer.MAX_VALUE);
        gateOfBabylonSize = common
                .comment("Maximum size for each edge of the Gate Of Babylon")
                .defineInRange("gate_of_babylon_size", 256, 3, Integer.MAX_VALUE);

        common.pop();


        COMMON= common.build();
    }
}
