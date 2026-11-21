package gigaherz.toolbelt.customslots;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import gigaherz.toolbelt.itemhandler.IItemHandlerModifiable;

public class ExtensionSlotItemHandler implements IExtensionSlot {

    protected final IExtensionContainer owner;
    protected final ResourceLocation slotType;
    protected final int slot;
    protected final IItemHandlerModifiable inventory;

    public ExtensionSlotItemHandler(IExtensionContainer owner, ResourceLocation slotType,
        IItemHandlerModifiable inventory, int slot) {
        this.owner = owner;
        this.slotType = slotType;
        this.slot = slot;
        this.inventory = inventory;
    }

    @Nonnull
    @Override
    public IExtensionContainer getContainer() {
        return owner;
    }

    @Nonnull
    @Override
    public ResourceLocation getType() {
        return slotType;
    }

    /**
     * @return The contents of the slot. The stack is *NOT* required to be of an IExtensionSlotItem!
     */
    @Nonnull
    @Override
    public ItemStack getContents() {
        return inventory.getStackInSlot(slot);
    }

    @Override
    public void setContents(@Nonnull ItemStack stack) {
        ItemStack oldStack = getContents();
        if (oldStack == stack) return;
        if (oldStack != null) notifyUnequip(oldStack);
        inventory.setStackInSlot(slot, stack);
        if (stack != null) notifyEquip(stack);
    }

    @Override
    public void onContentsChanged() {
        owner.onContentsChanged(this);
    }

    private void notifyEquip(ItemStack stack) {
        IExtensionSlotItem extItem = CapabilityExtensionSlotItem.get(stack);
        if (extItem == null) return;
        extItem.onEquipped(stack, this);
    }

    private void notifyUnequip(ItemStack stack) {
        IExtensionSlotItem extItem = CapabilityExtensionSlotItem.get(stack);
        if (extItem == null) return;
        extItem.onUnequipped(stack, this);
    }

    public void onWornTick() {
        ItemStack stack = getContents();
        if (stack == null) return;
        IExtensionSlotItem extItem = CapabilityExtensionSlotItem.get(stack);
        if (extItem == null) return;
        extItem.onWornTick(stack, this);
    }
}
