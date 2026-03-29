package net.mmly.openservermap;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;

public class OsmCommands {

    private static boolean ranByPlayer(CommandContext<CommandSourceStack> context) {
        if (context.getSource().getSender() instanceof Player) {
            return true;
        } else {
            context.getSource().getSender().sendMessage("Error: Sender is not a Player");
            return false;
        }

    }

    private static boolean playerCanRun(CommandContext<CommandSourceStack> context) {
        if (context.getSource().getSender().hasPermission("openservermap.viscommands")) {
            return true;
        } else {
            context.getSource().getSender().sendMessage("You do not have permission to use this command.");
            return false;
        }
    }

    public static int showself(CommandContext<CommandSourceStack> context) {
        CommandSourceStack stack = context.getSource();
        if (!ranByPlayer(context)) return 0;
        if (!playerCanRun(context)) return 0;

        if (Database.setPlayerVisibility(((Player) stack.getSender()).getUniqueId(), true)) {
            context.getSource().getSender().sendMessage("You are now visible globally.");
            return 1;
        } else {
            context.getSource().getSender().sendMessage("An error occurred. Check logs for more info.");
            return 0;
        }

    }

    public static int hideself(CommandContext<CommandSourceStack> context) {
        CommandSourceStack stack = context.getSource();
        if (!ranByPlayer(context)) return 0;
        if (!playerCanRun(context)) return 0;

        if (Database.setPlayerVisibility(((Player) stack.getSender()).getUniqueId(), false)) {
            context.getSource().getSender().sendMessage("You are no longer visible globally.");
            return 1;
        } else {
            context.getSource().getSender().sendMessage("An error occurred. Check logs for more info.");
            return 0;
        }
    }
}