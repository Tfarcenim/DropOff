package scp002.mod.dropoff.inventory;

import net.minecraft.util.math.BlockPos;

public class InventoryData {

    public final BlockPos pos;
    public boolean success = false;

    public InventoryData(BlockPos pos) {
        this.pos = pos;
    }

    public void setSuccessful(){
        success=true;
    }
}
