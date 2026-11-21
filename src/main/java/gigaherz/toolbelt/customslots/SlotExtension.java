package gigaherz.toolbelt.customslots;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

/**
 * To be used in Containers.
 */
public class SlotExtension extends Slot
{
    private static IInventory emptyInventory = new InventoryBasic("[Null]", true, 0);
    private final IExtensionSlot slot;

    public SlotExtension(IExtensionSlot slot, int x, int y)
    {
        super(emptyInventory, 0, x, y);
        this.slot = slot;
    }

    /**
     * Check if the stack is allowed to be placed in this slot, used for armor slots as well as furnace fuel.
     */
    @Override
    public boolean isItemValid(@Nonnull ItemStack stack)
    {
        if (stack == null)
            return false;

        return slot.canEquip(stack);
    }

    /**
     * Helper fnct to get the stack in the slot.
     */
    @Override
    @Nonnull
    public ItemStack getStack()
    {
        return slot.getContents();
    }

    // Override if your IItemHandler does not implement IItemHandlerModifiable
    /**
     * Helper method to put a stack in the slot.
     */
    @Override
    public void putStack(@Nonnull ItemStack stack)
    {
        slot.setContents(stack);
        this.onSlotChanged();
    }

    /**
     * if par2 has more items than par1, onCrafting(item,countIncrease) is called
     */
    @Override
    public void onSlotChange(@Nonnull ItemStack p_75220_1_, @Nonnull ItemStack p_75220_2_)
    {

    }

    /**
     * Returns the maximum stack size for a given slot (usually the same as getInventoryStackLimit(), but 1 in the case
     * of armor slots)
     */
    @Override
    public int getSlotStackLimit()
    {
        return 1;
    }

    /**
     * Return whether this slot's stack can be taken from this slot.
     */
    @Override
    public boolean canTakeStack(EntityPlayer playerIn)
    {
        return slot.canUnequip(slot.getContents());
    }

    /**
     * Decrease the size of the stack in slot (first int arg) by the amount of the second int arg. Returns the new
     * stack.
     */
    @Override
    @Nonnull
    public ItemStack decrStackSize(int amount)
    {
        ItemStack itemstack = slot.getContents();
        if (itemstack == null)
            return null;

        int available = Math.min(itemstack.stackSize, amount);
        int remaining = itemstack.stackSize - available;

        ItemStack split = itemstack.copy();
        split.stackSize = available;
        itemstack.stackSize = remaining;

        if (remaining <= 0)
            slot.setContents(null);

        this.onSlotChanged();

        return split;
    }

    public IExtensionSlot getExtensionSlot()
    {
        return slot;
    }
}
