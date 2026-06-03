package tfar.quickstack.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;

public class DropoffButton extends Button {
    public DropoffButton(int pX, int pY, int pWidth, int pHeight, Component pMessage, OnPress pOnPress) {
        super(pX, pY, pWidth, pHeight, pMessage, pOnPress, DEFAULT_NARRATION);
    }


    @Override
    protected void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        if (Minecraft.getInstance().screen instanceof CreativeModeInventoryScreen screen) {
            boolean isInventoryTab = screen.isInventoryOpen();
            this.active = isInventoryTab;
            if (!isInventoryTab) {
                return;
            }
        }
        super.renderWidget(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
    }
}
