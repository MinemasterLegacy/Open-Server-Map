package net.mmly.openservermap;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.Collection;

public class SendPlayerMapDataTask implements Runnable {

    OpenServerMap plugin;
    private static final byte PACKET_VERSION = 1;
    ByteArrayDataOutput out;
    ByteArrayDataOutput overrideOut;

    private static double CONVERSION_FACTOR = 182.0444;
    // 0-360 > 0-65535

    private static short encodeDiretion(float mcYaw) {
        return (short) Math.round((mcYaw % 360) * CONVERSION_FACTOR);
    }

    private void encodePlayer(ByteArrayDataOutput out, Player player, boolean ignoreVisibilitySetting) {
        if (player.getGameMode() == GameMode.SPECTATOR && !OpenServerMap.TRANSMIT_SPECTATORS) return;
        if (!Database.playerIsVisible(player.getUniqueId()) && !ignoreVisibilitySetting) return;
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

    private ByteArrayDataOutput encodePacket(Collection<? extends Player> players, boolean ignoreVisibilitySetting) {
        ByteArrayDataOutput stream = ByteStreams.newDataOutput();
        stream.writeByte(PACKET_VERSION);

        for (Player player : players.toArray(new Player[0])) {
            encodePlayer(stream, player, ignoreVisibilitySetting);
        }

        return stream;
    }

    @Override
    public void run() {
        //plugin.getServer().broadcast(Component.text("yayayaya"));

        if (!Projection.initialized) {
            Projection.initialize();
        }

        Collection<? extends Player> players = plugin.getServer().getOnlinePlayers();
        if (players.isEmpty()) return;

        out = encodePacket(players, false);
        if (OpenServerMap.OVERRIDE_PERMISSION_ENABLED) overrideOut = encodePacket(players, true);

        for (Player player : players) {
            if (denyViewFor(player)) continue;
            player.sendPluginMessage(
                    plugin,
                    "openservermap:channel",
                    overrideViewFor(player) ?
                            overrideOut.toByteArray() :
                            out.toByteArray());
        }

    }

    private boolean denyViewFor(Player player) {
        if (!OpenServerMap.DENY_PERMISSION_ENABLED) return false;
        if (player.isOp()) return false;
        return player.hasPermission("openservermap.denyvis");
    }

    private boolean overrideViewFor(Player player) {
        if (!OpenServerMap.OVERRIDE_PERMISSION_ENABLED) return false;
        if (player.isOp()) return true;
        return player.hasPermission("openservermap.overridevis");
    }
}
