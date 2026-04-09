package com.easyvillagerslegacy.init;

import com.easyvillagerslegacy.EasyVillagersLegacy;
import com.easyvillagerslegacy.item.ItemVillager;

import cpw.mods.fml.common.registry.GameRegistry;

public class ModItems {

    public static ItemVillager itemVillager;

    public static void init() {
        itemVillager = new ItemVillager();
        GameRegistry.registerItem(itemVillager, "villager");
    }
}
