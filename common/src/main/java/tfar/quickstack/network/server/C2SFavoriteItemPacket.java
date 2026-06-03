package tfar.quickstack.network.server;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import tfar.quickstack.network.PacketHandler;
import tfar.quickstack.util.ItemStackUtils;

public record C2SFavoriteItemPacket(int slotId) implements C2SModPacket {


    public static final StreamCodec<RegistryFriendlyByteBuf, C2SFavoriteItemPacket> STREAM_CODEC =
        StreamCodec.composite(ByteBufCodecs.INT, C2SFavoriteItemPacket::slotId, C2SFavoriteItemPacket::new);

    public static final Type<C2SFavoriteItemPacket> TYPE = new Type<>(PacketHandler.packet(C2SFavoriteItemPacket.class));

    public void handleServer(ServerPlayer player) {
        Slot slot = player.containerMenu.getSlot(slotId);
        ItemStack stack = slot.getItem();

        if (ItemStackUtils.isFavorite(stack)) {
            ItemStackUtils.removeFavorite(stack);
        } else {
            ItemStackUtils.setFavorite(stack);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
