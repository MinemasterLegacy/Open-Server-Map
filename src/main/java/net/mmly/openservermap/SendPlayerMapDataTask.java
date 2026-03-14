package net.mmly.openservermap;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.entity.Player;

import java.nio.ByteBuffer;
import java.util.Collection;

public class SendPlayerMapDataTask implements Runnable {

    OpenServerMap plugin;
    private static final byte PACKET_VERSION = 1;
    ByteArrayDataOutput out;

    private static double CONVERSION_FACTOR = 182.0444;
    // 0-360 > 0-65535

    private static short encodeDiretion(float mcYaw) {
        return (short) Math.round((mcYaw % 360) * CONVERSION_FACTOR);
    }

    private void encodePlayer(ByteArrayDataOutput out, Player player) {
        double[] playerLatLon;
        try {
            playerLatLon = Projection.to_geo(player.getX(), player.getZ());
            if (playerLatLon == null) return;
        } catch (CoordinateValueError e) {
            return;
        }
        out.writeLong(player.getUniqueId().getMostSignificantBits());
        out.writeLong(player.getUniqueId().getLeastSignificantBits());
        out.writeFloat((float) playerLatLon[0]);
        out.writeFloat((float) playerLatLon[1]);
        out.writeShort(encodeDiretion(player.getYaw()));
    }

    SendPlayerMapDataTask(OpenServerMap plugin) {
        this.plugin = plugin;

    }

    @Override
    public void run() {
        //plugin.getServer().broadcast(Component.text("yayayaya"));

        if (!Projection.initialized) {
            Projection.initialize();
        }

        out = ByteStreams.newDataOutput();
        out.writeByte(PACKET_VERSION);

        Collection<? extends Player> players = plugin.getServer().getOnlinePlayers();

        if (players.isEmpty()) return;

        for (Player player : players.toArray(new Player[0])) {
            encodePlayer(out, player);
        }

        for (Player player : players) {
            player.sendPluginMessage(plugin, "openservermap:channel", out.toByteArray());
        }

    }
}
