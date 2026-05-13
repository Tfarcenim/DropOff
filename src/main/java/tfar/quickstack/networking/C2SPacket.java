package tfar.quickstack.networking;

import net.minecraft.server.level.ServerPlayer;

public interface C2SPacket extends DropoffPacket {
    void handleServer(ServerPlayer player);

}
