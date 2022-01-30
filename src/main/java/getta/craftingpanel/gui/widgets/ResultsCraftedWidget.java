package getta.craftingpanel.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.widgets.WidgetBase;
import fi.dy.masa.malilib.render.RenderUtils;
import getta.craftingpanel.CraftingPanel;
import getta.craftingpanel.Utils;
import getta.craftingpanel.CraftingPanelItemOutput;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ResultsCraftedWidget extends WidgetBase {

    @NotNull List<CraftingPanelItemOutput> results;
    private int x;
    private int y;
    private int width;
    private int height;
    private boolean showResults;
    private List<CraftingPanelItemOutput> currentShow = null;

    public ResultsCraftedWidget(int x, int y, int width, int height) {
        super(x, y, width, height);

        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        this.results = null;
        this.showResults = false;
    }

    public void cycle() {
        if (Utils.cycle == Utils.outputTypes.length - 1) {
            Utils.cycle = 0;
        } else {
            Utils.cycle++;
        }
    }

    @Override
    public void render(int mouseX, int mouseY, boolean selected, MatrixStack matrixStack) {

        RenderUtils.color(1f, 1f, 1f, 1f);
        RenderSystem.pushMatrix();
        RenderSystem.translatef(0, 0, 1);

        RenderUtils.drawOutlinedBox(this.x, this.y, this.width, this.height + 4, 0xA0000000, GuiBase.COLOR_HORIZONTAL_BAR);

        this.textRenderer.drawWithShadow(matrixStack, Text.of("Results: " + Utils.outputTypes[Utils.cycle]), this.x + 4, this.y + 5, Color.WHITE.getRGB());

        String toggle = CraftingPanel.hud ? "ON" : "OFF";
        int color = CraftingPanel.hud ? Color.GREEN.getRGB() : Color.RED.getRGB();
        this.textRenderer.draw(matrixStack, toggle, 55 + this.textRenderer.getWidth("Calculate Materials  Export") + 3, CraftingPanel.screen.height - 23, color);

        if (showResults && !this.results.isEmpty()) {

            int xQuantity = 0;
            int yQuantity = 0;

            List<CraftingPanelItemOutput> items = Utils.convertItemToMaterials(results);

            float biggest = 0;

            this.currentShow = items;

            for (CraftingPanelItemOutput item : items) {

                if (Utils.cycle == 2) {
                    item.setCount(item.getCount() / 3456);
                } else if (Utils.cycle == 1) {
                    item.setCount(item.getCount() / 64);
                }

               if(item.getCount() > biggest) {

                    biggest = item.getCount();
                }

                ItemStack itemStack = Utils.getItemStackFromItemCommandOutputName(item.getName());
               try {
                   String amount = String.valueOf((int) item.getCount());

                this.mc.getItemRenderer().renderInGui(itemStack, this.x + 7 + xQuantity, this.y + 15 + yQuantity);
                this.textRenderer.drawWithShadow(matrixStack, "x" + amount, this.x + 30 + xQuantity, this.y + 20 + yQuantity, Color.WHITE.getRGB());

                yQuantity += 20;

                if (yQuantity + 20 >= this.height) {

                    yQuantity = 0;
                    xQuantity += 22 + this.textRenderer.getWidth("x " + String.valueOf(biggest));
                }
               } catch (Exception ignored) {}
            }
        }

        RenderSystem.popMatrix();
    }

    public void clearResults() {

        this.results = null;
        this.showResults = false;
    }

    public void recieveResults(@NotNull List<CraftingPanelItemOutput> results) {

        this.results = results;
        this.showResults = true;
    }

    public void setHud() {
        CraftingPanel.hud = !CraftingPanel.hud;
        if (CraftingPanel.hud && this.currentShow != null) {
            CraftingPanel.items = this.currentShow;
        }
    }
}
