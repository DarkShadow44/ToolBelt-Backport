package gigaherz.toolbelt.itemhandler;

import net.minecraft.item.ItemStack;

/**
 * Minimal backport of Forge's IItemHandlerModifiable. Empty stacks are null.
 */
public interface IItemHandlerModifiable extends IItemHandler {

    void setStackInSlot(int slot, ItemStack stack);
}
