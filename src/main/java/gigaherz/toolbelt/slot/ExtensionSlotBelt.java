package gigaherz.toolbelt.slot;

import java.util.List;
import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.IExtendedEntityProperties;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

import com.google.common.collect.ImmutableList;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import gigaherz.toolbelt.Config;
import gigaherz.toolbelt.ToolBelt;
import gigaherz.toolbelt.customslots.ExtensionSlotItemHandler;
import gigaherz.toolbelt.customslots.IExtensionContainer;
import gigaherz.toolbelt.customslots.IExtensionSlot;
import gigaherz.toolbelt.itemhandler.ItemHandlerHelper;
import gigaherz.toolbelt.itemhandler.ItemStackHandler;
import gigaherz.toolbelt.network.SyncBeltSlotContents;

public class ExtensionSlotBelt implements IExtensionContainer, IExtendedEntityProperties {
    ////////////////////////////////////////////////////////////
    // Capability support code
    //

    private static final ResourceLocation CAPABILITY_ID = new ResourceLocation("toolbelt", "belt_slot");

    public static void register() {
        MinecraftForge.EVENT_BUS.register(new AttachHandlers());
        Object handlers = Config.customBeltSlotEnabled ? new EventHandlers() : new EventHandlers.Disabled();
        MinecraftForge.EVENT_BUS.register(handlers);
        FMLCommonHandler.instance()
            .bus()
            .register(handlers);
    }

    @Nullable
    public static ExtensionSlotBelt get(EntityLivingBase player) {
        return (ExtensionSlotBelt) player.getExtendedProperties(CAPABILITY_ID.toString());
    }

    private static class AttachHandlers {

        @SubscribeEvent
        public void attachCapabilities(EntityEvent.EntityConstructing event) {
            if (event.entity instanceof EntityPlayer) {
                event.entity.registerExtendedProperties(
                    CAPABILITY_ID.toString(),
                    new ExtensionSlotBelt((EntityPlayer) event.entity) {

                        @Override
                        public void onContentsChanged(IExtensionSlot slot) {
                            if (Config.customBeltSlotEnabled) {
                                if (!getOwner().worldObj.isRemote) syncToSelfAndTracking();
                            }
                        }
                    });
            }
        }
    }

    private static class EventHandlers {

        static class Disabled {

            @SubscribeEvent
            public void entityTick(TickEvent.PlayerTickEvent event) {
                ExtensionSlotBelt instance = get(event.player);
                if (instance == null) return;
                instance.dropContents();
            }
        }

        @SubscribeEvent
        public void joinWorld(cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent event) {
            EntityPlayer target = event.player;
            if (target.worldObj.isRemote) return;
            ExtensionSlotBelt instance = get(target);
            if (Config.customBeltSlotEnabled) {
                if (instance == null) return;
                instance.syncToSelf();
            }
        }

        @SubscribeEvent
        public void joinWorld(cpw.mods.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent event) {
            EntityPlayer target = event.player;
            if (target.worldObj.isRemote) return;
            ExtensionSlotBelt instance = get(target);
            if (Config.customBeltSlotEnabled) {
                if (instance == null) return;
                instance.syncToSelf();
            }
        }

        // The client creates a new player entity on respawn
        @SubscribeEvent
        public void joinWorld(cpw.mods.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent event) {
            EntityPlayer target = event.player;
            if (target.worldObj.isRemote) return;
            ExtensionSlotBelt instance = get(target);
            if (Config.customBeltSlotEnabled) {
                if (instance == null) return;
                instance.syncToSelf();
            }
        }

        @SubscribeEvent
        public void track(PlayerEvent.StartTracking event) {
            Entity target = event.target;
            if (target.worldObj.isRemote) return;
            if (target instanceof EntityPlayer) {
                ExtensionSlotBelt instance = get((EntityLivingBase) target);
                if (Config.customBeltSlotEnabled) {
                    if (instance == null) return;
                    instance.syncTo(event.entityPlayer);
                }
            }
        }

        @SubscribeEvent
        public void entityTick(TickEvent.PlayerTickEvent event) {
            ExtensionSlotBelt instance = get(event.player);
            if (instance == null) return;
            instance.tickAllSlots();
        }

        @SubscribeEvent
        public void playerDeath(PlayerDropsEvent event) {
            EntityPlayer player = event.entityPlayer;
            ExtensionSlotBelt instance = get(player);
            if (instance == null) return;
            IExtensionSlot belt = instance.getBelt();
            ItemStack stack = belt.getContents();
            if (!player.worldObj.getGameRules()
                .getGameRuleBooleanValue("keepInventory")) {
                if (stack != null) {
                    event.drops.add(prepareDrop(player, stack, true));
                    belt.setContents(null);
                }
            }
        }

        @SubscribeEvent
        public void playerClone(PlayerEvent.Clone event) {
            EntityPlayer oldPlayer = event.original;
            EntityPlayer newPlayer = event.entityPlayer;
            ExtensionSlotBelt oldBelt = get(oldPlayer);
            ExtensionSlotBelt newBelt = get(newPlayer);
            if (oldBelt == null) return;
            ItemStack stack = oldBelt.getBelt()
                .getContents();
            if (newBelt == null) {
                if (stack != null) {
                    oldPlayer.func_146097_a(stack, true, false);
                }
            } else {
                // Transfer any remaining item. If it was death and keepInventory was off,
                // it will have been removed in LivingDropsEvent.
                newBelt.getBelt()
                    .setContents(stack);
            }
        }

