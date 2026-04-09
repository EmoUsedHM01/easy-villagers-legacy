package com.easyvillagerslegacy.config;

import java.io.File;

import com.easyvillagerslegacy.EasyVillagersLegacy;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.common.config.Configuration;

public class ModConfig {

    public static Configuration config;

    // Trader
    public static int tradeRerollCooldown = 20;

    // Farmer
    public static int farmSpeed = 200;

    // Breeder
    public static int breedingTime = 1200;
    public static int foodPerBreed = 3;

    // Iron Farm
    public static int golemSpawnTime = 4800;
    public static int golemDropMinIron = 3;
    public static int golemDropMaxIron = 5;
    public static int golemDropMinRose = 0;
    public static int golemDropMaxRose = 2;

    // General
    public static boolean enablePickupVillagers = true;

    public static void init(File configFile) {
        config = new Configuration(configFile);
        syncFromConfig();

        // Register config changed event handler so in-game edits take effect
        FMLCommonHandler.instance().bus().register(new ConfigChangeHandler());
    }

    /**
     * Reads all values from the Configuration object into the static fields,
     * then saves if anything changed. Called on init and after GUI edits.
     */
    public static void syncFromConfig() {
        config.load();

        // Trader
        tradeRerollCooldown = config.getInt("tradeRerollCooldown", "trader", 20, 1, 72000,
            "Cooldown in ticks between trade rerolls.");

        // Farmer
        farmSpeed = config.getInt("farmSpeed", "farmer", 200, 1, 72000,
            "Ticks between each farming operation. Lower = faster.");

        // Breeder
        breedingTime = config.getInt("breedingTime", "breeder", 1200, 1, 72000,
            "Ticks required to breed a villager (default 1200 = 1 minute).");
        foodPerBreed = config.getInt("foodPerBreed", "breeder", 3, 1, 64,
            "Amount of food items consumed per breeding cycle.");

        // Iron Farm
        golemSpawnTime = config.getInt("golemSpawnTime", "ironfarm", 4800, 1, 72000,
            "Ticks between iron golem spawns (default 4800 = 4 minutes).");
        golemDropMinIron = config.getInt("golemDropMinIron", "ironfarm", 3, 0, 64,
            "Minimum iron ingots dropped per golem.");
        golemDropMaxIron = config.getInt("golemDropMaxIron", "ironfarm", 5, 0, 64,
            "Maximum iron ingots dropped per golem.");
        golemDropMinRose = config.getInt("golemDropMinRose", "ironfarm", 0, 0, 64,
            "Minimum roses dropped per golem.");
        golemDropMaxRose = config.getInt("golemDropMaxRose", "ironfarm", 2, 0, 64,
            "Maximum roses dropped per golem.");

        // General
        enablePickupVillagers = config.getBoolean("enablePickupVillagers", "general", true,
            "Allow picking up villagers by right-clicking them while sneaking.");

        if (config.hasChanged()) {
            config.save();
        }
    }

    /**
     * Listens for config GUI changes and resyncs values + saves to disk.
     */
    public static class ConfigChangeHandler {
        @SubscribeEvent
        public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
            if (EasyVillagersLegacy.MOD_ID.equals(event.modID)) {
                syncFromConfig();
            }
        }
    }
}
