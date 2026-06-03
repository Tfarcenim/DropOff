package tfar.quickstack.util;

import net.minecraft.world.level.block.entity.BlockEntity;

public class SuccedableInventoryData {

    public final BlockEntity blockEntity;
    public boolean success = false;

    public SuccedableInventoryData(BlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    public void setSuccessful() {
        success = true;
    }
}
