package net.mmly.openservermap;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.io.InputStream;

public final class OpenServerMap extends JavaPlugin implements Listener {

    private static int interval = 0;
    private static OpenServerMap instance;

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;

        getServer().getPluginManager().registerEvents(this, this);
        getServer().getMessenger().registerOutgoingPluginChannel(this, "openservermap:channel");

        BukkitScheduler scheduler = this.getServer().getScheduler();
        scheduler.scheduleSyncRepeatingTask(this, new SendPlayerMapDataTask(this), 20, 20);
    }

    @Override
    public void onDisable() {
        getServer().getMessenger().unregisterOutgoingPluginChannel(this);
        // Plugin shutdown logic

    }

    public static InputStream getConformals() {
        return instance.getResource("conformal2.txt");
    }

}
