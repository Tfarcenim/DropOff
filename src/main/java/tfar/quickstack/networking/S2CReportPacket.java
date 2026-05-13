package tfar.quickstack.networking;

import java.util.List;

import net.minecraft.network.FriendlyByteBuf;
import tfar.quickstack.client.RendererCubeTarget;
import tfar.quickstack.task.ReportTask;

public record S2CReportPacket(int itemsCounter, int affectedContainers, int totalContainers,
                              List<RendererCubeTarget> rendererCubeTargets) implements S2CPacket {

    public S2CReportPacket(FriendlyByteBuf buf) {
        this(buf.readInt(), buf.readInt(), buf.readInt(), buf.readList(RendererCubeTarget::read));
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeInt(itemsCounter);
        buf.writeInt(affectedContainers);
        buf.writeInt(totalContainers);

        buf.writeCollection(rendererCubeTargets, (buf1, rendererCubeTarget) ->
            rendererCubeTarget.write(buf1));
    }

    public void handleClient() {
        ReportTask reportTask = new ReportTask(itemsCounter, affectedContainers,
            totalContainers, rendererCubeTargets);
        reportTask.run();
    }
}
