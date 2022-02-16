package getta.craftingpanel.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.GuiScrollBar;
import fi.dy.masa.malilib.gui.GuiTextFieldGeneric;
import fi.dy.masa.malilib.gui.interfaces.ITextFieldListener;
import fi.dy.masa.malilib.gui.widgets.WidgetBase;
import fi.dy.masa.malilib.gui.wrappers.TextFieldWrapper;
import fi.dy.masa.malilib.render.RenderUtils;
import getta.craftingpanel.Utils;
import getta.craftingpanel.CraftingPanelItemOutput;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.*;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.awt.*;
import java.util.*;
import java.util.List;

public class ItemsWidget extends WidgetBase {

    private final TextFieldWrapper<GuiTextFieldGeneric> searchBar;
    private final TextFieldWrapper<GuiTextFieldGeneric> amountSelected;
    protected final GuiScrollBar scrollBar = new GuiScrollBar();
    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private boolean shouldFilter;
    private List<CraftingPanelItemOutput> selected;
    private ItemStack selectedItem;
    private CraftingPanelItemOutput selectedEntry;
    protected int scrollbarWidth = 10;
    private MouseClick searchingItem;
    private MouseClick tabSelected;
    private Tabs selectedTab;
    private MouseClick searchingList;

    public ItemsWidget(int x, int y, int width, int height) {
        super(x, y, width, height);

        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        TextFieldListener listener = new TextFieldListener(this);

        this.searchBar = new TextFieldWrapper<>(new GuiTextFieldGeneric(this.x + 1, y - 20, (width + x) / 2, 16, this.textRenderer), listener);

        this.amountSelected = new TextFieldWrapper<>(new GuiTextFieldGeneric(this.x + 3 + this.textRenderer.getWidth("Amount:"), (int) (this.height * 1.5f + 32), 49, 16, this.textRenderer), listener);

        this.scrollBar.setMaxValue(22);
        this.scrollBar.setValue(0);

        this.amountSelected.getTextField().setMaxLength(String.valueOf(Integer.MAX_VALUE).length());

        this.selected = new ArrayList<>();
        this.selectedTab = Tabs.ALL;
    }

    protected void updateFilteredEntries() {

        if (this.searchBar.isFocused()) {

            String textToFilter = searchBar.getTextField().getText();

            shouldFilter = !textToFilter.isEmpty();

            this.scrollBar.setValue(0);
        }
    }

    @Override
    public void onMouseReleased(int mouseX, int mouseY, int mouseButton) {

        this.tabSelected = new MouseClick(mouseX, mouseY);

        this.searchBar.getTextField().setFocused(this.searchBar.getTextField().isMouseOver(mouseX, mouseY));

        if (this.amountSelected.getTextField().isMouseOver(mouseX, mouseY)) {

            if (this.amountSelected.getTextField().getText().equals("Fill")) {

                this.amountSelected.getTextField().setEditableColor(Color.WHITE.getRGB());
                this.amountSelected.getTextField().setText("");
            }
            this.amountSelected.getTextField().setFocused(true);
        } else {

            this.amountSelected.getTextField().setFocused(false);
        }

        this.searchingList = new MouseClick(mouseX, mouseY);

        this.scrollBar.setIsDragging(false);
    }

    @Override
    protected boolean onMouseClickedImpl(int mouseX, int mouseY, int mouseButton) {

        if (this.searchingItem != null) {

            this.searchingItem = null;
            this.selectedItem = null;
        }

        this.searchingItem = new MouseClick(mouseX, mouseY);

        if (this.scrollBar.wasMouseOver()) {

            this.scrollBar.setIsDragging(true);
        }

        return true;
    }

    @Override
    protected boolean onCharTypedImpl(char charIn, int modifiers) {
        if (this.searchBar.getTextField().isFocused()) {

            return this.searchBar.onCharTyped(charIn, modifiers);
        } else if (this.amountSelected.isFocused() && Character.isDigit(charIn)) {

            return this.amountSelected.onCharTyped(charIn, modifiers);
        }

        return false;
    }

