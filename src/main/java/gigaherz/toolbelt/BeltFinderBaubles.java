package gigaherz.toolbelt;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import baubles.api.BaublesApi;
import gigaherz.toolbelt.belt.ItemToolBelt;

public class BeltFinderBaubles extends BeltFinder {

    public static void initBaubles() {
        BeltFinder.instances.add(new BeltFinderBaubles());
    }

    @Override
    public void setToBaubles(EntityPlayer player, int slot, ItemStack stack) {
        IInventory baubles = BaublesApi.getBaubles(player);
        baubles.setInventorySlotContents(slot, stack);
    }

    @Nullable
    public BeltGetter findStack(EntityPlayer player) {
        IInventory baubles = BaublesApi.getBaubles(player);
        if (baubles == null) return null;
        for (int i = 0; i < baubles.getSizeInventory(); i++) {
            ItemStack inSlot = baubles.getStackInSlot(i);
            if (inSlot != null) {
                if (inSlot.getItem() instanceof ItemToolBelt) {
                    return new BaublesBeltGetter(player, i);
                }
            }
        }

        return null;
    }

    private class BaublesBeltGetter implements BeltGetter {

        private final EntityPlayer thePlayer;
        private final int slotNumber;

        private BaublesBeltGetter(EntityPlayer thePlayer, int slotNumber) {
            this.thePlayer = thePlayer;
            this.slotNumber = slotNumber;
        }

        @Override
        public ItemStack getBelt() {
            IInventory baubles = BaublesApi.getBaubles(thePlayer);
            return baubles.getStackInSlot(slotNumber);
        }

        @Override
        public void syncToClients() {
            // Baubles doesn't notice contents changing in place, setting the slot again makes it sync to everyone.
            if (thePlayer.worldObj.isRemote) return;
            IInventory baubles = BaublesApi.getBaubles(thePlayer);
            baubles.setInventorySlotContents(slotNumber, baubles.getStackInSlot(slotNumber));
        }
    }
}
