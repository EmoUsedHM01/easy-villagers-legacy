package com.easyvillagerslegacy.block;

import com.easyvillagerslegacy.EasyVillagersLegacy;
import com.easyvillagerslegacy.gui.GuiHandler;
import com.easyvillagerslegacy.item.ItemVillager;
import com.easyvillagerslegacy.tileentity.TileEntityBreeder;
import com.easyvillagerslegacy.tileentity.TileEntityVillagerBase;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.World;

/**
 * Breeder block - breeds baby villagers from 2 adult villagers + food.
 * Accent textures: planks_oak (front and back) - matching the original mod.
 */
public class BlockBreeder extends BlockVillagerBase {

    public BlockBreeder() {
        super("breeder", "minecraft:planks_oak", "minecraft:planks_oak");
    }

    @Override
    protected int getFacingOffset() {
        return 1; // 90° CW
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityBreeder();
    }

    @Override
    protected boolean handlePlaceVillager(World world, int x, int y, int z,
                                           EntityPlayer player, TileEntityVillagerBase baseTile, ItemStack held) {
        if (!(baseTile instanceof TileEntityBreeder)) return false;
        TileEntityBreeder tile = (TileEntityBreeder) baseTile;

        if (ItemVillager.isBaby(held)) {
            return false;
        }

        int slot = tile.getFirstEmptyVillagerSlot();
        if (slot < 0) {
            player.addChatMessage(new ChatComponentTranslation("msg.easyvillagerslegacy.block_full"));
            return false;
        }

        tile.setVillagerFromItem(slot, held);

        if (!player.capabilities.isCreativeMode) {
            held.stackSize--;
            if (held.stackSize <= 0) {
                player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
            }
        }

        tile.markDirty();
        tile.syncToClient();
        return true;
    }

    @Override
    protected boolean handleRemoveVillager(World world, int x, int y, int z,
                                            EntityPlayer player, TileEntityVillagerBase baseTile) {
        if (!(baseTile instanceof TileEntityBreeder)) return false;
        TileEntityBreeder tile = (TileEntityBreeder) baseTile;

        int slot = tile.hasVillager(1) ? 1 : (tile.hasVillager(0) ? 0 : -1);
        if (slot < 0) return false;

        ItemStack villagerItem = tile.createVillagerItem(slot);
        tile.removeVillager(slot);
        tile.markDirty();
        tile.syncToClient();

        if (!player.inventory.addItemStackToInventory(villagerItem)) {
            EntityItem entityItem = new EntityItem(world, x + 0.5, y + 1.0, z + 0.5, villagerItem);
            world.spawnEntityInWorld(entityItem);
        }

        return true;
    }

    @Override
    protected boolean handleBlockActivated(World world, int x, int y, int z,
                                            EntityPlayer player, TileEntityVillagerBase tile,
                                            ItemStack heldItem) {
        if (heldItem == null) {
            player.openGui(EasyVillagersLegacy.instance, GuiHandler.GUI_BREEDER, world, x, y, z);
            return true;
        }
        return false;
    }
}
