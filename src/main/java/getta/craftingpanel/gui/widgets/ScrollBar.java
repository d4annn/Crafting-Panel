package getta.craftingpanel.gui.widgets;

public class ScrollBar {

    private int value;
    private int maxValue;

    public ScrollBar() {
        this.value = 0;
        this.maxValue = 0;
    }

    public void onScroll(int value) {
        if (this.value + value != -1 && !(this.value + value > maxValue)) {
            this.value += value;
        }
    }

    public void setMaxValue(int maxValue) {
        this.maxValue = maxValue;
    }

    public int getValue() {
        return this.value;
    }

    public int getMaxValue() {
        return maxValue;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