    @Override
    protected boolean onKeyTypedImpl(int keyCode, int scanCode, int modifiers) {

        if (this.searchBar.getTextField().isFocused()) {


            return this.searchBar.onKeyTyped(keyCode, scanCode, modifiers);
        } else if (this.amountSelected.isFocused()) {

            return this.amountSelected.onKeyTyped(keyCode, scanCode, modifiers);
        }

        return false;
    }

    @Override
    public void onMouseReleasedImpl(int mouseX, int mouseY, int mouseButton) {
        this.scrollBar.setIsDragging(false);
    }

    @Override
    public boolean onMouseScrolledImpl(int mouseX, int mouseY, double mouseWheelDelta) {

        if (this.isMouseOver(mouseX, mouseY)) {

            int amount = mouseWheelDelta < 0 ? 1 : -1;
            this.scrollBar.offsetValue(amount);
        }

        return false;
    }

    //Provided by akali, he's the best
    private List<CraftingPanelItemOutput> provideCraftFromItem(Item item) {

        //ITEM, BLOCK, POTION, ENCHANTMENT, ENTITIES: Minecart, Boat.

        assert MinecraftClient.getInstance().player != null;
        Optional<? extends Recipe<?>> recipe = MinecraftClient.getInstance().player.world.getRecipeManager().get(new Identifier(item.toString()));
        if (!recipe.isPresent()) {

            return null;
        }


        Recipe<?> finalRecipe = recipe.get();

        return getIngredients(finalRecipe.getIngredients());
    }

    //Provided by akali, he's the best
    private List<CraftingPanelItemOutput> getIngredients(List<Ingredient> ingredients) {
        Map<String, CraftingPanelItemOutput> history = new HashMap<>();

        for (Ingredient ingredient : ingredients) {

            CraftingPanelItemOutput item = itemFromIngredient(ingredient);
            if (item != null) {

                if (history.containsKey(item.getName())) {

                    history.get(item.getName()).incrementCount();
                } else {

                    history.put(item.getName(), item);
                }
            }
        }

        return new ArrayList<>(history.values());
    }

    //Provided by akali, he's the best
    private static CraftingPanelItemOutput itemFromIngredient(Ingredient ingredient) {

        ItemStack[] itemStackOfIngredient = ingredient.getMatchingStacks();

        if (itemStackOfIngredient.length == 0) {

            return null;
        }

        ItemStack itemStack = itemStackOfIngredient[0];
        CraftingPanelItemOutput result = new CraftingPanelItemOutput();
        result.setName(Utils.getItemStackName(itemStack));
        result.incrementCount();

        return result;
    }

    public List<CraftingPanelItemOutput> convertSelectionsToResults() {

        List<CraftingPanelItemOutput> output = new ArrayList<>();

        if (this.selected.isEmpty()) {

            CraftingPanelItemOutput item = new CraftingPanelItemOutput();
            int quantity;

            try {
                if (this.amountSelected.getTextField().getText().trim().equals("")) {
                    quantity = 1;
                } else {
                    quantity = Integer.parseInt(this.amountSelected.getTextField().getText());
                }

                item.setCount(quantity);
                item.setName(Utils.getItemStackName(this.selectedItem));
                item.setMaterials(provideCraftFromItem(this.selectedItem.getItem()));
//              item.sort(itemSort);
                output.add(item);

            } catch (Exception ignored) {}
        }

        this.selected.forEach((item) -> {
//          item.sort(itemSort);
            CraftingPanelItemOutput newItem = item;
            newItem.setMaterials(provideCraftFromItem(Utils.getItemStackFromItemCommandOutputName(item.getName()).getItem()));
            output.add(item);
        });
        return output;
    }

    private boolean applySearch(ItemStack item) {

        String filteredText = this.searchBar.getTextField().getText();
        String itemName = item.getName().getString();


        return itemName.toLowerCase().contains(filteredText.toLowerCase());
    }

