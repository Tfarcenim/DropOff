package tfar.quickstack.config;

import com.google.common.collect.Lists;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;
import tfar.quickstack.QuickStack;

import java.util.List;
import java.util.stream.Collectors;

public class QuickStackConfig {

    final String categoryGeneral = "general";

    public static ModConfigSpec.IntValue scanRadius;

    public static final Client CLIENT;
    public static final ModConfigSpec CLIENT_SPEC;

    public static final QuickStackConfig SERVER;
    public static final ModConfigSpec SERVER_SPEC;

    static {
        final Pair<Client, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(Client::new);
        CLIENT_SPEC = specPair.getRight();
        CLIENT = specPair.getLeft();
        final Pair<QuickStackConfig, ModConfigSpec> specPair2 = new ModConfigSpec.Builder()
                .configure(QuickStackConfig::new);
        SERVER_SPEC = specPair2.getRight();
        SERVER = specPair2.getLeft();
    }

    public QuickStackConfig(ModConfigSpec.Builder builder) {
        builder.push(categoryGeneral);

        scanRadius = builder.comment("Radius in blocks to check containers around the player.")
                .defineInRange("Scan radius", DefaultValues.scanRadius, 0, Integer.MAX_VALUE);

        builder.pop();
    }

    private static class DefaultValues {

        private static final boolean ignoreHotbar = true;
        private static final boolean displayMessage = true;
        private static final boolean highlightContainers = true;
        private static final boolean showInventoryButton = true;

        private static final int minSlots = 6;
        private static final int creativeInventoryButtonXOffset = 71;
        private static final int creativeInventoryButtonYOffset = -63;
        private static final int highlightDelay = 3000;
        private static final int scanRadius = 6;
        private static final int survivalInventoryButtonXOffset = 50;
        private static final int survivalInventoryButtonYOffset = -18;
        private static final List<String> blacklist = Lists.newArrayList("minecraft:furnace", "minecraft:blast_furnace",
                "minecraft:smoker");

        private static final List<String> container_whitelist = Lists.newArrayList("curios:curios_container");

    }

    public static class Client {

        public static ModConfigSpec.BooleanValue highlightContainers;
        public static ModConfigSpec.BooleanValue showInventoryButton;
        public static ModConfigSpec.BooleanValue displayMessage;
        public static ModConfigSpec.BooleanValue ignoreHotBar;
        public static ModConfigSpec.BooleanValue enableDump;

        public static ModConfigSpec.IntValue creativeInventoryButtonXOffset;
        public static ModConfigSpec.IntValue creativeInventoryButtonYOffset;
        public static ModConfigSpec.IntValue minSlotCount;

        public static ModConfigSpec.IntValue survivalInventoryButtonXOffset;
        public static ModConfigSpec.IntValue survivalInventoryButtonYOffset;

        public static ModConfigSpec.IntValue highlightDelay;

        public static ModConfigSpec.ConfigValue<? extends String> favorite_color;

        private static ModConfigSpec.ConfigValue<List<String>> blacklistedTes;

        public static ModConfigSpec.ConfigValue<List<String>> whitelistedContainers;

        public Client(ModConfigSpec.Builder builder) {
            builder.push("general");
            // booleans
            enableDump = builder.comment("Enable dump button.").define("Enable Dump Button", true);
            ignoreHotBar = builder.comment("Ignore hotbar when transferring.").define("Ignore Hotbar", true);
            highlightContainers = builder.comment("Highlight nearby containers.").define("Highlight containers",
                    DefaultValues.highlightContainers);
            displayMessage = builder.comment(" information to the chat when task is complete.")
                    .define("Display Message", DefaultValues.displayMessage);
            showInventoryButton = builder.comment("Show button in the player inventory.")
                    .define("Show inventory button", DefaultValues.showInventoryButton);


            // Integers
            creativeInventoryButtonXOffset = builder.comment("Creative inventory button position width offset.")
                    .defineInRange("Creative inventory button X offset",
                            DefaultValues.creativeInventoryButtonXOffset, Integer.MIN_VALUE, Integer.MAX_VALUE);

            creativeInventoryButtonYOffset = builder.comment("Creative inventory button position height offset.")
                    .defineInRange("Creative inventory button Y offset",
                            DefaultValues.creativeInventoryButtonYOffset, Integer.MIN_VALUE, Integer.MAX_VALUE);

            highlightDelay = builder.comment("Blocks highlighting delay in milliseconds. Delay < 0 means forever.")
                    .defineInRange("Highlight delay", DefaultValues.highlightDelay, -1, Integer.MAX_VALUE);
            minSlotCount = builder.comment("Min number of slots that a " +
                    "container can be eligible for transfer to, this will exclude furnaces and most machines with a low slot count.")
                    .defineInRange("Minimum Slots", DefaultValues.minSlots, 0, Integer.MAX_VALUE);

            survivalInventoryButtonXOffset = builder.comment("Survival inventory button position width offset.")
                    .defineInRange("Survival inventory button X offset",
                            DefaultValues.survivalInventoryButtonXOffset, Integer.MIN_VALUE, Integer.MAX_VALUE);

            survivalInventoryButtonYOffset = builder.comment("Survival inventory button position height offset.")
                    .defineInRange("Survival inventory button Y offset",
                            DefaultValues.survivalInventoryButtonYOffset, Integer.MIN_VALUE, Integer.MAX_VALUE);

            //other

            favorite_color = builder.comment("favorites color background").define("favorite_color","#FFFFBB");

            blacklistedTes = builder.define("Blacklisted Block Entities", DefaultValues.blacklist);

            whitelistedContainers = builder.define("Whitelisted containers", DefaultValues.container_whitelist);

        }
    }

    public static List<BlockEntityType<?>> blockEntityBlacklist;
    public static int favorite_color_cache;

    public static void onConfigChanged() {
        blockEntityBlacklist = QuickStackConfig.Client.blacklistedTes.get()
            .stream()
            .map(ResourceLocation::parse).filter(resourceLocation -> {
                boolean b = BuiltInRegistries.BLOCK_ENTITY_TYPE.containsKey(resourceLocation);
                if (!b) {
                    QuickStack.LOG.warn("Ignoring unknown blockentity: " + resourceLocation);
                }
                return b;
            })
            .map(BuiltInRegistries.BLOCK_ENTITY_TYPE::get)
            .collect(Collectors.toList());

        favorite_color_cache = Integer.decode(QuickStackConfig.Client.favorite_color.get());

        QuickStack.LOG.info("Configuration changed.");
    }
}
