package tfar.quickstack.task;

import java.util.List;

import tfar.quickstack.client.QuickStackClient;
import tfar.quickstack.client.RendererCubeTarget;
import tfar.quickstack.config.QuickStackConfig;
import tfar.quickstack.util.MessageUtils;

public record ReportTask(int itemsCounter, int affectedContainers, int totalContainers,
                         List<RendererCubeTarget> rendererCubeTargets) implements Runnable {

    @Override
    public void run() {
        if (QuickStackConfig.Client.highlightContainers.get()) {
            QuickStackClient.draw(rendererCubeTargets);
        }

        if (QuickStackConfig.Client.displayMessage.get()) {
            String message = MessageUtils.red(String.valueOf(itemsCounter)) +
                " items moved to " + MessageUtils.red(String.valueOf(affectedContainers)) +
                " containers of " + MessageUtils.red(String.valueOf(totalContainers)) +
                " checked in total.";

            QuickStackClient.printToChat(message);
        }
    }
}