    public void addToList() {

        if (this.selectedItem != null) {

            CraftingPanelItemOutput addingItem = new CraftingPanelItemOutput();
            addingItem.setName(Utils.getItemStackName(this.selectedItem));

            int quantity = 0;

            try {

                if (Integer.parseInt(this.amountSelected.getTextField().getText()) > 0) {
                    quantity = Integer.parseInt(this.amountSelected.getTextField().getText());
                }
            } catch (NumberFormatException e) {
                quantity = 1;
            }

            for (CraftingPanelItemOutput item : this.selected) {
                if (item.getName().equals(Utils.getItemStackName(this.selectedItem))) {

                    float previousCount = item.getCount();
                    item.setCount(previousCount + quantity);
                    return;
                }
            }

            addingItem.setCount(quantity);
            addingItem.setMaterials(provideCraftFromItem(this.selectedItem.getItem()));
            this.selected.add(addingItem);
        }
    }


    public void clearSelected() {
        this.selected = new ArrayList<>();
    }

    public void removeLast() {
        try {
            this.selected.remove(this.selected.size() - 1);
        } catch (IndexOutOfBoundsException ignored) {
        }
    }

    private boolean isAvailable(Item item) {
        return provideCraftFromItem(item) != null &&
                !item.equals(Items.GLASS) &&
                !item.equals(Items.SPONGE) &&
                (!item.getDefaultStack().toString().contains("arrow") || item.equals(Items.ARROW)) &&
                !item.getDefaultStack().toString().contains("pattern") &&
                !item.equals(Items.FIREWORK_STAR) &&
                !item.equals(Items.DRAGON_BREATH) &&
                !item.getDefaultStack().toString().contains("rocket");
    }

    public void removeListSelected() {
        if (this.selectedEntry != null) {
            this.selected.remove(this.selectedEntry);
            this.selectedEntry = null;
            this.searchingList = null;
        }
    }

    public void clearSearch() {
        this.searchBar.getTextField().setText("");
        this.updateFilteredEntries();
    }

