package tfar.quickstack.network.client;

import java.util.List;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import tfar.quickstack.client.RendererCubeTarget;
import tfar.quickstack.network.PacketHandler;
import tfar.quickstack.task.ReportTask;

public record S2CReportPacket(int itemsCounter, int affectedContainers, int totalContainers,
                              List<RendererCubeTarget> rendererCubeTargets) implements S2CModPacket {


    public static final StreamCodec<RegistryFriendlyByteBuf, S2CReportPacket> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.INT, S2CReportPacket::itemsCounter,
            ByteBufCodecs.INT, S2CReportPacket::affectedContainers,
            ByteBufCodecs.INT, S2CReportPacket::totalContainers,
            RendererCubeTarget.STREAM_CODEC.apply(ByteBufCodecs.list()),S2CReportPacket::rendererCubeTargets,
            S2CReportPacket::new);

    public static final Type<S2CReportPacket> TYPE = new Type<>(PacketHandler.packet(S2CReportPacket.class));

    public void handleClient() {
        ReportTask reportTask = new ReportTask(itemsCounter, affectedContainers,
            totalContainers, rendererCubeTargets);
        reportTask.run();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
