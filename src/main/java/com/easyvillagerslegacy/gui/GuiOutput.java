package com.easyvillagerslegacy.gui;

import com.easyvillagerslegacy.EasyVillagersLegacy;
import com.easyvillagerslegacy.tileentity.TileEntityVillagerBase;
import com.easyvillagerslegacy.tileentity.TileEntityIronFarm;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * GUI for output-only blocks (Farmer, Iron Farm).
 * Uses the original mod's output.png GUI texture.
 */
@SideOnly(Side.CLIENT)
public class GuiOutput extends GuiContainer {

    private static final ResourceLocation TEXTURE =
        new ResourceLocation(EasyVillagersLegacy.MOD_ID, "textures/gui/output.png");

    private final TileEntityVillagerBase tile;
    private final String title;

    public GuiOutput(InventoryPlayer playerInv, TileEntityVillagerBase tile, String titleKey) {
        super(new ContainerOutput(playerInv, tile));
        this.tile = tile;
        this.title = StatCollector.translateToLocal(titleKey);
        this.xSize = 176;
        this.ySize = 132;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRendererObj.drawString(title, xSize / 2 - fontRendererObj.getStringWidth(title) / 2, 6, 0x404040);
        fontRendererObj.drawString(StatCollector.translateToLocal("container.inventory"),
            8, 42, 0x404040);

        // Draw status info below output slots
        String status;
        if (!tile.hasVillager(0)) {
            status = "\u00a7c" + "No villager";
        } else if (tile.isVillagerBaby(0)) {
            status = "\u00a7e" + "Baby (can't work)";
        } else {
            status = "\u00a7a" + "Working";
            if (tile instanceof TileEntityIronFarm) {
                TileEntityIronFarm ironFarm = (TileEntityIronFarm) tile;
                int pct = (int)(100.0 * ironFarm.getSpawnTimer() / ironFarm.getSpawnTimeTotal());
                status += " (" + pct + "%)";
            }
        }
        fontRendererObj.drawString(status, 8, 10, 0xFFFFFF);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(TEXTURE);
        int x = (width - xSize) / 2;
        int y = (height - ySize) / 2;
        drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
    }
}
