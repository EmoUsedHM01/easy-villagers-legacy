package com.easyvillagerslegacy.network;

import com.easyvillagerslegacy.tileentity.TileEntityTrader;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.World;

/**
 * Client -> Server packet when the player clicks the "Cycle Trades" button.
 * After cycling, sends a PacketSyncTrades back to the client to refresh the GUI.
 */
public class PacketCycleTrades implements IMessage {

    private int x, y, z;

    public PacketCycleTrades() {}

    public PacketCycleTrades(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
    }

    public static class Handler implements IMessageHandler<PacketCycleTrades, IMessage> {

        @Override
        public IMessage onMessage(PacketCycleTrades message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            World world = player.worldObj;

            TileEntity te = world.getTileEntity(message.x, message.y, message.z);
            if (!(te instanceof TileEntityTrader)) return null;

            TileEntityTrader tile = (TileEntityTrader) te;

            double distSq = player.getDistanceSq(message.x + 0.5, message.y + 0.5, message.z + 0.5);
            if (distSq > 64.0) return null;

            // Cycle the trades
            tile.cycleTrades();
            player.addChatMessage(new ChatComponentTranslation("msg.easyvillagerslegacy.trades_cycled"));

            // Send the updated trades back to the client so the GUI refreshes
            EntityVillager tempVillager = tile.createTemporaryVillager(0);
            if (tempVillager != null) {
                MerchantRecipeList recipes = tempVillager.getRecipes(player);
                if (recipes != null) {
                    NetworkHandler.INSTANCE.sendTo(new PacketSyncTrades(recipes), player);
                }
            }

            return null;
        }
    }
}
