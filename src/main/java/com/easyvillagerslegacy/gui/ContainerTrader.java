package com.easyvillagerslegacy.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ContainerMerchant;

/**
 * Container for the trader block. Extends vanilla ContainerMerchant
 * to work with our BlockMerchant implementation.
 */
public class ContainerTrader extends ContainerMerchant {

    private final BlockMerchant merchant;

    public ContainerTrader(InventoryPlayer playerInv, BlockMerchant merchant) {
        super(playerInv, merchant, playerInv.player.worldObj);
        this.merchant = merchant;
    }

    public BlockMerchant getBlockMerchant() {
        return merchant;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return merchant.getTileEntity().isUseableByPlayer(player);
    }
}
