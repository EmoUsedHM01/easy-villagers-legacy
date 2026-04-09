package com.easyvillagerslegacy.tileentity;

import com.easyvillagerslegacy.config.ModConfig;

import java.util.Random;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Iron Farm block tile entity.
 * Stores 1 villager. Periodically "spawns and kills" an iron golem,
 * depositing drops into the output inventory.
 *
 * Output inventory: 4 slots, accessible from all sides for pipe extraction.
 * Drops: 3-5 iron ingots + 0-2 red flowers (vanilla golem drops).
 */
public class TileEntityIronFarm extends TileEntityVillagerBase {

    private static final int OUTPUT_SLOTS = 4;

    /** Tick counter for golem spawning */
    private int spawnTimer = 0;

    private final Random random = new Random();

    public TileEntityIronFarm() {
        super(1, OUTPUT_SLOTS); // 1 villager, 4 output slots
    }

    // ========================
    // Iron Farm Logic
    // ========================

    @Override
    protected void serverTick() {
        if (!hasVillager(0)) {
            spawnTimer = 0;
            return;
        }
        if (isVillagerBaby(0)) return; // Babies can't summon golems

        spawnTimer++;
        if (spawnTimer >= ModConfig.golemSpawnTime) {
            spawnTimer = 0;
            spawnAndKillGolem();
        }
    }

    /**
     * Simulate spawning and immediately killing an iron golem.
     * Generates drops and adds them to the output inventory.
     */
    private void spawnAndKillGolem() {
        // Calculate iron ingot drops
        int ironCount = ModConfig.golemDropMinIron +
            random.nextInt(ModConfig.golemDropMaxIron - ModConfig.golemDropMinIron + 1);

        // Calculate rose drops
        int roseCount = ModConfig.golemDropMinRose +
            random.nextInt(ModConfig.golemDropMaxRose - ModConfig.golemDropMinRose + 1);

        // Add iron ingots to output
        if (ironCount > 0) {
            addToOutput(new ItemStack(Items.iron_ingot, ironCount), 0, OUTPUT_SLOTS - 1);
        }

        // Add roses to output (red flower in 1.7.10)
        if (roseCount > 0) {
            addToOutput(new ItemStack(Blocks.red_flower, roseCount, 0), 0, OUTPUT_SLOTS - 1);
        }

        markDirty();
    }

    public int getSpawnTimer() {
        return spawnTimer;
    }

    public int getSpawnTimeTotal() {
        return ModConfig.golemSpawnTime;
    }

    // ========================
    // ISidedInventory - output only from all sides
    // ========================

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return false; // Output only
    }

    @Override
    public boolean canInsertItem(int slot, ItemStack stack, int side) {
        return false;
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack stack, int side) {
        return true;
    }

    // ========================
    // NBT
    // ========================

    @Override
    protected void writeCustomNBT(NBTTagCompound nbt) {
        nbt.setInteger("SpawnTimer", spawnTimer);
    }

    @Override
    protected void readCustomNBT(NBTTagCompound nbt) {
        spawnTimer = nbt.getInteger("SpawnTimer");
    }
}
