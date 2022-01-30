package getta.craftingpanel.mixins;

import getta.craftingpanel.CraftingPanel;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public class ScreenMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void getScreen(CallbackInfo ci){
        Screen screen = (Screen)((Object)this);
        if(CraftingPanel.screen == null || CraftingPanel.screen.width < screen.width || CraftingPanel.screen.height < screen.height) {
            CraftingPanel.screen = (Screen) ((Object) this);
        }
    }
}
