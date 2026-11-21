package gigaherz.toolbelt.belt;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import baubles.api.expanded.BaubleExpandedSlots;
import baubles.api.expanded.IBaubleExpanded;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import gigaherz.toolbelt.ToolBelt;
import gigaherz.toolbelt.common.GuiHandler;
import gigaherz.toolbelt.customslots.ExtensionSlotItemHandler;
import gigaherz.toolbelt.customslots.IExtensionContainer;
import gigaherz.toolbelt.customslots.IExtensionSlot;
import gigaherz.toolbelt.customslots.IExtensionSlotItem;
import gigaherz.toolbelt.slot.ExtensionSlotBelt;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.*;
import net.minecraft.world.World;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

@Optional.InterfaceList({
        @Optional.Interface(modid = "Baubles", iface = "baubles.api.IBauble"),
        @Optional.Interface(modid = "Baubles|Expanded", iface = "baubles.api.expanded.IBaubleExpanded")
})
public class ItemToolBelt extends Item implements IBauble, IBaubleExpanded, IExtensionSlotItem
{
    public static final ImmutableSet<ResourceLocation> BELT_SLOT_LIST = ImmutableSet.of(ExtensionSlotBelt.BELT);

    @SideOnly(Side.CLIENT)
    private IIcon dyedIcon;
    @SideOnly(Side.CLIENT)
    private IIcon buckleIcon;
    public static IIcon emptySlotIcon;

    public ItemToolBelt(String name)
    {
        setUnlocalizedName(ToolBelt.MODID + "." + name);
        setTextureName(ToolBelt.MODID + ":" + name);
        maxStackSize = 1;
        setCreativeTab(CreativeTabs.tabTools);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World worldIn, EntityPlayer playerIn)
    {
        if (!worldIn.isRemote)
            playerIn.openGui(ToolBelt.instance, GuiHandler.BELT, worldIn, 0, 0, 0);

        return stack;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List tooltip, boolean advanced)
    {
        super.addInformation(stack, player, tooltip, advanced);

        int size = getSlotsCount(stack);

        tooltip.add(StatCollector.translateToLocalFormatted("text.toolbelt.tooltip", size - 2, size));
    }

    @Optional.Method(modid = "Baubles")
    @Override
    public BaubleType getBaubleType(ItemStack itemStack)
    {
        return BaubleType.BELT;
    }

    @Optional.Method(modid = "Baubles|Expanded")
    @Override
    public String[] getBaubleTypes(ItemStack itemstack)
    {
        return new String[] { BaubleExpandedSlots.beltType };
    }

    @Nonnull
    @Override
    public ImmutableSet<ResourceLocation> getAcceptableSlots(@Nonnull ItemStack stack)
    {
        return BELT_SLOT_LIST;
    }

    @Override
    public void onWornTick(ItemStack itemstack, EntityLivingBase player)
    {
        tickAllSlots(itemstack, player);
    }

    @Override
    public void onEquipped(ItemStack itemstack, EntityLivingBase player)
    {
    }

    @Override
    public void onUnequipped(ItemStack itemstack, EntityLivingBase player)
    {
    }

    @Override
    public boolean canEquip(ItemStack itemstack, EntityLivingBase player)
    {
        return true;
    }

    @Override
    public boolean canUnequip(ItemStack itemstack, EntityLivingBase player)
    {
        return true;
    }

    @Override
    public void onWornTick(ItemStack itemstack, IExtensionSlot slot)
    {
        tickAllSlots(itemstack, slot.getContainer().getOwner());
    }

