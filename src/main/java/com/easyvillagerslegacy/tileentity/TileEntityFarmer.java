package com.easyvillagerslegacy.tileentity;

import com.easyvillagerslegacy.config.ModConfig;

import java.util.ArrayList;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockStem;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemSeeds;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.IPlantable;

/**
 * Farmer block tile entity.
 * Stores 1 villager and 1 seed. When both are present, grows that specific crop
 * through visual stages, then produces drops and resets.
 *
 * Output inventory: 4 slots, accessible from all sides for pipe extraction.
 */
public class TileEntityFarmer extends TileEntityVillagerBase {

    private static final int OUTPUT_SLOTS = 4;

    /** The seed item placed in the block */
    private ItemStack storedSeed;

    /** The crop block this seed grows (resolved from seed) - used for drops */
    private Block cropBlock;

    /** The block to display visually (same as cropBlock, except for stems which show the fruit) */
    private Block displayBlock;

    /** The base/starting metadata for this crop (e.g. 0 for barley, 4 for cotton in Natura) */
    private int cropBaseMeta = 0;

    /** Max growth metadata for this crop */
    private int cropMaxMeta = 7;

    /** Current visual growth stage (0 to number of growth stages) */
    private int growthStage = 0;

    /** Tick counter within current growth stage */
    private int growthTimer = 0;

    private final Random random = new Random();

    public TileEntityFarmer() {
        super(1, OUTPUT_SLOTS); // 1 villager, 4 output slots
    }

    // ========================
    // Seed Storage
    // ========================

    public boolean hasSeed() {
        return storedSeed != null;
    }

    public ItemStack getStoredSeed() {
        return storedSeed;
    }

    /**
     * Places a seed in the farmer block. Resolves the crop block it grows.
     * @param seed Single seed item to store (stackSize will be set to 1)
     */
    public void setSeed(ItemStack seed) {
        if (seed == null) return;
        this.storedSeed = seed.copy();
        this.storedSeed.stackSize = 1;
        resolveCropBlock();
        this.growthStage = 0;
        this.growthTimer = 0;
        markDirty();
        syncToClient();
    }

    /**
     * Removes and returns the stored seed.
     */
    public ItemStack removeSeed() {
        if (storedSeed == null) return null;
        ItemStack seed = storedSeed.copy();
        storedSeed = null;
        cropBlock = null;
        displayBlock = null;
        growthStage = 0;
        growthTimer = 0;
        markDirty();
        syncToClient();
        return seed;
    }

    /**
     * Resolves the crop block from the stored seed item.
     * Also sets the display block (fruit block for stems, crop block otherwise).
     * Determines the base and max metadata for the crop's growth range.
     */
    private void resolveCropBlock() {
        if (storedSeed == null || storedSeed.getItem() == null) {
            cropBlock = null;
            displayBlock = null;
            return;
        }

        if (storedSeed.getItem() instanceof IPlantable) {
            IPlantable plantable = (IPlantable) storedSeed.getItem();
            try {
                cropBlock = plantable.getPlant(worldObj, xCoord, yCoord, zCoord);
                cropBaseMeta = plantable.getPlantMetadata(worldObj, xCoord, yCoord, zCoord);
            } catch (Exception e) {
                cropBlock = null;
                cropBaseMeta = 0;
            }
        } else {
            cropBlock = null;
        }

        if (cropBlock != null) {
            // If getPlantMetadata returns 0 but the seed has item damage, some mods
            // (like Natura) encode the crop type in the item damage and multiply it
            // to get the starting metadata. Detect this by checking if getDrops at
            // meta 0 returns a seed item matching our stored seed.
            if (cropBaseMeta == 0 && storedSeed.getItemDamage() > 0) {
                cropBaseMeta = detectBaseMeta();
            }

            cropMaxMeta = getMaxGrowthMeta(cropBlock, cropBaseMeta);

            // For stem crops, display the fruit block instead of the stem
            if (cropBlock instanceof BlockStem) {
                Item seedItem = storedSeed.getItem();
                if (seedItem == Items.melon_seeds) {
                    displayBlock = Blocks.melon_block;
                } else if (seedItem == Items.pumpkin_seeds) {
                    displayBlock = Blocks.pumpkin;
                } else {
                    displayBlock = cropBlock;
                }
            } else {
                displayBlock = cropBlock;
            }
        } else {
            displayBlock = null;
        }
    }

