package com.easyvillagerslegacy.block;

import com.easyvillagerslegacy.EasyVillagersLegacy;
import com.easyvillagerslegacy.gui.GuiHandler;
import com.easyvillagerslegacy.tileentity.TileEntityFarmer;
import com.easyvillagerslegacy.tileentity.TileEntityVillagerBase;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/**
 * Farmer block - auto-farms a specific crop when a villager and seed are placed inside.
 * Right-click with seed to set crop type. Sneak+right-click to remove seed (if no villager)
 * or villager (if present).
 * Accent textures: dirt (front and back) - matching the original mod.
 */
public class BlockFarmer extends BlockVillagerBase {

    public BlockFarmer() {
        super("farmer", "dirt", "dirt");
    }

    @Override
    protected int getFacingOffset() {
        return 3; // 90° CW + 180° flip
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityFarmer();
    }

    @Override
    protected boolean handleBlockActivated(World world, int x, int y, int z,
                                            EntityPlayer player, TileEntityVillagerBase tile,
                                            ItemStack heldItem) {
        if (!(tile instanceof TileEntityFarmer)) return false;
        TileEntityFarmer farmer = (TileEntityFarmer) tile;

        // Right-click with a plantable seed: place it in the farmer
        if (heldItem != null && TileEntityFarmer.isValidSeed(heldItem)) {
            if (!farmer.hasSeed()) {
                farmer.setSeed(heldItem);
                if (!player.capabilities.isCreativeMode) {
                    heldItem.stackSize--;
                    if (heldItem.stackSize <= 0) {
                        player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
                    }
                }
                return true;
            }
        }

        // Empty hand: open GUI
        if (heldItem == null) {
            player.openGui(EasyVillagersLegacy.instance, GuiHandler.GUI_FARMER, world, x, y, z);
            return true;
        }

        return false;
    }

    @Override
    protected boolean handleRemoveVillager(World world, int x, int y, int z,
                                            EntityPlayer player, TileEntityVillagerBase tile) {
        // Remove seed first, then villager
        if (tile instanceof TileEntityFarmer) {
            TileEntityFarmer farmer = (TileEntityFarmer) tile;
            if (farmer.hasSeed()) {
                ItemStack seed = farmer.removeSeed();
                if (seed != null) {
                    if (!player.inventory.addItemStackToInventory(seed)) {
                        EntityItem entityItem = new EntityItem(world, x + 0.5, y + 1.0, z + 0.5, seed);
                        world.spawnEntityInWorld(entityItem);
                    }
                }
                return true;
            }
        }

        // No seed - remove villager
        if (tile.hasVillager(0)) {
            return super.handleRemoveVillager(world, x, y, z, player, tile);
        }

        return false;
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, net.minecraft.block.Block block, int meta) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TileEntityFarmer) {
            TileEntityFarmer farmer = (TileEntityFarmer) te;
            // Drop stored seed
            if (farmer.hasSeed()) {
                ItemStack seed = farmer.removeSeed();
                if (seed != null) {
                    EntityItem entity = new EntityItem(world, x + 0.5, y + 0.5, z + 0.5, seed);
                    entity.motionX = world.rand.nextGaussian() * 0.05;
                    entity.motionY = world.rand.nextGaussian() * 0.05 + 0.2;
                    entity.motionZ = world.rand.nextGaussian() * 0.05;
                    world.spawnEntityInWorld(entity);
                }
            }
        }
        super.breakBlock(world, x, y, z, block, meta);
    }
}
