package gigaherz.toolbelt.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import gigaherz.toolbelt.ToolBelt;
import gigaherz.toolbelt.common.GuiHandler;
import io.netty.buffer.ByteBuf;

public class OpenBeltSlotInventory implements IMessage {

    public OpenBeltSlotInventory() {}

    @Override
    public void fromBytes(ByteBuf buf) {}

    @Override
    public void toBytes(ByteBuf buf) {}

    public static class Handler implements IMessageHandler<OpenBeltSlotInventory, IMessage> {

        @Override
        public IMessage onMessage(final OpenBeltSlotInventory message, MessageContext ctx) {
            final EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            final WorldServer world = (WorldServer) player.worldObj;

            // 1.7.10 handles packets on the main thread
            player.openGui(ToolBelt.instance, GuiHandler.BELT_SLOT, world, 0, 0, 0);

            return null; // no response in this case
        }
    }
}
