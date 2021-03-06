package getta.craftingpanel;

import fi.dy.masa.malilib.gui.GuiBase;
import getta.craftingpanel.gui.CraftingPanelScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class CraftingPanel implements ClientModInitializer {

    private static KeyBinding keyBinding;
    public static List<CraftingPanelItemOutput> items = null;
    public static boolean hud = false;

    @Override
    public void onInitializeClient() {
        keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding("Crafting panel", InputUtil.Type.KEYSYM, -1, "Crafting panel"));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if(keyBinding.isPressed()) {
                GuiBase.openGui(new CraftingPanelScreen(client.options.guiScale, null));
                client.options.guiScale = 2;
                client.onResolutionChanged();
            }
        });
    }

    public static void createScreenWithMaterials(List<CraftingPanelItemOutput> items) {
        int preButton = MinecraftClient.getInstance().options.guiScale;
        GuiBase.openGui(new CraftingPanelScreen(preButton, items));
        MinecraftClient.getInstance().options.guiScale = 2;
        MinecraftClient.getInstance().onResolutionChanged();
    }
}
