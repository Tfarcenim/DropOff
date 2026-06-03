package tfar.quickstack.util;

import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import tfar.quickstack.FavDataComponent;

public class ItemStackUtils {
    public static boolean isFavorite(ItemStack stack) {
        return stack.has(FavDataComponent.TYPE);
    }

    public static void setFavorite(ItemStack stack) {
        stack.set(FavDataComponent.TYPE, Unit.INSTANCE);
    }

    public static void removeFavorite(ItemStack stack) {
        stack.remove(FavDataComponent.TYPE);
    }
}
