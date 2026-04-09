package com.easyvillagerslegacy;

import com.easyvillagerslegacy.config.ModConfig;
import com.easyvillagerslegacy.gui.GuiHandler;
import com.easyvillagerslegacy.init.ModBlocks;
import com.easyvillagerslegacy.init.ModItems;
import com.easyvillagerslegacy.init.ModRecipes;
import com.easyvillagerslegacy.network.NetworkHandler;
import com.easyvillagerslegacy.proxy.CommonProxy;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;

@Mod(
    modid = EasyVillagersLegacy.MOD_ID,
    name = EasyVillagersLegacy.MOD_NAME,
    version = Tags.VERSION,
    acceptedMinecraftVersions = "[1.7.10]",
    guiFactory = "com.easyvillagerslegacy.config.GuiFactory"
)
public class EasyVillagersLegacy {

    public static final String MOD_ID = "easyvillagerslegacy";
    public static final String MOD_NAME = "Easy Villagers Legacy";

    @Mod.Instance(MOD_ID)
    public static EasyVillagersLegacy instance;

    @SidedProxy(
        clientSide = "com.easyvillagerslegacy.proxy.ClientProxy",
        serverSide = "com.easyvillagerslegacy.proxy.CommonProxy"
    )
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        ModConfig.init(event.getSuggestedConfigurationFile());
        NetworkHandler.init();
        ModBlocks.init();
        ModItems.init();
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        ModRecipes.init();
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());
        proxy.init(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }
}
