package tfar.quickstack.mixin;

import tfar.quickstack.QuickStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class MixinTitleScreen {

    @Inject(at = @At("HEAD"), method = "init()V")
    private void init(CallbackInfo info) {

        QuickStack.LOG.info("This line is printed by an example mod mixin from Fabric!");
        QuickStack.LOG.info("MC Version: {}", Minecraft.getInstance().getVersionType());
    }
}
