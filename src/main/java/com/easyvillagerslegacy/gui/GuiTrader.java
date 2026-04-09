package com.easyvillagerslegacy.gui;

import com.easyvillagerslegacy.EasyVillagersLegacy;
import com.easyvillagerslegacy.network.NetworkHandler;
import com.easyvillagerslegacy.network.PacketCycleTrades;
import com.easyvillagerslegacy.tileentity.TileEntityTrader;
import com.easyvillagerslegacy.tileentity.WorkstationRegistry;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMerchant;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Trading GUI that extends the vanilla merchant screen with a "Cycle Trades" button.
 * Uses arrow_button_base.png and arrow_button_highlighted.png for the cycle button.
 */
@SideOnly(Side.CLIENT)
public class GuiTrader extends GuiMerchant {

    private static final ResourceLocation CYCLE_BUTTON_BASE =
        new ResourceLocation(EasyVillagersLegacy.MOD_ID, "textures/gui/arrow_button_base.png");
    private static final ResourceLocation CYCLE_BUTTON_HIGHLIGHTED =
        new ResourceLocation(EasyVillagersLegacy.MOD_ID, "textures/gui/arrow_button_highlighted.png");

    private final BlockMerchant blockMerchant;
    private final TileEntityTrader tile;
    private GuiButton cycleButton;

    public GuiTrader(InventoryPlayer playerInv, BlockMerchant merchant, World world) {
        super(playerInv, merchant, world,
            merchant.getDisplayName().getUnformattedText());
        this.blockMerchant = merchant;
        this.tile = merchant.getTileEntity();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void initGui() {
        super.initGui();

        // Add cycle trades button - positioned to the right of the trade area
        // The arrow_button.png is 16x16, we make the button match
        int buttonX = guiLeft + xSize - 22;
        int buttonY = guiTop + 4;
        cycleButton = new CycleTradesButton(100, buttonX, buttonY);
        buttonList.add(cycleButton);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 100) {
            // Send cycle trades packet to server
            NetworkHandler.INSTANCE.sendToServer(
                new PacketCycleTrades(tile.xCoord, tile.yCoord, tile.zCoord));
            return;
        }
        super.actionPerformed(button);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);

        // Draw profession name
        int profession = tile.getVillagerProfession(0);
        if (profession >= 0) {
            String profName = WorkstationRegistry.getProfessionName(profession);
            // Draw in the title area
            fontRendererObj.drawString(profName, xSize / 2 - fontRendererObj.getStringWidth(profName) / 2, 5, 0x404040);
        }
    }

    /**
     * Custom button that renders the cycle trades arrow textures.
     */
    @SideOnly(Side.CLIENT)
    private class CycleTradesButton extends GuiButton {

        public CycleTradesButton(int id, int x, int y) {
            super(id, x, y, 16, 16, "");
        }

        @Override
        public void drawButton(net.minecraft.client.Minecraft mc, int mouseX, int mouseY) {
            if (!this.visible) return;

            boolean hovered = mouseX >= this.xPosition && mouseY >= this.yPosition
                && mouseX < this.xPosition + this.width
                && mouseY < this.yPosition + this.height;

            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

            // Use highlighted texture on hover, base texture otherwise
            if (hovered) {
                mc.getTextureManager().bindTexture(CYCLE_BUTTON_HIGHLIGHTED);
            } else {
                mc.getTextureManager().bindTexture(CYCLE_BUTTON_BASE);
            }

            // Draw using tessellator for custom-sized texture
            net.minecraft.client.renderer.Tessellator tess = net.minecraft.client.renderer.Tessellator.instance;
            tess.startDrawingQuads();
            tess.addVertexWithUV(this.xPosition, this.yPosition + this.height, 0, 0, 1);
            tess.addVertexWithUV(this.xPosition + this.width, this.yPosition + this.height, 0, 1, 1);
            tess.addVertexWithUV(this.xPosition + this.width, this.yPosition, 0, 1, 0);
            tess.addVertexWithUV(this.xPosition, this.yPosition, 0, 0, 0);
            tess.draw();

            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    /**
     * Returns the BlockMerchant so the trade sync packet can update it.
     */
    public BlockMerchant getMerchant() {
        return blockMerchant;
    }
}