    @Override
    public void render(int mouseX, int mouseY, boolean selected, MatrixStack matrixStack) {

        RenderUtils.color(1f, 1f, 1f, 1f);
        matrixStack.translate(0, 0, 1);

        Screen screen = this.mc.currentScreen;

        RenderUtils.drawOutlinedBox(this.x, this.y, this.width, this.height, 0xA0000000, GuiBase.COLOR_HORIZONTAL_BAR);
        assert screen != null;
        int scaledWidth = MinecraftClient.getInstance().getWindow().getScaledWidth();
        RenderUtils.drawOutlinedBox(this.width + 15, this.y, (int) (scaledWidth - scaledWidth / 1.5) - scaledWidth / 6 - 1, this.height + 50, 0xA0000000, GuiBase.COLOR_HORIZONTAL_BAR);

        if (this.selected != null && !this.selected.isEmpty()) {

            int xLayers = 0;
            int yLayers = 0;

            for (CraftingPanelItemOutput item : this.selected) {

                if (this.searchingList != null) {
                    if (this.width + 17 + xLayers <= this.searchingList.getMouseX() && this.width + 17 + xLayers + 18 >= this.searchingList.getMouseX() &&
                            this.y + 7 + yLayers <= this.searchingList.getMouseY() && this.y + 7 + yLayers + 18 >= this.searchingList.getMouseY()) {

                        this.selectedEntry = item;
                    }
                }

                ItemStack itemStack = Utils.getItemStackFromItemCommandOutputName(item.getName());
                int amount = (int) item.getCount();

                this.mc.getItemRenderer().renderInGui(itemStack, this.width + 17 + xLayers, this.y + 7 + yLayers);
                this.textRenderer.drawWithShadow(matrixStack, "x" + amount, this.width + 35 + xLayers, this.y + 12 + yLayers, Color.WHITE.getRGB());
                yLayers += 20;

                if (this.selectedEntry != null && this.selectedEntry.equals(item)) {
                    RenderUtils.drawOutline(this.width + 15 + xLayers, this.y - 15 + yLayers, this.width - 297 + xLayers + this.textRenderer.getWidth("x" + amount), 20, GuiBase.COLOR_HORIZONTAL_BAR);
                }

                if (yLayers + 20 >= this.height + 50) {

                    if (xLayers - 50 >= this.width / 6) {

                        break;
                    }

                    yLayers = 0;
                    xLayers += 40;
                }
            }
        }

        int xAmount = 0;
        int yAmount = 0;
        int layer = this.scrollBar.getValue();

        int helper = 0;

        List<ItemStack> preList = new ArrayList<>();
        List<ItemStack> items = new ArrayList<>();


        switch (selectedTab) {

            case ALL:
                for (Item item : Registry.ITEM) {
                    if (!isAvailable(item)) {
                        continue;
                    }
                    preList.add(item.getDefaultStack());
                }
                this.scrollBar.setMaxValue(26);
                break;

            case BUILDING_BLOCKS:
                for (Item item : Registry.ITEM) {
                    if (!isAvailable(item)) {
                        continue;
                    }
                    if (item.getGroup() == ItemGroup.BUILDING_BLOCKS) {
                        preList.add(item.getDefaultStack());
                    }
                }
                this.scrollBar.setMaxValue(3);
                break;

            case DECORATION_BLOCKS:
                for (Item item : Registry.ITEM) {
                    if (!isAvailable(item)) {
                        continue;
                    }
                    if (item.getGroup() == ItemGroup.DECORATIONS) {
                        preList.add(item.getDefaultStack());
                    }
                }
                this.scrollBar.setMaxValue(0);
                break;

            case REDSTONE:
                for (Item item : Registry.ITEM) {
                    if (!isAvailable(item)) {
                        continue;
                    }
                    if (item.getGroup() == ItemGroup.REDSTONE) {
                        preList.add(item.getDefaultStack());
                    }
                }
                this.scrollBar.setMaxValue(0);
                break;

            case TRANSPORTATION:
                for (Item item : Registry.ITEM) {
                    if (!isAvailable(item)) {
                        continue;
                    }
                    if (item.getGroup() == ItemGroup.TRANSPORTATION) {
                        preList.add(item.getDefaultStack());
                    }
                }
                this.scrollBar.setMaxValue(0);
                break;

            case MISCELLANEOUS:
                for (Item item : Registry.ITEM) {
                    if (!isAvailable(item)) {
                        continue;
                    }
                    if (item.getGroup() == ItemGroup.MISC) {
                        preList.add(item.getDefaultStack());
                    }
                }
                this.scrollBar.setMaxValue(0);
                break;

            case FOOD:
                for (Item item : Registry.ITEM) {
                    if (!isAvailable(item)) {
                        continue;
                    }
                    if (item.getGroup() == ItemGroup.FOOD) {
                        preList.add(item.getDefaultStack());
                    }
                }
                this.scrollBar.setMaxValue(0);
                break;

            case TOOLS:
                for (Item item : Registry.ITEM) {
                    if (!isAvailable(item)) {
                        continue;
                    }
                    if (item.getGroup() == ItemGroup.TOOLS) {
                        preList.add(item.getDefaultStack());
                    }
                }
                this.scrollBar.setMaxValue(0);
                break;

            case COMBAT:
                for (Item item : Registry.ITEM) {
                    if (!isAvailable(item)) {
                        continue;
                    }
                    if (item.getGroup() == ItemGroup.COMBAT) {
                        preList.add(item.getDefaultStack());
                    }
                }
                this.scrollBar.setMaxValue(0);
                break;

            case BREWING:
                for (Item item : Registry.ITEM) {
                    if (isAvailable(item)) {
                        if (item.getGroup() == ItemGroup.BREWING) {
                            preList.add(item.getDefaultStack());
                        }
                    }
                }
        }

        if (!shouldFilter) {
            items = preList;
        } else {
            for (ItemStack item : preList) {
                if (applySearch(item)) {
                    items.add(item);
                }
            }
        }

        for (ItemStack item : items) {
            if (layer > 0 || helper > 0) {
                if (helper == 0) {
                    helper = 17;
                    layer--;
                }
                helper--;
                continue;
            }
            xAmount += 18;

            this.mc.getItemRenderer().renderInGui(item, this.x - 16 + xAmount, this.y + 2 + yAmount);

            if (this.x - 16 + xAmount <= mouseX && this.x - 16 + xAmount + 18 >= mouseX &&
                    this.y + 2 + yAmount <= mouseY && this.y + 2 + yAmount + 18 >= mouseY) {

                List<Text> text = new ArrayList<>();
                text.add(item.getName());
                screen.renderTooltip(matrixStack, text, Optional.empty(), this.x - 24 + xAmount, this.y + 2 + yAmount);
            }

            if (this.searchingItem != null && this.selectedItem == null) {

                if (this.x - 16 + xAmount <= this.searchingItem.getMouseX() && this.x - 16 + xAmount + 18 >= this.searchingItem.getMouseX() &&
                        this.y + 2 + yAmount <= this.searchingItem.getMouseY() && this.y + 2 + yAmount + 18 >= this.searchingItem.getMouseY()) {

                    this.selectedItem = item;
                }
            }

            if (this.selectedItem != null) {
                if (item.getItem().equals(this.selectedItem.getItem()) && !item.toString().contains("potion") || item.equals(this.selectedItem)) {

                    RenderUtils.drawOutline(this.x - 18 + xAmount, this.y + 2 + yAmount - 2, 20, 20, Color.WHITE.getRGB());
                }
            }

            if (xAmount - scrollbarWidth + 25 >= this.width) {

                xAmount = 0;
                yAmount += 20;
            }

            if (yAmount + 18 >= this.height) {

                break;
            }

        }

        this.searchBar.draw(mouseX, mouseY, matrixStack);
        this.amountSelected.draw(mouseX, mouseY, matrixStack);
        this.textRenderer.drawWithShadow(matrixStack, "Amount:", this.x + 1, this.height * 1.5f + 36, Color.WHITE.getRGB());

        int x = this.x + this.width - this.scrollbarWidth - 1;
        int y = this.y + 1;
        this.scrollBar.render(mouseX, mouseY, 0, x, y, this.scrollbarWidth, this.height - 1, this.height * 4);

        int tabs = 0;
        MatrixStack stack = new MatrixStack();
        stack.scale(1.25f, 1.25f, 1.25f);

        for (Tabs tab : Tabs.values()) {
            int color = 0x24243B;

            if (tab == selectedTab) {
                color = GuiBase.COLOR_HORIZONTAL_BAR;
            }

            RenderUtils.drawOutlinedBox(this.x + 3 + tabs, this.height + 127, 24, 24, 0xA0000000, color);
            mc.getItemRenderer().renderInGui(tab.getStack(), this.x + 7 + tabs, this.height + 131);

            if (this.tabSelected != null) {
                if (this.x + 3 + tabs <= this.tabSelected.getMouseX() && this.x + 27 + tabs >= this.tabSelected.getMouseX() &&
                        this.height + 127 <= this.tabSelected.getMouseY() && this.height + 154 >= this.tabSelected.getMouseY()) {

                    this.searchBar.getTextField().setText("");
                    this.selectedTab = tab;
                }
            }

            tabs += 32;
        }
    }

