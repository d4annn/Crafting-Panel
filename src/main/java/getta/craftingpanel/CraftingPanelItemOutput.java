package getta.craftingpanel;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CraftingPanelItemOutput {

    private String name;
    private float count;
    @Nullable
    private List<CraftingPanelItemOutput> materials;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getCount() {
        return count;
    }

    public void setCount(float count) {
        this.count = count;
    }

    public @Nullable List<CraftingPanelItemOutput> getMaterials() {
        return materials;
    }

    public void setMaterials(@Nullable List<CraftingPanelItemOutput> materials) {
        this.materials = materials;
    }

    public void incrementCount() {
        this.count++;
    }

    public void addMaterial(CraftingPanelItemOutput item) {
        assert this.materials != null;
        this.materials.add(item);
    }

    public void sort(int quantity) {
        if (this.materials == null)
            return;
        for (CraftingPanelItemOutput item : this.materials) {
            int number = (int) this.count / quantity;
            if (number == 0 || number == 1) {
                item.setCount(1);
            } else if (number > quantity && number < quantity * 2) {
                item.setCount(2);
            }
            if (number % quantity != 0) {
                String operableNumber = String.valueOf(number);
                if (operableNumber.length() > 1) {
                    int lastNumber = operableNumber.charAt(operableNumber.length() - 1);
                    lastNumber = Math.abs(quantity - lastNumber);
                    item.setCount(lastNumber);
                } else {
                    item.setCount(Math.abs(number));
                }
            } else {
                item.setCount(number);
            }
        }
    }
}