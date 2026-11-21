package gigaherz.toolbelt;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import com.google.common.collect.Lists;

public abstract class BeltFinder {

    public static List<BeltFinder> instances = Lists.newArrayList();

    @Nullable
    public abstract BeltGetter findStack(EntityPlayer player);

    @Nullable
    public static BeltGetter findBelt(EntityPlayer player) {
        for (int i = instances.size() - 1; i >= 0; i--) {
            BeltFinder instance = instances.get(i);
            BeltGetter getter = instance.findStack(player);
            if (getter != null) return getter;
        }
        return null;
    }

    public static void sendSync(EntityPlayer player) {
        BeltFinder.BeltGetter stack = findBelt(player);
        if (stack != null) {
            stack.syncToClients();
        }
    }

    public void setToBaubles(EntityPlayer player, int slot, ItemStack stack) {}

    public void setToBeltSlot(EntityLivingBase player, ItemStack stack) {}

    public interface BeltGetter {

        ItemStack getBelt();

        void syncToClients();
    }
}
