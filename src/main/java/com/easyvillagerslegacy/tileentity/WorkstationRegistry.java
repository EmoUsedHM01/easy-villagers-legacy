package com.easyvillagerslegacy.tileentity;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

/**
 * Maps workstation blocks to villager profession IDs.
 * Covers vanilla 1.7.10 professions and GTNH-added professions.
 *
 * Vanilla 1.7.10 professions:
 *   0 = Farmer, 1 = Librarian, 2 = Priest, 3 = Blacksmith, 4 = Butcher
 *
 * GTNH mod-added professions:
 *   80 = Apiarist (Forestry), 81 = Arborist (Forestry)
 *   100 = SGCraft Trader, 190 = Wizard (Thaumcraft), 191 = Banker (Thaumcraft)
 *   456 = Railcraft Trader, 666 = Heretic (Forbidden Magic)
 *   2435 = Apothecary (Witchery), 6156 = Radio Trader (OpenBlocks)
 */
public class WorkstationRegistry {

    private static final Map<String, Integer> WORKSTATION_MAP = new HashMap<String, Integer>();

    /**
     * Initialize the default workstation mappings.
     * Called during mod init. Uses string-based block lookup so modded blocks
     * that aren't loaded yet at construction time are resolved at lookup time.
     *
     * Format: "modid:blockname" or "modid:blockname:meta" -> professionId
     */
    public static void init() {
        // Vanilla professions
        register("minecraft:farmland", 0);           // Farmer
        register("minecraft:composter", 0);           // Farmer (etfuturum if present)
        register("minecraft:bookshelf", 1);           // Librarian
        register("minecraft:brewing_stand", 2);       // Priest/Cleric
        register("minecraft:anvil", 3);               // Blacksmith
        register("minecraft:furnace", 3);             // Blacksmith (alternative)
        register("minecraft:lit_furnace", 3);         // Blacksmith (lit furnace)
        register("minecraft:smoker", 4);              // Butcher (etfuturum if present)

        // Forestry professions
        register("Forestry:apiary", 80);              // Apiarist
        register("Forestry:arboriculture", 81);       // Arborist

        // Thaumcraft professions
        register("Thaumcraft:blockTable", 190);       // Wizard (arcane workbench)

        // Witchery professions
        register("witchery:cauldron", 2435);          // Apothecary
        register("witchery:kettle", 2435);            // Apothecary (alt)

        // Railcraft professions
        register("Railcraft:machine.alpha", 456);     // Railcraft Trader

        // OpenBlocks professions
        register("OpenBlocks:radio", 6156);           // Radio Trader
    }

    /**
     * Register a workstation block -> profession mapping.
     */
    public static void register(String blockId, int professionId) {
        WORKSTATION_MAP.put(blockId, professionId);
    }

    /**
     * Look up the profession ID for a given block.
     * Returns -1 if the block is not a recognized workstation.
     */
    public static int getProfession(Block block, int meta) {
        if (block == null) return -1;

        String blockName = Block.blockRegistry.getNameForObject(block);
        if (blockName == null) return -1;

        // Check exact block name first
        Integer profession = WORKSTATION_MAP.get(blockName);
        if (profession != null) return profession;

        // Check with meta
        profession = WORKSTATION_MAP.get(blockName + ":" + meta);
        if (profession != null) return profession;

        return -1;
    }

    /**
     * Check if a block is a recognized workstation.
     */
    public static boolean isWorkstation(Block block, int meta) {
        return getProfession(block, meta) >= 0;
    }

    /**
     * Get the profession name for display purposes.
     */
    public static String getProfessionName(int professionId) {
        switch (professionId) {
            case 0: return "Farmer";
            case 1: return "Librarian";
            case 2: return "Priest";
            case 3: return "Blacksmith";
            case 4: return "Butcher";
            case 80: return "Apiarist";
            case 81: return "Arborist";
            case 100: return "SGCraft Trader";
            case 190: return "Wizard";
            case 191: return "Banker";
            case 456: return "Railcraft Trader";
            case 666: return "Heretic";
            case 2435: return "Apothecary";
            case 6156: return "Radio Trader";
            default: return "Unknown (" + professionId + ")";
        }
    }
}
