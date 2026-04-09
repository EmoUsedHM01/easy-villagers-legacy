package com.easyvillagerslegacy.gui;

import com.easyvillagerslegacy.tileentity.TileEntityBreeder;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

/**
 * Container for the Breeder block.
 * Slot positions match the original input_output.png GUI texture:
 *   Top row (y=17): 4 food input slots at x=53, 18px spacing
 *   Bottom row (y=35): baby output in first slot position at x=53
 */
public class ContainerBreeder extends Container {

    private final TileEntityBreeder tile;
    private int lastBreedTimer = 0;

    public ContainerBreeder(InventoryPlayer playerInv, TileEntityBreeder tile) {
        this.tile = tile;

        // Food input slots - top row of input_output.png at (52,20)
        for (int i = 0; i < 4; i++) {
            addSlotToContainer(new SlotFood(tile, i, 52 + i * 18, 20));
        }

        // Baby output slot - second row at (52,51)
        addSlotToContainer(new SlotOutput(tile, 4, 52, 51));

        // Player inventory (3 rows, starting at y=83 in input_output.png)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlotToContainer(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 82 + row * 18));
            }
        }

        // Hotbar (y=140 in input_output.png)
        for (int col = 0; col < 9; col++) {
            addSlotToContainer(new Slot(playerInv, col, 8 + col * 18, 140));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return tile.isUseableByPlayer(player);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        for (Object obj : crafters) {
            ICrafting crafter = (ICrafting) obj;
            if (lastBreedTimer != tile.getBreedTimer()) {
                crafter.sendProgressBarUpdate(this, 0, tile.getBreedTimer());
            }
        }
        lastBreedTimer = tile.getBreedTimer();
    }

    @Override
    public void updateProgressBar(int id, int value) {
        // Handled client-side for GUI display
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slotIndex) {
        ItemStack result = null;
        Slot slot = (Slot) inventorySlots.get(slotIndex);

        if (slot != null && slot.getHasStack()) {
            ItemStack stackInSlot = slot.getStack();
            result = stackInSlot.copy();

            int tileSlots = 5; // 4 food + 1 output

            if (slotIndex < tileSlots) {
                if (!mergeItemStack(stackInSlot, tileSlots, inventorySlots.size(), true)) {
                    return null;
                }
            } else {
                if (TileEntityBreeder.isValidFood(stackInSlot)) {
                    if (!mergeItemStack(stackInSlot, 0, 4, false)) {
                        return null;
                    }
                } else {
                    return null;
                }
            }

            if (stackInSlot.stackSize == 0) {
                slot.putStack(null);
            } else {
                slot.onSlotChanged();
            }
        }
        return result;
    }

    public TileEntityBreeder getTileEntity() {
        return tile;
    }

    private static class SlotFood extends Slot {
        public SlotFood(TileEntityBreeder inv, int index, int x, int y) {
            super(inv, index, x, y);
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return TileEntityBreeder.isValidFood(stack);
        }
    }

    private static class SlotOutput extends Slot {
        public SlotOutput(TileEntityBreeder inv, int index, int x, int y) {
            super(inv, index, x, y);
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return false;
        }
    }
}
