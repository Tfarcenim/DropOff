package tfar.quickstack.client;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record RendererCubeTarget(BlockPos blockPos, int color) {
    public static final StreamCodec<RegistryFriendlyByteBuf,RendererCubeTarget> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC,RendererCubeTarget::blockPos, ByteBufCodecs.INT,RendererCubeTarget::color,RendererCubeTarget::new);
}
