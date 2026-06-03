package tfar.quickstack.network;

import net.minecraft.resources.ResourceLocation;

import tfar.quickstack.QuickStack;
import tfar.quickstack.network.server.C2SFavoriteItemPacket;
import tfar.quickstack.network.server.C2SPacketRequestDropoff;
import tfar.quickstack.network.client.S2CReportPacket;
import tfar.quickstack.platform.Services;

import java.util.Locale;

public class PacketHandler {

    public static void registerPackets() {

        Services.PLATFORM.registerServerPacket(C2SPacketRequestDropoff.TYPE, C2SPacketRequestDropoff.STREAM_CODEC);
        Services.PLATFORM.registerClientPacket(S2CReportPacket.TYPE, S2CReportPacket.STREAM_CODEC);
        Services.PLATFORM.registerServerPacket(C2SFavoriteItemPacket.TYPE, C2SFavoriteItemPacket.STREAM_CODEC);
        ///////server to client

    }

    public static ResourceLocation packet(Class<?> clazz) {
        return QuickStack.id(clazz.getName().toLowerCase(Locale.ROOT));
    }

}
