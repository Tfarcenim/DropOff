package tfar.quickstack;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.HorseInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import tfar.quickstack.client.DropoffButton;
import tfar.quickstack.client.QuickStackClient;
import tfar.quickstack.client.events.HotkeysRegistrar;
import tfar.quickstack.config.QuickStackConfig;
import tfar.quickstack.network.PacketHandler;
import tfar.quickstack.network.server.C2SFavoriteItemPacket;
import tfar.quickstack.platform.Services;
import tfar.quickstack.util.ItemStackUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mod(value = QuickStack.MOD_ID,dist = Dist.CLIENT)
public class QuickStackClientNeoForge {

    public QuickStackClientNeoForge(IEventBus bus) {
        bus.addListener(this::registerBindings);
        NeoForge.EVENT_BUS.addListener(this::tryToRender);
        NeoForge.EVENT_BUS.addListener(this::onGuiOpen);
        NeoForge.EVENT_BUS.addListener(this::onItemClick);
        NeoForge.EVENT_BUS.addListener(this::tooltip);
        NeoForge.EVENT_BUS.addListener(this::onClientTick);
    }

    void registerBindings(RegisterKeyMappingsEvent e) {
        e.register(HotkeysRegistrar.DUMP_MAPPING);
        e.register(HotkeysRegistrar.DEPOSIT_MAPPING);
    }

    public void onClientTick(ClientTickEvent.Post event) {
        QuickStackClient.tick();
    }

    /**
     * This method called by RenderWorldLastEvent handler.
     * It does nothing until the draw() method assign the necessary delay to the
     * global field named currentTime.
     */
    void tryToRender(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
        QuickStackClient.renderBlocks(event.getPoseStack());
    }

    public void onGuiOpen(ScreenEvent.Init.Post event) {
        if (!canDisplay(event.getScreen()) || !QuickStackConfig.Client.showInventoryButton.get()) {
            return;
        }

        AbstractContainerScreen<?> containerScreen = (AbstractContainerScreen<?>) event.getScreen();

        boolean isCreative = Minecraft.getInstance().player.getAbilities().instabuild;

        int xPos = containerScreen.getGuiLeft() + 80 +
            (isCreative ? QuickStackConfig.Client.creativeInventoryButtonXOffset.get()
                : QuickStackConfig.Client.survivalInventoryButtonXOffset.get());
        int yPos = containerScreen.getGuiTop() + 80
            + (isCreative ? QuickStackConfig.Client.creativeInventoryButtonYOffset.get()
            : QuickStackConfig.Client.survivalInventoryButtonYOffset.get());
        if (QuickStackConfig.Client.enableDump.get()) {
            DropoffButton dump = new DropoffButton(xPos, yPos,10,10,Component.literal("^"), b -> actionPerformed(true));
            dump.setTooltip(Tooltip.create(Component.translatable("dropoff.dump_nearby")));
            event.addListener(dump);
        }

        DropoffButton deposit = new DropoffButton(xPos + 12, yPos,10,10,Component.literal("^"),b -> actionPerformed(false));
        deposit.setTooltip(Tooltip.create(Component.translatable("dropoff.quick_stack")));
        event.addListener(deposit);
    }

    private static void actionPerformed(boolean dump) {
        QuickStackClient.sendNoSpectator(dump);
    }

    public void onItemClick(ScreenEvent.MouseButtonPressed.Pre event) {
        if (!canDisplay(event.getScreen()) || !(event.getScreen() instanceof InventoryScreen containerScreen)
            || !Screen.hasControlDown())
            return;

        Slot slotUnderMouse = containerScreen.getSlotUnderMouse();
        if (slotUnderMouse != null && slotUnderMouse.hasItem()) {
            event.setCanceled(true);
            Services.PLATFORM.sendToServer(new C2SFavoriteItemPacket(containerScreen.getSlotUnderMouse().index));
        }
    }

   /* @SubscribeEvent(priority = EventPriority.HIGH)
    @SuppressWarnings("unchecked")
    public static <T extends AbstractContainerMenu> void drawFavorites(ContainerScreenEvent.Render.Background event) {
        AbstractContainerScreen<T> containerScreen = (AbstractContainerScreen<T>) event.getContainerScreen();
        if (!canDisplay(containerScreen))
            return;
        T playerContainer = containerScreen.getMenu();
        GuiGraphics matrices = event.getGuiGraphics();

        for (int k = 0; k < 3; ++k) {
            for (int j = 0; j < 9; ++j) {
                Slot slot = playerContainer.slots.get(j + (k + 1) * 9);
                ItemStack stack = slot.getItem();
                if (ItemStackUtils.isFavorite(stack)) {
                    int xoffset = 8;
                    int yoffset = 84;
                    matrices.fill(containerScreen.getGuiLeft() + j * 18 + xoffset,
                        containerScreen.getGuiTop() + k * 18 + yoffset,
                        containerScreen.getGuiLeft() + j * 18 + 16 + xoffset,
                        containerScreen.getGuiTop() + k * 18 + 16 + yoffset,
                        QuickStackConfig.favorite_color_cache<<8);
                }
            }
        }
        List<ItemStack> stacks = playerContainer.getItems();

        for (int i = 0; i < 9; ++i) {
            ItemStack stack = stacks.get(i + 36);
            if (ItemStackUtils.isFavorite(stack)) {
                int xoffset = 8;
                int yoffset = 142;
                matrices.fill(containerScreen.getGuiLeft() + i * 18 + xoffset,
                    containerScreen.getGuiTop() + yoffset,
                    containerScreen.getGuiLeft() + i * 18 + 16 + xoffset,
                    containerScreen.getGuiTop() + 16 + yoffset,
                    QuickStackConfig.favorite_color_cache<<8);
            }
        }

        RenderSystem.clearColor(1, 1, 1, 1);
    }*/

    public static boolean canDisplay(Screen screen) {
        return screen instanceof AbstractContainerScreen && canDisplay((AbstractContainerScreen<?>) screen);
    }

    private static final Set<Class<?>> bad_classes = new HashSet<>();

    public static <T extends AbstractContainerMenu> boolean canDisplay(AbstractContainerScreen<T> screen) {
        if (screen instanceof InventoryScreen || screen instanceof CreativeModeInventoryScreen)
            return true;
        if (screen instanceof HorseInventoryScreen)
            return false;
        try {
            var screenMenuRegistry = BuiltInRegistries.MENU.getKey(screen.getMenu().getType()).toString();
            return QuickStackConfig.Client.whitelistedContainers.get().contains(screenMenuRegistry);
        } catch (Exception e) {
            Class<?> clazz = screen.getMenu().getClass();
            if (!bad_classes.contains(clazz)) {
                QuickStack.LOG.error(clazz + " does not have a container type registered to it! " +
                    "This is a bug in the other mod and should be reported to them.  The buttons will not display in this gui!");
                bad_classes.add(clazz);
            }
            return false;
        }
    }

    public void tooltip(ItemTooltipEvent e) {
        ItemStack stack = e.getItemStack();
        if (ItemStackUtils.isFavorite(stack)) {
            e.getToolTip().add(Component.translatable("dropoff.tooltip.favorited"));
        }
    }
}
