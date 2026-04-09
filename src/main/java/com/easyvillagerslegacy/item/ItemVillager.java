package com.easyvillagerslegacy.item;

import com.easyvillagerslegacy.EasyVillagersLegacy;
import com.easyvillagerslegacy.block.BlockVillagerBase;
import com.easyvillagerslegacy.tileentity.WorkstationRegistry;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Facing;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

/**
 * Villager item that stores full villager data including profession and trades.
 * GTNH assigns professions randomly on spawn (no workstations), so the item
 * must preserve all villager data to maintain their job and trade list.
 *
 * NBT structure:
 *   "VillagerData" -> full EntityVillager NBT compound
 *   "IsBaby" -> boolean (for quick access without parsing full NBT)
 *   "Profession" -> int (for quick access / display)
 */
public class ItemVillager extends Item {

    public ItemVillager() {
        setUnlocalizedName(EasyVillagersLegacy.MOD_ID + ".villager");
        setTextureName(EasyVillagersLegacy.MOD_ID + ":villager");
        setCreativeTab(CreativeTabs.tabMisc);
        setMaxStackSize(1);
    }

    /**
     * Spawn egg behavior: when used on a non-mod block, spawn the stored villager
     * at the clicked position. Does nothing when used on mod blocks (those handle
     * villager placement themselves via BlockVillagerBase).
     */
    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world,
                              int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
        // Don't handle mod blocks — they do their own villager placement
        Block clickedBlock = world.getBlock(x, y, z);
        if (clickedBlock instanceof BlockVillagerBase) {
            return false; // Let BlockVillagerBase.onBlockActivated handle it
        }

        if (world.isRemote) return true;

        // Offset spawn position by the clicked face (like spawn eggs)
        double spawnX = x + Facing.offsetsXForSide[side] + 0.5;
        double spawnY = y + Facing.offsetsYForSide[side];
        double spawnZ = z + Facing.offsetsZForSide[side] + 0.5;

        EntityVillager villager = createEntity(world, stack);
        villager.setLocationAndAngles(spawnX, spawnY, spawnZ,
            MathHelper.wrapAngleTo180_float(world.rand.nextFloat() * 360.0F), 0.0F);
        villager.rotationYawHead = villager.rotationYaw;
        villager.renderYawOffset = villager.rotationYaw;
        world.spawnEntityInWorld(villager);
        villager.playLivingSound();

        if (!player.capabilities.isCreativeMode) {
            stack.stackSize--;
            if (stack.stackSize <= 0) {
                player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
            }
        }

        return true;
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("Profession")) {
            int profession = stack.getTagCompound().getInteger("Profession");
            String profName = WorkstationRegistry.getProfessionName(profession);
            boolean baby = stack.hasTagCompound() && stack.getTagCompound().getBoolean("IsBaby");
            if (baby) {
                return "Baby " + profName + " Villager";
            }
            return profName + " Villager";
        }
        return "Villager";
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced) {
        if (stack.hasTagCompound()) {
            int profession = stack.getTagCompound().getInteger("Profession");
            String profName = WorkstationRegistry.getProfessionName(profession);
            list.add("\u00a77Profession: " + profName);

            if (stack.getTagCompound().getBoolean("IsBaby")) {
                list.add("\u00a7e" + "Baby");
            }
        }
    }

    /**
     * Creates a villager item from a living EntityVillager.
     * Preserves full villager data including profession, trades, etc.
     */
    public static ItemStack createFromEntity(EntityVillager villager) {
        ItemStack stack = new ItemStack(com.easyvillagerslegacy.init.ModItems.itemVillager, 1, 0);

        NBTTagCompound itemNBT = new NBTTagCompound();

        // Store full villager data
        NBTTagCompound villagerNBT = new NBTTagCompound();
        villager.writeEntityToNBT(villagerNBT);
        itemNBT.setTag("VillagerData", villagerNBT);

        // Quick-access fields for display
        itemNBT.setInteger("Profession", villager.getProfession());
        itemNBT.setBoolean("IsBaby", villager.isChild());

        stack.setTagCompound(itemNBT);
        return stack;
    }

    /**
     * Creates an EntityVillager from the stored item data.
     * Restores full profession, trades, and other data.
     */
    public static EntityVillager createEntity(World world, ItemStack stack) {
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("VillagerData")) {
            NBTTagCompound villagerNBT = stack.getTagCompound().getCompoundTag("VillagerData");
            int profession = stack.getTagCompound().getInteger("Profession");
            EntityVillager villager = new EntityVillager(world, profession);
            villager.readEntityFromNBT(villagerNBT);
            return villager;
        }

        // Fallback: create a basic villager with profession 0
        return new EntityVillager(world, 0);
    }

    /**
     * Gets the stored profession ID from a villager item. Returns -1 if none.
     */
    public static int getProfession(ItemStack stack) {
        if (stack != null && stack.hasTagCompound() && stack.getTagCompound().hasKey("Profession")) {
            return stack.getTagCompound().getInteger("Profession");
        }
        return -1;
    }

    /**
     * Gets the stored villager NBT data. Returns null if none.
     */
    public static NBTTagCompound getVillagerData(ItemStack stack) {
        if (stack != null && stack.hasTagCompound() && stack.getTagCompound().hasKey("VillagerData")) {
            return stack.getTagCompound().getCompoundTag("VillagerData");
        }
        return null;
    }

    public static boolean isVillagerItem(ItemStack stack) {
        return stack != null && stack.getItem() instanceof ItemVillager;
    }

    public static boolean isBaby(ItemStack stack) {
        if (!isVillagerItem(stack)) return false;
        if (stack.hasTagCompound()) {
            return stack.getTagCompound().getBoolean("IsBaby");
        }
        return false;
    }
}
