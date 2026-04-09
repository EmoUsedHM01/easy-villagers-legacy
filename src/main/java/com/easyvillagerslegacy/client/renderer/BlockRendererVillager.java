package com.easyvillagerslegacy.client.renderer;

import com.easyvillagerslegacy.block.BlockVillagerBase;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Custom block renderer for the villager display case blocks.
 * Iron base frame, glass walls/ceiling, teal corner pillars and top frame,
 * per-block accent floor textures.
 */
@SideOnly(Side.CLIENT)
public class BlockRendererVillager implements ISimpleBlockRenderingHandler {

    public static int RENDER_ID;

    public static void register() {
        RENDER_ID = RenderingRegistry.getNextAvailableRenderId();
        BlockVillagerBase.renderId = RENDER_ID;
        RenderingRegistry.registerBlockHandler(new BlockRendererVillager());
    }

    @Override
    public int getRenderId() {
        return RENDER_ID;
    }

    @Override
    public boolean shouldRender3DInInventory(int modelId) {
        return true;
    }

    // ========================
    // World rendering
    // ========================

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z,
                                     Block block, int modelId, RenderBlocks renderer) {
        if (!(block instanceof BlockVillagerBase)) return false;
        BlockVillagerBase vBlock = (BlockVillagerBase) block;

        IIcon ironIcon = vBlock.getIronIcon();
        IIcon glassIcon = vBlock.getGlassIcon();
        IIcon frameIcon = vBlock.getFrameIcon();
        IIcon frontIcon = vBlock.getAccentFrontIcon();
        IIcon backIcon = vBlock.getAccentBackIcon();

        renderer.renderAllFaces = true;

        // Bottom iron frame (4 border strips)
        renderBox(renderer, block, x, y, z, 0, 0, 0, 16, 1, 1, ironIcon);
        renderBox(renderer, block, x, y, z, 0, 0, 15, 16, 1, 16, ironIcon);
        renderBox(renderer, block, x, y, z, 0, 0, 1, 1, 1, 15, ironIcon);
        renderBox(renderer, block, x, y, z, 15, 0, 1, 16, 1, 15, ironIcon);

        // Top teal frame (4 border strips)
        renderBox(renderer, block, x, y, z, 0, 15, 0, 16, 16, 1, frameIcon);
        renderBox(renderer, block, x, y, z, 0, 15, 15, 16, 16, 16, frameIcon);
        renderBox(renderer, block, x, y, z, 0, 15, 1, 1, 16, 15, frameIcon);
        renderBox(renderer, block, x, y, z, 15, 15, 1, 16, 16, 15, frameIcon);

        // Corner pillars (teal frame)
        renderBox(renderer, block, x, y, z, 0, 1, 0, 1, 15, 1, frameIcon);
        renderBox(renderer, block, x, y, z, 0, 1, 15, 1, 15, 16, frameIcon);
        renderBox(renderer, block, x, y, z, 15, 1, 0, 16, 15, 1, frameIcon);
        renderBox(renderer, block, x, y, z, 15, 1, 15, 16, 15, 16, frameIcon);

        // Glass panels (4 sides + ceiling)
        renderBox(renderer, block, x, y, z, 1, 1, 0, 15, 15, 1, glassIcon);
        renderBox(renderer, block, x, y, z, 1, 1, 15, 15, 15, 16, glassIcon);
        renderBox(renderer, block, x, y, z, 0, 1, 1, 1, 15, 15, glassIcon);
        renderBox(renderer, block, x, y, z, 15, 1, 1, 16, 15, 15, glassIcon);
        renderBox(renderer, block, x, y, z, 1, 15, 1, 15, 16, 15, glassIcon);

        // Floor accent (split front/back, rotated to match block facing)
        int meta = world.getBlockMetadata(x, y, z);
        switch (meta) {
            case 0: // front at -X
                renderBox(renderer, block, x, y, z, 1, 0, 1, 8, 1, 15, frontIcon);
                renderBox(renderer, block, x, y, z, 8, 0, 1, 15, 1, 15, backIcon);
                break;
            case 1: // front at -Z
                renderBox(renderer, block, x, y, z, 1, 0, 1, 15, 1, 8, frontIcon);
                renderBox(renderer, block, x, y, z, 1, 0, 8, 15, 1, 15, backIcon);
                break;
            case 2: // front at +X
                renderBox(renderer, block, x, y, z, 8, 0, 1, 15, 1, 15, frontIcon);
                renderBox(renderer, block, x, y, z, 1, 0, 1, 8, 1, 15, backIcon);
                break;
            default: // front at +Z
                renderBox(renderer, block, x, y, z, 1, 0, 8, 15, 1, 15, frontIcon);
                renderBox(renderer, block, x, y, z, 1, 0, 1, 15, 1, 8, backIcon);
                break;
        }

        renderer.clearOverrideBlockTexture();
        renderer.renderAllFaces = false;
        renderer.setRenderBounds(0, 0, 0, 1, 1, 1);

        return true;
    }

    /**
     * Render a sub-block box using renderStandardBlockWithColorMultiplier directly.
     * This bypasses AO which can cause issues with partial render bounds in ISBRHs.
     * Pixel coordinates (0-16).
     */
    private void renderBox(RenderBlocks renderer, Block block, int x, int y, int z,
                            int px1, int py1, int pz1, int px2, int py2, int pz2, IIcon icon) {
        renderer.setOverrideBlockTexture(icon);
        renderer.setRenderBounds(px1 / 16.0, py1 / 16.0, pz1 / 16.0,
                                  px2 / 16.0, py2 / 16.0, pz2 / 16.0);
        renderer.renderStandardBlockWithColorMultiplier(block, x, y, z, 1.0F, 1.0F, 1.0F);
    }

    // ========================
    // Inventory rendering
    // ========================

    @Override
    public void renderInventoryBlock(Block block, int meta, int modelId, RenderBlocks renderer) {
        if (!(block instanceof BlockVillagerBase)) return;
        BlockVillagerBase vBlock = (BlockVillagerBase) block;

        IIcon ironIcon = vBlock.getIronIcon();
        IIcon glassIcon = vBlock.getGlassIcon();
        IIcon frameIcon = vBlock.getFrameIcon();
        IIcon frontIcon = vBlock.getAccentFrontIcon();
        IIcon backIcon = vBlock.getAccentBackIcon();

        GL11.glTranslatef(-0.5F, -0.5F, -0.5F);

        // Bottom iron frame
        invBox(0, 0, 0, 16, 1, 1, ironIcon);
        invBox(0, 0, 15, 16, 1, 16, ironIcon);
        invBox(0, 0, 1, 1, 1, 15, ironIcon);
        invBox(15, 0, 1, 16, 1, 15, ironIcon);

        // Top teal frame
        invBox(0, 15, 0, 16, 16, 1, frameIcon);
        invBox(0, 15, 15, 16, 16, 16, frameIcon);
        invBox(0, 15, 1, 1, 16, 15, frameIcon);
        invBox(15, 15, 1, 16, 16, 15, frameIcon);

        // Corner pillars
        invBox(0, 1, 0, 1, 15, 1, frameIcon);
        invBox(0, 1, 15, 1, 15, 16, frameIcon);
        invBox(15, 1, 0, 16, 15, 1, frameIcon);
        invBox(15, 1, 15, 16, 15, 16, frameIcon);

        // Glass panels
        invBox(1, 1, 0, 15, 15, 1, glassIcon);
        invBox(1, 1, 15, 15, 15, 16, glassIcon);
        invBox(0, 1, 1, 1, 15, 15, glassIcon);
        invBox(15, 1, 1, 16, 15, 15, glassIcon);
        invBox(1, 15, 1, 15, 16, 15, glassIcon);

        // Floor accent
        invBox(1, 0, 1, 15, 1, 8, frontIcon);
        invBox(1, 0, 8, 15, 1, 15, backIcon);

        GL11.glTranslatef(0.5F, 0.5F, 0.5F);
    }

    private void invBox(int px1, int py1, int pz1, int px2, int py2, int pz2, IIcon icon) {
        Tessellator tess = Tessellator.instance;
        double x1 = px1 / 16.0, y1 = py1 / 16.0, z1 = pz1 / 16.0;
        double x2 = px2 / 16.0, y2 = py2 / 16.0, z2 = pz2 / 16.0;
        double minU = icon.getMinU(), maxU = icon.getMaxU();
        double minV = icon.getMinV(), maxV = icon.getMaxV();

        tess.startDrawingQuads();
        tess.setNormal(0.0F, -1.0F, 0.0F);
        tess.addVertexWithUV(x1, y1, z2, minU, maxV);
        tess.addVertexWithUV(x1, y1, z1, minU, minV);
        tess.addVertexWithUV(x2, y1, z1, maxU, minV);
        tess.addVertexWithUV(x2, y1, z2, maxU, maxV);
        tess.draw();

        tess.startDrawingQuads();
        tess.setNormal(0.0F, 1.0F, 0.0F);
        tess.addVertexWithUV(x1, y2, z1, minU, minV);
        tess.addVertexWithUV(x1, y2, z2, minU, maxV);
        tess.addVertexWithUV(x2, y2, z2, maxU, maxV);
        tess.addVertexWithUV(x2, y2, z1, maxU, minV);
        tess.draw();

        tess.startDrawingQuads();
        tess.setNormal(0.0F, 0.0F, -1.0F);
        tess.addVertexWithUV(x2, y2, z1, minU, minV);
        tess.addVertexWithUV(x1, y2, z1, maxU, minV);
        tess.addVertexWithUV(x1, y1, z1, maxU, maxV);
        tess.addVertexWithUV(x2, y1, z1, minU, maxV);
        tess.draw();

        tess.startDrawingQuads();
        tess.setNormal(0.0F, 0.0F, 1.0F);
        tess.addVertexWithUV(x1, y2, z2, minU, minV);
        tess.addVertexWithUV(x2, y2, z2, maxU, minV);
        tess.addVertexWithUV(x2, y1, z2, maxU, maxV);
        tess.addVertexWithUV(x1, y1, z2, minU, maxV);
        tess.draw();

        tess.startDrawingQuads();
        tess.setNormal(-1.0F, 0.0F, 0.0F);
        tess.addVertexWithUV(x1, y2, z1, minU, minV);
        tess.addVertexWithUV(x1, y2, z2, maxU, minV);
        tess.addVertexWithUV(x1, y1, z2, maxU, maxV);
        tess.addVertexWithUV(x1, y1, z1, minU, maxV);
        tess.draw();

        tess.startDrawingQuads();
        tess.setNormal(1.0F, 0.0F, 0.0F);
        tess.addVertexWithUV(x2, y2, z2, minU, minV);
        tess.addVertexWithUV(x2, y2, z1, maxU, minV);
        tess.addVertexWithUV(x2, y1, z1, maxU, maxV);
        tess.addVertexWithUV(x2, y1, z2, minU, maxV);
        tess.draw();
    }
}
