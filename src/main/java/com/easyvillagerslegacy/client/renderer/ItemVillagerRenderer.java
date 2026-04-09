package com.easyvillagerslegacy.client.renderer;

import com.easyvillagerslegacy.item.ItemVillager;

import cpw.mods.fml.common.registry.VillagerRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelVillager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Custom item renderer that draws a villager model on the item
 * based on the stored profession. Uses the same texture lookup
 * as TileEntityVillagerRenderer for consistency.
 */
@SideOnly(Side.CLIENT)
public class ItemVillagerRenderer implements IItemRenderer {

    // Vanilla profession textures (same as TileEntityVillagerRenderer)
    private static final ResourceLocation TEX_VILLAGER   = new ResourceLocation("textures/entity/villager/villager.png");
    private static final ResourceLocation TEX_FARMER     = new ResourceLocation("textures/entity/villager/farmer.png");
    private static final ResourceLocation TEX_LIBRARIAN  = new ResourceLocation("textures/entity/villager/librarian.png");
    private static final ResourceLocation TEX_PRIEST     = new ResourceLocation("textures/entity/villager/priest.png");
    private static final ResourceLocation TEX_SMITH      = new ResourceLocation("textures/entity/villager/smith.png");
    private static final ResourceLocation TEX_BUTCHER    = new ResourceLocation("textures/entity/villager/butcher.png");

    private final ModelVillager adultModel = new ModelVillager(0.0F);
    private final ModelVillager childModel = new ModelVillager(0.5F);

    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type) {
        return true; // Handle all render types: inventory, held, equipped, entity
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
        // Use render helpers for 3D rendering in inventory and entity contexts
        return true;
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
        boolean isBaby = ItemVillager.isBaby(item);
        int profession = ItemVillager.getProfession(item);
        if (profession < 0) profession = 0;

        ModelVillager model = isBaby ? childModel : adultModel;
        TextureManager texManager = Minecraft.getMinecraft().getTextureManager();

        GL11.glPushMatrix();
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);

        // Position and scale based on render context
        switch (type) {
            case INVENTORY:
                // Centered upright in the 16x16 inventory slot, facing straight forward
                GL11.glTranslatef(0.0F, -0.6F, 0.0F);
                GL11.glScalef(0.55F, 0.55F, 0.55F);
                break;
            case EQUIPPED:
            case EQUIPPED_FIRST_PERSON:
                // Held in hand - rotated 90 ACW to face the player
                GL11.glTranslatef(0.5F, 0.5F, 0.5F);
                GL11.glScalef(0.6F, 0.6F, 0.6F);
                GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
                break;
            case ENTITY:
                // Dropped on the ground
                GL11.glTranslatef(0.0F, 0.5F, 0.0F);
                GL11.glScalef(0.5F, 0.5F, 0.5F);
                break;
            default:
                break;
        }

        // Flip upright (models render upside down by default)
        GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
        GL11.glTranslatef(0.0F, -1.5F, 0.0F);

        // Enable lighting for proper shading
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        texManager.bindTexture(getVillagerTexture(profession));
        model.render(null, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);

        // Straw hat for farmer profession
        if (profession == 0 && !isBaby) {
            TileEntityVillagerRenderer.renderStrawHat();
        }

        GL11.glPopAttrib();
        GL11.glPopMatrix();
    }

    /**
     * Gets the texture for a villager profession, matching vanilla RenderVillager logic.
     * Same as TileEntityVillagerRenderer.getVillagerTexture().
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
}
