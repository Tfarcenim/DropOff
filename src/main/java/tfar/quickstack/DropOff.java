package tfar.quickstack;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import tfar.quickstack.client.ClientUtils;
import tfar.quickstack.config.DropOffConfig;
import tfar.quickstack.networking.PacketHandler;
import tfar.quickstack.util.LogMessageFactory;

@Mod(DropOff.MOD_ID)
public class DropOff {

    public static final String MOD_ID = "quickstack";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID, LogMessageFactory.INSTANCE);

    public DropOff() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        bus.addListener(this::setup);

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, DropOffConfig.CLIENT_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, DropOffConfig.SERVER_SPEC);
        bus.addListener(DropOffConfig::onConfigChanged);

        if (FMLEnvironment.dist.isClient()) {
            ClientUtils.init(bus);
        }
    }

    private void setup(FMLCommonSetupEvent event) {
        PacketHandler.registerMessages();
    }
}
