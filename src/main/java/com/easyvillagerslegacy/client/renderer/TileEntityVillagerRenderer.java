package com.easyvillagerslegacy.client.renderer;

import com.easyvillagerslegacy.tileentity.TileEntityBreeder;
import com.easyvillagerslegacy.tileentity.TileEntityFarmer;
import com.easyvillagerslegacy.tileentity.TileEntityIronFarm;
import com.easyvillagerslegacy.tileentity.TileEntityVillagerBase;

import cpw.mods.fml.common.registry.VillagerRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelIronGolem;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.ModelVillager;
import net.minecraft.client.model.ModelZombie;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * TESR that renders villager models inside blocks with profession-correct skins.
 * Uses same texture lookup as vanilla RenderVillager: 5 vanilla professions
 * + Forge VillagerRegistry for modded ones (GTNH Forestry, Thaumcraft, etc.).
 */
@SideOnly(Side.CLIENT)
public class TileEntityVillagerRenderer extends TileEntitySpecialRenderer {

    // Vanilla profession textures (same as RenderVillager)
    private static final ResourceLocation TEX_VILLAGER   = new ResourceLocation("textures/entity/villager/villager.png");
    private static final ResourceLocation TEX_FARMER     = new ResourceLocation("textures/entity/villager/farmer.png");
    private static final ResourceLocation TEX_LIBRARIAN  = new ResourceLocation("textures/entity/villager/librarian.png");
    private static final ResourceLocation TEX_PRIEST     = new ResourceLocation("textures/entity/villager/priest.png");
    private static final ResourceLocation TEX_SMITH      = new ResourceLocation("textures/entity/villager/smith.png");
    private static final ResourceLocation TEX_BUTCHER    = new ResourceLocation("textures/entity/villager/butcher.png");

    private static final ResourceLocation TEX_ZOMBIE     = new ResourceLocation("textures/entity/zombie/zombie.png");
    private static final ResourceLocation TEX_IRON_GOLEM = new ResourceLocation("textures/entity/iron_golem.png");

