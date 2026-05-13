package tfar.quickstack.client.events;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import tfar.quickstack.DropOff;

public class HotkeysRegistrar {
    public static final KeyMapping DUMP_MAPPING = new KeyMapping("dropoff.key.dump", GLFW.GLFW_KEY_X, DropOff.MOD_ID);
    public static final KeyMapping DEPOSIT_MAPPING = new KeyMapping("dropoff.key.deposit", GLFW.GLFW_KEY_C, DropOff.MOD_ID);

    public static void registerBindings(RegisterKeyMappingsEvent e) {
        e.register(DUMP_MAPPING);
        e.register(DEPOSIT_MAPPING);
    }
}
