package gigaherz.toolbelt.client;

import gigaherz.toolbelt.BeltFinder;
import gigaherz.toolbelt.Config;
import gigaherz.toolbelt.ISideProxy;
import gigaherz.toolbelt.ToolBelt;
import gigaherz.toolbelt.network.BeltContentsChange;
import gigaherz.toolbelt.network.OpenBeltSlotInventory;
import gigaherz.toolbelt.network.SyncBeltSlotContents;
import gigaherz.toolbelt.slot.ExtensionSlotBelt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

public class ClientProxy implements ISideProxy
{
    public static KeyBinding keyOpenToolMenu;
    public static KeyBinding keyCycleToolMenuL;
    public static KeyBinding keyCycleToolMenuR;

    public static KeyBinding keyOpenBeltSlot;

    @SubscribeEvent
    public void handleKeys(InputEvent ev) // Not a mistake, I want both kb & mouse events handled.
    {
        Minecraft mc = Minecraft.getMinecraft();

        while (keyOpenToolMenu.isPressed())
        {
            if (mc.currentScreen == null)
            {
                ItemStack inHand = mc.thePlayer.getCurrentEquippedItem();
                if (Config.isItemStackAllowed(inHand))
                {
                    BeltFinder.BeltGetter getter = BeltFinder.findBelt(mc.thePlayer);
                    if (getter == null)
                        return;

                    mc.displayGuiScreen(new GuiRadialMenu(getter));
                }
            }
        }

        if (Config.customBeltSlotEnabled)
        {
            while (keyOpenBeltSlot.isPressed())
            {
                if (mc.currentScreen == null)
                {
                    ToolBelt.channel.sendToServer(new OpenBeltSlotInventory());
                }
            }
        }
    }

    public static void wipeOpen()
    {
        while (keyOpenToolMenu.isPressed())
        {
        }
    }

    @Override
    public void init()
    {
        ClientRegistry.registerKeyBinding(keyOpenToolMenu =
                new KeyBinding("key.toolbelt.open", Keyboard.KEY_R, "key.toolbelt.category"));
        //keyOpenToolMenu.

        ClientRegistry.registerKeyBinding(keyCycleToolMenuL =
                new KeyBinding("key.toolbelt.cycle.left", 0, "key.toolbelt.category"));

        ClientRegistry.registerKeyBinding(keyCycleToolMenuR =
                new KeyBinding("key.toolbelt.cycle.right", 0, "key.toolbelt.category"));

        if (Config.customBeltSlotEnabled)
        {
            ClientRegistry.registerKeyBinding(keyOpenBeltSlot =
                    new KeyBinding("key.toolbelt.slot", Keyboard.KEY_V, "key.toolbelt.category"));
        }

        MinecraftForge.EVENT_BUS.register(new LayerToolBelt());
        MinecraftForge.EVENT_BUS.register(new GuiRadialMenu.OverlayHandler());
        FMLCommonHandler.instance().bus().register(this);
        FMLCommonHandler.instance().bus().register(new Config());
    }

    @Override
    public void handleBeltContentsChange(final BeltContentsChange message)
    {
        // 1.7.10 handles packets on the main thread
        {
            Entity entity = Minecraft.getMinecraft().theWorld.getEntityByID(message.player);
            if (!(entity instanceof EntityPlayer))
                return;
            EntityPlayer player = (EntityPlayer) entity;
            switch (message.where)
            {
                case MAIN:
                    player.inventory.setInventorySlotContents(message.slot, message.stack);
                    break;
                case BELT_SLOT:
                    BeltFinder.instances.forEach((i) -> i.setToBeltSlot(player, message.stack));
                    break;
                case BAUBLES:
                    BeltFinder.instances.forEach((i) -> i.setToBaubles(player, message.slot, message.stack));
                    break;
            }
        }
    }

    @Override
    public void handleBeltSlotContents(SyncBeltSlotContents message)
    {
        // 1.7.10 handles packets on the main thread
        {
            Entity entity = Minecraft.getMinecraft().theWorld.getEntityByID(message.entityId);
            if (entity instanceof EntityPlayer)
            {
                ExtensionSlotBelt.get((EntityPlayer) entity).setAll(message.stacks);
            }
        }
    }
}
