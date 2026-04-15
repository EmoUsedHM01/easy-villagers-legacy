package com.easyvillagerslegacy.tileentity;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Random;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;

/**
 * Soft-dependency IC2 crop support for the Farmer block.
 * <p>
 * Rather than hardcoding per-crop outputs (there are 150+ across IC2, GregTech,
 * Crops++, GoodGenerators, and GT++, and GregTech overrides IC2's base cards
 * so outputs change per-pack), this delegates to the installed CropCard's own
 * {@code getGain(ICropTile)} method via reflection. Whatever crop drop the
 * installed mod defines is what the farmer produces — including Ferru Leaves
 * (GTNH override) instead of stock IC2's iron dust, GT++ metal leaves, etc.
 * <p>
 * IC2 is scheduled for removal in GTNH 2.9. All access is reflective; when
 * IC2 is absent at runtime, every method returns safely (false/null).
 */
public final class IC2CropSupport {

    private static final String SEED_CLASS     = "ic2.core.item.ItemCropSeed";
    private static final String CROPS_CLASS    = "ic2.api.crops.Crops";
    private static final String CARD_CLASS     = "ic2.api.crops.CropCard";
    private static final String TILE_CLASS     = "ic2.api.crops.ICropTile";
    private static final String BASESEED_CLASS = "ic2.api.crops.BaseSeed";

    private static boolean initialized = false;
    private static boolean available = false;
    private static Class<?> itemCropSeedClass;
    private static Class<?> cropsClass;
    private static Class<?> cropCardClass;
    private static Class<?> cropTileClass;
    private static Object cropsInstance;
    private static Method getCropCardByStack;
    private static Method getBaseSeedByStack;
    private static Field  baseSeedCropField;
    private static Method cardGetGain;
    private static Method cardMaxSize;
    private static Method cardName;

    private IC2CropSupport() {}

    private static synchronized void init() {
        if (initialized) return;
        initialized = true;
        try {
            itemCropSeedClass = Class.forName(SEED_CLASS);
            cropsClass        = Class.forName(CROPS_CLASS);
            cropCardClass     = Class.forName(CARD_CLASS);
            cropTileClass     = Class.forName(TILE_CLASS);
            cropsInstance     = cropsClass.getField("instance").get(null);
            getCropCardByStack = cropsClass.getMethod("getCropCard", ItemStack.class);
            getBaseSeedByStack = cropsClass.getMethod("getBaseSeed", ItemStack.class);
            Class<?> baseSeedClass = Class.forName(BASESEED_CLASS);
            baseSeedCropField = baseSeedClass.getField("crop");
            cardGetGain       = cropCardClass.getMethod("getGain", cropTileClass);
            cardMaxSize       = cropCardClass.getMethod("maxSize");
            cardName          = cropCardClass.getMethod("name");
            available = true;
        } catch (Throwable t) {
            available = false;
        }
    }

    public static boolean isAvailable() {
        if (!initialized) init();
        return available;
    }

    /**
     * True if this ItemStack is any kind of IC2-system crop seed.
     * <p>
     * Stock IC2 uses {@code ic2.core.item.ItemCropSeed}, but GregTech, Crops++,
     * GT++, and GoodGenerators may wrap or replace that item with their own
     * class. We accept the stock class directly, and fall back to asking IC2's
     * {@code Crops} registry whether it can resolve a CropCard from this stack
     * — which works regardless of the item's concrete class, because IC2's
     * lookup reads NBT (owner/name tags) rather than the item identity.
     */
    public static boolean isIC2Seed(ItemStack stack) {
        if (stack == null || stack.getItem() == null || !isAvailable()) return false;
        if (itemCropSeedClass.isInstance(stack.getItem())) return true;
        return resolveCropCard(stack) != null;
    }

    /**
     * Resolve a crop name for a seed. Prefers the NBT "name" tag (fast path),
     * falls back to the registered CropCard's {@code name()} when the tag is
     * missing (common for non-IC2 seed items that still register with Crops).
     * Returns a non-null marker for any registered crop seed.
     */
    public static String getCropName(ItemStack seed) {
        if (seed == null || !isAvailable()) return null;
        NBTTagCompound nbt = seed.getTagCompound();
        if (nbt != null) {
            String name = nbt.getString("name");
            if (name != null && !name.isEmpty()) return name;
        }
        // Fallback: ask IC2 for the CropCard and use its own name().
        Object card = resolveCropCard(seed);
        if (card != null) {
            try {
                Object n = cardName.invoke(card);
                if (n instanceof String && !((String) n).isEmpty()) return (String) n;
            } catch (Throwable t) {
                // Fall through
            }
            // Registered but unnamed — return a sentinel so callers still see
            // this as a valid IC2 crop and take the IC2 code path.
            return "<ic2-crop>";
        }
        return null;
    }

