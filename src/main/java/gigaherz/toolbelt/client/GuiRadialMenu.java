package gigaherz.toolbelt.client;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import gigaherz.toolbelt.BeltFinder;
import gigaherz.toolbelt.Config;
import gigaherz.toolbelt.ToolBelt;
import gigaherz.toolbelt.belt.ToolBeltInventory;
import gigaherz.toolbelt.itemhandler.IItemHandler;
import gigaherz.toolbelt.network.SwapItems;

public class GuiRadialMenu extends GuiScreen {

    private final BeltFinder.BeltGetter getter;
    private ItemStack stackEquipped;
    private IItemHandler inventory;
    private List<ItemStack> cachedStacks = null;

    private boolean closing;
    private boolean doneClosing;
    private double startAnimation;

    private int selectedItem = -1;
    private boolean keyCycleBeforeL = false;
    private boolean keyCycleBeforeR = false;

    GuiRadialMenu(BeltFinder.BeltGetter getter) {
        this.getter = getter;
        this.stackEquipped = getter.getBelt();
        inventory = stackEquipped != null ? new ToolBeltInventory(stackEquipped) : null;
        startAnimation = Minecraft.getMinecraft().theWorld.getTotalWorldTime();
    }

    public static class OverlayHandler {

        @SubscribeEvent
        public void overlayEvent(RenderGameOverlayEvent.Pre event) {
            if (event.type != RenderGameOverlayEvent.ElementType.CROSSHAIRS) return;

            if (Minecraft.getMinecraft().currentScreen instanceof GuiRadialMenu) {
                event.setCanceled(true);
            }
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();

        if (closing) {
            if (doneClosing || inventory == null) {
                Minecraft.getMinecraft()
                    .displayGuiScreen(null);

                ClientProxy.wipeOpen();
            }

            return;
        }

        ItemStack inHand = mc.thePlayer.getCurrentEquippedItem();
        if (!Config.isItemStackAllowed(inHand)) {
            inventory = null;
        } else {
            ItemStack stack = getter.getBelt();
            if (stack == null) {
                inventory = null;
                stackEquipped = null;
            }
            // Reference comparison intended
            else if (stackEquipped != stack) {
                stackEquipped = stack;
                inventory = new ToolBeltInventory(stack);
                cachedStacks = null;
            }
        }

        if (inventory == null) {
            Minecraft.getMinecraft()
                .displayGuiScreen(null);
        } else if (!GameSettings.isKeyDown(ClientProxy.keyOpenToolMenu)) {
            if (Config.releaseToSwap) {
                processClick(false);
            } else {
                animateClose();
            }
        }
    }

    @Override
    protected void mouseMovedOrUp(int mouseX, int mouseY, int state) {
        // Only called on button release in 1.7.10
        super.mouseMovedOrUp(mouseX, mouseY, state);
        processClick(true);
    }

    protected void processClick(boolean triggeredByMouse) {
        if (closing) return;

        if (inventory == null) return;

        ItemStack inHand = mc.thePlayer.getCurrentEquippedItem();
        if (!Config.isItemStackAllowed(inHand)) return;

        List<Integer> items = Lists.newArrayList();
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack inSlot = inventory.getStackInSlot(i);
            if (inSlot != null || Config.displayEmptySlots) items.add(i);
        }

        boolean hasAddButton = false;
        int numItems = items.size();
        if (!Config.displayEmptySlots && (numItems < inventory.getSlots() && inHand != null)) {
            hasAddButton = true;
            numItems++;
        }
        if (numItems <= 0) return;

        if (selectedItem >= 0) {
            int swapWith;
            if (hasAddButton) swapWith = selectedItem == 0 ? -1 : items.get(selectedItem - 1);
            else swapWith = items.get(selectedItem);

            if (inHand == null && inventory.getStackInSlot(swapWith) == null) {
                if (triggeredByMouse) {
                    // ignore click
                    return;
                }
            } else {
                SwapItems.swapItem(swapWith, mc.thePlayer);
                ToolBelt.channel.sendToServer(new SwapItems(swapWith));
            }
        }

        animateClose();
    }

