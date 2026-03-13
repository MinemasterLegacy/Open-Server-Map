package net.mmly.openservermap;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public class SendPlayerMapDataTask implements Runnable {

    OpenServerMap plugin;
    ByteArrayDataOutput out = ByteStreams.newDataOutput();

    SendPlayerMapDataTask(OpenServerMap plugin) {
        this.plugin = plugin;

        out.writeUTF("OpenServerMap");
        out.writeUTF("playerData");
    }

    @Override
    public void run() {
        plugin.getServer().broadcast(Component.text("yayayaya"));
        if (plugin.getServer().getOnlinePlayers().toArray().length != 0) {
            Player player = (Player) plugin.getServer().getOnlinePlayers().toArray()[0];
            player.sendMessage("h");
            player.sendPluginMessage(plugin, "openservermap:channel", out.toByteArray());
        }

    }
}
