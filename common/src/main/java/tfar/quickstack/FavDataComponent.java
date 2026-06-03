package tfar.quickstack;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Unit;

import java.util.function.UnaryOperator;

public class FavDataComponent {
    public static final DataComponentType<Unit> TYPE = register("favorite",unitBuilder ->
        unitBuilder.persistent(Codec.unit(Unit.INSTANCE))
        .networkSynchronized(StreamCodec.unit(Unit.INSTANCE)));

    private static <T> DataComponentType<T> register(String name, UnaryOperator<DataComponentType.Builder<T>> builder) {
        return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, QuickStack.id(name), builder.apply(DataComponentType.builder()).build());
    }

    public static void init() {

    }
}
