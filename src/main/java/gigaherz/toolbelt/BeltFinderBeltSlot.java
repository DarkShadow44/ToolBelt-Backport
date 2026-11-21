package gigaherz.toolbelt;

import gigaherz.toolbelt.belt.ItemToolBelt;
import gigaherz.toolbelt.customslots.IExtensionSlot;
import gigaherz.toolbelt.network.BeltContentsChange;
import gigaherz.toolbelt.slot.ExtensionSlotBelt;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.world.WorldServer;

import javax.annotation.Nullable;

public class BeltFinderBeltSlot extends BeltFinder
{
    public static void initBaubles()
    {
        BeltFinder.instances.add(new BeltFinderBeltSlot());
    }

    @Nullable
    public BeltGetter findStack(EntityPlayer player)
    {
        ExtensionSlotBelt beltSlot = ExtensionSlotBelt.get(player);

        if (beltSlot == null)
            return null;

        for (IExtensionSlot slot : beltSlot.getSlots())
        {
            ItemStack inSlot = slot.getContents();
            if (inSlot != null)
            {
                if (inSlot.getItem() instanceof ItemToolBelt)
                {
                    return new ExtensionSlotBeltGetter(slot);
                }
            }
        }

        return null;
    }

    @Override
    public void setToBeltSlot(EntityLivingBase player, ItemStack stack)
    {
        ExtensionSlotBelt baubles = ExtensionSlotBelt.get(player);
        baubles.getBelt().setContents(stack);
    }

    private class ExtensionSlotBeltGetter implements BeltGetter
    {
        private final IExtensionSlot slot;

        private ExtensionSlotBeltGetter(IExtensionSlot slot)
        {
            this.slot = slot;
        }

        @Override
        public ItemStack getBelt()
        {
            return slot.getContents();
        }

        @Override
        public void syncToClients()
        {
            EntityLivingBase thePlayer = slot.getContainer().getOwner();
            if (thePlayer.worldObj.isRemote)
                return;
            BeltContentsChange message = new BeltContentsChange(thePlayer, BeltContentsChange.ContainingInventory.BELT_SLOT, 0, slot.getContents());
            ((WorldServer) thePlayer.worldObj).getEntityTracker().getTrackingPlayers(thePlayer).forEach((p) -> {
                if (p instanceof EntityPlayerMP)
                    ToolBelt.channel.sendTo(message, (EntityPlayerMP) p);
            });
        }
    }
}
