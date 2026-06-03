package tfar.quickstack.client.events;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.KeyMapping;
import tfar.quickstack.QuickStack;

public class HotkeysRegistrar {
    public static final KeyMapping DUMP_MAPPING = new KeyMapping("dropoff.key.dump", GLFW.GLFW_KEY_X, QuickStack.MOD_ID);
    public static final KeyMapping DEPOSIT_MAPPING = new KeyMapping("dropoff.key.deposit", GLFW.GLFW_KEY_C, QuickStack.MOD_ID);
}