    private void animateClose() {
        closing = true;
        doneClosing = false;
        startAnimation = Minecraft.getMinecraft().theWorld.getTotalWorldTime();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        if (inventory == null) return;

        List<ItemStack> items = cachedStacks;
        if (items == null) {
            items = Lists.newArrayList();
            for (int i = 0; i < inventory.getSlots(); i++) {
                ItemStack inSlot = inventory.getStackInSlot(i);
                if (inSlot != null || Config.displayEmptySlots) items.add(inSlot);
            }
            cachedStacks = items;
        }

        ItemStack inHand = mc.thePlayer.getCurrentEquippedItem();
        if (!Config.isItemStackAllowed(inHand)) return;

        boolean hasAddButton = false;
        int numItems = items.size();
        if (!Config.displayEmptySlots && (numItems < inventory.getSlots() && inHand != null)) {
            hasAddButton = true;
            numItems++;
        }
        if (numItems <= 0) {
            drawCenteredString(
                fontRendererObj,
                I18n.format("text.toolbelt.empty"),
                width / 2,
                (height - fontRendererObj.FONT_HEIGHT) / 2,
                0xFFFFFFFF);
            if (closing) doneClosing = true;
            return;
        }

        if (hasAddButton) {
            drawCenteredString(
                fontRendererObj,
                I18n.format("text.toolbelt.insert"),
                width / 2,
                height / 2 + 45 - fontRendererObj.FONT_HEIGHT / 2,
                0xFFFFFFFF);
        }

        final float OPEN_ANIMATION_LENGTH = 2.5f;

        float openAnimation = closing
            ? (float) (1 - ((Minecraft.getMinecraft().theWorld.getTotalWorldTime() + partialTicks - startAnimation)
                / OPEN_ANIMATION_LENGTH))
            : (float) ((Minecraft.getMinecraft().theWorld.getTotalWorldTime() + partialTicks - startAnimation)
                / OPEN_ANIMATION_LENGTH);

        if (closing && openAnimation <= 0) doneClosing = true;

        float animProgress = MathHelper.clamp_float(openAnimation, 0, 1);
        float radiusIn = Math.max(0.1f, 30 * animProgress);
        float radiusOut = radiusIn * 2;
        float itemRadius = (radiusIn + radiusOut) * 0.5f;
        float animTop = (1 - animProgress) * height / 2.0f;

        int x = width / 2;
        int y = height / 2;

        if (!closing) {
            if (GameSettings.isKeyDown(ClientProxy.keyCycleToolMenuL)) {
                if (!keyCycleBeforeL) {
                    selectedItem--;
                    if (selectedItem < 0) selectedItem = numItems - 1;
                    setMousePosition(
                        x + itemRadius * Math.cos(-0.5 * Math.PI - selectedItem * 2 * Math.PI / numItems),
                        y + itemRadius * Math.sin(-0.5 * Math.PI + selectedItem * 2 * Math.PI / numItems));
                }
                keyCycleBeforeL = true;
            } else {
                keyCycleBeforeL = false;
            }

            if (GameSettings.isKeyDown(ClientProxy.keyCycleToolMenuR)) {
                if (!keyCycleBeforeR) {
                    if (selectedItem < 0) selectedItem = 0;
                    else {
                        selectedItem++;
                        if (selectedItem >= numItems) selectedItem = 0;
                    }
                    setMousePosition(
                        x + itemRadius * Math.cos(-0.5 * Math.PI - selectedItem * 2 * Math.PI / numItems),
                        y + itemRadius * Math.sin(-0.5 * Math.PI + selectedItem * 2 * Math.PI / numItems));
                }
                keyCycleBeforeR = true;
            } else {
                keyCycleBeforeR = false;
            }
        }

        double a = Math.toDegrees(Math.atan2(mouseY - y, mouseX - x));
        double d = Math.sqrt(Math.pow(mouseX - x, 2) + Math.pow(mouseY - y, 2));
        float s0 = (((0 - 0.5f) / (float) numItems) + 0.25f) * 360;
        if (a < s0) a += 360;

        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        GL11.glTranslatef(0, animTop, 0);

        Tessellator tessellator = Tessellator.instance;
        Tessellator buffer = tessellator;
        buffer.startDrawingQuads();
        boolean hasMouseOver = false;
        ItemStack itemMouseOver = null;

        if (!closing) {
            selectedItem = -1;
            for (int i = 0; i < numItems; i++) {
                float s = (((i - 0.5f) / (float) numItems) + 0.25f) * 360;
                float e = (((i + 0.5f) / (float) numItems) + 0.25f) * 360;
                if (a >= s && a < e && d >= radiusIn && d < radiusOut) if (a >= s && a < e
                    && d >= radiusIn
                    && (d < radiusOut || Config.clipMouseToCircle || Config.allowClickOutsideBounds)) {
                        selectedItem = i;
                        break;
                    }
            }
        }

        for (int i = 0; i < numItems; i++) {
            float s = (((i - 0.5f) / (float) numItems) + 0.25f) * 360;
            float e = (((i + 0.5f) / (float) numItems) + 0.25f) * 360;
            if (Config.minecraftHasNoCircles) {
                int color = selectedItem == i ? 255 : 0;
                float size = selectedItem == i ? 12 : 10;
                float angle = (float) Math.toRadians((s + e) * 0.5f);
                drawSquare(
                    buffer,
                    x + itemRadius * (float) Math.cos(angle),
                    y + itemRadius * (float) Math.sin(angle),
                    zLevel,
                    size,
                    color,
                    color,
                    color,
                    64);
            }
            if (selectedItem == i) {
                if (!Config.minecraftHasNoCircles)
                    drawPieArc(buffer, x, y, zLevel, radiusIn, radiusOut, s, e, 255, 255, 255, 64);

                if (i > 0 || !hasAddButton) {
                    hasMouseOver = true;

                    ItemStack inSlot = null;
                    if (hasAddButton) {
                        if (i > 0) {
                            inSlot = items.get(i - 1);
                        }
                    } else {
                        inSlot = items.get(i);
                    }

                    itemMouseOver = inSlot;
                }
            } else if (!Config.minecraftHasNoCircles) {
                drawPieArc(buffer, x, y, zLevel, radiusIn, radiusOut, s, e, 0, 0, 0, 64);
            }
        }

        if (Config.clipMouseToCircle) {
            double scaledX = Mouse.getX() - (mc.displayWidth / 2.0f);
            double scaledY = Mouse.getY() - (mc.displayHeight / 2.0f);

            double distance = Math.sqrt(scaledX * scaledX + scaledY * scaledY);
            double radius = 60.0 * (mc.displayWidth / width);

            if (distance > radius) {
                double fixedX = scaledX * radius / distance;
                double fixedY = scaledY * radius / distance;

                setCursorPosition((int) (mc.displayWidth / 2 + fixedX), (int) (mc.displayHeight / 2 + fixedY));
            }
        }

        tessellator.draw();
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        boolean hasItemInHand = inHand != null;
        if (hasMouseOver) {
            if (hasItemInHand) {
                if (itemMouseOver != null) drawCenteredString(
                    fontRendererObj,
                    I18n.format("text.toolbelt.swap"),
                    width / 2,
                    (height - fontRendererObj.FONT_HEIGHT) / 2,
                    0xFFFFFFFF);
                else drawCenteredString(
                    fontRendererObj,
                    I18n.format("text.toolbelt.insert"),
                    width / 2,
                    (height - fontRendererObj.FONT_HEIGHT) / 2,
                    0xFFFFFFFF);
            } else if (itemMouseOver != null) drawCenteredString(
                fontRendererObj,
                I18n.format("text.toolbelt.extract"),
                width / 2,
                (height - fontRendererObj.FONT_HEIGHT) / 2,
                0xFFFFFFFF);
        }

        if (Config.displayEmptySlots) {
            for (int i = 0; i < inventory.getSlots(); i++) {
                ItemStack inSlot = inventory.getStackInSlot(i);
                if (inSlot == null) {
                    float angle1 = ((i / (float) numItems) + 0.25f) * 2 * (float) Math.PI;
                    float posX = x + itemRadius * (float) Math.cos(angle1);
                    float posY = y + itemRadius * (float) Math.sin(angle1);
                    drawCenteredString(
                        fontRendererObj,
                        I18n.format("text.toolbelt.empty"),
                        (int) posX,
                        (int) posY - fontRendererObj.FONT_HEIGHT / 2,
                        0x7FFFFFFF);
                }
            }
        }

        RenderHelper.enableGUIStandardItemLighting();
        for (int i = 0; i < numItems; i++) {
            float angle1 = ((i / (float) numItems) + 0.25f) * 2 * (float) Math.PI;
            float posX = x - 8 + itemRadius * (float) Math.cos(angle1);
            float posY = y - 8 + itemRadius * (float) Math.sin(angle1);
            ItemStack inSlot = null;
            if (hasAddButton) {
                if (i > 0) {
                    inSlot = items.get(i - 1);
                }
            } else {
                inSlot = items.get(i);
            }

            if (inSlot != null) {
                this.itemRender.renderItemAndEffectIntoGUI(
                    this.fontRendererObj,
                    mc.getTextureManager(),
                    inSlot,
                    (int) posX,
                    (int) posY);
                this.itemRender.renderItemOverlayIntoGUI(
                    this.fontRendererObj,
                    mc.getTextureManager(),
                    inSlot,
                    (int) posX,
                    (int) posY,
                    "");
            }
        }
        RenderHelper.disableStandardItemLighting();

        GL11.glPopMatrix();

        if (itemMouseOver != null) renderToolTip(itemMouseOver, mouseX, mouseY);
    }

