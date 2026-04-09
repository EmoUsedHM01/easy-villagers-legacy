package com.easyvillagerslegacy.events;

import com.easyvillagerslegacy.config.ModConfig;
import com.easyvillagerslegacy.item.ItemVillager;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.EntityInteractEvent;

/**
 * Handles picking up villagers from the world by sneak+right-clicking them.
 * Preserves full villager data (profession, trades, etc.) in the item.
 */
public class VillagerEvents {

    @SubscribeEvent
    public void onEntityInteract(EntityInteractEvent event) {
        if (!ModConfig.enablePickupVillagers) return;
        if (event.entity.worldObj.isRemote) return;

        EntityPlayer player = event.entityPlayer;
        if (!player.isSneaking()) return;

        if (!(event.target instanceof EntityVillager)) return;
        EntityVillager villager = (EntityVillager) event.target;

        // Guard against the event firing multiple times for the same interaction
        if (!villager.isEntityAlive()) return;

        // Create item with full villager data (preserves profession, trades)
        ItemStack villagerItem = ItemVillager.createFromEntity(villager);

        // Kill first to prevent any re-entrant processing
        villager.setDead();
        event.setCanceled(true);

        if (!player.inventory.addItemStackToInventory(villagerItem)) {
            player.entityDropItem(villagerItem, 0.5f);
        }
    }
}
