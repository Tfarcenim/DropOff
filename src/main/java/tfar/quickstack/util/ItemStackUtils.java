package tfar.quickstack.util;

import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

//prep for data components
public class ItemStackUtils {
    public static boolean isFavorite(ItemStack stack) {
        return getBooleanOrDefault(stack, FAVORITE,false);
    }

    public static void setFavorite(ItemStack stack) {
        setBoolean(stack,FAVORITE,true);
    }

    public static void removeFavorite(ItemStack stack) {
        setBoolean(stack,FAVORITE,null);
    }

    public static final String FAVORITE = "favorite";

    public static Boolean getBoolean(ItemStack stack, String key) {//booleans are bytes internally
        return stack.hasTag() && stack.getTag().contains(key, Tag.TAG_BYTE) ? stack.getTag().getBoolean(key) : null;
    }

    public static Boolean getBooleanOrDefault(ItemStack stack, String key,boolean defaultValue) {
        return stack.hasTag() && stack.getTag().contains(key, Tag.TAG_BYTE) ? stack.getTag().getBoolean(key) : defaultValue;
    }

    public static void setBoolean(ItemStack stack, String key, Boolean value) {
        if (value == null) {
            stack.removeTagKey(key);
        } else {
            stack.getOrCreateTag().putBoolean(key,value);
        }
    }
}
