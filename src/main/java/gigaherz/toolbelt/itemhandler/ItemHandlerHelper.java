package gigaherz.toolbelt.itemhandler;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

/**
 * Minimal backport of the parts of Forge's ItemHandlerHelper used by the belt.
 */
public class ItemHandlerHelper
{
    public static ItemStack insertItem(IItemHandler dest, ItemStack stack, boolean simulate)
    {
        if (dest == null || stack == null)
            return stack;

        for (int i = 0; i < dest.getSlots(); i++)
        {
            stack = dest.insertItem(i, stack, simulate);
            if (stack == null)
                return null;
        }

        return stack;
    }

    public static boolean canItemStacksStack(ItemStack a, ItemStack b)
    {
        if (a == null || b == null || !a.isItemEqual(b))
            return false;

        return a.isStackable() && ItemStack.areItemStackTagsEqual(a, b);
    }

    public static ItemStack copyStackWithSize(ItemStack itemStack, int size)
    {
        if (size == 0)
            return null;
        ItemStack copy = itemStack.copy();
        copy.stackSize = size;
        return copy;
    }

    public static void giveItemToPlayer(EntityPlayer player, ItemStack stack)
    {
        if (!player.inventory.addItemStackToInventory(stack) && stack.stackSize > 0)
            player.dropPlayerItemWithRandomChoice(stack, false);
    }
}
