package tfar.quickstack.networking;

import net.minecraft.network.FriendlyByteBuf;

public interface DropoffPacket {
    void write(FriendlyByteBuf to);
}
