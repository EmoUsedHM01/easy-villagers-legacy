package com.easyvillagerslegacy.gui;

import com.easyvillagerslegacy.tileentity.TileEntityTrader;

import java.lang.reflect.Method;
import java.util.Random;

import net.minecraft.entity.IMerchant;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;

/**
 * IMerchant implementation that wraps the TileEntityTrader.
 * Allows using the vanilla merchant GUI/container with block-stored villagers.
 */
public class BlockMerchant implements IMerchant {

    private final TileEntityTrader tile;
    private EntityPlayer customer;
    private EntityVillager cachedVillager;
    private MerchantRecipeList cachedRecipes;

    public BlockMerchant(TileEntityTrader tile) {
        this.tile = tile;
        refreshVillager();
    }

    private void refreshVillager() {
        if (tile.hasVillager(0)) {
            cachedVillager = tile.createTemporaryVillager(0);
            if (cachedVillager != null && customer != null) {
                cachedRecipes = cachedVillager.getRecipes(customer);
            }
        }
    }

    @Override
    public void setCustomer(EntityPlayer player) {
        this.customer = player;
        if (cachedVillager != null) {
            cachedRecipes = cachedVillager.getRecipes(player);
        }
    }

    @Override
    public EntityPlayer getCustomer() {
        return customer;
    }

    @Override
    public MerchantRecipeList getRecipes(EntityPlayer player) {
        if (cachedVillager == null) {
            refreshVillager();
        }
        if (cachedVillager != null) {
            cachedRecipes = cachedVillager.getRecipes(player);
            return cachedRecipes;
        }
        return new MerchantRecipeList();
    }

    @Override
    public void setRecipes(MerchantRecipeList recipes) {
        this.cachedRecipes = recipes;
    }

    @Override
    public void useRecipe(MerchantRecipe recipe) {
        if (cachedVillager != null) {
            cachedVillager.useRecipe(recipe);

            // Vanilla refreshes trades on entity tick after using the last trade,
            // but our villager never ticks. Simulate the refresh here:
            // re-enable disabled trades and potentially add new ones.
            MerchantRecipeList recipes = cachedVillager.getRecipes(customer);
            if (recipes != null && recipes.size() > 0
                    && recipe.hasSameIDsAs((MerchantRecipe) recipes.get(recipes.size() - 1))) {
                Random rand = new Random();

                // Re-enable disabled trades (increase maxUses)
                for (int i = 0; i < recipes.size(); i++) {
                    MerchantRecipe r = (MerchantRecipe) recipes.get(i);
                    if (r.isRecipeDisabled()) {
                        r.func_82783_a(rand.nextInt(6) + rand.nextInt(6) + 2);
                    }
                }

                // Add new trades via reflection (addDefaultEquipmentAndRecipies is private)
                try {
                    Method addRecipes = EntityVillager.class.getDeclaredMethod(
                        "addDefaultEquipmentAndRecipies", int.class);
                    addRecipes.setAccessible(true);
                    addRecipes.invoke(cachedVillager, 1);
                } catch (Exception e) {
                    // Fallback: try SRG name for obfuscated environment
                    try {
                        Method addRecipes = EntityVillager.class.getDeclaredMethod(
                            "func_70950_c", int.class);
                        addRecipes.setAccessible(true);
                        addRecipes.invoke(cachedVillager, 1);
                    } catch (Exception ignored) {}
                }
            }

            // Save the updated trade state back to the tile entity
            tile.saveVillagerState(cachedVillager);
        }
    }

    @Override
    public void func_110297_a_(ItemStack stack) {
        // verifySellingItem - called when a trade item is picked up
    }

    public IChatComponent getDisplayName() {
        int profession = tile.getVillagerProfession(0);
        String name = com.easyvillagerslegacy.tileentity.WorkstationRegistry.getProfessionName(profession);
        return new ChatComponentText(name);
    }

    public TileEntityTrader getTileEntity() {
        return tile;
    }

    /**
     * Refresh the cached villager after trades are cycled.
     */
    public void onTradesCycled() {
        refreshVillager();
    }
}
