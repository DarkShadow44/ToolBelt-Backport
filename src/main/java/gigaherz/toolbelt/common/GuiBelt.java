package gigaherz.toolbelt.common;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gigaherz.toolbelt.ToolBelt;

@SideOnly(Side.CLIENT)
public class GuiBelt extends GuiContainer {

    /**
     * The ResourceLocation containing the chest GUI texture.
     */
    private static final ResourceLocation GUI_TEXTURE = ToolBelt.location("textures/gui/belt.png");
    private final IInventory playerInventory;
    private final ItemStack beltStack;

    public GuiBelt(IInventory playerInventory, int blockedSlot, ItemStack beltStack) {
        super(new ContainerBelt(playerInventory, blockedSlot, beltStack));
        this.playerInventory = playerInventory;
        this.beltStack = beltStack;
        this.allowUserInput = false;
        this.xSize = 176;
        this.ySize = 133 + (((ContainerBelt) this.inventorySlots).beltRows - 1) * 18;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        this.fontRendererObj.drawString(this.beltStack.getDisplayName(), 8, 6, 4210752);
        this.fontRendererObj
            .drawString(I18n.format(this.playerInventory.getInventoryName()), 8, this.ySize - 96 + 2, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager()
            .bindTexture(GUI_TEXTURE);
        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;
        ContainerBelt container = (ContainerBelt) this.inventorySlots;
        int extra = (container.beltRows - 1) * 18;

        // Stretch the background by repeating the empty band behind the belt row
        this.drawTexturedModalRect(i, j, 0, 0, this.xSize, 38);
        for (int row = 1; row < container.beltRows; row++) {
            this.drawTexturedModalRect(i, j + 20 + row * 18, 0, 20, this.xSize, 18);
        }
        this.drawTexturedModalRect(i, j + 38 + extra, 0, 38, this.xSize, 133 - 38);

        for (int row = 0; row < container.beltRows; row++) {
            int slots = container.getRowSlots(row);
            int width = slots * 18;
            int x = 7 + ((9 - slots) * 18) / 2;
            this.drawTexturedModalRect(i + x, j + 19 + row * 18, 0, 133, width, 18);
        }
    }
}
