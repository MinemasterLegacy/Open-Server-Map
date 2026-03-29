package net.mmly.openservermap;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Level;

public final class OpenServerMap extends JavaPlugin implements Listener {

    private static OpenServerMap instance;
    public static int PACKET_INTERVAL;
    public static boolean TRANSMIT_SPECTATORS;
    public static boolean VISIBILITY_OPT_IN;
    public static ArrayList<UUID> hiddenPlayers = new ArrayList<>();

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;

        saveDefaultConfig();
        loadConfigOptions();
        Database.establishConnection();

        getServer().getPluginManager().registerEvents(this, this);
        getServer().getMessenger().registerOutgoingPluginChannel(this, "openservermap:channel");

        BukkitScheduler scheduler = this.getServer().getScheduler();
        if (PACKET_INTERVAL > 0) scheduler.scheduleSyncRepeatingTask(this, new SendPlayerMapDataTask(this), PACKET_INTERVAL, PACKET_INTERVAL);
        else this.getLogger().log(Level.WARNING, "Invalid Configuration value \"" + PACKET_INTERVAL + "\" for packet-interval. Packets will not be sent.");

        registerCommands();

        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(), this);

    }

    private void registerCommands() {
        LifecycleEventManager<Plugin> manager = this.getLifecycleManager();
        manager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();
            commands.register(Commands.literal("showself").executes(OsmCommands::showself).build());
        });
        /*
        LiteralCommandNode<CommandSourceStack> pluginCommands = Commands.literal("osm")
                .then(Commands.literal("showself").executes(OsmCommands::showself))
                .then(Commands.literal("hideself").executes(OsmCommands::hideself))
                .then(Commands.literal("reload").executes(OsmCommands::reload))
                .build();
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().register(pluginCommands);
        });

         */
    }

    private void loadConfigOptions() {
        PACKET_INTERVAL = this.getConfig().getInt("packet-interval");
        VISIBILITY_OPT_IN = this.getConfig().getBoolean("visibility-opt-in");
        TRANSMIT_SPECTATORS = this.getConfig().getBoolean("transmit-spectators");
    }

    @Override
    public void onDisable() {
        getServer().getMessenger().unregisterOutgoingPluginChannel(this);
        // Plugin shutdown logic
        Database.closeConnection();
        saveConfig();
    }

    public static void log(Level level, String message) {
        instance.getLogger().log(level, message);
    }

    public static InputStream getConformals() {
        return instance.getResource("conformal2.txt");
    }

}
