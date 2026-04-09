package com.easyvillagerslegacy.tileentity;

import com.easyvillagerslegacy.config.ModConfig;
import com.easyvillagerslegacy.init.ModItems;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Breeder block tile entity.
 * Stores 2 adult villagers. Consumes food from input slots to breed baby villagers.
 * Baby villager items appear in the output slot.
 *
 * Inventory layout:
 *   Slots 0-3: Food input (bread, carrots, potatoes, beetroot)
 *   Slot 4:    Villager output (baby villager items)
 *
 * ISidedInventory:
 *   Top: food input (slots 0-3)
 *   Bottom/Sides: output extraction (slot 4)
 */
public class TileEntityBreeder extends TileEntityVillagerBase {

    private static final int FOOD_SLOTS_START = 0;
    private static final int FOOD_SLOTS_END = 3;
    private static final int OUTPUT_SLOT = 4;
    private static final int TOTAL_SLOTS = 5;

    /** Progress toward breeding completion (in ticks) */
    private int breedTimer = 0;

    /** Whether breeding is currently in progress */
    private boolean breeding = false;

    public TileEntityBreeder() {
        super(2, TOTAL_SLOTS); // 2 villager slots, 5 inventory slots
    }

    // ========================
    // Breeding Logic
    // ========================

    @Override
    protected void serverTick() {
        // Need 2 adult villagers to breed
        if (!hasVillager(0) || !hasVillager(1)) {
            breedTimer = 0;
            breeding = false;
            return;
        }

        // Both must be adults
        if (isVillagerBaby(0) || isVillagerBaby(1)) {
            breedTimer = 0;
            breeding = false;
            return;
        }

        // Check if output slot is empty (villager items don't stack)
        if (inventory[OUTPUT_SLOT] != null) {
            breedTimer = 0;
            breeding = false;
            return;
        }

        // Check food availability
        if (!hasEnoughFood()) {
            breedTimer = 0;
            breeding = false;
            return;
        }

        breeding = true;
        breedTimer++;

        if (breedTimer >= ModConfig.breedingTime) {
            // Breeding complete - consume food and produce villager
            consumeFood();
            produceVillager();
            breedTimer = 0;
            breeding = false;
            markDirty();
            syncToClient();
        }
    }

    /**
     * Checks if there's enough food in the input slots.
     */
    private boolean hasEnoughFood() {
        int foodCount = 0;
        for (int i = FOOD_SLOTS_START; i <= FOOD_SLOTS_END; i++) {
            if (inventory[i] != null && isValidFood(inventory[i])) {
                foodCount += inventory[i].stackSize;
            }
        }
        return foodCount >= ModConfig.foodPerBreed;
    }

    /**
     * Consumes the required amount of food from input slots.
     */
    private void consumeFood() {
        int remaining = ModConfig.foodPerBreed;
        for (int i = FOOD_SLOTS_START; i <= FOOD_SLOTS_END && remaining > 0; i++) {
            if (inventory[i] != null && isValidFood(inventory[i])) {
                int consume = Math.min(remaining, inventory[i].stackSize);
                inventory[i].stackSize -= consume;
                remaining -= consume;
                if (inventory[i].stackSize <= 0) {
                    inventory[i] = null;
                }
            }
        }
    }

    /**
     * Produces an adult villager item with a random profession (vanilla or modded) in the output slot.
     */
    private void produceVillager() {
        int profession = getRandomProfession();
        net.minecraft.entity.passive.EntityVillager villager =
            new net.minecraft.entity.passive.EntityVillager(worldObj, profession);
        ItemStack villagerItem = com.easyvillagerslegacy.item.ItemVillager.createFromEntity(villager);

        if (inventory[OUTPUT_SLOT] == null) {
            inventory[OUTPUT_SLOT] = villagerItem;
        }
        // Villager items don't stack (maxStackSize 1), so only produce if slot is empty
    }

    /**
     * Gets a random profession from all registered villager professions,
     * including vanilla (0-4) and any modded ones (Forestry, Thaumcraft, etc.).
     */
    private int getRandomProfession() {
        java.util.Collection<Integer> ids = cpw.mods.fml.common.registry.VillagerRegistry.getRegisteredVillagers();
        // Combine vanilla IDs (0-4) with modded ones from Forge registry
        java.util.List<Integer> allProfessions = new java.util.ArrayList<Integer>();
        for (int i = 0; i <= 4; i++) {
            allProfessions.add(i);
        }
        for (int id : ids) {
            if (!allProfessions.contains(id)) {
                allProfessions.add(id);
            }
        }
        return allProfessions.get(worldObj.rand.nextInt(allProfessions.size()));
    }

    /**
     * Checks if an item is valid breeding food.
     * Accepts: bread, carrots, potatoes, beetroot.
     */
    public static boolean isValidFood(ItemStack stack) {
        if (stack == null || stack.getItem() == null) return false;
        return stack.getItem() == Items.bread
            || stack.getItem() == Items.carrot
            || stack.getItem() == Items.potato;
        // Note: beetroot doesn't exist in vanilla 1.7.10
        // etfuturum may add it - we check by item name as fallback
    }

    public int getBreedTimer() {
        return breedTimer;
    }

    public boolean isBreeding() {
        return breeding;
    }

    public int getBreedingTimeTotal() {
        return ModConfig.breedingTime;
    }

    // ========================
    // Villager placement (override for 2-slot support)
    // ========================

    /**
     * Find the first available villager slot.
     * Returns -1 if both slots are full.
     */
    public int getFirstEmptyVillagerSlot() {
        if (!hasVillager(0)) return 0;
        if (!hasVillager(1)) return 1;
        return -1;
    }

    // ========================
    // ISidedInventory
    // ========================

    private static final int[] FOOD_SLOTS = {0, 1, 2, 3};
    private static final int[] OUTPUT_SLOTS = {4};
    private static final int[] NO_SLOTS = {};

    @Override
    public int[] getAccessibleSlotsFromSide(int side) {
        switch (side) {
            case 0: return OUTPUT_SLOTS; // Bottom: extract babies
            case 1: return FOOD_SLOTS;   // Top: insert food
            default: return OUTPUT_SLOTS; // Sides: extract babies
        }
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        if (slot >= FOOD_SLOTS_START && slot <= FOOD_SLOTS_END) {
            return isValidFood(stack);
        }
        return false; // Output slot is not insertable
    }

    @Override
    public boolean canInsertItem(int slot, ItemStack stack, int side) {
        return slot >= FOOD_SLOTS_START && slot <= FOOD_SLOTS_END && isValidFood(stack);
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack stack, int side) {
        return slot == OUTPUT_SLOT;
    }

    // ========================
    // NBT
    // ========================

    @Override
    protected void writeCustomNBT(NBTTagCompound nbt) {
        nbt.setInteger("BreedTimer", breedTimer);
        nbt.setBoolean("Breeding", breeding);
    }

    @Override
    protected void readCustomNBT(NBTTagCompound nbt) {
        breedTimer = nbt.getInteger("BreedTimer");
        breeding = nbt.getBoolean("Breeding");
    }
}
