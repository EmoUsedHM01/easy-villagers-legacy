package com.easyvillagerslegacy.proxy;

import com.easyvillagerslegacy.client.renderer.BlockRendererVillager;
import com.easyvillagerslegacy.client.renderer.ItemBlockVillagerRenderer;
import com.easyvillagerslegacy.client.renderer.ItemVillagerRenderer;
import com.easyvillagerslegacy.client.renderer.TileEntityVillagerRenderer;
import com.easyvillagerslegacy.init.ModBlocks;
import com.easyvillagerslegacy.init.ModItems;
import com.easyvillagerslegacy.tileentity.TileEntityTrader;
import com.easyvillagerslegacy.tileentity.TileEntityFarmer;
import com.easyvillagerslegacy.tileentity.TileEntityBreeder;
import com.easyvillagerslegacy.tileentity.TileEntityIronFarm;

import com.easyvillagerslegacy.client.events.ClientRenderEvents;

import net.minecraft.item.Item;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.MinecraftForge;

public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);

        // Register the custom block renderer (ISBRH) - must be done in preInit
        // so the render ID is available when blocks are registered
        BlockRendererVillager.register();
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        registerRenderers();
        MinecraftForge.EVENT_BUS.register(new ClientRenderEvents());
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        super.postInit(event);
    }

    private void registerRenderers() {
        TileEntityVillagerRenderer renderer = new TileEntityVillagerRenderer();
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTrader.class, renderer);
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityFarmer.class, renderer);
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBreeder.class, renderer);
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityIronFarm.class, renderer);

        // Custom item renderer for villager items - renders profession-specific model
        MinecraftForgeClient.registerItemRenderer(ModItems.itemVillager, new ItemVillagerRenderer());

        // Custom item renderer for block items - renders entities inside the display case
        ItemBlockVillagerRenderer blockItemRenderer = new ItemBlockVillagerRenderer();
        MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(ModBlocks.blockTrader), blockItemRenderer);
        MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(ModBlocks.blockFarmer), blockItemRenderer);
        MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(ModBlocks.blockBreeder), blockItemRenderer);
        MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(ModBlocks.blockIronFarm), blockItemRenderer);
    }
}
