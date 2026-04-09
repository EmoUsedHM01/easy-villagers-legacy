package com.easyvillagerslegacy.client.renderer;

import com.easyvillagerslegacy.block.BlockBreeder;
import com.easyvillagerslegacy.block.BlockFarmer;
import com.easyvillagerslegacy.block.BlockIronFarm;
import com.easyvillagerslegacy.block.BlockTrader;
import com.easyvillagerslegacy.block.BlockVillagerBase;

import cpw.mods.fml.common.registry.VillagerRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelIronGolem;
import net.minecraft.client.model.ModelVillager;
import net.minecraft.client.model.ModelZombie;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Custom item renderer for block items that shows villagers (and other entities)
 * inside the display case, matching how the placed blocks look with their TESRs.
 * Uses GL immediate mode for all geometry to avoid Tessellator issues in IItemRenderer.
 */
@SideOnly(Side.CLIENT)
public class ItemBlockVillagerRenderer implements IItemRenderer {

    private static final ResourceLocation TEX_VILLAGER   = new ResourceLocation("textures/entity/villager/villager.png");
    private static final ResourceLocation TEX_FARMER     = new ResourceLocation("textures/entity/villager/farmer.png");
    private static final ResourceLocation TEX_LIBRARIAN  = new ResourceLocation("textures/entity/villager/librarian.png");
    private static final ResourceLocation TEX_PRIEST     = new ResourceLocation("textures/entity/villager/priest.png");
    private static final ResourceLocation TEX_SMITH      = new ResourceLocation("textures/entity/villager/smith.png");
    private static final ResourceLocation TEX_BUTCHER    = new ResourceLocation("textures/entity/villager/butcher.png");
    private static final ResourceLocation TEX_ZOMBIE     = new ResourceLocation("textures/entity/zombie/zombie.png");
    private static final ResourceLocation TEX_IRON_GOLEM = new ResourceLocation("textures/entity/iron_golem.png");
    private static final ResourceLocation TEX_BLOCKS     = new ResourceLocation("textures/atlas/blocks.png");