    /**
     * Ask IC2's Crops registry for the CropCard of this stack.
     * Tries two paths:
     *   1. {@code Crops.getCropCard(stack)} — works for real IC2 crop seeds
     *      (any class that stores the owner/name NBT tags).
     *   2. {@code Crops.getBaseSeed(stack).crop} — works for items registered
     *      via {@code registerBaseSeed} as crop starters, including Tinker's
     *      Construct oreberries (metadata variants 0–4 map to iron/gold/
     *      copper/tin/aluminium OreBerry crops), Natura berries, etc.
     */
    private static Object resolveCropCard(ItemStack stack) {
        if (!isAvailable() || stack == null) return null;
        try {
            Object card = getCropCardByStack.invoke(cropsInstance, stack);
            if (card != null) return card;
        } catch (Throwable t) {
            // fall through to base seed lookup
        }
        try {
            Object baseSeed = getBaseSeedByStack.invoke(cropsInstance, stack);
            if (baseSeed != null) {
                return baseSeedCropField.get(baseSeed);
            }
        } catch (Throwable t) {
            // ignore
        }
        return null;
    }

    /**
     * Generate a harvest drop for an IC2 crop seed by calling the registered
     * CropCard's own {@code getGain(ICropTile)} method.
     *
     * @param seed    the stored IC2 crop seed (must be an ItemCropSeed)
     * @param world   world reference for the mock ICropTile
     * @param x       tile x
     * @param y       tile y
     * @param z       tile z
     * @param random  random source (many getGain implementations roll against dropGainChance)
     * @return the drop ItemStack, or null if the card refuses to drop this tick or is unknown
     */
    public static ItemStack getHarvestDrop(ItemStack seed, World world, int x, int y, int z, Random random) {
        Object card = resolveCropCard(seed);
        if (card == null) return null;
        int maxSize;
        try {
            maxSize = ((Integer) cardMaxSize.invoke(card)).intValue();
        } catch (Throwable t) {
            return null;
        }

        Object mockTile = createMockTile(card, maxSize, world, x, y, z);

        // Many getGain impls roll against dropGainChance and return null on miss.
        // Retry a few times so a successful harvest still produces output.
        for (int attempt = 0; attempt < 16; attempt++) {
            try {
                Object result = cardGetGain.invoke(card, mockTile);
                if (result instanceof ItemStack) {
                    ItemStack stack = (ItemStack) result;
                    if (stack.stackSize > 0) {
                        return stack.copy();
                    }
                }
            } catch (Throwable t) {
                return null;
            }
        }
        return null;
    }

    /**
     * Simulated growth-stage count for IC2 crops. IC2 cards report their own
     * maxSize (usually 3–7); use that if the card is loadable, else fall back
     * to a sensible default.
     */
    public static int getGrowthStageCount(ItemStack seed) {
        Object card = resolveCropCard(seed);
        if (card == null) return 4;
        try {
            return ((Integer) cardMaxSize.invoke(card)).intValue();
        } catch (Throwable t) {
            return 4;
        }
    }

    /**
     * Build a java.lang.reflect.Proxy implementing ICropTile that returns
     * sensible defaults for every method a CropCard's getGain() might call.
     * We pretend the crop is fully grown, healthy, and in ideal conditions.
     */
    private static Object createMockTile(final Object card, final int maxSize,
                                          final World world, final int x, final int y, final int z) {
        final NBTTagCompound customData = new NBTTagCompound();
        final ChunkCoordinates location = new ChunkCoordinates(x, y, z);

        InvocationHandler handler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) {
                String n = method.getName();
                Class<?> ret = method.getReturnType();

                // Crop card + state
                if ("getCrop".equals(n))      return card;
                if ("getSize".equals(n))      return (byte) maxSize;       // fully grown
                if ("getGrowth".equals(n))    return (byte) 31;            // max stats
                if ("getGain".equals(n) && ret == byte.class) return (byte) 31;
                if ("getResistance".equals(n)) return (byte) 31;
                if ("getScanLevel".equals(n)) return (byte) 4;
                if ("getID".equals(n))        return (short) 0;

                // Environment — pretend all conditions are ideal so conditional drops fire
                if ("getHumidity".equals(n))   return (byte) 10;
                if ("getNutrients".equals(n))  return (byte) 10;
                if ("getAirQuality".equals(n)) return (byte) 10;
                if ("getLightLevel".equals(n)) return 15;
                if ("getNutrientStorage".equals(n)) return 100;
                if ("getHydrationStorage".equals(n)) return 100;
                if ("getWeedExStorage".equals(n))    return 100;

                // World / location — some getGain impls use isBlockBelow for bonus drops.
                // Returning true unconditionally causes cards like Aurelia (needs gold
                // below) to grant their metal drop instead of refusing.
                if ("getWorld".equals(n))    return world;
                if ("getLocation".equals(n)) return location;
                if ("isBlockBelow".equals(n)) return Boolean.TRUE;

                if ("getCustomData".equals(n)) return customData;

                // Default return values by type
                if (ret == boolean.class) return Boolean.FALSE;
                if (ret == byte.class)    return (byte) 0;
                if (ret == short.class)   return (short) 0;
                if (ret == int.class)     return 0;
                if (ret == long.class)    return 0L;
                if (ret == float.class)   return 0f;
                if (ret == double.class)  return 0d;
                if (ret == char.class)    return (char) 0;
                return null;
            }
        };

        return Proxy.newProxyInstance(cropTileClass.getClassLoader(),
            new Class<?>[]{ cropTileClass }, handler);
    }
}
