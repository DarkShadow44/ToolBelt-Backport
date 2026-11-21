package gigaherz.toolbelt;

import gigaherz.toolbelt.belt.BeltDyeRecipe;
import gigaherz.toolbelt.belt.ItemToolBelt;
import gigaherz.toolbelt.common.BeltEvents;
import gigaherz.toolbelt.common.GuiHandler;
import gigaherz.toolbelt.network.BeltContentsChange;
import gigaherz.toolbelt.network.OpenBeltSlotInventory;
import gigaherz.toolbelt.network.SwapItems;
import gigaherz.toolbelt.network.SyncBeltSlotContents;
import gigaherz.toolbelt.slot.ExtensionSlotBelt;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.oredict.RecipeSorter;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import org.apache.logging.log4j.Logger;

import java.io.File;

@Mod(modid = ToolBelt.MODID,
        name = "ToolBelt Backport",
        version = ToolBelt.VERSION,
        acceptedMinecraftVersions = "[1.7.10]",
        dependencies = "after:Baubles;after:Baubles|Expanded",
        guiFactory = "gigaherz.toolbelt.client.config.ConfigGuiFactory")
public class ToolBelt
{
    public static final String MODID = "toolbelt";
    public static final String VERSION = Tags.VERSION;
    public static final String CHANNEL = MODID;

    public static ItemToolBelt belt;
    public static Item pouch;

    @Mod.Instance(value = ToolBelt.MODID)
    public static ToolBelt instance;

    @SidedProxy(clientSide = "gigaherz.toolbelt.client.ClientProxy", serverSide = "gigaherz.toolbelt.server.ServerProxy")
    public static ISideProxy proxy;

    public static Logger logger;

    public static GuiHandler guiHandler;

    public static SimpleNetworkWrapper channel;

    public static void registerItems()
    {
        GameRegistry.registerItem(belt = new ItemToolBelt("belt"), "belt");
        GameRegistry.registerItem(pouch = new Item().setUnlocalizedName(MODID + ".pouch").setTextureName(MODID + ":pouch").setCreativeTab(CreativeTabs.tabTools), "pouch");
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();

        channel = NetworkRegistry.INSTANCE.newSimpleChannel(CHANNEL);

        int messageNumber = 0;
        channel.registerMessage(SwapItems.Handler.class, SwapItems.class, messageNumber++, Side.SERVER);
        channel.registerMessage(BeltContentsChange.Handler.class, BeltContentsChange.class, messageNumber++, Side.CLIENT);
        channel.registerMessage(OpenBeltSlotInventory.Handler.class, OpenBeltSlotInventory.class, messageNumber++, Side.SERVER);
        channel.registerMessage(SyncBeltSlotContents.Handler.class, SyncBeltSlotContents.class, messageNumber++, Side.CLIENT);
        logger.debug("Final message number: " + messageNumber);

        File configurationFile = event.getSuggestedConfigurationFile();
        Config.loadConfig(configurationFile);

        registerItems();

        ExtensionSlotBelt.register();

        BeltFinderBeltSlot.initBaubles();
        if (Loader.isModLoaded("Baubles"))
            BeltFinderBaubles.initBaubles();

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new BeltEvents());
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        proxy.init();

        NetworkRegistry.INSTANCE.registerGuiHandler(this, guiHandler = new GuiHandler());

        GameRegistry.addShapedRecipe(new ItemStack(belt), "sls", "l l", "lil", 's', Items.string, 'l', Items.leather, 'i', Items.iron_ingot);
        GameRegistry.addShapedRecipe(new ItemStack(pouch), "sgs", "l l", "sls", 's', Items.string, 'l', Items.leather, 'g', Items.gold_ingot);
        RecipeSorter.register(MODID + ":belt_dyes", BeltDyeRecipe.class, RecipeSorter.Category.SHAPELESS, "after:minecraft:shapeless");
        GameRegistry.addRecipe(new BeltDyeRecipe());
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        Config.postInit();
    }

    @SubscribeEvent
    public void anvilChange(AnvilUpdateEvent ev)
    {
        if (Config.disableAnvilUpgrading)
            return;

        ItemStack left = ev.left;
        ItemStack right = ev.right;
        if (left == null || left.getItem() != belt)
            return;
        if (right == null || right.getItem() != pouch)
            return;
        int cost = ItemToolBelt.getUpgradeXP(left);
        if (cost < 0)
        {
            ev.setCanceled(true);
            return;
        }
        ev.cost = cost;
        ev.materialCost = 1;

        ev.output = ItemToolBelt.upgrade(left);
    }

    public static ResourceLocation location(String path)
    {
        return new ResourceLocation(MODID, path);
    }

}
