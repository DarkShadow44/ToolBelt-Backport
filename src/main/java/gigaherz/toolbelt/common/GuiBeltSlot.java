package gigaherz.toolbelt.common;

import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.InventoryEffectRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiBeltSlot extends InventoryEffectRenderer {

    /** The old x position of the mouse pointer */
    private float oldMouseX;
    /** The old y position of the mouse pointer */
    private float oldMouseY;

    private static final ResourceLocation GUI_TEXTURE = new ResourceLocation("textures/gui/container/inventory.png");

    public GuiBeltSlot(EntityPlayer player) {
        super(new ContainerBeltSlot(player.inventory, !player.worldObj.isRemote, player));
        this.allowUserInput = true;
    }

    /**
     * Draw the foreground layer for the GuiContainer (everything in front of the items)
     */
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        this.fontRendererObj.drawString(I18n.format("container.crafting"), 86, 16, 4210752);
    }

    /**
     * Draws the screen and all the components in it.
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.oldMouseX = (float) mouseX;
        this.oldMouseY = (float) mouseY;
    }

    /**
     * Draws the background layer of this container (behind the items).
     */
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager()
            .bindTexture(GUI_TEXTURE);
        int i = this.guiLeft;
        int j = this.guiTop;
        this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);
        // The vanilla texture has no belt slot, borrow the frame of the helmet slot
        this.drawTexturedModalRect(i + ContainerBeltSlot.SLOT_X - 1, j + ContainerBeltSlot.SLOT_Y - 1, 7, 7, 18, 18);
        GuiInventory.func_147046_a(
            i + 51,
            j + 75,
            30,
            (float) (i + 51) - this.oldMouseX,
            (float) (j + 75 - 50) - this.oldMouseY,
            this.mc.thePlayer);
    }
}
