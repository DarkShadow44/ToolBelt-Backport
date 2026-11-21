package gigaherz.toolbelt.itemhandler;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

/**
 * Minimal backport of Forge's ItemStackHandler. Empty stacks are null.
 */
public class ItemStackHandler implements IItemHandlerModifiable {

    protected ItemStack[] stacks;

    public ItemStackHandler(int size) {
        stacks = new ItemStack[size];
    }

    @Override
    public int getSlots() {
        return stacks.length;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        validateSlotIndex(slot);
        return stacks[slot];
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        validateSlotIndex(slot);
        stacks[slot] = stack;
        onContentsChanged(slot);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (stack == null || stack.stackSize <= 0) return null;

        if (!isItemValid(slot, stack)) return stack;

        validateSlotIndex(slot);

        ItemStack existing = stacks[slot];

        int limit = Math.min(getSlotLimit(slot), stack.getMaxStackSize());

        if (existing != null) {
            if (!ItemHandlerHelper.canItemStacksStack(stack, existing)) return stack;

            limit -= existing.stackSize;
        }

        if (limit <= 0) return stack;

        boolean reachedLimit = stack.stackSize > limit;

        if (!simulate) {
            if (existing == null)
                stacks[slot] = reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, limit) : stack;
            else existing.stackSize += reachedLimit ? limit : stack.stackSize;
            onContentsChanged(slot);
        }

        return reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, stack.stackSize - limit) : null;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount == 0) return null;

        validateSlotIndex(slot);

        ItemStack existing = stacks[slot];

        if (existing == null) return null;

        int toExtract = Math.min(amount, existing.getMaxStackSize());

        if (existing.stackSize <= toExtract) {
            if (!simulate) {
                stacks[slot] = null;
                onContentsChanged(slot);
            }
            return existing;
        } else {
            if (!simulate) {
                stacks[slot] = ItemHandlerHelper.copyStackWithSize(existing, existing.stackSize - toExtract);
                onContentsChanged(slot);
            }
            return ItemHandlerHelper.copyStackWithSize(existing, toExtract);
        }
    }

    @Override
    public int getSlotLimit(int slot) {
        return 64;
    }

    public boolean isItemValid(int slot, ItemStack stack) {
        return true;
    }

    public NBTTagCompound serializeNBT() {
        NBTTagList nbtTagList = new NBTTagList();
        for (int i = 0; i < stacks.length; i++) {
            if (stacks[i] != null) {
                NBTTagCompound itemTag = new NBTTagCompound();
                itemTag.setInteger("Slot", i);
                stacks[i].writeToNBT(itemTag);
                nbtTagList.appendTag(itemTag);
            }
        }
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setTag("Items", nbtTagList);
        nbt.setInteger("Size", stacks.length);
        return nbt;
    }

    public void deserializeNBT(NBTTagCompound nbt) {
        stacks = new ItemStack[nbt.hasKey("Size", Constants.NBT.TAG_INT) ? nbt.getInteger("Size") : stacks.length];
        NBTTagList tagList = nbt.getTagList("Items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < tagList.tagCount(); i++) {
            NBTTagCompound itemTags = tagList.getCompoundTagAt(i);
            int slot = itemTags.getInteger("Slot");

            if (slot >= 0 && slot < stacks.length) stacks[slot] = ItemStack.loadItemStackFromNBT(itemTags);
        }
    }

    protected void validateSlotIndex(int slot) {
        if (slot < 0 || slot >= stacks.length)
            throw new RuntimeException("Slot " + slot + " not in valid range - [0," + stacks.length + ")");
    }

    protected void onContentsChanged(int slot) {}
}
