package com.easyvillagerslegacy.tileentity;

import com.easyvillagerslegacy.tileentity.WorkstationRegistry;

import net.minecraft.block.Block;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;

/**
 * Trader block tile entity.
 * Stores a villager with full profession/trade data.
 * Supports workstation-based profession assignment and trade cycling.
 *
 * No inventory slots - trading is handled via vanilla merchant GUI.
 */
public class TileEntityTrader extends TileEntityVillagerBase {

    /** The workstation block ID that determines profession. Null = no workstation. */
    private Block workstationBlock;
    private int workstationMeta;

    /** Cooldown timer for trade cycling */
    private int cycleCooldown = 0;

    public TileEntityTrader() {
        super(1, 0); // 1 villager slot, 0 inventory slots
    }

    // ========================
    // Workstation / Profession
    // ========================

    /**
     * Sets the workstation block, which determines the villager's profession.
     * If the workstation is recognized, the villager's profession is updated.
     */
    public void setWorkstation(Block block, int meta) {
        this.workstationBlock = block;
        this.workstationMeta = meta;

        // Look up what profession this workstation maps to
        int professionId = WorkstationRegistry.getProfession(block, meta);
        if (professionId >= 0 && hasVillager(0)) {
            setVillagerProfession(0, professionId);
        }

        markDirty();
        syncToClient();
    }

    public void clearWorkstation() {
        this.workstationBlock = null;
        this.workstationMeta = 0;

        if (hasVillager(0)) {
            // Reset to base villager (profession 0)
            setVillagerProfession(0, 0);
        }

        markDirty();
        syncToClient();
    }

    public Block getWorkstationBlock() {
        return workstationBlock;
    }

    public int getWorkstationMeta() {
        return workstationMeta;
    }

    public boolean hasWorkstation() {
        return workstationBlock != null;
    }

    // ========================
    // Trading
    // ========================

    /**
     * Gets the trade list from the stored villager.
     * Creates a temporary entity to access trades.
     */
    public MerchantRecipeList getTrades(EntityPlayer player) {
        EntityVillager villager = createTemporaryVillager(0);
        if (villager == null) return null;

        return villager.getRecipes(player);
    }

    /**
     * Cycles (refreshes) the villager's trades.
     * Creates a new villager with the same profession and copies fresh trades.
     */
    public void cycleTrades() {
        if (!hasVillager(0) || cycleCooldown > 0) return;

        int profession = getVillagerProfession(0);

        // Create a fresh villager with the same profession to get new trades
        EntityVillager fresh = new EntityVillager(worldObj, profession);
        // Force trade generation by calling getRecipes
        fresh.getRecipes(null);

        // Get the fresh villager's NBT
        NBTTagCompound freshNBT = new NBTTagCompound();
        fresh.writeEntityToNBT(freshNBT);

        // Copy the new trades (Offers tag) to our stored villager
        if (freshNBT.hasKey("Offers")) {
            getVillagerData(0).setTag("Offers", freshNBT.getTag("Offers"));
        }

        cycleCooldown = com.easyvillagerslegacy.config.ModConfig.tradeRerollCooldown;
        markDirty();
        syncToClient();
    }

    /**
     * Called after a trade is completed to save the updated villager state.
     */
    public void saveVillagerState(EntityVillager villager) {
        NBTTagCompound nbt = new NBTTagCompound();
        villager.writeEntityToNBT(nbt);
        setVillagerData(0, nbt);
        markDirty();
    }

    // ========================
    // Ticking
    // ========================

    @Override
    protected void serverTick() {
        if (cycleCooldown > 0) {
            cycleCooldown--;
        }
    }

    // ========================
    // NBT
    // ========================

    @Override
    protected void writeCustomNBT(NBTTagCompound nbt) {
        if (workstationBlock != null) {
            nbt.setString("WorkstationBlock",
                Block.blockRegistry.getNameForObject(workstationBlock));
            nbt.setInteger("WorkstationMeta", workstationMeta);
        }
        nbt.setInteger("CycleCooldown", cycleCooldown);
    }

    @Override
    protected void readCustomNBT(NBTTagCompound nbt) {
        if (nbt.hasKey("WorkstationBlock")) {
            String blockName = nbt.getString("WorkstationBlock");
            workstationBlock = (Block) Block.blockRegistry.getObject(blockName);
            workstationMeta = nbt.getInteger("WorkstationMeta");
        } else {
            workstationBlock = null;
            workstationMeta = 0;
        }
        cycleCooldown = nbt.getInteger("CycleCooldown");
    }

    // ========================
    // Inventory (none for Trader)
    // ========================

    @Override
    public int[] getAccessibleSlotsFromSide(int side) {
        return new int[0];
    }

    @Override
    public boolean canInsertItem(int slot, ItemStack stack, int side) {
        return false;
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack stack, int side) {
        return false;
    }
}
