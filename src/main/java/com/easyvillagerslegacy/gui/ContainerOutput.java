package com.easyvillagerslegacy.gui;

import com.easyvillagerslegacy.tileentity.TileEntityVillagerBase;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

/**
 * Container for blocks with output-only inventory (Farmer, Iron Farm).
 * Slot positions match the original output.png GUI texture:
 *   4 output slots centered at y=17, x starting at 53, 18px spacing.
 */
public class ContainerOutput extends Container {

    private final TileEntityVillagerBase tile;
    private final int outputSlots;

    public ContainerOutput(InventoryPlayer playerInv, TileEntityVillagerBase tile) {
        this.tile = tile;
        this.outputSlots = tile.getSizeInventory();

        // Output slots - 4 slots matching output.png slot backgrounds at (52,20)
        for (int i = 0; i < outputSlots; i++) {
            addSlotToContainer(new SlotOutput(tile, i, 52 + i * 18, 20));
        }

        // Player inventory (3 rows, starting at y=52 in output.png)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlotToContainer(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 51 + row * 18));
            }
        }

        // Hotbar (y=109 in output.png)
        for (int col = 0; col < 9; col++) {
            addSlotToContainer(new Slot(playerInv, col, 8 + col * 18, 109));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return tile.isUseableByPlayer(player);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slotIndex) {
        ItemStack result = null;
        Slot slot = (Slot) inventorySlots.get(slotIndex);

        if (slot != null && slot.getHasStack()) {
            ItemStack stackInSlot = slot.getStack();
            result = stackInSlot.copy();

            if (slotIndex < outputSlots) {
                if (!mergeItemStack(stackInSlot, outputSlots, inventorySlots.size(), true)) {
                    return null;
                }
            } else {
                return null; // Can't insert into output slots
            }

            if (stackInSlot.stackSize == 0) {
                slot.putStack(null);
            } else {
                slot.onSlotChanged();
            }
        }
        return result;
    }

    public TileEntityVillagerBase getTileEntity() {
        return tile;
    }

    private static class SlotOutput extends Slot {
        public SlotOutput(TileEntityVillagerBase inv, int index, int x, int y) {
            super(inv, index, x, y);
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return false;
        }
    }
}
