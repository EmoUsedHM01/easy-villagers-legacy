package com.easyvillagerslegacy.init;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import cpw.mods.fml.common.registry.GameRegistry;

/**
 * Vanilla crafting recipes for the mod blocks.
 * These are placeholder recipes - intended to be replaced with GregTech recipes later.
 */
public class ModRecipes {

    public static void init() {
        // Trader Block:
        // E I E
        // I C I
        // E I E
        // (E = Emerald, I = Iron Ingot, C = Chest)
        GameRegistry.addRecipe(new ItemStack(ModBlocks.blockTrader),
            "EIE", "ICI", "EIE",
            'E', Items.emerald,
            'I', Items.iron_ingot,
            'C', Blocks.chest
        );

        // Farmer Block:
        // WHW
        // HCH
        // WHW
        // (W = Wheat, H = Hoe, C = Chest)
        GameRegistry.addRecipe(new ItemStack(ModBlocks.blockFarmer),
            "WHW", "HCH", "WHW",
            'W', Items.wheat,
            'H', Items.iron_hoe,
            'C', Blocks.chest
        );

        // Breeder Block:
        // BGB
        // GCG
        // BGB
        // (B = Bread, G = Gold Ingot, C = Chest)
        GameRegistry.addRecipe(new ItemStack(ModBlocks.blockBreeder),
            "BGB", "GCG", "BGB",
            'B', Items.bread,
            'G', Items.gold_ingot,
            'C', Blocks.chest
        );

        // Iron Farm Block:
        // IPI
        // PCP
        // IPI
        // (I = Iron Block, P = Pumpkin, C = Chest)
        GameRegistry.addRecipe(new ItemStack(ModBlocks.blockIronFarm),
            "IPI", "PCP", "IPI",
            'I', Blocks.iron_block,
            'P', Blocks.pumpkin,
            'C', Blocks.chest
        );
    }
}
