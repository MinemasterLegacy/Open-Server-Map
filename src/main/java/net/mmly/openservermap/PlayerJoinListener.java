package net.mmly.openservermap;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        //System.out.println("entry init");
        Database.initializePlayerEntryIfAbsent(event.getPlayer().getUniqueId());
        //Database.printTable();
    }

}
