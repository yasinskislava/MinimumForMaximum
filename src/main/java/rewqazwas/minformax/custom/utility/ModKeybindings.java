package rewqazwas.minformax.custom.utility;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

public class ModKeybindings {
    public static final KeyMapping OPEN_INDEX_MENU = new KeyMapping(
            "key.minformax.open_index_menu",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_I,
            "category.minformax.keys"
    );
    public static final KeyMapping OPEN_GUIDE_KEY = new KeyMapping(
            "key.minformax.open_guide",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            "category.minformax.keys"
    );

    public static void register(RegisterKeyMappingsEvent event) {
        event.register(OPEN_INDEX_MENU);
        event.register(OPEN_GUIDE_KEY);
    }
}
