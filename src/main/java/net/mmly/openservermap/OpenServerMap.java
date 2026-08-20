package net.mmly.openservermap;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Level;

public final class OpenServerMap extends JavaPlugin implements Listener {

    private static OpenServerMap instance;
    public static ArrayList<UUID> hiddenPlayers = new ArrayList<>();

    public static int PACKET_INTERVAL;
    public static boolean TRANSMIT_SPECTATORS;
    public static boolean VISIBILITY_OPT_IN;
    public static boolean PERSISTENT_VISIBILITY;
    public static boolean DENY_PERMISSION_ENABLED;
    public static boolean OVERRIDE_PERMISSION_ENABLED;

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

        LiteralCommandNode<CommandSourceStack> pluginCommands = Commands.literal("osm").requires(sender -> sender.getSender().hasPermission("openservermap.viscommands"))
                .then(Commands.literal("showself").executes(OsmCommands::showself))
                .then(Commands.literal("hideself").executes(OsmCommands::hideself))
                .then(Commands.literal("amivisible").executes(OsmCommands::amivisible))
                .build();
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().register(pluginCommands);
        });


    }

    private void loadConfigOptions() {
        PACKET_INTERVAL = this.getConfig().getInt("packet-interval");
        VISIBILITY_OPT_IN = this.getConfig().getBoolean("visibility-opt-in");
        TRANSMIT_SPECTATORS = this.getConfig().getBoolean("transmit-spectators");
        PERSISTENT_VISIBILITY = this.getConfig().getBoolean("persistent-visibility");
        DENY_PERMISSION_ENABLED = this.getConfig().getBoolean("deny-view");
        OVERRIDE_PERMISSION_ENABLED = this.getConfig().getBoolean("override-view");
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
