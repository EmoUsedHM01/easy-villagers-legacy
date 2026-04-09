package com.easyvillagerslegacy.client.events;

import com.easyvillagerslegacy.client.renderer.TileEntityVillagerRenderer;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.util.Timer;
import net.minecraftforge.client.event.RenderLivingEvent;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.ReflectionHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Client-side render event handler.
 * Adds the 1.14-style straw hat to farmer villager entities in the world.
 */
@SideOnly(Side.CLIENT)
public class ClientRenderEvents {

    private Timer mcTimer;

    private float getPartialTicks() {
        if (mcTimer == null) {
            mcTimer = ReflectionHelper.getPrivateValue(
                Minecraft.class, Minecraft.getMinecraft(), "timer", "field_71428_T");
        }
        return mcTimer.renderPartialTicks;
    }

    @SubscribeEvent
    public void onRenderLivingPost(RenderLivingEvent.Post event) {
        if (!(event.entity instanceof EntityVillager)) return;
        EntityVillager villager = (EntityVillager) event.entity;

        // Only adult farmer villagers (profession 0) get the hat
        if (villager.getProfession() != 0 || villager.isChild()) return;

        float partialTicks = getPartialTicks();

        GL11.glPushMatrix();

        // Position at the entity's render location
        GL11.glTranslatef((float) event.x, (float) event.y, (float) event.z);

        // Match the entity's body rotation (yaw interpolation)
        float bodyYaw = villager.prevRenderYawOffset
            + (villager.renderYawOffset - villager.prevRenderYawOffset) * partialTicks;
        GL11.glRotatef(-bodyYaw, 0.0F, 1.0F, 0.0F);

        // Flip upright and translate to match ModelVillager's coordinate space
        GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
        GL11.glTranslatef(0.0F, -1.5F, 0.0F);

        // Match head pitch for the hat
        float headPitch = villager.prevRotationPitch
            + (villager.rotationPitch - villager.prevRotationPitch) * partialTicks;
        float headYawRelative = villager.prevRotationYawHead
            + (villager.rotationYawHead - villager.prevRotationYawHead) * partialTicks
            - bodyYaw;

        // Apply head rotation at the head's rotation point (0, 0, 0 in model space)
        GL11.glRotatef(headYawRelative, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(headPitch, 1.0F, 0.0F, 0.0F);

        TileEntityVillagerRenderer.renderStrawHat();

        GL11.glPopMatrix();
    }
}
