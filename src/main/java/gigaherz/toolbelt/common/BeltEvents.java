package gigaherz.toolbelt.common;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import gigaherz.toolbelt.BeltFinder;

public class BeltEvents {

    @SubscribeEvent
    public void startTracking(PlayerEvent.StartTracking event) {
        Entity entity = event.target;
        if (!(entity instanceof EntityPlayer)) return;

        EntityPlayer player = (EntityPlayer) entity;
        BeltFinder.sendSync(player);
    }
}
