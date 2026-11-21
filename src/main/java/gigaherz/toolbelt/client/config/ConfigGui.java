package gigaherz.toolbelt.client.config;

import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.config.ConfigElement;

import com.google.common.collect.Lists;

import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.client.config.IConfigElement;
import gigaherz.toolbelt.Config;
import gigaherz.toolbelt.ToolBelt;

public class ConfigGui extends GuiConfig {

    public ConfigGui(GuiScreen parent) {
        super(parent, getConfigElements(), ToolBelt.MODID, false, false, getTitle());
    }

    private static String getTitle() {
        return I18n.format("text." + ToolBelt.MODID + ".name");
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static List<IConfigElement> getConfigElements() {
        List<IConfigElement> configElements = Lists.newArrayList();

        configElements.addAll(new ConfigElement(Config.display).getChildElements());

        configElements.addAll(new ConfigElement(Config.input).getChildElements());

        configElements.addAll(new ConfigElement(Config.behaviour).getChildElements());

        return configElements;
    }
}