    protected static class TextFieldListener implements ITextFieldListener<GuiTextFieldGeneric> {

        protected final ItemsWidget widget;

        protected TextFieldListener(ItemsWidget widget) {
            this.widget = widget;
        }

        @Override
        public boolean onTextChange(GuiTextFieldGeneric textField) {

            this.widget.updateFilteredEntries();
            return true;
        }
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    private static class MouseClick {

        private final int mouseX;
        private final int mouseY;

        public MouseClick(int mouseX, int mouseY) {
            this.mouseX = mouseX;
            this.mouseY = mouseY;
        }

        public int getMouseX() {
            return mouseX;
        }

        public int getMouseY() {
            return mouseY;
        }
    }

    private enum Tabs {

        ALL(Items.COMPASS.getDefaultStack()),
        BUILDING_BLOCKS(Items.BRICKS.getDefaultStack()),
        DECORATION_BLOCKS(Items.PEONY.getDefaultStack()),
        REDSTONE(Items.REDSTONE.getDefaultStack()),
        TRANSPORTATION(Items.POWERED_RAIL.getDefaultStack()),
        MISCELLANEOUS(Items.LAVA_BUCKET.getDefaultStack()),
        FOOD(Items.APPLE.getDefaultStack()),
        TOOLS(Items.IRON_AXE.getDefaultStack()),
        COMBAT(Items.GOLDEN_SWORD.getDefaultStack()),
        BREWING(Items.GLASS_BOTTLE.getDefaultStack());

        private final ItemStack stack;

        Tabs(ItemStack stack) {
            this.stack = stack;
        }

        public ItemStack getStack() {
            return stack;
        }
    }
}
