package gigaherz.toolbelt.customslots;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import com.google.common.collect.ImmutableSet;

public interface IExtensionSlot {

    // Context
    @Nonnull
    IExtensionContainer getContainer();

    @Nonnull
    ResourceLocation getType();

    // Access
    @Nonnull
    ItemStack getContents();

    void setContents(@Nonnull ItemStack stack);

    void onContentsChanged();

    // Permissions

    /**
     * Queries wether or not the stack can be placed in this slot.
     *
     * @param stack The ItemStack in the slot.
     */
    default boolean canEquip(@Nonnull ItemStack stack) {
        IExtensionSlotItem extItem = CapabilityExtensionSlotItem.get(stack);
        return extItem != null && IExtensionSlot.isAcceptableSlot(this, stack, extItem)
            && extItem.canEquip(stack, this);
    }

    /**
     * Queries wether or not the stack can be removed from this slot.
     *
     * @param stack The ItemStack in the slot.
     */
    default boolean canUnequip(@Nonnull ItemStack stack) {
        IExtensionSlotItem extItem = CapabilityExtensionSlotItem.get(stack);
        return (extItem == null || extItem.canUnequip(stack, this));
    }

    static boolean isAcceptableSlot(@Nonnull IExtensionSlot slot, @Nonnull ItemStack stack,
        @Nonnull IExtensionSlotItem extItem) {
        ImmutableSet<ResourceLocation> slots = extItem.getAcceptableSlots(stack);
        return slots.contains(CapabilityExtensionSlotItem.ANY_SLOT) || slots.contains(slot.getType());
    }
}
