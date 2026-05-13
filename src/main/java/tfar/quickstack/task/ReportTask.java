package tfar.quickstack.task;

import static tfar.quickstack.util.MessageUtils.red;

import java.util.List;

import tfar.quickstack.client.ClientUtils;
import tfar.quickstack.client.RendererCubeTarget;
import tfar.quickstack.client.events.RenderWorldLastEventHandler;
import tfar.quickstack.config.DropOffConfig;

public record ReportTask(int itemsCounter, int affectedContainers, int totalContainers,
                         List<RendererCubeTarget> rendererCubeTargets) implements Runnable {

    @Override
    public void run() {
        if (DropOffConfig.Client.highlightContainers.get()) {
            RenderWorldLastEventHandler.RendererCube.draw(rendererCubeTargets);
        }

        if (DropOffConfig.Client.displayMessage.get()) {
            String message = red(String.valueOf(itemsCounter)) +
                " items moved to " + red(String.valueOf(affectedContainers)) +
                " containers of " + red(String.valueOf(totalContainers)) +
                " checked in total.";

            ClientUtils.printToChat(message);
        }
    }
}