    private final ModelVillager adultModel = new ModelVillager(0.0F);
    private final ModelZombie zombieModel = new ModelZombie();
    private final ModelIronGolem ironGolemModel = new ModelIronGolem();

    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type) {
        return true;
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
        return true;
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
        Block block = ((ItemBlock) item.getItem()).field_150939_a;
        if (!(block instanceof BlockVillagerBase)) return;
        BlockVillagerBase vBlock = (BlockVillagerBase) block;

        TextureManager texManager = Minecraft.getMinecraft().getTextureManager();

        GL11.glPushMatrix();
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);

        // Adjust position for held-in-hand rendering
        if (type == ItemRenderType.EQUIPPED || type == ItemRenderType.EQUIPPED_FIRST_PERSON) {
            GL11.glTranslatef(0.5F, 0.5F, 0.5F);
        }

        // Render the display case using GL immediate mode
        renderDisplayCase(texManager, vBlock);

        // Render entities inside the case
        if (block instanceof BlockBreeder) {
            renderBreederEntities(texManager);
        } else if (block instanceof BlockIronFarm) {
            renderIronFarmEntities(texManager);
        } else if (block instanceof BlockFarmer) {
            renderFarmerEntities(texManager);
        } else if (block instanceof BlockTrader) {
            renderTraderEntities(texManager);
        }

        GL11.glPopAttrib();
        GL11.glPopMatrix();
    }

    // ========================
    // Display case rendering (GL immediate mode)
    // ========================

    private void renderDisplayCase(TextureManager texManager, BlockVillagerBase vBlock) {
        GL11.glPushMatrix();
        GL11.glTranslatef(-0.5F, -0.5F, -0.5F);

        texManager.bindTexture(TEX_BLOCKS);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_COLOR_MATERIAL);
        GL11.glColorMaterial(GL11.GL_FRONT_AND_BACK, GL11.GL_AMBIENT_AND_DIFFUSE);
        GL11.glEnable(GL11.GL_NORMALIZE);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        IIcon ironIcon = vBlock.getIronIcon();
        IIcon glassIcon = vBlock.getGlassIcon();
        IIcon frameIcon = vBlock.getFrameIcon();
        IIcon frontIcon = vBlock.getAccentFrontIcon();
        IIcon backIcon = vBlock.getAccentBackIcon();

        // Bottom iron frame (4 border strips)
        drawTexturedBox(0, 0, 0, 16, 1, 1, ironIcon);
        drawTexturedBox(0, 0, 15, 16, 1, 16, ironIcon);
        drawTexturedBox(0, 0, 1, 1, 1, 15, ironIcon);
        drawTexturedBox(15, 0, 1, 16, 1, 15, ironIcon);

        // Top teal frame (4 border strips)
        drawTexturedBox(0, 15, 0, 16, 16, 1, frameIcon);
        drawTexturedBox(0, 15, 15, 16, 16, 16, frameIcon);
        drawTexturedBox(0, 15, 1, 1, 16, 15, frameIcon);
        drawTexturedBox(15, 15, 1, 16, 16, 15, frameIcon);

        // Corner pillars (teal frame)
        drawTexturedBox(0, 1, 0, 1, 15, 1, frameIcon);
        drawTexturedBox(0, 1, 15, 1, 15, 16, frameIcon);
        drawTexturedBox(15, 1, 0, 16, 15, 1, frameIcon);
        drawTexturedBox(15, 1, 15, 16, 15, 16, frameIcon);

        // Glass panels (4 sides + ceiling)
        drawTexturedBox(1, 1, 0, 15, 15, 1, glassIcon);
        drawTexturedBox(1, 1, 15, 15, 15, 16, glassIcon);
        drawTexturedBox(0, 1, 1, 1, 15, 15, glassIcon);
        drawTexturedBox(15, 1, 1, 16, 15, 15, glassIcon);
        drawTexturedBox(1, 15, 1, 15, 16, 15, glassIcon);

        // Floor accent (split front/back)
        drawTexturedBox(1, 0, 1, 15, 1, 8, frontIcon);
        drawTexturedBox(1, 0, 8, 15, 1, 15, backIcon);

        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_COLOR_MATERIAL);
        GL11.glPopMatrix();
    }

    /**
     * Draws a textured box using GL immediate mode. Pixel coordinates (0-16).
     * Uses the icon's UV coordinates for texture mapping.
     */
    private void drawTexturedBox(int px1, int py1, int pz1, int px2, int py2, int pz2, IIcon icon) {
        double x1 = px1 / 16.0, y1 = py1 / 16.0, z1 = pz1 / 16.0;
        double x2 = px2 / 16.0, y2 = py2 / 16.0, z2 = pz2 / 16.0;
        double minU = icon.getMinU(), maxU = icon.getMaxU();
        double minV = icon.getMinV(), maxV = icon.getMaxV();

        GL11.glBegin(GL11.GL_QUADS);

        // Bottom face (Y-)
        GL11.glNormal3f(0, -1, 0);
        GL11.glTexCoord2d(minU, maxV); GL11.glVertex3d(x1, y1, z2);
        GL11.glTexCoord2d(minU, minV); GL11.glVertex3d(x1, y1, z1);
        GL11.glTexCoord2d(maxU, minV); GL11.glVertex3d(x2, y1, z1);
        GL11.glTexCoord2d(maxU, maxV); GL11.glVertex3d(x2, y1, z2);

        // Top face (Y+)
        GL11.glNormal3f(0, 1, 0);
        GL11.glTexCoord2d(minU, minV); GL11.glVertex3d(x1, y2, z1);
        GL11.glTexCoord2d(minU, maxV); GL11.glVertex3d(x1, y2, z2);
        GL11.glTexCoord2d(maxU, maxV); GL11.glVertex3d(x2, y2, z2);
        GL11.glTexCoord2d(maxU, minV); GL11.glVertex3d(x2, y2, z1);

        // North face (Z-)
        GL11.glNormal3f(0, 0, -1);
        GL11.glTexCoord2d(minU, minV); GL11.glVertex3d(x2, y2, z1);
        GL11.glTexCoord2d(maxU, minV); GL11.glVertex3d(x1, y2, z1);
        GL11.glTexCoord2d(maxU, maxV); GL11.glVertex3d(x1, y1, z1);
        GL11.glTexCoord2d(minU, maxV); GL11.glVertex3d(x2, y1, z1);

        // South face (Z+)
        GL11.glNormal3f(0, 0, 1);
        GL11.glTexCoord2d(minU, minV); GL11.glVertex3d(x1, y2, z2);
        GL11.glTexCoord2d(maxU, minV); GL11.glVertex3d(x2, y2, z2);
        GL11.glTexCoord2d(maxU, maxV); GL11.glVertex3d(x2, y1, z2);
        GL11.glTexCoord2d(minU, maxV); GL11.glVertex3d(x1, y1, z2);

        // West face (X-)
        GL11.glNormal3f(-1, 0, 0);
        GL11.glTexCoord2d(minU, minV); GL11.glVertex3d(x1, y2, z1);
        GL11.glTexCoord2d(maxU, minV); GL11.glVertex3d(x1, y2, z2);
        GL11.glTexCoord2d(maxU, maxV); GL11.glVertex3d(x1, y1, z2);
        GL11.glTexCoord2d(minU, maxV); GL11.glVertex3d(x1, y1, z1);

        // East face (X+)
        GL11.glNormal3f(1, 0, 0);
        GL11.glTexCoord2d(minU, minV); GL11.glVertex3d(x2, y2, z2);
        GL11.glTexCoord2d(maxU, minV); GL11.glVertex3d(x2, y2, z1);
        GL11.glTexCoord2d(maxU, maxV); GL11.glVertex3d(x2, y1, z1);
        GL11.glTexCoord2d(minU, maxV); GL11.glVertex3d(x2, y1, z2);

        GL11.glEnd();
    }

    // ========================
    // Entity rendering
    // ========================

    private void renderTraderEntities(TextureManager texManager) {
        GL11.glPushMatrix();
        GL11.glTranslatef(0.0F, -0.4F, 0.0F);
        float scale = 0.35F;
        GL11.glScalef(scale, scale, scale);
        renderVillagerModel(texManager, TEX_VILLAGER);
        GL11.glPopMatrix();
    }

    private void renderBreederEntities(TextureManager texManager) {
        renderBed();

        float scale = 0.35F;

        // Villager 1 (front side of bed, facing villager 2) - farmer with hat
        GL11.glPushMatrix();
        GL11.glTranslatef(0.0F, -0.4F, -0.37F);
        GL11.glScalef(scale, scale, scale);
        renderVillagerModel(texManager, TEX_FARMER, true);
        GL11.glPopMatrix();

        // Villager 2 (back side of bed, facing villager 1)
        GL11.glPushMatrix();
        GL11.glTranslatef(0.0F, -0.4F, 0.37F);
        GL11.glScalef(scale, scale, scale);
        GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
        renderVillagerModel(texManager, TEX_LIBRARIAN);
        GL11.glPopMatrix();
    }

    private void renderIronFarmEntities(TextureManager texManager) {
        float smallScale = 0.245F;

        // Villager (stone side)
        GL11.glPushMatrix();
        GL11.glTranslatef(-0.2F, -0.4F, 0.3F);
        GL11.glScalef(smallScale, smallScale, smallScale);
        GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
        renderVillagerModel(texManager, TEX_VILLAGER);
        GL11.glPopMatrix();

        // Zombie (stone side, facing villager)
        GL11.glPushMatrix();
        GL11.glTranslatef(0.2F, -0.4F, 0.3F);
        GL11.glScalef(smallScale, smallScale, smallScale);
        zombieModel.isChild = false;
        zombieModel.aimedBow = true;
        GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
        GL11.glTranslatef(0.0F, -1.5F, 0.0F);
        texManager.bindTexture(TEX_ZOMBIE);
        zombieModel.render(null, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
        GL11.glPopMatrix();

        // Iron Golem (lava side, facing outward)
        GL11.glPushMatrix();
        GL11.glTranslatef(0.0F, -0.4F, -0.25F);
        float golemScale = 0.286F;
        GL11.glScalef(golemScale, golemScale, golemScale);
        GL11.glRotatef(-180.0F, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
        GL11.glTranslatef(0.0F, -1.5F, 0.0F);
        GL11.glColor4f(1.0F, 0.7F, 0.7F, 1.0F);
        texManager.bindTexture(TEX_IRON_GOLEM);
        ironGolemModel.render(null, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glPopMatrix();
    }

    private void renderFarmerEntities(TextureManager texManager) {
        GL11.glPushMatrix();
        GL11.glTranslatef(0.0F, -0.4F, -0.25F);
        float scale = 0.35F;
        GL11.glScalef(scale, scale, scale);
        GL11.glRotatef(0.0F, 0.0F, 1.0F, 0.0F);
        renderVillagerModel(texManager, TEX_FARMER, true);
        GL11.glPopMatrix();

        // Wheat offset to opposite side of villager
        GL11.glPushMatrix();
        GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
        renderWheatCrop(texManager);
        GL11.glPopMatrix();
    }

    private void renderVillagerModel(TextureManager texManager, ResourceLocation texture) {
        renderVillagerModel(texManager, texture, false);
    }

    private void renderVillagerModel(TextureManager texManager, ResourceLocation texture, boolean hasHat) {
        GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
        GL11.glTranslatef(0.0F, -1.5F, 0.0F);
        texManager.bindTexture(texture);
        adultModel.render(null, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
        if (hasHat) {
            TileEntityVillagerRenderer.renderStrawHat();
        }
    }

    // ========================
    // Bed rendering (untextured colored boxes)
    // ========================

    private void renderBed() {
        GL11.glPushMatrix();
        GL11.glTranslatef(-0.5F, -0.5F, -0.5F);

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_COLOR_MATERIAL);
        GL11.glColorMaterial(GL11.GL_FRONT_AND_BACK, GL11.GL_AMBIENT_AND_DIFFUSE);
        GL11.glEnable(GL11.GL_NORMALIZE);

        double bedY1 = 1.0 / 16.0, bedY2 = 4.0 / 16.0;
        double fX1 = 3.0 / 16.0, fX2 = 13.0 / 16.0;
        double fZ1 = 5.0 / 16.0, fZ2 = 11.0 / 16.0;
        drawColoredBox(fX1, bedY1, fZ1, fX2, bedY2, fZ2, 0.55F, 0.36F, 0.20F);

        drawColoredBox(3.0/16.0, bedY1, fZ1, 4.0/16.0, 7.0/16.0, fZ2, 0.55F, 0.36F, 0.20F);
        drawColoredBox(12.0/16.0, bedY1, fZ1, 13.0/16.0, 5.5/16.0, fZ2, 0.55F, 0.36F, 0.20F);
        drawColoredBox(6.0/16.0, bedY2, 4.5/16.0, 12.5/16.0, 4.5/16.0, 11.5/16.0, 0.75F, 0.10F, 0.10F);
        drawColoredBox(4.0/16.0, bedY2, 6.0/16.0, 6.5/16.0, 5.0/16.0, 10.0/16.0, 0.90F, 0.90F, 0.90F);

        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_COLOR_MATERIAL);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glPopMatrix();
    }

    // ========================
    // Wheat crop rendering (GL immediate mode with texture)
    // ========================

    private void renderWheatCrop(TextureManager texManager) {
        IIcon icon = Blocks.wheat.getIcon(0, 7);
        if (icon == null) return;

        GL11.glPushMatrix();
        GL11.glTranslatef(-0.18F, -0.5F, 0.0F);

        float scale = 0.5F;
        GL11.glScalef(scale, scale, scale);
        GL11.glTranslatef(0.0F, 0.13F, 0.0F);

        texManager.bindTexture(TEX_BLOCKS);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);

        float minU = icon.getMinU(), maxU = icon.getMaxU();
        float minV = icon.getMinV(), maxV = icon.getMaxV();

        GL11.glBegin(GL11.GL_QUADS);

        // Quad 1: along X axis (both sides)
        GL11.glNormal3f(0, 0, 1);
        GL11.glTexCoord2f(minU, minV); GL11.glVertex3f(-0.5F, 1.0F, 0.0F);
        GL11.glTexCoord2f(minU, maxV); GL11.glVertex3f(-0.5F, 0.0F, 0.0F);
        GL11.glTexCoord2f(maxU, maxV); GL11.glVertex3f( 0.5F, 0.0F, 0.0F);
        GL11.glTexCoord2f(maxU, minV); GL11.glVertex3f( 0.5F, 1.0F, 0.0F);

        GL11.glNormal3f(0, 0, -1);
        GL11.glTexCoord2f(minU, minV); GL11.glVertex3f( 0.5F, 1.0F, 0.0F);
        GL11.glTexCoord2f(minU, maxV); GL11.glVertex3f( 0.5F, 0.0F, 0.0F);
        GL11.glTexCoord2f(maxU, maxV); GL11.glVertex3f(-0.5F, 0.0F, 0.0F);
        GL11.glTexCoord2f(maxU, minV); GL11.glVertex3f(-0.5F, 1.0F, 0.0F);

        // Quad 2: along Z axis (both sides)
        GL11.glNormal3f(1, 0, 0);
        GL11.glTexCoord2f(minU, minV); GL11.glVertex3f(0.0F, 1.0F, -0.5F);
        GL11.glTexCoord2f(minU, maxV); GL11.glVertex3f(0.0F, 0.0F, -0.5F);
        GL11.glTexCoord2f(maxU, maxV); GL11.glVertex3f(0.0F, 0.0F,  0.5F);
        GL11.glTexCoord2f(maxU, minV); GL11.glVertex3f(0.0F, 1.0F,  0.5F);

        GL11.glNormal3f(-1, 0, 0);
        GL11.glTexCoord2f(minU, minV); GL11.glVertex3f(0.0F, 1.0F,  0.5F);
        GL11.glTexCoord2f(minU, maxV); GL11.glVertex3f(0.0F, 0.0F,  0.5F);
        GL11.glTexCoord2f(maxU, maxV); GL11.glVertex3f(0.0F, 0.0F, -0.5F);
        GL11.glTexCoord2f(maxU, minV); GL11.glVertex3f(0.0F, 1.0F, -0.5F);

        GL11.glEnd();

        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glPopMatrix();
    }

    // ========================
    // Utility
    // ========================

    private void drawColoredBox(double x1, double y1, double z1,
                                 double x2, double y2, double z2, float r, float g, float b) {
        GL11.glColor3f(r, g, b);
        GL11.glBegin(GL11.GL_QUADS);

        GL11.glNormal3f(0, -1, 0);
        GL11.glVertex3d(x1, y1, z2); GL11.glVertex3d(x1, y1, z1);
        GL11.glVertex3d(x2, y1, z1); GL11.glVertex3d(x2, y1, z2);

        GL11.glNormal3f(0, 1, 0);
        GL11.glVertex3d(x1, y2, z1); GL11.glVertex3d(x1, y2, z2);
        GL11.glVertex3d(x2, y2, z2); GL11.glVertex3d(x2, y2, z1);

        GL11.glNormal3f(0, 0, -1);
        GL11.glVertex3d(x2, y2, z1); GL11.glVertex3d(x1, y2, z1);
        GL11.glVertex3d(x1, y1, z1); GL11.glVertex3d(x2, y1, z1);

        GL11.glNormal3f(0, 0, 1);
        GL11.glVertex3d(x1, y2, z2); GL11.glVertex3d(x2, y2, z2);
        GL11.glVertex3d(x2, y1, z2); GL11.glVertex3d(x1, y1, z2);

        GL11.glNormal3f(-1, 0, 0);
        GL11.glVertex3d(x1, y2, z1); GL11.glVertex3d(x1, y2, z2);
        GL11.glVertex3d(x1, y1, z2); GL11.glVertex3d(x1, y1, z1);

        GL11.glNormal3f(1, 0, 0);
        GL11.glVertex3d(x2, y2, z2); GL11.glVertex3d(x2, y2, z1);
        GL11.glVertex3d(x2, y1, z1); GL11.glVertex3d(x2, y1, z2);

        GL11.glEnd();
    }
}
