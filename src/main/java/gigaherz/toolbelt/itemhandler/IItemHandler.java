package gigaherz.toolbelt.itemhandler;

import net.minecraft.item.ItemStack;

/**
 * Minimal backport of Forge's IItemHandler. Empty stacks are null.
 */
public interface IItemHandler {

    int getSlots();

    ItemStack getStackInSlot(int slot);

    ItemStack insertItem(int slot, ItemStack stack, boolean simulate);

    ItemStack extractItem(int slot, int amount, boolean simulate);

    int getSlotLimit(int slot);
}