    private final ModelVillager adultModel = new ModelVillager(0.0F);
    private final ModelVillager childModel = new ModelVillager(0.5F);
    private final ModelZombie zombieModel = new ModelZombie();
    private final ModelIronGolem ironGolemModel = new ModelIronGolem();

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks) {
        if (!(te instanceof TileEntityVillagerBase)) return;
        TileEntityVillagerBase tile = (TileEntityVillagerBase) te;

        // Render bed inside breeder blocks
        if (tile instanceof TileEntityBreeder) {
            renderBed(tile, x, y, z);
        }

        // Render growing crop inside farmer blocks
        if (tile instanceof TileEntityFarmer) {
            TileEntityFarmer farmer = (TileEntityFarmer) tile;
            if (farmer.hasSeed() && farmer.getDisplayBlock() != null) {
                renderCrop(farmer, x, y, z);
            }
        }

        // Render iron farm extras (zombie on stone side, iron golem on lava side)
        if (tile instanceof TileEntityIronFarm) {
            TileEntityIronFarm ironFarm = (TileEntityIronFarm) tile;
            if (ironFarm.hasVillager(0)) {
                renderZombie(tile, x, y, z, partialTicks);
                renderIronGolem(tile, x, y, z, partialTicks);
            }
        }

        for (int i = 0; i < tile.getVillagerSlotCount(); i++) {
            if (tile.hasVillager(i)) {
                renderVillager(tile, i, x, y, z, partialTicks);
            }
        }
    }

    private void renderVillager(TileEntityVillagerBase tile, int slot, double x, double y, double z, float partialTicks) {
        boolean isBaby = tile.isVillagerBaby(slot);
        ModelVillager model = isBaby ? childModel : adultModel;
        boolean isBreeder = tile instanceof TileEntityBreeder;
        boolean isIronFarm = tile instanceof TileEntityIronFarm;
        boolean isFarmer = tile instanceof TileEntityFarmer;

        GL11.glPushMatrix();

        // Position in rotated local space for special blocks
        int meta = tile.getBlockMetadata();
        float blockRotation = getRotationFromMeta(meta);

        if (isIronFarm || isBreeder || isFarmer) {
            // Transform into local block space so offsets are relative to block facing
            GL11.glTranslated(x + 0.5, y, z + 0.5);
            GL11.glRotatef(blockRotation, 0.0F, 1.0F, 0.0F);

            float localX = 0.0F;
            float localZ = 0.0F;
            float extraRotation = 0.0F;

            if (isBreeder) {
                localZ = slot == 0 ? -0.37F : 0.37F;
                extraRotation = (slot == 0) ? 0.0F : 180.0F;
            } else if (isIronFarm) {
                localX = 0.3F;
                localZ = 0.2F;
                extraRotation = -180.0F;
            } else if (isFarmer) {
                // Villager offset to one side, crop goes on the other
                localX = 0.25F;
                extraRotation = -90.0F; // Face toward the crop
            }

            GL11.glTranslatef(localX, 0.1F, localZ);

            float scale;
            if (isIronFarm) {
                scale = isBaby ? 0.18F : 0.245F; // ~30% smaller for iron farm villager
            } else {
                scale = isBaby ? 0.25F : 0.35F;
            }
            GL11.glScalef(scale, scale, scale);
            GL11.glRotatef(extraRotation, 0.0F, 1.0F, 0.0F);
        } else {
            // Default: centered in block
            GL11.glTranslated(x + 0.5, y + 0.1, z + 0.5);
            float scale = isBaby ? 0.25F : 0.35F;
            GL11.glScalef(scale, scale, scale);
            GL11.glRotatef(blockRotation, 0.0F, 1.0F, 0.0F);
        }

        // Flip upright (models render upside down by default)
        GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
        GL11.glTranslatef(0.0F, -1.5F, 0.0F);

        // Bind profession-specific texture
        int profession = tile.getVillagerProfession(slot);
        bindTexture(getVillagerTexture(profession));

        // Slight idle animation - sway head
        float time = (tile.getWorldObj().getTotalWorldTime() + partialTicks) * 0.05F;
        model.villagerHead.rotateAngleX = (float) Math.sin(time + slot) * 0.05F;

        model.render(null, 0.0F, 0.0F, time, 0.0F, 0.0F, 0.0625F);

        // Render straw hat for farmer profession (ID 0)
        if (profession == 0 && !isBaby) {
            renderStrawHat();
        }

        GL11.glPopMatrix();
    }

    // 1.14-style farmer straw hat
    private static final ResourceLocation TEX_FARMER_HAT =
        new ResourceLocation("easyvillagerslegacy:textures/entity/farmer_hat.png");
    private static ModelRenderer hatOverlay;
    private static ModelRenderer hatBrim;

    private static void initHatModel() {
        if (hatOverlay != null) return;
        // Dummy ModelBase with 64x64 texture size matching the 1.14 texture
        ModelBase hatModel = new ModelBase() {};
        hatModel.textureWidth = 64;
        hatModel.textureHeight = 64;

        // Hat overlay (crown layer) - same as 1.14: texOffset(32,0), 8x10x8, inflated
        hatOverlay = new ModelRenderer(hatModel, 32, 0);
        hatOverlay.addBox(-4.0F, -10.0F, -4.0F, 8, 10, 8, 0.51F);
        hatOverlay.setRotationPoint(0.0F, 0.0F, 0.0F);

        // Hat brim - same as 1.14: texOffset(30,47), 16x16x1, rotated horizontal
        hatBrim = new ModelRenderer(hatModel, 30, 47);
        hatBrim.addBox(-8.0F, -8.0F, -6.0F, 16, 16, 1, 0.0F);
        hatBrim.setRotationPoint(0.0F, 0.0F, 0.0F);
        hatBrim.rotateAngleX = -(float) Math.PI / 2.0F;

        hatOverlay.addChild(hatBrim);
    }

    /**
     * Renders the 1.14-style straw hat using ModelRenderer with the farmer_hat.png texture.
     * Called in the GL space after flip (180° X) and translate (0, -1.5, 0),
     * matching the coordinate system used by model.render().
     */
    public static void renderStrawHat() {
        initHatModel();

        Minecraft.getMinecraft().renderEngine.bindTexture(TEX_FARMER_HAT);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        hatOverlay.render(0.0625F);

        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
    }

    /**
     * Renders a zombie on the stone (back) side of the iron farm block,
     * facing the villager.
     */
    private void renderZombie(TileEntityVillagerBase tile, double x, double y, double z, float partialTicks) {
        GL11.glPushMatrix();

        int meta = tile.getBlockMetadata();
        float blockRotation = getRotationFromMeta(meta);

        // Transform into local block space
        GL11.glTranslated(x + 0.5, y, z + 0.5);
        GL11.glRotatef(blockRotation, 0.0F, 1.0F, 0.0F);

        // Stone side is back half (+Z local). Zombie on -Z side, facing villager on +Z side
        GL11.glTranslatef(0.3F, 0.1F, -0.2F);

        float scale = 0.245F; // 30% smaller
        GL11.glScalef(scale, scale, scale);

        // Face toward villager (rotated 90° ACW from previous)
        GL11.glRotatef(0.0F, 0.0F, 1.0F, 0.0F);

        // Flip upright
        GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
        GL11.glTranslatef(0.0F, -1.5F, 0.0F);

        bindTexture(TEX_ZOMBIE);

        // Adult zombie with arms raised
        zombieModel.isChild = false;
        zombieModel.aimedBow = true;

        float time = (tile.getWorldObj().getTotalWorldTime() + partialTicks) * 0.05F;
        zombieModel.render(null, 0.0F, 0.0F, time, 0.0F, 0.0F, 0.0625F);

        GL11.glPopMatrix();
    }

    /**
     * Renders an iron golem on the lava (front) side of the iron farm block,
     * facing outward. Flashes red periodically to simulate taking lava damage.
     */
    private void renderIronGolem(TileEntityVillagerBase tile, double x, double y, double z, float partialTicks) {
        GL11.glPushMatrix();

        int meta = tile.getBlockMetadata();
        float blockRotation = getRotationFromMeta(meta);

        // Transform into local block space
        GL11.glTranslated(x + 0.5, y, z + 0.5);
        GL11.glRotatef(blockRotation, 0.0F, 1.0F, 0.0F);

        // Lava side is front half (-X local). Golem faces outward
        GL11.glTranslatef(-0.25F, 0.1F, 0.0F);

        // Iron golem - 30% bigger
        float scale = 0.286F;
        GL11.glScalef(scale, scale, scale);

        // Face outward (rotated 90° ACW)
        GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);

        // Flip upright
        GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
        GL11.glTranslatef(0.0F, -1.5F, 0.0F);

        // Damage flash - oscillate red tint
        float time = (tile.getWorldObj().getTotalWorldTime() + partialTicks);
        float flashCycle = (float) Math.sin(time * 0.15F);
        if (flashCycle > 0.3F) {
            // Flash red during "damage" phase
            float intensity = (flashCycle - 0.3F) / 0.7F;
            GL11.glColor4f(1.0F, 1.0F - intensity * 0.6F, 1.0F - intensity * 0.6F, 1.0F);
        } else {
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        }

        bindTexture(TEX_IRON_GOLEM);
        ironGolemModel.render(null, 0.0F, 0.0F, time * 0.05F, 0.0F, 0.0F, 0.0625F);

        // Reset color
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        GL11.glPopMatrix();
    }

    /**
     * Renders a small red bed in the center of the breeder block.
     * Bed runs along X axis (between the two villagers).
     * Uses GL immediate mode for reliable TESR colored geometry.
     */

    /**
     * Renders a growing crop inside the farmer block.
     * Uses crossed quad rendering (like vanilla crops) with the crop's icon
     * at the current growth stage.
     */
    private void renderCrop(TileEntityFarmer farmer, double x, double y, double z) {
        Block display = farmer.getDisplayBlock();
        int stage = farmer.getGrowthStage();
        boolean isFullBlock = farmer.isDisplayFullBlock();

        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y, z + 0.5);

        // Rotate to match block facing
        int meta = farmer.getBlockMetadata();
        float rotation = getRotationFromMeta(meta);
        GL11.glRotatef(rotation, 0.0F, 1.0F, 0.0F);

        // Offset crop to opposite side of villager
        GL11.glTranslatef(-0.18F, 0.0F, 0.0F);

        if (isFullBlock) {
            // Render full block (melon/pumpkin) — grows in size based on stage
            float maxMeta = farmer.getCropMaxMeta();
            float progress = maxMeta > 0 ? (float) stage / maxMeta : 1.0F;
            float scale = 0.15F + progress * 0.35F; // Grows from small to 0.5

            // Position so bottom of block sits on the floor (y=1/16 = 0.0625)
            float halfHeight = scale * 0.5F;
            GL11.glTranslatef(0.0F, 0.065F + halfHeight, 0.0F);
            GL11.glScalef(scale, scale, scale);

            // Bind the terrain texture atlas
            bindTexture(new ResourceLocation("textures/atlas/blocks.png"));

            GL11.glDisable(GL11.GL_CULL_FACE);

            // Get icons for each face of the full block
            IIcon topIcon = display.getIcon(1, 0);
            IIcon sideIcon = display.getIcon(2, 0);
            IIcon bottomIcon = display.getIcon(0, 0);
            if (topIcon == null || sideIcon == null) {
                GL11.glEnable(GL11.GL_CULL_FACE);
                GL11.glPopMatrix();
                return;
            }

            Tessellator tess = Tessellator.instance;

            // Render a small cube using the block's textures
            renderFullBlockFace(tess, topIcon, 0, 1, 0);    // Top
            renderFullBlockFace(tess, bottomIcon, 0, -1, 0); // Bottom
            renderFullBlockFace(tess, sideIcon, 0, 0, -1);   // North
            renderFullBlockFace(tess, sideIcon, 0, 0, 1);    // South
            renderFullBlockFace(tess, sideIcon, -1, 0, 0);   // West
            renderFullBlockFace(tess, sideIcon, 1, 0, 0);    // East

            GL11.glEnable(GL11.GL_CULL_FACE);
        } else {
            // Render crossed quads (normal crops like wheat, carrots, etc.)
            IIcon icon;
            try {
                icon = display.getIcon(0, farmer.getDisplayMeta());
            } catch (Exception e) {
                GL11.glPopMatrix();
                return;
            }
            if (icon == null) {
                GL11.glPopMatrix();
                return;
            }

            float scale = 0.5F;
            GL11.glScalef(scale, scale, scale);
            GL11.glTranslatef(0.0F, 0.13F, 0.0F);

            bindTexture(new ResourceLocation("textures/atlas/blocks.png"));

            GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glEnable(GL11.GL_ALPHA_TEST);
            GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);

            float minU = icon.getMinU();
            float maxU = icon.getMaxU();
            float minV = icon.getMinV();
            float maxV = icon.getMaxV();

            Tessellator tess = Tessellator.instance;

            // Quad 1: along X axis
            tess.startDrawingQuads();
            tess.setNormal(0, 0, 1);
            tess.setColorOpaque_F(1.0F, 1.0F, 1.0F);
            tess.addVertexWithUV(-0.5, 1.0, 0.0, minU, minV);
            tess.addVertexWithUV(-0.5, 0.0, 0.0, minU, maxV);
            tess.addVertexWithUV( 0.5, 0.0, 0.0, maxU, maxV);
            tess.addVertexWithUV( 0.5, 1.0, 0.0, maxU, minV);
            tess.draw();

            tess.startDrawingQuads();
            tess.setNormal(0, 0, -1);
            tess.setColorOpaque_F(1.0F, 1.0F, 1.0F);
            tess.addVertexWithUV( 0.5, 1.0, 0.0, minU, minV);
            tess.addVertexWithUV( 0.5, 0.0, 0.0, minU, maxV);
            tess.addVertexWithUV(-0.5, 0.0, 0.0, maxU, maxV);
            tess.addVertexWithUV(-0.5, 1.0, 0.0, maxU, minV);
            tess.draw();

            // Quad 2: along Z axis
            tess.startDrawingQuads();
            tess.setNormal(1, 0, 0);
            tess.setColorOpaque_F(1.0F, 1.0F, 1.0F);
            tess.addVertexWithUV(0.0, 1.0, -0.5, minU, minV);
            tess.addVertexWithUV(0.0, 0.0, -0.5, minU, maxV);
            tess.addVertexWithUV(0.0, 0.0,  0.5, maxU, maxV);
            tess.addVertexWithUV(0.0, 1.0,  0.5, maxU, minV);
            tess.draw();

            tess.startDrawingQuads();
            tess.setNormal(-1, 0, 0);
            tess.setColorOpaque_F(1.0F, 1.0F, 1.0F);
            tess.addVertexWithUV(0.0, 1.0,  0.5, minU, minV);
            tess.addVertexWithUV(0.0, 0.0,  0.5, minU, maxV);
            tess.addVertexWithUV(0.0, 0.0, -0.5, maxU, maxV);
            tess.addVertexWithUV(0.0, 1.0, -0.5, maxU, minV);
            tess.draw();

            GL11.glEnable(GL11.GL_CULL_FACE);
        }

        GL11.glPopMatrix();
    }

    /**
     * Renders one face of a full block (melon/pumpkin) display.
     */
    private void renderFullBlockFace(Tessellator tess, IIcon icon, int nx, int ny, int nz) {
        float minU = icon.getMinU();
        float maxU = icon.getMaxU();
        float minV = icon.getMinV();
        float maxV = icon.getMaxV();

        tess.startDrawingQuads();
        tess.setNormal(nx, ny, nz);
        tess.setColorOpaque_F(ny != 0 ? 1.0F : 0.8F, ny != 0 ? 1.0F : 0.8F, ny != 0 ? 1.0F : 0.8F);

        if (ny == 1) { // Top
            tess.addVertexWithUV(-0.5, 0.5, -0.5, minU, minV);
            tess.addVertexWithUV(-0.5, 0.5,  0.5, minU, maxV);
            tess.addVertexWithUV( 0.5, 0.5,  0.5, maxU, maxV);
            tess.addVertexWithUV( 0.5, 0.5, -0.5, maxU, minV);
        } else if (ny == -1) { // Bottom
            tess.addVertexWithUV(-0.5, -0.5,  0.5, minU, maxV);
            tess.addVertexWithUV(-0.5, -0.5, -0.5, minU, minV);
            tess.addVertexWithUV( 0.5, -0.5, -0.5, maxU, minV);
            tess.addVertexWithUV( 0.5, -0.5,  0.5, maxU, maxV);
        } else if (nz == -1) { // North
            tess.addVertexWithUV( 0.5,  0.5, -0.5, minU, minV);
            tess.addVertexWithUV(-0.5,  0.5, -0.5, maxU, minV);
            tess.addVertexWithUV(-0.5, -0.5, -0.5, maxU, maxV);
            tess.addVertexWithUV( 0.5, -0.5, -0.5, minU, maxV);
        } else if (nz == 1) { // South
            tess.addVertexWithUV(-0.5,  0.5,  0.5, minU, minV);
            tess.addVertexWithUV( 0.5,  0.5,  0.5, maxU, minV);
            tess.addVertexWithUV( 0.5, -0.5,  0.5, maxU, maxV);
            tess.addVertexWithUV(-0.5, -0.5,  0.5, minU, maxV);
        } else if (nx == -1) { // West
            tess.addVertexWithUV(-0.5,  0.5, -0.5, minU, minV);
            tess.addVertexWithUV(-0.5,  0.5,  0.5, maxU, minV);
            tess.addVertexWithUV(-0.5, -0.5,  0.5, maxU, maxV);
            tess.addVertexWithUV(-0.5, -0.5, -0.5, minU, maxV);
        } else if (nx == 1) { // East
            tess.addVertexWithUV( 0.5,  0.5,  0.5, minU, minV);
            tess.addVertexWithUV( 0.5,  0.5, -0.5, maxU, minV);
            tess.addVertexWithUV( 0.5, -0.5, -0.5, maxU, maxV);
            tess.addVertexWithUV( 0.5, -0.5,  0.5, minU, maxV);
        }

        tess.draw();
    }

    private void renderBed(TileEntityVillagerBase tile, double x, double y, double z) {
        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y, z + 0.5);

        // Rotate bed to match block facing
        int meta = tile.getBlockMetadata();
        float rotation = getRotationFromMeta(meta);
        GL11.glRotatef(rotation, 0.0F, 1.0F, 0.0F);

        GL11.glTranslated(-0.5, 0, -0.5);

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_COLOR_MATERIAL);
        GL11.glColorMaterial(GL11.GL_FRONT_AND_BACK, GL11.GL_AMBIENT_AND_DIFFUSE);
        GL11.glEnable(GL11.GL_NORMALIZE);

        // Bed runs along X axis (between villagers at X=0.13 and X=0.87)
        // Narrow on Z axis (centered)

        double bedY1 = 1.0 / 16.0;   // sits on floor frame
        double bedY2 = 4.0 / 16.0;   // frame top

        // === Wood frame - runs along X between the villagers ===
        double fX1 = 3.0 / 16.0, fX2 = 13.0 / 16.0;  // long axis
        double fZ1 = 5.0 / 16.0, fZ2 = 11.0 / 16.0;   // 6px wide, centered
        drawColoredBox(fX1, bedY1, fZ1, fX2, bedY2, fZ2, 0.55F, 0.36F, 0.20F);

        // === Headboard (taller piece at X1/left end, near slot 0 villager) ===
        double hbY2 = 7.0 / 16.0;
        drawColoredBox(3.0 / 16.0, bedY1, fZ1, 4.0 / 16.0, hbY2, fZ2, 0.55F, 0.36F, 0.20F);

        // === Footboard (shorter piece at X2/right end, near slot 1 villager) ===
        double fbY2 = 5.5 / 16.0;
        drawColoredBox(12.0 / 16.0, bedY1, fZ1, 13.0 / 16.0, fbY2, fZ2, 0.55F, 0.36F, 0.20F);

        // === Red blanket on top (covers most of bed except pillow area) ===
        double blY = 4.5 / 16.0;
        drawColoredBox(6.0 / 16.0, bedY2, 4.5 / 16.0, 12.5 / 16.0, blY, 11.5 / 16.0, 0.75F, 0.10F, 0.10F);

        // === White pillow (at headboard/X1 end) ===
        double plY2 = 5.0 / 16.0;
        drawColoredBox(4.0 / 16.0, bedY2, 6.0 / 16.0, 6.5 / 16.0, plY2, 10.0 / 16.0, 0.90F, 0.90F, 0.90F);

        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_COLOR_MATERIAL);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glPopMatrix();
    }

    /**
     * Draws a solid colored box using GL immediate mode.
     * More reliable than Tessellator in TESR context for untextured geometry.
     */
    private void drawColoredBox(double x1, double y1, double z1,
                                 double x2, double y2, double z2, float r, float g, float b) {
        // Set color once - lighting handles face shading via normals
        GL11.glColor3f(r, g, b);

        GL11.glBegin(GL11.GL_QUADS);

        // Bottom face
        GL11.glNormal3f(0, -1, 0);
        GL11.glVertex3d(x1, y1, z2);
        GL11.glVertex3d(x1, y1, z1);
        GL11.glVertex3d(x2, y1, z1);
        GL11.glVertex3d(x2, y1, z2);

        // Top face
        GL11.glNormal3f(0, 1, 0);
        GL11.glVertex3d(x1, y2, z1);
        GL11.glVertex3d(x1, y2, z2);
        GL11.glVertex3d(x2, y2, z2);
        GL11.glVertex3d(x2, y2, z1);

        // North face (Z-)
        GL11.glNormal3f(0, 0, -1);
        GL11.glVertex3d(x2, y2, z1);
        GL11.glVertex3d(x1, y2, z1);
        GL11.glVertex3d(x1, y1, z1);
        GL11.glVertex3d(x2, y1, z1);

        // South face (Z+)
        GL11.glNormal3f(0, 0, 1);
        GL11.glVertex3d(x1, y2, z2);
        GL11.glVertex3d(x2, y2, z2);
        GL11.glVertex3d(x2, y1, z2);
        GL11.glVertex3d(x1, y1, z2);

        // West face (X-)
        GL11.glNormal3f(-1, 0, 0);
        GL11.glVertex3d(x1, y2, z1);
        GL11.glVertex3d(x1, y2, z2);
        GL11.glVertex3d(x1, y1, z2);
        GL11.glVertex3d(x1, y1, z1);

        // East face (X+)
        GL11.glNormal3f(1, 0, 0);
        GL11.glVertex3d(x2, y2, z2);
        GL11.glVertex3d(x2, y2, z1);
        GL11.glVertex3d(x2, y1, z1);
        GL11.glVertex3d(x2, y1, z2);

        GL11.glEnd();
    }

    /**
     * Gets the texture for a villager profession, matching vanilla RenderVillager logic.
     * Vanilla professions 0-4 use hardcoded textures.
     * Modded professions (GTNH Forestry, Thaumcraft, etc.) use Forge's VillagerRegistry.
     */
    private static ResourceLocation getVillagerTexture(int profession) {
        switch (profession) {
            case 0:  return TEX_FARMER;
            case 1:  return TEX_LIBRARIAN;
            case 2:  return TEX_PRIEST;
            case 3:  return TEX_SMITH;
            case 4:  return TEX_BUTCHER;
            default: return VillagerRegistry.getVillagerSkin(profession, TEX_VILLAGER);
        }
    }

    private float getRotationFromMeta(int meta) {
        switch (meta) {
            case 0: return 0.0F;
            case 1: return 90.0F;
            case 2: return 180.0F;
            case 3: return 270.0F;
            default: return 0.0F;
        }
    }
}
