package gigaherz.toolbelt.customslots;

import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import com.google.common.collect.ImmutableSet;

public class CapabilityExtensionSlotItem {

    // Special slot IDs
    public static final ResourceLocation ANY_SLOT = new ResourceLocation("forge:any");
    public static final ImmutableSet<ResourceLocation> ANY_SLOT_LIST = ImmutableSet.of(ANY_SLOT);

    // No capabilities in 1.7.10, the Item implements IExtensionSlotItem directly
    @Nullable
    public static IExtensionSlotItem get(@Nullable ItemStack stack) {
        if (stack == null || !(stack.getItem() instanceof IExtensionSlotItem)) return null;
        return (IExtensionSlotItem) stack.getItem();
    }
}
