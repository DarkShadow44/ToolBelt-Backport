package gigaherz.toolbelt.belt;

import gigaherz.toolbelt.Config;
import gigaherz.toolbelt.itemhandler.IItemHandlerModifiable;
import gigaherz.toolbelt.itemhandler.ItemHandlerHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.util.Constants;

public class ToolBeltInventory implements IItemHandlerModifiable
{
    private final ItemStack itemStack;

    public ToolBeltInventory(ItemStack itemStack)
    {
        this.itemStack = itemStack;
    }

    private NBTTagCompound getTag()
    {
        NBTTagCompound tag;
        tag = itemStack.getTagCompound();
        if (tag == null)
            itemStack.setTagCompound(tag = new NBTTagCompound());
        return tag;
    }

    // Ensure that the serialization is always compatible, even if it were to change upstream
    @Override
    public int getSlots()
    {
        return MathHelper.clamp_int(getTag().getInteger("Size"), 2, 9);
    }

    @Override
    public ItemStack getStackInSlot(int slot)
    {
        validateSlotIndex(slot);
        NBTTagList tagList = getTag().getTagList("Items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < tagList.tagCount(); i++)
        {
            NBTTagCompound itemTags = tagList.getCompoundTagAt(i);
            if (itemTags.getInteger("Slot") != slot)
                continue;

            return ItemStack.loadItemStackFromNBT(itemTags);
        }

        return null;
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack)
    {
        validateSlotIndex(slot);

        NBTTagCompound itemTag = null;
        boolean hasStack = stack != null && stack.stackSize > 0;
        if (hasStack)
        {
            itemTag = new NBTTagCompound();
            itemTag.setInteger("Slot", slot);
            stack.writeToNBT(itemTag);
        }

        NBTTagList tagList = getTag().getTagList("Items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < tagList.tagCount(); i++)
        {
            NBTTagCompound existing = tagList.getCompoundTagAt(i);
            if (existing.getInteger("Slot") != slot)
                continue;

            if (hasStack)
                tagList.func_150304_a(i, itemTag);
            else
                tagList.removeTag(i);
            return;
        }

        if (hasStack)
            tagList.appendTag(itemTag);

        getTag().setTag("Items", tagList);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
    {
        if (!canInsertItem(slot, stack))
            return stack;

        if (stack == null || stack.stackSize <= 0)
            return null;

        validateSlotIndex(slot);

        ItemStack existing = getStackInSlot(slot);

        int limit = stack.getMaxStackSize();

        if (existing != null)
        {
            if (!ItemHandlerHelper.canItemStacksStack(stack, existing))
                return stack;

            limit -= existing.stackSize;
        }

        if (limit <= 0)
            return stack;

        boolean reachedLimit = stack.stackSize > limit;

        if (!simulate)
        {
            if (existing == null)
            {
                existing = reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, limit) : stack;
            }
            else
            {
                existing.stackSize += reachedLimit ? limit : stack.stackSize;
            }
            setStackInSlot(slot, existing);
        }

        return reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, stack.stackSize - limit) : null;
    }

    protected boolean canInsertItem(int slot, ItemStack stack)
    {
        return Config.isItemStackAllowed(stack);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate)
    {
        if (amount == 0)
            return null;

        validateSlotIndex(slot);

        ItemStack existing = getStackInSlot(slot);

        if (existing == null)
            return null;

        int toExtract = Math.min(amount, existing.getMaxStackSize());

        if (existing.stackSize <= toExtract)
        {
            if (!simulate)
            {
                setStackInSlot(slot, null);
            }
            return existing;
        }
        else
        {
            if (!simulate)
            {
                setStackInSlot(slot, ItemHandlerHelper.copyStackWithSize(existing, existing.stackSize - toExtract));
            }
            return ItemHandlerHelper.copyStackWithSize(existing, toExtract);
        }
    }

    @Override
    public int getSlotLimit(int slot)
    {
        return 64;
    }

    private void validateSlotIndex(int slot)
    {
        if (slot < 0 || slot >= getSlots())
            throw new RuntimeException("Slot " + slot + " not in valid range - [0," + getSlots() + ")");
    }
}
