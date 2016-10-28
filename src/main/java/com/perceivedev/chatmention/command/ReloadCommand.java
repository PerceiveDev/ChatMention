package com.perceivedev.chatmention.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.perceivedev.chatmention.ChatMention;
import com.perceivedev.perceivecore.util.Unicode;

/**
 * A reload command
 */
public class ReloadCommand implements CommandExecutor {

    private ChatMention plugin;

    /**
     * @param plugin The Plugin
     */
    public ReloadCommand(ChatMention plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        plugin.reload();
        sender.sendMessage(ChatColor.DARK_GRAY.toString() + Unicode.DOUBLE_ANGLE_RIGHT + ChatColor.GREEN + " Reloaded. Check the console for potential errors.");
        return true;
    }
}