    /**
     * Detects the base metadata for modded crops that encode crop type in metadata.
     * Searches for the metadata value where getDrops produces items matching our seed.
     */
    private int detectBaseMeta() {
        if (cropBlock == null || worldObj == null) return 0;
        Item seedItem = storedSeed.getItem();
        int seedDmg = storedSeed.getItemDamage();

        // Check metadata values in steps of 4 (common pattern for modded multi-crops)
        for (int baseMeta = 4; baseMeta < 16; baseMeta += 4) {
            try {
                ArrayList<ItemStack> testDrops = cropBlock.getDrops(worldObj, xCoord, yCoord, zCoord, baseMeta, 0);
                if (testDrops != null) {
                    for (ItemStack drop : testDrops) {
                        if (drop != null && drop.getItem() == seedItem && drop.getItemDamage() == seedDmg) {
                            return baseMeta;
                        }
                    }
                }
            } catch (Exception e) {
                // Skip this meta if it causes errors
            }
        }
        return 0;
    }

    /**
     * Checks if an item is a valid seed/plantable for the farmer.
     */
    public static boolean isValidSeed(ItemStack stack) {
        if (stack == null || stack.getItem() == null) return false;
        return stack.getItem() instanceof IPlantable;
    }

    public Block getCropBlock() {
        return cropBlock;
    }

    /**
     * Gets the block to display visually (fruit block for stems, crop block otherwise).
     */
    public Block getDisplayBlock() {
        return displayBlock;
    }

    /**
     * Whether the display is a full block (like melon/pumpkin) vs a cross-shaped crop.
     */
    public boolean isDisplayFullBlock() {
        return displayBlock != null && cropBlock instanceof BlockStem;
    }

    public int getGrowthStage() {
        return growthStage;
    }

    public int getCropMaxMeta() {
        return cropMaxMeta - cropBaseMeta;
    }

    /**
     * Gets the actual block metadata for the current growth stage.
     * Maps the visual stage (0 to numStages) to the real metadata (cropBaseMeta to cropMaxMeta).
     */
    public int getDisplayMeta() {
        return cropBaseMeta + growthStage;
    }

    // ========================
    // Farming Logic
    // ========================

    @Override
    protected void serverTick() {
        if (!hasVillager(0)) return;
        if (isVillagerBaby(0)) return;
        if (!hasSeed()) return;

        // Resolve crop block if not yet done (e.g., after world load)
        if (cropBlock == null) {
            resolveCropBlock();
            if (cropBlock == null) return;
        }

        // Number of growth stages for this crop
        int numStages = cropMaxMeta - cropBaseMeta;
        if (numStages <= 0) numStages = 1;

        // Calculate ticks per growth stage
        int ticksPerStage = Math.max(1, ModConfig.farmSpeed / (numStages + 1));

        growthTimer++;
        if (growthTimer >= ticksPerStage) {
            growthTimer = 0;

            if (growthStage < numStages) {
                // Advance to next growth stage
                growthStage++;
                syncToClient();
            } else {
                // Fully grown - harvest and reset
                doHarvest();
                growthStage = 0;
                syncToClient();
            }
        }
    }

    /**
     * Harvest the crop - generate drops from the crop block at max growth.
     * Handles special cases like stem crops (melon/pumpkin).
     */
    private void doHarvest() {
        if (cropBlock == null || storedSeed == null) return;

        // Special case: stem crops (melon/pumpkin) don't drop fruit via getDrops
        if (cropBlock instanceof BlockStem) {
            doStemHarvest();
            return;
        }

        ArrayList<ItemStack> drops;
        try {
            drops = cropBlock.getDrops(worldObj, xCoord, yCoord, zCoord, cropMaxMeta, 0);
        } catch (Exception e) {
            return;
        }

        if (drops == null || drops.isEmpty()) return;

        // Only filter out ONE copy of the exact planted seed item (farmer replants it).
        // Everything else is output — don't filter by instanceof ItemSeeds since modded
        // crops (cotton, berries, etc.) may use seed-like items as their main output.
        Item seedItem = storedSeed.getItem();
        int seedMeta = storedSeed.getItemDamage();
        boolean skippedOneSeed = false;

        for (ItemStack drop : drops) {
            if (drop == null) continue;

            // Skip exactly one copy of the planted seed (the replanted one)
            if (!skippedOneSeed && drop.getItem() == seedItem && drop.getItemDamage() == seedMeta) {
                if (drop.stackSize > 1) {
                    // Has extras beyond the replanted seed — output the extras
                    ItemStack output = drop.copy();
                    output.stackSize = drop.stackSize - 1;
                    addToOutput(output, 0, OUTPUT_SLOTS - 1);
                }
                skippedOneSeed = true;
                continue;
            }

            addToOutput(drop, 0, OUTPUT_SLOTS - 1);
        }
    }

