package com.easyvillagerslegacy.network;

import com.easyvillagerslegacy.EasyVillagersLegacy;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

public class NetworkHandler {

    public static SimpleNetworkWrapper INSTANCE;

    private static int packetId = 0;

    public static void init() {
        INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(EasyVillagersLegacy.MOD_ID);

        // Client -> Server
        INSTANCE.registerMessage(
            PacketCycleTrades.Handler.class, PacketCycleTrades.class, packetId++, Side.SERVER);

        // Server -> Client
        INSTANCE.registerMessage(
            PacketSyncTrades.Handler.class, PacketSyncTrades.class, packetId++, Side.CLIENT);
    }
}
