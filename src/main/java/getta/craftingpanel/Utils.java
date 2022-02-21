package getta.craftingpanel;

import fi.dy.masa.malilib.data.DataDump;
import fi.dy.masa.malilib.util.InventoryUtils;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.registry.Registry;

import java.util.*;

public class Utils {

    public static String[] outputTypes = {"Items", "Stacks", "Shulkers"};
    public static int cycle = 0;
    public static final int shulker = 3456;
    public static final int stack = 64;

    public static ItemStack getItemStackFromItemCommandOutputName(String name) {
        String identifier = "minecraft:" + getItemStackName(Registry.ITEM.get(new Identifier("minecraft:" + name.replaceAll(" ", "_").toLowerCase())).getDefaultStack()).toLowerCase();

        return Registry.ITEM.get(new Identifier(identifier)).getDefaultStack();
    }

    public static String getItemStackName(ItemStack item) {

        String[] split = item.toString().toLowerCase().split(" ");
        return split[1];
    }

    public static DataDump getMaterialsListDump(List<CraftingPanelItemOutput> materialList) {

        DataDump dump = new DataDump(4, DataDump.Format.ASCII);

        for (CraftingPanelItemOutput item : materialList) {

            int total = (int) item.getCount();
            int missing = getMissing(item, false);
            int available = getMissing(item, true);
            dump.addData(item.getName(), String.valueOf(total), String.valueOf(missing), String.valueOf(available));
        }

        String titleTotal = "Total";
        dump.addTitle("Item", titleTotal, "Missing", "Available");
        dump.addHeader("Material List");
        dump.setColumnProperties(1, DataDump.Alignment.RIGHT, true);
        dump.setColumnProperties(2, DataDump.Alignment.RIGHT, true);
        dump.setColumnProperties(3, DataDump.Alignment.RIGHT, true);
        dump.setSort(false);
        dump.setUseColumnSeparator(true);

        return dump;
    }

    public static int getMissing(CraftingPanelItemOutput item, boolean max) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;

        int found = 0;
        int required = (int) item.getCount();

        for (int i = 0; i < Objects.requireNonNull(player).inventory.size(); i++) {

            if (found >= required && !max) {
                return 0;
            } else if (player.inventory.getStack(i).getItem() instanceof BlockItem && ((BlockItem) player.inventory.getStack(i).getItem()).getBlock() instanceof ShulkerBoxBlock) {
                DefaultedList<ItemStack> items = InventoryUtils.getStoredItems(player.inventory.getStack(i), -1);

                for (ItemStack stack : items) {

                    if (found >= required && !max) {
                        return 0;
                    } else if (stack.getItem().equals(getItemStackFromItemCommandOutputName(item.getName()).getItem())) {
                        found += stack.getCount();
                    }
                }
            } else if (player.inventory.getStack(i).getItem().equals(getItemStackFromItemCommandOutputName(item.getName()).getItem())) {
                found += player.inventory.getStack(i).getCount();
            }
        }

        if (max) {
            return found;
        }

        return required - found;
    }

    public static List<CraftingPanelItemOutput> convertItemToMaterials(List<CraftingPanelItemOutput> itemList) {

        List<CraftingPanelItemOutput> items1 = new ArrayList<>();

        for (CraftingPanelItemOutput item : itemList) {
            assert MinecraftClient.getInstance().player != null;
            Optional<? extends Recipe<?>> recipe1 = MinecraftClient.getInstance().player.world.getRecipeManager().get(new Identifier(Utils.getItemStackFromItemCommandOutputName(item.getName()).getItem().toString()));
            if(!recipe1.isPresent()) return null;
            int outQuantity = recipe1.get().getOutput().getCount();
            int quantity = 0;
            boolean found = false;
            if (outQuantity > 1) {
                while (!found) {
                    if (quantity >= item.getCount()) {
                        found = true;
                    } else {
                        quantity += outQuantity;
                    }
                }
            }
            assert item.getMaterials() != null;
            if (found) {
                for(CraftingPanelItemOutput recipe : item.getMaterials()) {
                    if(recipe.getCount() == 0) {
                        continue;
                    }
                    float newAmount = (recipe.getCount() * (quantity / outQuantity));
                    boolean found1 = false;
                    for (CraftingPanelItemOutput list : items1) {
                        if (list.getName().equals(recipe.getName())) {

                            list.setCount(list.getCount() + newAmount);
                            found1 = true;
                        }
                    }
                    if(!found1) {
                        CraftingPanelItemOutput item2 = new CraftingPanelItemOutput();
                        item2.setName(recipe.getName());
                        item2.setCount(newAmount);
                        item2.setCount(newAmount);
                        items1.add(item2);
                    }
                }
            } else {
                for (CraftingPanelItemOutput recipe : item.getMaterials()) {
                    boolean repeated = false;
                    int finalQuantity = (int)(recipe.getCount() * item.getCount());
                    for (CraftingPanelItemOutput list : items1) {
                        if (list.getName().equals(recipe.getName())) {

                            items1.get(items1.indexOf(list)).setCount(items1.get(items1.indexOf(list)).getCount() + item.getCount());
                            repeated = true;
                        }
                    }
                    if (!repeated) {
                        CraftingPanelItemOutput output = new CraftingPanelItemOutput();
                        output.setName(recipe.getName());
                        output.setCount(finalQuantity);
                        items1.add(output);
                    }
                }
            }
        }
        return items1;
    }

    public static List<CraftingPanelItemOutput> provideCraftFromItem(Item item) {

        //ITEM, BLOCK, POTION, ENCHANTMENT, ENTITIES: Minecart, Boat.

        assert MinecraftClient.getInstance().player != null;
        Optional<? extends Recipe<?>> recipe = MinecraftClient.getInstance().player.world.getRecipeManager().get(new Identifier(item.toString()));
        if (!recipe.isPresent()) {

            return null;
        }

        Recipe<?> finalRecipe = recipe.get();

        return getIngredients(finalRecipe.getIngredients());
    }

    public static List<CraftingPanelItemOutput> getIngredients(List<Ingredient> ingredients) {
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

    public static CraftingPanelItemOutput itemFromIngredient(Ingredient ingredient) {

        ItemStack[] itemStackOfIngredient = ingredient.getMatchingStacksClient();

        if (itemStackOfIngredient.length == 0) {

            return null;
        }

        ItemStack itemStack = itemStackOfIngredient[0];
        CraftingPanelItemOutput result = new CraftingPanelItemOutput();
        result.setName(Utils.getItemStackName(itemStack));
        result.incrementCount();

        return result;
    }
}
