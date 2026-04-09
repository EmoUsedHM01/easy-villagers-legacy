package com.easyvillagerslegacy.block;

import com.easyvillagerslegacy.EasyVillagersLegacy;
import com.easyvillagerslegacy.gui.GuiHandler;
import com.easyvillagerslegacy.tileentity.TileEntityTrader;
import com.easyvillagerslegacy.tileentity.TileEntityVillagerBase;
import com.easyvillagerslegacy.tileentity.WorkstationRegistry;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.World;

/**
 * Trader block - allows trading with a stored villager.
 * Accent textures: iron_block (front and back) - matching the original mod.
 */
public class BlockTrader extends BlockVillagerBase {

    public BlockTrader() {
        super("trader", "iron_block", "iron_block");
    }

    @Override
    protected int getFacingOffset() {
        return 2; // 180° flip
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityTrader();
    }

    @Override
    protected boolean handleBlockActivated(World world, int x, int y, int z,
                                            EntityPlayer player, TileEntityVillagerBase baseTile,
                                            ItemStack heldItem) {
        if (!(baseTile instanceof TileEntityTrader)) return false;
        TileEntityTrader tile = (TileEntityTrader) baseTile;

        // Right-click with a block item: set/clear workstation
        if (heldItem != null && heldItem.getItem() instanceof ItemBlock) {
            Block block = ((ItemBlock) heldItem.getItem()).field_150939_a;
            int meta = heldItem.getItemDamage();

            if (player.isSneaking()) {
                tile.clearWorkstation();
                player.addChatMessage(new ChatComponentTranslation("msg.easyvillagerslegacy.workstation_cleared"));
                return true;
            }

            if (WorkstationRegistry.isWorkstation(block, meta)) {
                tile.setWorkstation(block, meta);
                int profId = WorkstationRegistry.getProfession(block, meta);
                String profName = WorkstationRegistry.getProfessionName(profId);
                player.addChatMessage(new ChatComponentTranslation("msg.easyvillagerslegacy.workstation_set", profName));
                return true;
            }
        }

        // Right-click empty hand: open trading GUI
        if (heldItem == null && tile.hasVillager(0)) {
            player.openGui(EasyVillagersLegacy.instance, GuiHandler.GUI_TRADER, world, x, y, z);
            return true;
        }

        if (heldItem == null && !tile.hasVillager(0)) {
            player.addChatMessage(new ChatComponentTranslation("msg.easyvillagerslegacy.need_villager"));
            return true;
        }

        return false;
    }
}
