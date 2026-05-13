package tfar.quickstack.client.events;

import java.util.ArrayList;
import java.util.List;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import tfar.quickstack.DropOff;
import tfar.quickstack.client.ClientUtils;
import tfar.quickstack.client.RendererCubeTarget;
import tfar.quickstack.config.DropOffConfig;

@Mod.EventBusSubscriber(modid = DropOff.MOD_ID, bus = Bus.FORGE, value = Dist.CLIENT)
public class RenderWorldLastEventHandler {

    @SubscribeEvent
    public static void onRenderWorldLastEvent(RenderLevelStageEvent event) {
        RendererCube.tryToRender(event);
    }

    public static class RendererCube {

        private static List<RendererCubeTarget> rendererCubeTargets = new ArrayList<>();
        private static long lastDrawTime;

        public static void draw(List<RendererCubeTarget> rendererCubeTargets) {
            RendererCube.rendererCubeTargets = rendererCubeTargets;
            lastDrawTime = System.currentTimeMillis();
        }

        /**
         * This method called by RenderWorldLastEvent handler.
         * It does nothing until the draw() method assign the necessary delay to the
         * global field named currentTime.
         */
        static void tryToRender(RenderLevelStageEvent event) {
            long timeWhenDissapear = lastDrawTime + DropOffConfig.Client.highlightDelay.get();
            if ((System.currentTimeMillis() >= timeWhenDissapear) && DropOffConfig.Client.highlightDelay.get() >= 0L) {
                return;
            }
            ClientUtils.renderBlocks(event, rendererCubeTargets);
        }
    }
}
