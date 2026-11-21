package gigaherz.toolbelt.common;

import gigaherz.toolbelt.BeltFinder;
import gigaherz.toolbelt.Config;
import gigaherz.toolbelt.belt.ToolBeltInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerBelt extends Container
{
    public final int beltSlots;
    private final int blockedSlot;

    public ContainerBelt(IInventory playerInventory, int blockedSlot, ItemStack heldItem)
    {
        ToolBeltInventory beltInventory = new ToolBeltInventory(heldItem);

        this.blockedSlot = blockedSlot;
        beltSlots = beltInventory.getSlots();
        int xoff = ((9 - beltSlots) * 18) / 2;
        for (int k = 0; k < beltSlots; ++k)
        {
            this.addSlotToContainer(new SlotBelt(playerInventory, blockedSlot, k, 8 + xoff + k * 18, 20));
        }

        bindPlayerInventory(playerInventory, blockedSlot);
    }

    private void bindPlayerInventory(IInventory playerInventory, int blockedSlot)
    {
        for (int l = 0; l < 3; ++l)
        {
            for (int j1 = 0; j1 < 9; ++j1)
            {
                int index = j1 + l * 9 + 9;
                this.addSlotToContainer(
                        blockedSlot == index
                                ? new SlotLocked(playerInventory, index, 8 + j1 * 18, l * 18 + 51)
                                : new Slot(playerInventory, index, 8 + j1 * 18, l * 18 + 51)
                );
            }
        }

        for (int i1 = 0; i1 < 9; ++i1)
        {
            this.addSlotToContainer(
                    blockedSlot == i1
                            ? new SlotLocked(playerInventory, i1, 8 + i1 * 18, 109)
                            : new Slot(playerInventory, i1, 8 + i1 * 18, 109)
            );
        }
    }

    @Override
    public void onContainerClosed(EntityPlayer playerIn)
    {
        super.onContainerClosed(playerIn);
        if (!playerIn.worldObj.isRemote)
            BeltFinder.sendSync(playerIn);
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn)
    {
        return true;
    }

    @Override
    public ItemStack slotClick(int slotId, int button, int mode, EntityPlayer player)
    {
        // Number keys would move the open belt out of its hotbar slot
        if (mode == 2 && button == blockedSlot)
            return null;
        return super.slotClick(slotId, button, mode, player);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index)
    {
        ItemStack itemstack = null;
        Slot slot = (Slot) this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack())
        {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            if (index < beltSlots)
            {
                if (!this.mergeItemStack(itemstack1, beltSlots, this.inventorySlots.size(), true))
                {
                    return null;
                }
            }
            // mergeItemStack doesn't check isItemValid in 1.7.10
            else if (!Config.isItemStackAllowed(itemstack1) || !this.mergeItemStack(itemstack1, 0, beltSlots, false))
            {
                return null;
            }

            if (itemstack1.stackSize == 0)
            {
                slot.putStack(null);
            }
            else
            {
                slot.onSlotChanged();
            }
        }

        return itemstack;
    }
}