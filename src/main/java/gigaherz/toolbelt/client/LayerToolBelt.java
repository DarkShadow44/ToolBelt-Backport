package gigaherz.toolbelt.client;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.RenderPlayerEvent;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import gigaherz.toolbelt.BeltFinder;
import gigaherz.toolbelt.Config;
import gigaherz.toolbelt.ToolBelt;
import gigaherz.toolbelt.belt.ItemToolBelt;
import gigaherz.toolbelt.belt.ToolBeltInventory;
import gigaherz.toolbelt.itemhandler.IItemHandler;

public class LayerToolBelt {

    private static final ResourceLocation TEXTURE_BELT = ToolBelt.location("textures/entity/belt.png");
    private static final ResourceLocation TEXTURE_BELT_DYED = ToolBelt.location("textures/entity/dyed_belt.png");

    private RenderPlayer livingEntityRenderer;
    private int dyeColor = -1;

    private final ModelBase beltModel = new ModelBase() {

        final ModelRenderer belt = new ModelRenderer(this);
        final ModelRenderer buckle = new ModelRenderer(this, 10, 10);
        final ModelRenderer pocketL = new ModelRenderer(this, 0, 10);
        final ModelRenderer pocketR = new ModelRenderer(this, 0, 10);

        {
            belt.addBox(-5, 10, -3, 10, 4, 6);

            buckle.addBox(-2.5f, 9.5f, -3.5f, 5, 5, 1);

            pocketL.addBox(-2, 12, 5, 4, 4, 1);
            pocketL.rotateAngleY = (float) Math.toRadians(-90);
            pocketR.addBox(-2, 12, 5, 4, 4, 1);
            pocketR.rotateAngleY = (float) Math.toRadians(90);
        }

        @Override
        public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
            float headPitch, float scale) {
            GL11.glDisable(GL12.GL_RESCALE_NORMAL);
            GL11.glDisable(GL11.GL_CULL_FACE);
            if (dyeColor != -1) {
                Minecraft.getMinecraft()
                    .getTextureManager()
                    .bindTexture(TEXTURE_BELT_DYED);
                GL11.glColor4f(
                    ((dyeColor >> 16) & 255) / 255.0F,
                    ((dyeColor >> 8) & 255) / 255.0F,
                    (dyeColor & 255) / 255.0F,
                    1.0F);
            }
            belt.render(scale);
            pocketL.render(scale);
            pocketR.render(scale);
            if (dyeColor != -1) {
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                Minecraft.getMinecraft()
                    .getTextureManager()
                    .bindTexture(TEXTURE_BELT);
            }

            GL11.glPushMatrix();
            GL11.glScalef(0.8f, 1, 1);
            buckle.render(scale);
            GL11.glPopMatrix();
        }
    };

    // 1.7.10 has no render layers, the specials event runs with the same transforms
    @SubscribeEvent
    public void renderPlayerSpecials(RenderPlayerEvent.Specials.Post event) {
        this.livingEntityRenderer = event.renderer;
        doRenderLayer(event.entityPlayer, 0, 0, event.partialRenderTick, 0, 0, 0, 0.0625F);
    }

    public void doRenderLayer(EntityPlayer player, float limbSwing, float limbSwingAmount, float partialTicks,
        float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        boolean flag = true; // No left-handed players in 1.7.10

        if (!Config.showBeltOnPlayers) return;

        BeltFinder.BeltGetter getter = BeltFinder.findBelt(player);
        if (getter == null) return;

        ItemStack stack = getter.getBelt();
        if (stack == null) return;

        IItemHandler cap = new ToolBeltInventory(stack);

        ItemStack firstItem = cap.getStackInSlot(0);
        ItemStack secondItem = cap.getStackInSlot(1);

        ItemStack leftItem = flag ? firstItem : secondItem;
        ItemStack rightItem = flag ? secondItem : firstItem;

        GL11.glPushMatrix();

        // 1.7.10 sneaking doesn't move the model down, no translate needed

        this.translateToBody();

        if (leftItem != null || rightItem != null) {
            GL11.glPushMatrix();

            if (this.livingEntityRenderer.modelBipedMain.isChild) {
                GL11.glTranslatef(0.0F, 0.75F, 0.0F);
                GL11.glScalef(0.5F, 0.5F, 0.5F);
            }

            this.renderHeldItem(player, rightItem, false);
            this.renderHeldItem(player, leftItem, true);

            GL11.glPopMatrix();
        }

        GL11.glTranslatef(0.0F, 0.19F, 0.0F);
        GL11.glScalef(0.85f, 0.6f, 0.78f);

        dyeColor = ItemToolBelt.hasColor(stack) ? ItemToolBelt.getColor(stack) : -1;
        Minecraft.getMinecraft()
            .getTextureManager()
            .bindTexture(TEXTURE_BELT);
        this.beltModel.render(player, 0, 0, 0, 0, 0, scale);

        GL11.glPopMatrix();
    }

    private void translateToBody() {
        this.livingEntityRenderer.modelBipedMain.bipedBody.postRender(0.0625F);
    }

    private void renderHeldItem(EntityLivingBase player, ItemStack stack, boolean leftHand) {
        if (stack == null) return;

        GL11.glPushMatrix();

        if (leftHand) GL11.glTranslatef(-4.35f / 16.0F, 0.7f, -0.1f);
        else GL11.glTranslatef(4.35f / 16.0F, 0.7f, -0.1f);
        GL11.glRotatef(40.0F, 1.0F, 0.0F, 0.0F);
        float scale = Config.beltItemScale;
        GL11.glScalef(scale, scale, scale);
        renderItemSide(player, stack, leftHand);
        GL11.glPopMatrix();
    }

    // The third person held item transforms from RenderPlayer.renderEquippedItems, mirrored for the left hand
    private void renderItemSide(EntityLivingBase player, ItemStack stack, boolean leftHand) {
        if (leftHand) {
            GL11.glScalef(-1.0F, 1.0F, 1.0F);
            GL11.glFrontFace(GL11.GL_CW);
        }

        Item item = stack.getItem();
        IItemRenderer customRenderer = MinecraftForgeClient
            .getItemRenderer(stack, IItemRenderer.ItemRenderType.EQUIPPED);
        boolean is3D = customRenderer != null && customRenderer.shouldUseRenderHelper(
            IItemRenderer.ItemRenderType.EQUIPPED,
            stack,
            IItemRenderer.ItemRendererHelper.BLOCK_3D);
        boolean isBlock3D = item instanceof ItemBlock && RenderBlocks.renderItemIn3d(
            Block.getBlockFromItem(item)
                .getRenderType());

        float f2;
        if (is3D || isBlock3D) {
            f2 = 0.5F * 0.75F;
            GL11.glTranslatef(0.0F, 0.1875F, -0.3125F);
            GL11.glRotatef(20.0F, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
            GL11.glScalef(-f2, -f2, f2);
        } else if (item == Items.bow) {
            f2 = 0.625F;
            GL11.glTranslatef(0.0F, 0.125F, 0.3125F);
            GL11.glRotatef(-20.0F, 0.0F, 1.0F, 0.0F);
            GL11.glScalef(f2, -f2, f2);
            GL11.glRotatef(-100.0F, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
        } else if (item.isFull3D()) {
            f2 = 0.625F;
            if (item.shouldRotateAroundWhenRendering()) {
                GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
                GL11.glTranslatef(0.0F, -0.125F, 0.0F);
            }
            GL11.glTranslatef(0.0F, 0.1875F, 0.0F);
            GL11.glScalef(f2, -f2, f2);
            GL11.glRotatef(-100.0F, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
        } else {
            f2 = 0.375F;
            GL11.glTranslatef(0.25F, 0.1875F, -0.1875F);
            GL11.glScalef(f2, f2, f2);
            GL11.glRotatef(60.0F, 0.0F, 0.0F, 1.0F);
            GL11.glRotatef(-90.0F, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(20.0F, 0.0F, 0.0F, 1.0F);
        }

        int passes = item.requiresMultipleRenderPasses() ? item.getRenderPasses(stack.getItemDamage()) : 1;
        for (int pass = 0; pass < passes; ++pass) {
            int color = item.getColorFromItemStack(stack, pass);
            GL11.glColor4f(((color >> 16) & 255) / 255.0F, ((color >> 8) & 255) / 255.0F, (color & 255) / 255.0F, 1.0F);
            RenderManager.instance.itemRenderer.renderItem(player, stack, pass);
        }
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        if (leftHand) GL11.glFrontFace(GL11.GL_CCW);
    }
}
