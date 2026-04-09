package com.easyvillagerslegacy.tileentity;

import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/**
 * Base tile entity for all villager machine blocks.
 * Provides villager storage (NBT-based), inventory (ISidedInventory for pipe compat),
 * ticking, and client sync.
 *
 * Villager data is stored as full NBT compounds in the tile entity.
 * The blocks store the complete villager (with profession, trades, etc.)
 * but when removed, the villager is always stripped to a base villager item.
 */
public abstract class TileEntityVillagerBase extends TileEntity implements ISidedInventory {

    /** Stored villager NBT data. Null entries mean no villager in that slot. */
    protected NBTTagCompound[] villagerData;

    /** Inventory slots for items (food input, crop output, etc.) */
    protected ItemStack[] inventory;

    /** Number of villager storage slots (1 for most blocks, 2 for breeder) */
    private final int villagerSlotCount;

    public TileEntityVillagerBase(int villagerSlots, int inventorySize) {
        this.villagerSlotCount = villagerSlots;
        this.villagerData = new NBTTagCompound[villagerSlots];
        this.inventory = new ItemStack[inventorySize];
    }

    // ========================
    // Villager Storage
    // ========================

    public int getVillagerSlotCount() {
        return villagerSlotCount;
    }

    public boolean hasVillager(int slot) {
        return slot >= 0 && slot < villagerSlotCount && villagerData[slot] != null;
    }

    /**
     * Gets the stored villager NBT for a slot.
     */
    public NBTTagCompound getVillagerData(int slot) {
        if (slot < 0 || slot >= villagerSlotCount) return null;
        return villagerData[slot];
    }

    /**
     * Gets the profession ID of the stored villager.
     */
    public int getVillagerProfession(int slot) {
        if (!hasVillager(slot)) return -1;
        return villagerData[slot].getInteger("Profession");
    }

    /**
     * Creates a villager ItemStack from the stored data in a slot.
     * Preserves full profession, trades, and other data.
     */
    public ItemStack createVillagerItem(int slot) {
        if (!hasVillager(slot)) return null;
        ItemStack stack = new ItemStack(com.easyvillagerslegacy.init.ModItems.itemVillager, 1, 0);
        NBTTagCompound itemNBT = new NBTTagCompound();
        itemNBT.setTag("VillagerData", villagerData[slot].copy());
        itemNBT.setInteger("Profession", getVillagerProfession(slot));
        itemNBT.setBoolean("IsBaby", isVillagerBaby(slot));
        stack.setTagCompound(itemNBT);
        return stack;
    }

    /**
     * Checks if the stored villager is a baby.
     */
    public boolean isVillagerBaby(int slot) {
        if (!hasVillager(slot)) return false;
        return villagerData[slot].getInteger("Age") < 0;
    }

    /**
     * Creates a temporary EntityVillager from stored NBT for interaction purposes.
     * The entity is NOT added to the world - it's just for accessing trades, etc.
     */
    public EntityVillager createTemporaryVillager(int slot) {
        if (!hasVillager(slot) || worldObj == null) return null;
        EntityVillager villager = new EntityVillager(worldObj, 0);
        villager.readEntityFromNBT(villagerData[slot]);
        villager.setPosition(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5);
        return villager;
    }

    /**
     * Sets villager data from a villager item. Creates a fresh base villager entity
     * in the world context so it gets initialized properly, then stores its NBT.
     */
    public void setVillagerFromItem(int slot, ItemStack villagerItem) {
        if (slot < 0 || slot >= villagerSlotCount) return;
        EntityVillager villager = com.easyvillagerslegacy.item.ItemVillager.createEntity(worldObj, villagerItem);
        NBTTagCompound nbt = new NBTTagCompound();
        villager.writeEntityToNBT(nbt);
        villagerData[slot] = nbt;
    }

    /**
     * Sets villager data directly from a full entity (preserving profession, trades, etc.)
     * Used internally when the block needs to update stored villager data.
     */
    public void setVillagerFromEntity(int slot, EntityVillager villager) {
        if (slot < 0 || slot >= villagerSlotCount) return;
        NBTTagCompound nbt = new NBTTagCompound();
        villager.writeEntityToNBT(nbt);
        villagerData[slot] = nbt;
    }

    /**
     * Sets villager data directly from NBT.
     */
    public void setVillagerData(int slot, NBTTagCompound nbt) {
        if (slot < 0 || slot >= villagerSlotCount) return;
        villagerData[slot] = nbt;
    }

    /**
     * Removes the villager from a slot.
     */
    public void removeVillager(int slot) {
        if (slot < 0 || slot >= villagerSlotCount) return;
        villagerData[slot] = null;
    }

    /**
     * Sets the profession of the stored villager and regenerates trades.
     */
    public void setVillagerProfession(int slot, int professionId) {
        if (!hasVillager(slot)) return;
        villagerData[slot].setInteger("Profession", professionId);

        // Clear existing trades so they regenerate with new profession
        villagerData[slot].removeTag("Offers");

        markDirty();
        syncToClient();
    }

    // ========================
    // Ticking
    // ========================

    @Override
    public void updateEntity() {
        if (worldObj == null) return;
        if (!worldObj.isRemote) {
            serverTick();
        } else {
            clientTick();
        }
    }

    /** Override in subclasses for server-side logic. */
    protected void serverTick() {}

    /** Override in subclasses for client-side logic (animations, etc.) */
    protected void clientTick() {}

