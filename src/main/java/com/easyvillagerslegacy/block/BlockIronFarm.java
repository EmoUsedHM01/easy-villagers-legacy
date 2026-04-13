package com.easyvillagerslegacy.block;

import com.easyvillagerslegacy.EasyVillagersLegacy;
import com.easyvillagerslegacy.gui.GuiHandler;
import com.easyvillagerslegacy.tileentity.TileEntityIronFarm;
import com.easyvillagerslegacy.tileentity.TileEntityVillagerBase;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/**
 * Iron Farm block - spawns and kills iron golems for drops.
 * Accent textures: lava_still (front), stone (back) - matching the original mod.
 */
public class BlockIronFarm extends BlockVillagerBase {

    public BlockIronFarm() {
        super("iron_farm", "minecraft:lava_still", "minecraft:stone");
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityIronFarm();
    }

    @Override
    protected boolean handleBlockActivated(World world, int x, int y, int z,
                                            EntityPlayer player, TileEntityVillagerBase tile,
                                            ItemStack heldItem) {
        if (heldItem == null) {
            player.openGui(EasyVillagersLegacy.instance, GuiHandler.GUI_IRON_FARM, world, x, y, z);
            return true;
        }
        return false;
    }
}
