package getta.craftingpanel.mixins;

import fi.dy.masa.litematica.gui.GuiMaterialList;
import fi.dy.masa.litematica.materials.MaterialListBase;
import fi.dy.masa.litematica.materials.MaterialListEntry;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import getta.craftingpanel.CraftingPanel;
import getta.craftingpanel.CraftingPanelItemOutput;
import getta.craftingpanel.Utils;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(GuiMaterialList.class)
public abstract class LitematicaMixin extends GuiBase {

    @Shadow
    @Final
    private MaterialListBase materialList;

    @Inject(method = "initGui", at = @At("TAIL"), remap = false)
    private void addMaterialsButton(CallbackInfo ci) {
        int y = this.height - 22;
        int x = this.width - (43 + this.textRenderer.getWidth("Show sub-materials"));
        ButtonGeneric button = new ButtonGeneric(x, y, -1, true, "Show sub-materials");
        this.addButton(button, (btn, mbtn) -> {

            List<CraftingPanelItemOutput> outPut = new ArrayList<>();
            List<CraftingPanelItemOutput> temporal = new ArrayList<>();
            for (MaterialListEntry itemEntry : this.materialList.getMaterialsAll()) {
                ItemStack stack = itemEntry.getStack();
                int count = itemEntry.getCountTotal();
                CraftingPanelItemOutput out = new CraftingPanelItemOutput();
                out.setName(Utils.getItemStackName(stack));
                out.setCount(count);
                if (Utils.provideCraftFromItem(stack.getItem()) != null) {
                    out.setMaterials(Utils.provideCraftFromItem(stack.getItem()));
                }
                temporal.add(out);
            }
            List<CraftingPanelItemOutput> helper = new ArrayList<>();
            List<CraftingPanelItemOutput> iHateConcurrentModification = new ArrayList<>();
            for (CraftingPanelItemOutput item : temporal) {
                if (Utils.provideCraftFromItem(Utils.getItemStackFromItemCommandOutputName(item.getName()).getItem()) == null) {
                    helper.add(item);
                } else {
                    iHateConcurrentModification.add(item);
                }
            }
            outPut = Utils.convertItemToMaterials(iHateConcurrentModification);
            List<CraftingPanelItemOutput> listed = new ArrayList<>();
            for (CraftingPanelItemOutput item : helper) {
                for (CraftingPanelItemOutput item1 : outPut) {
                    if (item1.getName().equals(item.getName())) {
                        item1.setCount(item1.getCount() + item.getCount());
                        continue;
                    }
                }
                listed.add(item);
            }
            if (!listed.isEmpty()) {
                outPut.addAll(listed);
            }
            CraftingPanel.createScreenWithMaterials(outPut);
        });
    }
}