    // ========================
    // NBT Save/Load
    // ========================

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);

        // Save villager data
        NBTTagList villagerList = new NBTTagList();
        for (int i = 0; i < villagerSlotCount; i++) {
            NBTTagCompound slotNBT = new NBTTagCompound();
            slotNBT.setInteger("Slot", i);
            if (villagerData[i] != null) {
                slotNBT.setTag("VillagerData", villagerData[i]);
            }
            villagerList.appendTag(slotNBT);
        }
        nbt.setTag("Villagers", villagerList);

        // Save inventory
        NBTTagList inventoryList = new NBTTagList();
        for (int i = 0; i < inventory.length; i++) {
            if (inventory[i] != null) {
                NBTTagCompound slotNBT = new NBTTagCompound();
                slotNBT.setByte("Slot", (byte) i);
                inventory[i].writeToNBT(slotNBT);
                inventoryList.appendTag(slotNBT);
            }
        }
        nbt.setTag("Inventory", inventoryList);

        // Let subclasses save their data
        writeCustomNBT(nbt);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        // Load villager data - clear first so removed villagers don't persist
        for (int i = 0; i < villagerSlotCount; i++) {
            villagerData[i] = null;
        }
        NBTTagList villagerList = nbt.getTagList("Villagers", 10);
        for (int i = 0; i < villagerList.tagCount(); i++) {
            NBTTagCompound slotNBT = villagerList.getCompoundTagAt(i);
            int slot = slotNBT.getInteger("Slot");
            if (slot >= 0 && slot < villagerSlotCount && slotNBT.hasKey("VillagerData")) {
                villagerData[slot] = slotNBT.getCompoundTag("VillagerData");
            }
        }

        // Load inventory
        NBTTagList inventoryList = nbt.getTagList("Inventory", 10);
        inventory = new ItemStack[inventory.length];
        for (int i = 0; i < inventoryList.tagCount(); i++) {
            NBTTagCompound slotNBT = inventoryList.getCompoundTagAt(i);
            int slot = slotNBT.getByte("Slot") & 0xFF;
            if (slot < inventory.length) {
                inventory[slot] = ItemStack.loadItemStackFromNBT(slotNBT);
            }
        }

        // Let subclasses load their data
        readCustomNBT(nbt);
    }

    /** Override to save additional data. */
    protected void writeCustomNBT(NBTTagCompound nbt) {}

    /** Override to load additional data. */
    protected void readCustomNBT(NBTTagCompound nbt) {}

    // ========================
    // Client Sync
    // ========================

    public void syncToClient() {
        if (worldObj != null && !worldObj.isRemote) {
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        }
    }

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound nbt = new NBTTagCompound();
        writeToNBT(nbt);
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 1, nbt);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        readFromNBT(pkt.func_148857_g());
    }

    // ========================
    // ISidedInventory
    // ========================

    @Override
    public int getSizeInventory() {
        return inventory.length;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        if (slot < 0 || slot >= inventory.length) return null;
        return inventory[slot];
    }

    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        if (inventory[slot] != null) {
            ItemStack result;
            if (inventory[slot].stackSize <= amount) {
                result = inventory[slot];
                inventory[slot] = null;
            } else {
                result = inventory[slot].splitStack(amount);
                if (inventory[slot].stackSize == 0) {
                    inventory[slot] = null;
                }
            }
            markDirty();
            return result;
        }
        return null;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot) {
        if (inventory[slot] != null) {
            ItemStack stack = inventory[slot];
            inventory[slot] = null;
            return stack;
        }
        return null;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        if (slot < 0 || slot >= inventory.length) return;
        inventory[slot] = stack;
        if (stack != null && stack.stackSize > getInventoryStackLimit()) {
            stack.stackSize = getInventoryStackLimit();
        }
        markDirty();
    }

    @Override
    public String getInventoryName() {
        return "container.easyvillagerslegacy";
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return worldObj.getTileEntity(xCoord, yCoord, zCoord) == this
            && player.getDistanceSq(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5) <= 64.0;
    }

    @Override
    public void openInventory() {}

    @Override
    public void closeInventory() {}

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return true;
    }

    // ISidedInventory - subclasses override for specific slot access rules
    @Override
    public int[] getAccessibleSlotsFromSide(int side) {
        int[] slots = new int[inventory.length];
        for (int i = 0; i < slots.length; i++) {
            slots[i] = i;
        }
        return slots;
    }

    @Override
    public boolean canInsertItem(int slot, ItemStack stack, int side) {
        return isItemValidForSlot(slot, stack);
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack stack, int side) {
        return true;
    }

    // ========================
    // Helper: add item to output inventory
    // ========================

    /**
     * Tries to add an ItemStack to the output slots.
     * Returns the remainder that couldn't be inserted, or null if fully inserted.
     */
    protected ItemStack addToOutput(ItemStack stack, int startSlot, int endSlot) {
        if (stack == null) return null;
        stack = stack.copy();

        // First pass: try to merge with existing stacks
        for (int i = startSlot; i <= endSlot && stack.stackSize > 0; i++) {
            if (inventory[i] != null && inventory[i].isItemEqual(stack)
                && ItemStack.areItemStackTagsEqual(inventory[i], stack)) {
                int space = Math.min(getInventoryStackLimit(), inventory[i].getMaxStackSize()) - inventory[i].stackSize;
                int transfer = Math.min(stack.stackSize, space);
                if (transfer > 0) {
                    inventory[i].stackSize += transfer;
                    stack.stackSize -= transfer;
                }
            }
        }

        // Second pass: try empty slots
        for (int i = startSlot; i <= endSlot && stack.stackSize > 0; i++) {
            if (inventory[i] == null) {
                inventory[i] = stack.copy();
                inventory[i].stackSize = Math.min(stack.stackSize,
                    Math.min(getInventoryStackLimit(), stack.getMaxStackSize()));
                stack.stackSize -= inventory[i].stackSize;
            }
        }

        markDirty();
        return stack.stackSize > 0 ? stack : null;
    }
}
