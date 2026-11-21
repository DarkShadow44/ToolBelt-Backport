package gigaherz.toolbelt.network;

import gigaherz.toolbelt.BeltFinder;
import gigaherz.toolbelt.Config;
import gigaherz.toolbelt.belt.ToolBeltInventory;
import gigaherz.toolbelt.itemhandler.IItemHandlerModifiable;
import gigaherz.toolbelt.itemhandler.ItemHandlerHelper;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class SwapItems
        implements IMessage
{
    public int swapWith;

    public SwapItems()
    {
    }

    public SwapItems(int windowId)
    {
        this.swapWith = windowId;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        swapWith = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(swapWith);
    }

    public static class Handler implements IMessageHandler<SwapItems, IMessage>
    {
        @Override
        public IMessage onMessage(final SwapItems message, MessageContext ctx)
        {
            final EntityPlayerMP player = ctx.getServerHandler().playerEntity;

            // 1.7.10 handles packets on the main thread
            swapItem(message.swapWith, player);

            return null; // no response in this case
        }
    }

    public static void swapItem(int swapWith, EntityPlayer player)
    {
        BeltFinder.BeltGetter getter = BeltFinder.findBelt(player);
        if (getter == null)
            return;

        ItemStack stack = getter.getBelt();
        if (stack == null)
            return;

        ItemStack inHand = player.getCurrentEquippedItem();

        if (!Config.isItemStackAllowed(inHand))
            return;

        IItemHandlerModifiable cap = new ToolBeltInventory(stack);
        if (swapWith >= cap.getSlots())
            return;

        if (swapWith < 0)
        {
            player.inventory.setInventorySlotContents(player.inventory.currentItem, ItemHandlerHelper.insertItem(cap, inHand, false));
        }
        else
        {
            ItemStack inSlot = cap.getStackInSlot(swapWith);
            player.inventory.setInventorySlotContents(player.inventory.currentItem, inSlot);
            cap.setStackInSlot(swapWith, inHand);
        }

        getter.syncToClients();
    }
}
