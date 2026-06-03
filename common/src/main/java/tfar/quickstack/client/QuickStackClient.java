package tfar.quickstack.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import tfar.quickstack.client.events.HotkeysRegistrar;
import tfar.quickstack.config.QuickStackConfig;
import tfar.quickstack.network.server.C2SPacketRequestDropoff;
import tfar.quickstack.platform.Services;

import java.util.ArrayList;
import java.util.List;

public class QuickStackClient {

    public static void renderBlocks(PoseStack stack) {

        long timeWhenDissapear = lastDrawTime + QuickStackConfig.Client.highlightDelay.get();
        if ((System.currentTimeMillis() >= timeWhenDissapear) && QuickStackConfig.Client.highlightDelay.get() >= 0L) {
            return;
        }

        MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();

        stack.pushPose();

        Vec3 cam = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        stack.translate(-cam.x, -cam.y, -cam.z);

        rendererCubeTargets.forEach(rendererCubeTarget -> {
            VertexConsumer builder = buffer.getBuffer(RenderType.LINES);
            AABB bb = Shapes.block().bounds().move(rendererCubeTarget.blockPos().getX(),
                rendererCubeTarget.blockPos().getY(), rendererCubeTarget.blockPos().getZ());
            float red = (rendererCubeTarget.color() >> 16 & 0xff) / 255f;
            float green = (rendererCubeTarget.color() >> 8 & 0xff) / 255f;
            float blue = (rendererCubeTarget.color() & 0xff) / 255f;

            LevelRenderer.renderLineBox(stack, builder, bb, red, green, blue, 1);
            buffer.endBatch(RenderType.LINES);
        });

        stack.popPose();
    }

    public static void tick(){
        while (HotkeysRegistrar.DEPOSIT_MAPPING.consumeClick()) {
            QuickStackClient.sendNoSpectator(false);
        }
        while (HotkeysRegistrar.DUMP_MAPPING.consumeClick()) {
            QuickStackClient.sendNoSpectator(true);
        }
    }

    private static List<RendererCubeTarget> rendererCubeTargets = new ArrayList<>();
    private static long lastDrawTime;

    public static void draw(List<RendererCubeTarget> rendererCubeTargets) {
        QuickStackClient.rendererCubeTargets = rendererCubeTargets;
        lastDrawTime = System.currentTimeMillis();
    }

    public static void printToChat(String message) {
        message = "[" + ChatFormatting.BLUE + "QuickStack" + ChatFormatting.RESET + "]: " + message;

        LocalPlayer player = Minecraft.getInstance().player;
        var textComponentString = Component.literal(message);

        player.sendSystemMessage(textComponentString);
    }

    public static void sendNoSpectator(boolean dump) {
        if (Minecraft.getInstance().player.isSpectator()) {
            printToChat("Action not allowed in spectator mode.");
        } else {
            C2SPacketRequestDropoff dropoffMessage = new C2SPacketRequestDropoff(
                    QuickStackConfig.Client.ignoreHotBar.get(),
                    dump,
                    QuickStackConfig.blockEntityBlacklist,
                    QuickStackConfig.Client.minSlotCount.get());
            Services.PLATFORM.sendToServer(dropoffMessage);
        }
    }
}
