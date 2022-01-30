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
}
