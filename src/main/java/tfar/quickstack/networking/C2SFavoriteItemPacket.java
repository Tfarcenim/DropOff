package tfar.quickstack.networking;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import tfar.quickstack.util.ItemStackUtils;

public record C2SFavoriteItemPacket(int slotId) implements C2SPacket {

    public C2SFavoriteItemPacket(FriendlyByteBuf buf) {
        this(buf.readInt());
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeInt(slotId);
    }

    public void handleServer(ServerPlayer player) {
        Slot slot = player.containerMenu.getSlot(slotId);
        ItemStack stack = slot.getItem();

        CompoundTag stackTag = stack.getTag();
        if (ItemStackUtils.isFavorite(stack)) {
            ItemStackUtils.removeFavorite(stack);
        } else {
            ItemStackUtils.setFavorite(stack);
        }
    }
}