        private Random rand = new Random();

        @Nullable
        private EntityItem prepareDrop(EntityPlayer player, ItemStack droppedItem, boolean dropAround) {
            if (droppedItem == null) {
                return null;
            } else {
                double d0 = player.posY - 0.30000001192092896D + (double) player.getEyeHeight();
                EntityItem entityitem = new EntityItem(player.worldObj, player.posX, d0, player.posZ, droppedItem);
                entityitem.delayBeforeCanPickup = 40;

                if (dropAround) {
                    float f = rand.nextFloat() * 0.5F;
                    float f1 = rand.nextFloat() * ((float) Math.PI * 2F);
                    entityitem.motionX = -MathHelper.sin(f1) * f;
                    entityitem.motionZ = MathHelper.cos(f1) * f;
                    entityitem.motionY = 0.20000000298023224D;
                } else {
                    float f2 = 0.3F;
                    entityitem.motionX = -MathHelper.sin(player.rotationYaw * 0.017453292F)
                        * MathHelper.cos(player.rotationPitch * 0.017453292F)
                        * f2;
                    entityitem.motionZ = MathHelper.cos(player.rotationYaw * 0.017453292F)
                        * MathHelper.cos(player.rotationPitch * 0.017453292F)
                        * f2;
                    entityitem.motionY = -MathHelper.sin(player.rotationPitch * 0.017453292F) * f2 + 0.1F;
                    float f3 = rand.nextFloat() * ((float) Math.PI * 2F);
                    f2 = 0.02F * rand.nextFloat();
                    entityitem.motionX += Math.cos((double) f3) * (double) f2;
                    entityitem.motionY += (rand.nextFloat() - rand.nextFloat()) * 0.1F;
                    entityitem.motionZ += Math.sin((double) f3) * (double) f2;
                }

                return entityitem;
            }
        }
    }

    private void dropContents() {
        for (IExtensionSlot slot : slots) {
            ItemStack stack = slot.getContents();
            if (stack != null) {
                if (owner instanceof EntityPlayer) ItemHandlerHelper.giveItemToPlayer((EntityPlayer) owner, stack);
                else owner.entityDropItem(stack, 0.1f);
                slot.setContents(null);
            }
        }
    }

    private void syncToSelf() {
        syncTo((EntityPlayerMP) owner);
    }

    protected void syncTo(EntityPlayer target) {
        // Fake players have no connection
        if (((EntityPlayerMP) target).playerNetServerHandler == null) return;
        SyncBeltSlotContents message = new SyncBeltSlotContents((EntityPlayer) owner, this);
        ToolBelt.channel.sendTo(message, (EntityPlayerMP) target);
    }

    protected void syncToSelfAndTracking() {
        SyncBeltSlotContents message = new SyncBeltSlotContents((EntityPlayer) owner, this);
        ((WorldServer) owner.worldObj).getEntityTracker()
            .func_151247_a(owner, ToolBelt.channel.getPacketFrom(message));
        syncTo((EntityPlayer) owner);
    }

    ////////////////////////////////////////////////////////////
    // Equipment container implementation
    //
    public static final ResourceLocation BELT = new ResourceLocation("examplemod", "belt");

    private final EntityLivingBase owner;
    private final ItemStackHandler inventory = new ItemStackHandler(1) {

        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            belt.onContentsChanged();
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (!Config.customBeltSlotEnabled) return stack;
            return super.insertItem(slot, stack, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            if (!Config.customBeltSlotEnabled) return 0;
            return super.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (!Config.customBeltSlotEnabled) return false;
            return super.isItemValid(slot, stack);
        }
    };
    private final ExtensionSlotItemHandler belt = new ExtensionSlotItemHandler(this, BELT, inventory, 0);
    private final ImmutableList<IExtensionSlot> slots = ImmutableList.of(belt);

    private ExtensionSlotBelt(EntityLivingBase owner) {
        this.owner = owner;
    }

    @Nonnull
    @Override
    public EntityLivingBase getOwner() {
        return owner;
    }

    @Nonnull
    @Override
    public ImmutableList<IExtensionSlot> getSlots() {
        return slots;
    }

    @Override
    public void onContentsChanged(IExtensionSlot slot) {

    }

    @Nonnull
    public IExtensionSlot getBelt() {
        return belt;
    }

    private void tickAllSlots() {
        for (IExtensionSlot slot : slots) {
            ((ExtensionSlotItemHandler) slot).onWornTick();
        }
    }

    public void setAll(List<ItemStack> stacks) {
        List<IExtensionSlot> slots = getSlots();
        for (int i = 0; i < slots.size(); i++) {
            slots.get(i)
                .setContents(stacks.get(i));
        }
    }

    @Override
    public void saveNBTData(NBTTagCompound compound) {
        compound.setTag(CAPABILITY_ID.toString(), inventory.serializeNBT());
    }

    @Override
    public void loadNBTData(NBTTagCompound compound) {
        inventory.deserializeNBT(compound.getCompoundTag(CAPABILITY_ID.toString()));
    }

    @Override
    public void init(Entity entity, World world) {}
}