    private void setMousePosition(double x, double y) {
        setCursorPosition((int) (x * mc.displayWidth / width), (int) (y * mc.displayHeight / height));
    }

    private static final boolean LWJGL3IFY = Loader.isModLoaded("lwjgl3ify");

    /**
     * Mouse.setCursorPosition with lwjgl2 semantics (y = 0 at the bottom). lwjgl3ify flips y in Mouse.getY() but not
     * in setCursorPosition, which goes straight to SDL with y = 0 at the top.
     */
    private void setCursorPosition(int x, int y) {
        if (LWJGL3IFY) y = mc.displayHeight - 1 - y;
        Mouse.setCursorPosition(x, y);
    }

    private static final float PRECISION = 5;

    private void drawSquare(Tessellator buffer, float x, float y, float z, float size, int r, int g, int b, int a) {
        buffer.setColorRGBA(r, g, b, a);
        buffer.addVertex(x - size, y + size, z);
        buffer.addVertex(x + size, y + size, z);
        buffer.addVertex(x + size, y - size, z);
        buffer.addVertex(x - size, y - size, z);
    }

    private void drawPieArc(Tessellator buffer, float x, float y, float z, float radiusIn, float radiusOut,
        float startAngle, float endAngle, int r, int g, int b, int a) {
        float angle = endAngle - startAngle;
        int sections = Math.max(1, MathHelper.ceiling_float_int(angle / PRECISION));

        buffer.setColorRGBA(r, g, b, a);

        startAngle = (float) Math.toRadians(startAngle);
        endAngle = (float) Math.toRadians(endAngle);
        angle = endAngle - startAngle;

        for (int i = 0; i < sections; i++) {
            float angle1 = startAngle + (i / (float) sections) * angle;
            float angle2 = startAngle + ((i + 1) / (float) sections) * angle;

            float pos1InX = x + radiusIn * (float) Math.cos(angle1);
            float pos1InY = y + radiusIn * (float) Math.sin(angle1);
            float pos1OutX = x + radiusOut * (float) Math.cos(angle1);
            float pos1OutY = y + radiusOut * (float) Math.sin(angle1);
            float pos2OutX = x + radiusOut * (float) Math.cos(angle2);
            float pos2OutY = y + radiusOut * (float) Math.sin(angle2);
            float pos2InX = x + radiusIn * (float) Math.cos(angle2);
            float pos2InY = y + radiusIn * (float) Math.sin(angle2);

            buffer.addVertex(pos1OutX, pos1OutY, z);
            buffer.addVertex(pos1InX, pos1InY, z);
            buffer.addVertex(pos2InX, pos2InY, z);
            buffer.addVertex(pos2OutX, pos2OutY, z);
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