    @Override
    public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected)
    {
        if (entityIn instanceof EntityLivingBase)
        {
            tickAllSlots(stack, (EntityLivingBase) entityIn);
        }
    }

    // Dyeing, using the same NBT as leather armor

    public static boolean hasColor(ItemStack stack)
    {
        NBTTagCompound nbt = stack.getTagCompound();
        return nbt != null && nbt.hasKey("display", 10) && nbt.getCompoundTag("display").hasKey("color", 3);
    }

    public static int getColor(ItemStack stack)
    {
        return hasColor(stack) ? stack.getTagCompound().getCompoundTag("display").getInteger("color") : 0xFFFFFF;
    }

    public static void setColor(ItemStack stack, int color)
    {
        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt == null)
            stack.setTagCompound(nbt = new NBTTagCompound());
        NBTTagCompound display = nbt.getCompoundTag("display");
        display.setInteger("color", color);
        nbt.setTag("display", display);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IIconRegister register)
    {
        itemIcon = register.registerIcon(getIconString());
        dyedIcon = register.registerIcon(ToolBelt.MODID + ":dyed_belt");
        buckleIcon = register.registerIcon(ToolBelt.MODID + ":belt_buckle");
        emptySlotIcon = register.registerIcon(ToolBelt.MODID + ":empty_belt_slot_background");
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean requiresMultipleRenderPasses()
    {
        return true;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIcon(ItemStack stack, int pass)
    {
        if (hasColor(stack))
            return pass == 0 ? dyedIcon : buckleIcon;
        return itemIcon;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public int getColorFromItemStack(ItemStack stack, int pass)
    {
        return pass == 0 ? getColor(stack) : 0xFFFFFF;
    }

    public static int getSlotsCount(ItemStack stack)
    {
        int size = 2;

        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt != null)
        {
            size = MathHelper.clamp_int(nbt.getInteger("Size"), 2, 9);
        }
        return size;
    }

    public static void setSlotsCount(ItemStack stack, int newSize)
    {
        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt == null)
        {
            nbt = new NBTTagCompound();
            nbt.setTag("Items", new NBTTagList());
        }

        nbt.setInteger("Size", newSize);
        stack.setTagCompound(nbt);
    }

    public static int[] xpCost = {3, 5, 8, 12, 15, 20, 30};

    public static int getUpgradeXP(ItemStack stack)
    {
        int slots = getSlotsCount(stack);

        if (slots >= 9)
            return -1;

        if (slots < 2)
            return 1;

        return xpCost[slots - 2];
    }

    public static ItemStack upgrade(ItemStack stack)
    {
        int slots = getSlotsCount(stack);

        if (slots >= 9)
            return stack.copy();

        stack = stack.copy();
        setSlotsCount(stack, slots + 1);
        return stack;
    }

    public ItemStack of(int upgradeLevel)
    {
        if (upgradeLevel < 0 || upgradeLevel >= 9)
            return null;

        ItemStack stack = new ItemStack(this);
        setSlotsCount(stack, upgradeLevel + 2);
        return stack;
    }

    private void tickAllSlots(ItemStack source, EntityLivingBase player)
    {
        BeltExtensionContainer container = new BeltExtensionContainer(source, player);
        for (IExtensionSlot slot : container.getSlots())
        {
            ((ExtensionSlotItemHandler) slot).onWornTick();
        }
    }

    public static class BeltExtensionContainer implements IExtensionContainer
    {
        private static final ResourceLocation SLOT_TYPE = new ResourceLocation("toolbelt", "pocket");
        private final ToolBeltInventory inventory;
        private final EntityLivingBase owner;
        private final ImmutableList<IExtensionSlot> slots;

        public BeltExtensionContainer(ItemStack source, EntityLivingBase owner)
        {
            this.inventory = new ToolBeltInventory(source);
            this.owner = owner;

            ExtensionSlotItemHandler[] slots = new ExtensionSlotItemHandler[inventory.getSlots()];

            for (int i = 0; i < inventory.getSlots(); i++)
            {
                slots[i] = new ExtensionSlotItemHandler(this, SLOT_TYPE, inventory, i)
                {
                    @Override
                    public boolean canEquip(@Nonnull ItemStack stack)
                    {
                        return BeltExtensionContainer.this.inventory.canInsertItem(this.slot, stack);
                    }
                };
            }

            this.slots = ImmutableList.copyOf(slots);
        }

        @Nonnull
        @Override
        public EntityLivingBase getOwner()
        {
            return owner;
        }

        @Nonnull
        @Override
        public ImmutableList<IExtensionSlot> getSlots()
        {
            return slots;
        }

        @Override
        public void onContentsChanged(IExtensionSlot slot)
        {

        }
    }

}