package com.easyvillagerslegacy.gui;

import com.easyvillagerslegacy.EasyVillagersLegacy;
import com.easyvillagerslegacy.tileentity.TileEntityBreeder;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * GUI for the Breeder block.
 * Uses the original mod's input_output.png GUI texture.
 * Top row: 4 food input slots. Second row: baby output slot.
 */
@SideOnly(Side.CLIENT)
public class GuiBreeder extends GuiContainer {

    private static final ResourceLocation TEXTURE =
        new ResourceLocation(EasyVillagersLegacy.MOD_ID, "textures/gui/input_output.png");

    private final TileEntityBreeder tile;

    public GuiBreeder(InventoryPlayer playerInv, TileEntityBreeder tile) {
        super(new ContainerBreeder(playerInv, tile));
        this.tile = tile;
        this.xSize = 176;
        this.ySize = 163;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String title = StatCollector.translateToLocal("gui.easyvillagerslegacy.breeder");
        fontRendererObj.drawString(title, xSize / 2 - fontRendererObj.getStringWidth(title) / 2, 6, 0x404040);
        fontRendererObj.drawString(StatCollector.translateToLocal("container.inventory"),
            8, 73, 0x404040);

        // Villager status - between output slots and player inventory
        int villagerCount = 0;
        if (tile.hasVillager(0)) villagerCount++;
        if (tile.hasVillager(1)) villagerCount++;

        String villagerStatus = "Villagers: " + villagerCount + "/2";
        int statusColor = villagerCount == 2 ? 0x00AA00 : 0xAA0000;
        fontRendererObj.drawString(villagerStatus, 8, 10, statusColor);

        // Breeding progress
        if (tile.isBreeding()) {
            int pct = (int)(100.0 * tile.getBreedTimer() / tile.getBreedingTimeTotal());
            String progress = "Breeding: " + pct + "%";
            fontRendererObj.drawString(progress, 96, 10, 0x00AA00);
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(TEXTURE);
        int x = (width - xSize) / 2;
        int y = (height - ySize) / 2;
        drawTexturedModalRect(x, y, 0, 0, xSize, ySize);

        // Draw breeding progress bar overlay between slot rows
        if (tile.isBreeding()) {
            int progressWidth = (int)(24.0 * tile.getBreedTimer() / tile.getBreedingTimeTotal());
            drawRect(x + 76, y + 40, x + 76 + progressWidth, y + 44, 0xFF00CC00);
        }
    }
}
