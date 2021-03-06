package scp002.quickstack.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.screen.inventory.CreativeScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.fml.client.gui.GuiUtils;
import net.minecraftforge.fml.client.gui.widget.ExtendedButton;

import java.util.ArrayList;
import java.util.List;

class DropOffGuiButton extends ExtendedButton {

  public boolean dump;
  final List<String> hoverText = new ArrayList<>();

  DropOffGuiButton(int xPos, int yPos, Button.IPressable callback, boolean b) {
    super(xPos, yPos, 10, 15, "^",callback);
    this.dump = b;
    if (dump)
    hoverText.add("Dump to Nearby Chests");
    else
    hoverText.add("Quick Stack to Nearby Chests");
  }

  @Override
  public void playDownSound(SoundHandler p_playDownSound_1_) {
  }

  @Override
  public void render(int p_render_1_, int p_render_2_, float p_render_3_) {
    Minecraft mc = Minecraft.getInstance();
    this.visible = !(mc.currentScreen instanceof CreativeScreen) || ((CreativeScreen)Minecraft.getInstance().currentScreen).getSelectedTabIndex() == ItemGroup.INVENTORY.getIndex();
    super.render(p_render_1_, p_render_2_, p_render_3_);
    if (visible)
    renderToolTip(p_render_1_,p_render_2_);
  }

  @Override
  public void renderToolTip(int p_renderToolTip_1_, int p_renderToolTip_2_) {
    if (isHovered){
      RenderSystem.enableDepthTest();
      Minecraft mc = Minecraft.getInstance();
      int guiwidth = mc.currentScreen.width;
      int guiheight = mc.currentScreen.height;
      GuiUtils.drawHoveringText(hoverText,p_renderToolTip_1_+ 10,p_renderToolTip_2_ -10,guiwidth,guiheight,100,mc.fontRenderer);
      RenderSystem.disableDepthTest();
    }
  }
}
