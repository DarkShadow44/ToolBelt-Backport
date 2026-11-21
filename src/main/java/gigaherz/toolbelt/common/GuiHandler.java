package gigaherz.toolbelt.common;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import cpw.mods.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler {

    public static final int BELT = 0;
    public static final int BELT_SLOT = 1;

    @Nullable
    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        switch (id) {
            case BELT:
                ItemStack heldItem = player.getCurrentEquippedItem();
                if (heldItem != null) {
                    int blockedSlot = player.inventory.currentItem;

                    return new ContainerBelt(player.inventory, blockedSlot, heldItem);
                }
                break;
            case BELT_SLOT:
                return new ContainerBeltSlot(player.inventory, !player.worldObj.isRemote, player);
        }
        return null;
    }

    @Nullable
    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        switch (id) {
            case BELT:
                ItemStack heldItem = player.getCurrentEquippedItem();
                if (heldItem != null) {
                    int blockedSlot = player.inventory.currentItem;

                    return new GuiBelt(player.inventory, blockedSlot, heldItem);
                }
                break;
            case BELT_SLOT:
                return new GuiBeltSlot(player);
        }
        return null;
    }
}
