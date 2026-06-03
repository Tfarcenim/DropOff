package tfar.quickstack.network.server;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import tfar.quickstack.network.PacketHandler;
import tfar.quickstack.platform.Services;

import java.util.List;

public record C2SPacketRequestDropoff(boolean ignoreHotbar, boolean dump, List<BlockEntityType<?>> teTypes,
                                      int minSlotCount) implements C2SModPacket {


    public static final StreamCodec<RegistryFriendlyByteBuf, C2SPacketRequestDropoff> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.BOOL, C2SPacketRequestDropoff::ignoreHotbar,
            ByteBufCodecs.BOOL, C2SPacketRequestDropoff::dump,
            ByteBufCodecs.registry(Registries.BLOCK_ENTITY_TYPE).apply(ByteBufCodecs.list()), C2SPacketRequestDropoff::teTypes,
            ByteBufCodecs.INT, C2SPacketRequestDropoff::minSlotCount,
            C2SPacketRequestDropoff::new);

    public static final Type<C2SPacketRequestDropoff> TYPE = new Type<>(PacketHandler.packet(C2SPacketRequestDropoff.class));

    @Override
    public void handleServer(ServerPlayer player) {
        Services.PLATFORM.handleDropOff(player,this);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
