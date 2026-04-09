package com.easyvillagerslegacy.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.village.MerchantRecipeList;

import com.easyvillagerslegacy.gui.GuiTrader;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Server -> Client packet to sync updated trade list after cycling.
 * Forces the open trader GUI to refresh with the new trades.
 */
public class PacketSyncTrades implements IMessage {

    private NBTTagCompound tradesNBT;

    public PacketSyncTrades() {}

    public PacketSyncTrades(MerchantRecipeList recipes) {
        this.tradesNBT = recipes.getRecipiesAsTags();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        tradesNBT = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeTag(buf, tradesNBT);
    }

    public static class Handler implements IMessageHandler<PacketSyncTrades, IMessage> {

        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(PacketSyncTrades message, MessageContext ctx) {
            Minecraft mc = Minecraft.getMinecraft();

            // Schedule on main thread
            mc.func_152344_a(new Runnable() {
                @Override
                public void run() {
                    GuiScreen screen = Minecraft.getMinecraft().currentScreen;
                    if (screen instanceof GuiTrader) {
                        GuiTrader traderGui = (GuiTrader) screen;

                        // Deserialize the new trade list and update the merchant
                        MerchantRecipeList newRecipes;
                        if (message.tradesNBT != null) {
                            newRecipes = new MerchantRecipeList(message.tradesNBT);
                        } else {
                            newRecipes = new MerchantRecipeList();
                        }

                        traderGui.getMerchant().setRecipes(newRecipes);
                        traderGui.getMerchant().onTradesCycled();
                    }
                }
            });

            return null;
        }
    }
}
