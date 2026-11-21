package gigaherz.toolbelt.belt;

import net.minecraft.block.BlockColored;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

/** Dyes the belt like leather armor, see RecipesArmorDyes. Accepts vanilla dyes and the dyeXxx ore names. */
public class BeltDyeRecipe implements IRecipe
{
    private static final String[] DYE_ORE_NAMES = { "dyeBlack", "dyeRed", "dyeGreen", "dyeBrown", "dyeBlue",
        "dyePurple", "dyeCyan", "dyeLightGray", "dyeGray", "dyePink", "dyeLime", "dyeYellow", "dyeLightBlue",
        "dyeMagenta", "dyeOrange", "dyeWhite" };

    @Override
    public boolean matches(InventoryCrafting inv, World world)
    {
        ItemStack belt = null;
        boolean hasDye = false;

        for (int i = 0; i < inv.getSizeInventory(); ++i)
        {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack == null)
                continue;

            if (stack.getItem() instanceof ItemToolBelt)
            {
                if (belt != null)
                    return false;
                belt = stack;
            }
            else if (getDyeIndex(stack) >= 0)
            {
                hasDye = true;
            }
            else
            {
                return false;
            }
        }

        return belt != null && hasDye;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv)
    {
        ItemStack result = null;
        int[] rgb = new int[3];
        int maxSum = 0;
        int count = 0;

        for (int i = 0; i < inv.getSizeInventory(); ++i)
        {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack == null)
                continue;

            if (stack.getItem() instanceof ItemToolBelt)
            {
                if (result != null)
                    return null;

                result = stack.copy();
                result.stackSize = 1;

                if (ItemToolBelt.hasColor(stack))
                {
                    int color = ItemToolBelt.getColor(stack);
                    float r = (float) ((color >> 16) & 255) / 255.0F;
                    float g = (float) ((color >> 8) & 255) / 255.0F;
                    float b = (float) (color & 255) / 255.0F;
                    maxSum = (int) ((float) maxSum + Math.max(r, Math.max(g, b)) * 255.0F);
                    rgb[0] = (int) ((float) rgb[0] + r * 255.0F);
                    rgb[1] = (int) ((float) rgb[1] + g * 255.0F);
                    rgb[2] = (int) ((float) rgb[2] + b * 255.0F);
                    ++count;
                }
            }
            else
            {
                int dye = getDyeIndex(stack);
                if (dye < 0)
                    return null;

                float[] color = EntitySheep.fleeceColorTable[BlockColored.func_150032_b(dye)];
                int r = (int) (color[0] * 255.0F);
                int g = (int) (color[1] * 255.0F);
                int b = (int) (color[2] * 255.0F);
                maxSum += Math.max(r, Math.max(g, b));
                rgb[0] += r;
                rgb[1] += g;
                rgb[2] += b;
                ++count;
            }
        }

        if (result == null || count == 0)

            return null;

        int r = rgb[0] / count;
        int g = rgb[1] / count;
        int b = rgb[2] / count;
        float avgMax = (float) maxSum / (float) count;
        float max = (float) Math.max(r, Math.max(g, b));
        r = (int) ((float) r * avgMax / max);
        g = (int) ((float) g * avgMax / max);
        b = (int) ((float) b * avgMax / max);
        ItemToolBelt.setColor(result, (r << 16) | (g << 8) | b);
        return result;
    }

    /** @return the vanilla dye metadata for this stack, or -1 if it is not a dye */
    private static int getDyeIndex(ItemStack stack)
    {
        if (stack.getItem() == Items.dye)
            return stack.getItemDamage() & 15;

        for (int id : OreDictionary.getOreIDs(stack))
        {
            String name = OreDictionary.getOreName(id);
            for (int i = 0; i < DYE_ORE_NAMES.length; i++)
            {
                if (DYE_ORE_NAMES[i].equals(name))
                    return i;
            }
        }
        return -1;
    }

    @Override
    public int getRecipeSize()
    {
        return 10;
    }

    @Override
    public ItemStack getRecipeOutput()
    {
        return null;
    }
}
