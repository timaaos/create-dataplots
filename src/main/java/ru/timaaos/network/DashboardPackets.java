package ru.timaaos.network;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import ru.timaaos.blocks.DashboardBlockEntity;

public class DashboardPackets {

    public static final Identifier OPEN_SCREEN =
            new Identifier("dataplots", "open_dashboard");

    public static final Identifier CONFIG =
            new Identifier("dataplots", "dashboard_config");

    public static void registerServer() {
        ServerPlayNetworking.registerGlobalReceiver(CONFIG,
                (server, player, handler, buf, responseSender) -> {

                    BlockPos pos = buf.readBlockPos();
                    int width = buf.readInt();
                    int height = buf.readInt();

                    server.execute(() -> {
                        if (player.getWorld().getBlockEntity(pos)
                                instanceof DashboardBlockEntity be) {

                            be.plotWidth = width;
                            be.plotHeight = height;
                            be.markDirty();
                            be.sendData();
                        }
                    });
                });
    }

    public static void openScreen(ServerPlayerEntity player, BlockPos pos) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(pos);
        ServerPlayNetworking.send(player, OPEN_SCREEN, buf);
    }
}
