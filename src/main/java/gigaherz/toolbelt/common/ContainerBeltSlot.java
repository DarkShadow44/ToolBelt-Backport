package gigaherz.toolbelt.common;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;

import gigaherz.toolbelt.BeltFinder;
import gigaherz.toolbelt.belt.ItemToolBelt;
import gigaherz.toolbelt.customslots.IExtensionSlot;
import gigaherz.toolbelt.customslots.SlotExtension;
import gigaherz.toolbelt.slot.ExtensionSlotBelt;

public class ContainerBeltSlot extends ContainerPlayer {

    // The 1.12 position overlaps the 1.7.10 crafting grid
    public static final int SLOT_X = 80;
    public static final int SLOT_Y = 62;

    private final SlotExtension slotBelt;
    private final IExtensionSlot extensionSlot;

    public ContainerBeltSlot(InventoryPlayer playerInventory, boolean localWorld, EntityPlayer playerIn) {
        super(playerInventory, localWorld, playerIn);

        ExtensionSlotBelt container = ExtensionSlotBelt.get(playerIn);

        extensionSlot = container.getBelt();

        this.addSlotToContainer(slotBelt = new SlotExtension(extensionSlot, SLOT_X, SLOT_Y));
        slotBelt.setBackgroundIcon(ItemToolBelt.emptySlotIcon);
    }

    @Override
    public void onContainerClosed(EntityPlayer playerIn) {
        super.onContainerClosed(playerIn);
        if (!playerIn.worldObj.isRemote) BeltFinder.sendSync(playerIn);
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return true;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        Slot slot = (Slot) this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack remaining = null;
            ItemStack slotContents = slot.getStack();
            remaining = slotContents.copy();

            if (index == slotBelt.slotNumber) {
                if (!this.mergeItemStack(slotContents, 9, 45, false)) {
                    return null;
                }

                if (slotContents.stackSize == 0) slot.putStack(null);
                return remaining;
            }
            // mergeItemStack doesn't check isItemValid in 1.7.10
            else if (slotBelt.isItemValid(slotContents)) {
                if (!this.mergeItemStack(slotContents, slotBelt.slotNumber, slotBelt.slotNumber + 1, false)) {
                    return null;
                }

                if (slotContents.stackSize == 0) slot.putStack(null);
                return remaining;
            }
        }

        return super.transferStackInSlot(playerIn, index);
    }
}
