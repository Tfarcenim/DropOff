package tfar.quickstack.client;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public record RendererCubeTarget(BlockPos blockPos, int color) {

    public void write(FriendlyByteBuf buf) {
       buf.writeLong(blockPos().asLong());
       buf.writeInt(color);
    }

    public static RendererCubeTarget read(FriendlyByteBuf buf) {
        BlockPos blockPos = BlockPos.of(buf.readLong());
        int color = buf.readInt();
        return new RendererCubeTarget(blockPos, color);
    }
}
