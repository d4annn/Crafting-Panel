package getta.craftingpanel.gui;

import fi.dy.masa.malilib.data.DataDump;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.util.FileUtils;
import fi.dy.masa.malilib.util.GuiUtils;
import fi.dy.masa.malilib.util.StringUtils;
import getta.craftingpanel.CraftingPanel;
import getta.craftingpanel.CraftingPanelItemOutput;
import getta.craftingpanel.Utils;
import getta.craftingpanel.gui.widgets.ItemsWidget;
import getta.craftingpanel.gui.widgets.ResultsCraftedWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;

import java.io.File;
import java.util.List;

public class CraftingPanelScreen extends GuiBase {

    protected ItemsWidget itemsWidget;
    protected ResultsCraftedWidget resultsCraftedWidget;
    private final int preButton;
    private final int width;
    private final int height;

    public CraftingPanelScreen(int preButton, List<CraftingPanelItemOutput> items) {
        super();

        this.preButton = preButton;
        this.title = StringUtils.translate("Crafting Panel");
        this.width = MinecraftClient.getInstance().getWindow().getWidth();
        this.height = MinecraftClient.getInstance().getWindow().getHeight();

        this.itemsWidget = new ItemsWidget(10, (int)(height / 8.3), width / 6 , (int) (height / 3.9));
        this.resultsCraftedWidget = new ResultsCraftedWidget((int) (width / 3.2), (int) (height / 8.3), (int)(width / 5.5f), (int) (height / 2.92), items);
    }

    @Override
    public void initGui() {

        super.initGui();

        this.clearWidgets();
        this.clearButtons();
        this.createButtons();
    }

    protected void createButtons() {

        super.initGui();

        //Widgets instance
        String name = StringUtils.translate("Calculate Materials");
        ButtonGeneric calculateMaterialsButton = new ButtonGeneric(20 + textRenderer.getWidth("Calculate Materials"), GuiUtils.getScaledWindowHeight() - 30, -1, true, name);
        ButtonGeneric clearResults = new ButtonGeneric(this.resultsCraftedWidget.getX() + this.textRenderer.getWidth("Clear Result###|"), GuiUtils.getScaledWindowHeight() - 30, -1, true, "Clear Results");
        ButtonGeneric addToList = new ButtonGeneric((int) (this.itemsWidget.getX() + 22 * 4.8), (int) (this.itemsWidget.getHeight() * 1.5f + 30), -1, true, GuiBase.TXT_GREEN + "+");
        ButtonGeneric removeLastSelected = new ButtonGeneric((int) (this.width / 5.8), (int) (this.itemsWidget.getHeight() * 1.5f - 6) + 58, -1, true, "Remove Last");
        ButtonGeneric clearSelected = new ButtonGeneric((int) (this.width / 5.8), (int) (this.itemsWidget.getHeight() * 1.5f + 31), -1, true, "Clear Selected");
        ButtonGeneric changeTypeButton = new ButtonGeneric(this.resultsCraftedWidget.getX() + this.textRenderer.getWidth("Clear Result#############|@#~€###|") + 4, GuiUtils.getScaledWindowHeight() - 30, -1, true, "Change Output Type");
        ButtonGeneric hud = new ButtonGeneric(51 + this.textRenderer.getWidth("Calculate Materials  Export"), GuiUtils.getScaledWindowHeight() - 30, -1, true, "HUD");
        ButtonGeneric export = new ButtonGeneric(64 + this.textRenderer.getWidth("Calculate Materials"), GuiUtils.getScaledWindowHeight() - 30, -1, true, "Export");
        int scaledWidth = GuiUtils.getScaledWindowWidth();
        ButtonGeneric removeList = new ButtonGeneric((int) (this.width / 5.8) + (int) ((scaledWidth - scaledWidth / 1.5) - scaledWidth / 6) + 20, (int)(GuiUtils.getScaledWindowHeight() / 2.5), -1, true, GuiBase.TXT_RED + "-");
        ButtonGeneric clearSearch = new ButtonGeneric(((width / 6) + 110) / 2, (int)(height / 8.3) - 22, -1, true, "Clear");

        this.addWidget(this.itemsWidget);
        this.addWidget(this.resultsCraftedWidget);

        this.addButton(calculateMaterialsButton, (btn, mbtn) -> {
            this.resultsCraftedWidget.receiveResults(this.itemsWidget.convertSelectionsToResults());
        });

        this.addButton(clearResults, (btn, mbtn) -> {
            this.resultsCraftedWidget.clearResults();
        });

        this.addButton(addToList, (btn, mbtn) -> {
            this.itemsWidget.addToList();
        });

        this.addButton(removeLastSelected, (btn, mbtn) -> {
            this.itemsWidget.removeLast();
        });

        this.addButton(clearSelected, (btn, mbtn) -> {
            this.itemsWidget.clearSelected();
        });

        this.addButton(changeTypeButton, (btn, mbtn) -> {
            this.resultsCraftedWidget.cycle();
        });

        this.addButton(hud, (btn, mbtn) -> {
            this.resultsCraftedWidget.setHud();
        });

        this.addButton(export, (btn, mbtn) -> {
            File dir = new File(FileUtils.getConfigDirectory(), "Crafting Panel");
            File file = DataDump.dumpDataToFile(dir, "material_list", ".txt", Utils.getMaterialsListDump(Utils.convertItemToMaterials(this.itemsWidget.convertSelectionsToResults())).getLines());
        });

        this.addButton(removeList, (btn, mbtn) -> {
            this.itemsWidget.removeListSelected();
        });

        this.addButton(clearSearch, (btn, mbtn) -> {
            this.itemsWidget.clearSearch();
        });
    }

    @Override
    protected void closeGui(boolean showParent) {
        super.closeGui(showParent);
        mc.options.guiScale = preButton;
        mc.onResolutionChanged();
    }
}
