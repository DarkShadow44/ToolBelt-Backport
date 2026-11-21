package gigaherz.toolbelt.common;

import gigaherz.toolbelt.Config;
import gigaherz.toolbelt.belt.ItemToolBelt;
import gigaherz.toolbelt.belt.ToolBeltInventory;
import gigaherz.toolbelt.itemhandler.IItemHandlerModifiable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import java.util.Optional;

public class SlotBelt extends Slot
{
    public SlotBelt(IInventory playerInventory, int blockedSlot, int index, int xPosition, int yPosition)
    {
        super(new IInventory(){

            final IInventory sourceInventory = playerInventory;
            final int slot = blockedSlot;
            final int subSlot = index;

            ItemStack beltStack = null;
            IItemHandlerModifiable inventory = null;
            // Container code changes the returned stack in place and then calls markDirty
            ItemStack lastStack = null;

            Optional<IItemHandlerModifiable> findStack()
            {
                ItemStack stack = sourceInventory.getStackInSlot(slot);
                if (stack != beltStack)
                {
                    beltStack = stack;
                    inventory = stack != null && stack.getItem() instanceof ItemToolBelt ? new ToolBeltInventory(stack) : null;
                    lastStack = null;
                }
                return Optional.ofNullable(inventory);
            }

            @Override
            public String getInventoryName()
            {
                return "toolbelt.slot.wrapper";
            }

            @Override
            public boolean hasCustomInventoryName()
            {
                return false;
            }

            @Override
            public int getSizeInventory()
            {
                return 1;
            }

            @Override
            public ItemStack getStackInSlot(int n)
            {
                lastStack = findStack().map(inv -> inv.getStackInSlot(subSlot)).orElse(null);
                return lastStack;
            }

            @Override
            public ItemStack decrStackSize(int n, int count)
            {
                ItemStack result = findStack().map(inv -> inv.extractItem(subSlot, count, false)).orElse(null);
                getStackInSlot(n);
                return result;
            }

            @Override
            public ItemStack getStackInSlotOnClosing(int n)
            {
                return null;
            }

            @Override
            public void setInventorySlotContents(int n, ItemStack stack)
            {
                findStack().ifPresent(inv -> inv.setStackInSlot(subSlot, stack));
                lastStack = stack;
            }

            @Override
            public int getInventoryStackLimit()
            {
                return findStack().map(inv -> inv.getSlotLimit(subSlot)).orElse(0);
            }

            @Override
            public void markDirty()
            {
                if (lastStack != null)
                    findStack().ifPresent(inv -> inv.setStackInSlot(subSlot, lastStack.stackSize > 0 ? lastStack : null));
            }

            @Override
            public boolean isUseableByPlayer(EntityPlayer player)
            {
                return false;
            }

            @Override
            public void openInventory()
            {

            }

            @Override
            public void closeInventory()
            {

            }

            @Override
            public boolean isItemValidForSlot(int index, ItemStack stack)
            {
                return Config.isItemStackAllowed(stack);
            }
        }, blockedSlot, xPosition, yPosition);
    }

    @Override
    public boolean isItemValid(ItemStack stack)
    {
        return Config.isItemStackAllowed(stack);
    }

}
