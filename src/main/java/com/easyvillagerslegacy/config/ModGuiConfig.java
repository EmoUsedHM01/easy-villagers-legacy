package com.easyvillagerslegacy.config;

import com.easyvillagerslegacy.EasyVillagersLegacy;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;

import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.client.config.IConfigElement;

public class ModGuiConfig extends GuiConfig {

    @SuppressWarnings("rawtypes")
    public ModGuiConfig(GuiScreen parent) {
        super(
            parent,
            getConfigElements(),
            EasyVillagersLegacy.MOD_ID,
            false,
            false,
            EasyVillagersLegacy.MOD_NAME + " Configuration"
        );
    }

    @SuppressWarnings("rawtypes")
    private static List<IConfigElement> getConfigElements() {
        List<IConfigElement> elements = new ArrayList<IConfigElement>();
        Configuration config = ModConfig.config;

        elements.addAll(new ConfigElement(config.getCategory("general")).getChildElements());
        elements.addAll(new ConfigElement(config.getCategory("trader")).getChildElements());
        elements.addAll(new ConfigElement(config.getCategory("farmer")).getChildElements());
        elements.addAll(new ConfigElement(config.getCategory("breeder")).getChildElements());
        elements.addAll(new ConfigElement(config.getCategory("ironfarm")).getChildElements());

        return elements;
    }
}
