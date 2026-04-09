package com.easyvillagerslegacy.init;

import com.easyvillagerslegacy.block.BlockTrader;
import com.easyvillagerslegacy.block.BlockFarmer;
import com.easyvillagerslegacy.block.BlockBreeder;
import com.easyvillagerslegacy.block.BlockIronFarm;
import com.easyvillagerslegacy.tileentity.TileEntityTrader;
import com.easyvillagerslegacy.tileentity.TileEntityFarmer;
import com.easyvillagerslegacy.tileentity.TileEntityBreeder;
import com.easyvillagerslegacy.tileentity.TileEntityIronFarm;
import com.easyvillagerslegacy.tileentity.WorkstationRegistry;

import cpw.mods.fml.common.registry.GameRegistry;

public class ModBlocks {

    public static BlockTrader blockTrader;
    public static BlockFarmer blockFarmer;
    public static BlockBreeder blockBreeder;
    public static BlockIronFarm blockIronFarm;

    public static void init() {
        // Register blocks
        blockTrader = new BlockTrader();
        GameRegistry.registerBlock(blockTrader, "trader");

        blockFarmer = new BlockFarmer();
        GameRegistry.registerBlock(blockFarmer, "farmer");

        blockBreeder = new BlockBreeder();
        GameRegistry.registerBlock(blockBreeder, "breeder");

        blockIronFarm = new BlockIronFarm();
        GameRegistry.registerBlock(blockIronFarm, "iron_farm");

        // Register tile entities
        GameRegistry.registerTileEntity(TileEntityTrader.class, "easyvillagerslegacy:trader");
        GameRegistry.registerTileEntity(TileEntityFarmer.class, "easyvillagerslegacy:farmer");
        GameRegistry.registerTileEntity(TileEntityBreeder.class, "easyvillagerslegacy:breeder");
        GameRegistry.registerTileEntity(TileEntityIronFarm.class, "easyvillagerslegacy:iron_farm");

        // Initialize workstation registry
        WorkstationRegistry.init();
    }
}
