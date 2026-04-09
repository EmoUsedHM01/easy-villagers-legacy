package com.easyvillagerslegacy.gui;

import com.easyvillagerslegacy.tileentity.TileEntityTrader;
import com.easyvillagerslegacy.tileentity.TileEntityFarmer;
import com.easyvillagerslegacy.tileentity.TileEntityBreeder;
import com.easyvillagerslegacy.tileentity.TileEntityIronFarm;
import com.easyvillagerslegacy.tileentity.TileEntityVillagerBase;

import cpw.mods.fml.common.network.IGuiHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/**
 * GUI handler that creates server-side Containers and client-side Gui screens.
 */
public class GuiHandler implements IGuiHandler {

    public static final int GUI_TRADER = 0;
    public static final int GUI_FARMER = 1;
    public static final int GUI_BREEDER = 2;
    public static final int GUI_IRON_FARM = 3;

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity te = world.getTileEntity(x, y, z);

        switch (id) {
            case GUI_TRADER:
                if (te instanceof TileEntityTrader) {
                    TileEntityTrader tile = (TileEntityTrader) te;
                    BlockMerchant merchant = new BlockMerchant(tile);
                    merchant.setCustomer(player);
                    return new ContainerTrader(player.inventory, merchant);
                }
                break;

            case GUI_FARMER:
                if (te instanceof TileEntityFarmer) {
                    return new ContainerOutput(player.inventory, (TileEntityVillagerBase) te);
                }
                break;

            case GUI_BREEDER:
                if (te instanceof TileEntityBreeder) {
                    return new ContainerBreeder(player.inventory, (TileEntityBreeder) te);
                }
                break;

            case GUI_IRON_FARM:
                if (te instanceof TileEntityIronFarm) {
                    return new ContainerOutput(player.inventory, (TileEntityVillagerBase) te);
                }
                break;
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity te = world.getTileEntity(x, y, z);

        switch (id) {
            case GUI_TRADER:
                if (te instanceof TileEntityTrader) {
                    TileEntityTrader tile = (TileEntityTrader) te;
                    BlockMerchant merchant = new BlockMerchant(tile);
                    merchant.setCustomer(player);
                    return new GuiTrader(player.inventory, merchant, world);
                }
                break;

            case GUI_FARMER:
                if (te instanceof TileEntityFarmer) {
                    return new GuiOutput(player.inventory, (TileEntityVillagerBase) te,
                        "gui.easyvillagerslegacy.farmer");
                }
                break;

            case GUI_BREEDER:
                if (te instanceof TileEntityBreeder) {
                    return new GuiBreeder(player.inventory, (TileEntityBreeder) te);
                }
                break;

            case GUI_IRON_FARM:
                if (te instanceof TileEntityIronFarm) {
                    return new GuiOutput(player.inventory, (TileEntityVillagerBase) te,
                        "gui.easyvillagerslegacy.iron_farm");
                }
                break;
        }
        return null;
    }
}
