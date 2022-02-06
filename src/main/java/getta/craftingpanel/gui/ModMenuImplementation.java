package getta.craftingpanel.gui;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import fi.dy.masa.malilib.gui.GuiBase;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class ModMenuImplementation implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {

        return screen -> new Screen(Text.of("")) {
            @Override
            protected void init() {
                CraftingPanelScreen configScreen = new CraftingPanelScreen(MinecraftClient.getInstance().options.guiScale);
                assert client != null;
                client.options.guiScale = 2;
                client.onResolutionChanged();
                GuiBase.openGui(configScreen);
                MinecraftClient.getInstance().setScreen(null);
                GuiBase.openGui(configScreen);
            }
        };
    }
}
