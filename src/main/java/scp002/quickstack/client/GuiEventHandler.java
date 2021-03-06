package scp002.quickstack.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.screen.inventory.CreativeScreen;
import net.minecraft.client.gui.screen.inventory.HorseInventoryScreen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.client.event.GuiContainerEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import scp002.quickstack.DropOff;
import scp002.quickstack.config.DropOffConfig;
import scp002.quickstack.message.C2SFavoriteItemPacket;
import scp002.quickstack.message.PacketHandler;
import scp002.quickstack.util.Utils;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static net.minecraft.client.gui.AbstractGui.fill;

public class GuiEventHandler {

	@SubscribeEvent
	public void onGuiOpen(GuiScreenEvent.InitGuiEvent.Post event) {
		if (!canDisplay(event.getGui()) || !DropOffConfig.Client.showInventoryButton.get()) {
			return;
		}

		ContainerScreen containerScreen = (ContainerScreen) event.getGui();

		boolean isCreative = Minecraft.getInstance().player.abilities.isCreativeMode;

		int xPos = containerScreen.getGuiLeft() + 80 +
						(isCreative ? DropOffConfig.Client.creativeInventoryButtonXOffset.get() :
										DropOffConfig.Client.survivalInventoryButtonXOffset.get());
		int yPos = containerScreen.getGuiTop() + 80 + (isCreative ?
						DropOffConfig.Client.creativeInventoryButtonYOffset.get() :
						DropOffConfig.Client.survivalInventoryButtonYOffset.get());
		if (DropOffConfig.Client.enableDump.get()) {
			Button dump = new DropOffGuiButton(xPos, yPos, this::actionPerformed, true);
			event.addWidget(dump);
		}
		Button deposit = new DropOffGuiButton(xPos + 12, yPos, this::actionPerformed, false);
		event.addWidget(deposit);
	}

	protected void actionPerformed(@Nonnull net.minecraft.client.gui.widget.button.Button button) {
		ClientUtils.sendNoSpectator(((DropOffGuiButton) button).dump);
	}

	@SubscribeEvent
	@SuppressWarnings("unchecked")
	public <T extends Container> void onItemClick(GuiScreenEvent.MouseClickedEvent.Pre event) {
		if (!canDisplay(event.getGui()) || !(event.getGui() instanceof InventoryScreen) || !Screen.hasControlDown()) return;
		ContainerScreen<T> containerScreen = (ContainerScreen<T>) event.getGui();
		double mouseX = event.getMouseX();
		double mouseY = event.getMouseY();

		containerScreen.getContainer().inventorySlots
						.stream()
						.filter(s -> containerScreen.isSlotSelected(s, mouseX, mouseY) && s.isEnabled())
						.findFirst()
						.ifPresent(slot -> {
											event.setCanceled(true);
											PacketHandler.INSTANCE.sendToServer(new C2SFavoriteItemPacket(slot.slotNumber));
										}
						);
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	@SuppressWarnings("unchecked")
	public <T extends Container> void drawFavorites(GuiContainerEvent.DrawBackground event) {
		ContainerScreen<T> containerScreen = event.getGuiContainer();
		if (!canDisplay(containerScreen)) return;
		T playerContainer = containerScreen.getContainer();

		for (int k = 0; k < 3; ++k) {
			for (int j = 0; j < 9; ++j) {
				Slot slot = playerContainer.inventorySlots.get(j + (k + 1) * 9);
				if (slot == null) continue;
				ItemStack stack = slot.getStack();
				if (Utils.isFavorited(stack)) {
					int xoffset = 8;
					int yoffset = 84;
					fill(containerScreen.getGuiLeft() + j * 18 + xoffset,
									containerScreen.getGuiTop() + k * 18 + yoffset,
									containerScreen.getGuiLeft() + j * 18 + 16 + xoffset,
									containerScreen.getGuiTop() + k * 18 + 16 + yoffset,
									0xFFFFBB00);
				}
			}
		}
		List<ItemStack> stacks = playerContainer.getInventory();

		for (int i = 0; i < 9; ++i) {
			ItemStack stack = stacks.get(i + 36);
			if (Utils.isFavorited(stack)) {
				int xoffset = 8;
				int yoffset = 142;
				fill(containerScreen.getGuiLeft() + i * 18 + xoffset,
								containerScreen.getGuiTop() + yoffset,
								containerScreen.getGuiLeft() + i * 18 + 16 + xoffset,
								containerScreen.getGuiTop() + 16 + yoffset,
								0xFFFFBB00);
			}
		}

		RenderSystem.color4f(1, 1, 1, 1);
	}

	public boolean canDisplay(Screen screen) {
		return screen instanceof ContainerScreen && canDisplay((ContainerScreen) screen);
	}

	private static final Set<Class<?>> bad_classes = new HashSet<>();

	public <T extends Container> boolean canDisplay(ContainerScreen<T> screen) {
		if (screen instanceof InventoryScreen || screen instanceof CreativeScreen) return true;
		if (screen instanceof HorseInventoryScreen) return false;
		try {
			return DropOffConfig.Client.whitelistedContainers.get().contains(screen.getContainer().getType().getRegistryName().toString());
		} catch (Exception e) {
			Class<?> clazz = screen.getContainer().getClass();
			if (bad_classes.contains(clazz)) {
				DropOff.LOGGER.error(clazz + " does not have a container type registered to it! " +
								"This is a bug in the other mod and should be reported to them.  The buttons will not display in this gui!");
				bad_classes.add(clazz);
			}
			return false;
		}
	}

	@SubscribeEvent
	public void tooltip(ItemTooltipEvent e) {
		ItemStack stack = e.getItemStack();
		if (stack.hasTag() && stack.getTag().getBoolean("favorite")) {
			e.getToolTip().add(new StringTextComponent("Favorited!"));
		}
	}
}