    /**
     * Special harvest for stem crops (melon/pumpkin).
     * Stems don't drop fruit via getDrops - the fruit spawns adjacent.
     * We manually produce the correct fruit item.
     */
    private void doStemHarvest() {
        ItemStack fruit = null;
        Item seedItem = storedSeed.getItem();

        if (seedItem == Items.melon_seeds) {
            fruit = new ItemStack(Items.melon, random.nextInt(5) + 3); // 3-7 melon slices
        } else if (seedItem == Items.pumpkin_seeds) {
            fruit = new ItemStack(Blocks.pumpkin, 1);
        }

        if (fruit != null) {
            addToOutput(fruit, 0, OUTPUT_SLOTS - 1);
        }
    }

    /**
     * Get the maximum growth metadata for a crop block, given its base metadata.
     * For vanilla crops (BlockCrops/BlockStem), max is always 7.
     * For modded crops with metadata-encoded types, searches for the actual max.
     */
    private int getMaxGrowthMeta(Block crop, int baseMeta) {
        if (crop instanceof BlockCrops) return 7;
        if (crop instanceof BlockStem) return 7;

        // For non-vanilla crops, find max meta by checking when getDrops changes
        // behavior (returns crop items instead of just seeds at maturity).
        // Try metadata values from baseMeta upward.
        if (worldObj != null) {
            Item seedItem = storedSeed != null ? storedSeed.getItem() : null;
            int lastValidMeta = baseMeta;
            for (int meta = baseMeta; meta < baseMeta + 16; meta++) {
                try {
                    ArrayList<ItemStack> drops = crop.getDrops(worldObj, xCoord, yCoord, zCoord, meta, 0);
                    if (drops != null && !drops.isEmpty()) {
                        // Check if any drop is NOT the seed — that indicates maturity
                        boolean hasNonSeedDrop = false;
                        for (ItemStack drop : drops) {
                            if (drop != null && drop.getItem() != seedItem) {
                                hasNonSeedDrop = true;
                                break;
                            }
                        }
                        if (hasNonSeedDrop) {
                            return meta;
                        }
                        lastValidMeta = meta;
                    } else if (meta > baseMeta) {
                        // Empty drops after having some means we passed the valid range
                        break;
                    }
                } catch (Exception e) {
                    break;
                }
            }
            return lastValidMeta;
        }
        return baseMeta + 7;
    }

    // ========================
    // ISidedInventory - output only from all sides
    // ========================

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return false;
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
        nbt.setInteger("FarmTimer", growthTimer);
        nbt.setInteger("GrowthStage", growthStage);
        nbt.setInteger("CropBaseMeta", cropBaseMeta);
        nbt.setInteger("CropMaxMeta", cropMaxMeta);

        if (storedSeed != null) {
            NBTTagCompound seedNBT = new NBTTagCompound();
            storedSeed.writeToNBT(seedNBT);
            nbt.setTag("StoredSeed", seedNBT);
        }
    }

    @Override
    protected void readCustomNBT(NBTTagCompound nbt) {
        growthTimer = nbt.getInteger("FarmTimer");
        growthStage = nbt.getInteger("GrowthStage");
        cropBaseMeta = nbt.getInteger("CropBaseMeta");
        cropMaxMeta = nbt.getInteger("CropMaxMeta");
        if (cropMaxMeta <= 0) cropMaxMeta = 7;

        if (nbt.hasKey("StoredSeed")) {
            storedSeed = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("StoredSeed"));
            // Crop block will be resolved on first tick when worldObj is available
            if (worldObj != null) {
                resolveCropBlock();
            }
        } else {
            storedSeed = null;
            cropBlock = null;
        }
    }
}
