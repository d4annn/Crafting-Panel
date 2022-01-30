package getta.craftingpanel.mixins;

import getta.craftingpanel.CraftingPanel;
import getta.craftingpanel.Utils;
import getta.craftingpanel.CraftingPanelItemOutput;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Shadow private int scaledHeight;

    @Shadow private int scaledWidth;

    @Inject(method = "render", at = @At("HEAD"))
    private void hud(MatrixStack matrices, float tickDelta, CallbackInfo ci) {

        if(CraftingPanel.hud && CraftingPanel.items != null) {
            int yQuantity = 0;
            int xQuantity = 0;

            for(CraftingPanelItemOutput item : CraftingPanel.items) {

                int missing = Utils.getMissing(item, false);

                if(missing == 0) {

                    continue;
                }

                MinecraftClient.getInstance().getItemRenderer().renderInGui(Utils.getItemStackFromItemCommandOutputName(item.getName()),
                        this.scaledWidth - 20 - MinecraftClient.getInstance().textRenderer.getWidth("x" + missing) - xQuantity,
                        this.scaledHeight - 28 - yQuantity);

                MinecraftClient.getInstance().textRenderer.drawWithShadow(matrices,"x" + missing,
                        this.scaledWidth - 2 - MinecraftClient.getInstance().textRenderer.getWidth("x" + missing) - xQuantity,
                    this.scaledHeight - 23 - yQuantity, Color.WHITE.getRGB());

                yQuantity += 20;

                if((this.scaledHeight - yQuantity) - 60 <= 0) {
                    yQuantity = 0;
                    xQuantity += 22 + MinecraftClient.getInstance().textRenderer.getWidth("x " + 222);
                }
            }
        }
     }
}
