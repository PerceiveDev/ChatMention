package com.perceivedev.chatmention;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import com.perceivedev.chatmention.command.ReloadCommand;
import com.perceivedev.chatmention.config.ConfigParser;
import com.perceivedev.chatmention.listener.ChatPacketListener;
import com.perceivedev.chatmention.listener.PlayerListener;
import com.perceivedev.chatmention.replacing.MessageReplacer;
import com.perceivedev.chatmention.replacing.Replacer;
import com.perceivedev.perceivecore.packet.PacketManager;

public class ChatMention extends JavaPlugin {

    private MessageReplacer messageReplacer;

    private ChatPacketListener packetListener;
    private PlayerListener     playerListener;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        reload();
        getCommand("chatmention").setExecutor(new ReloadCommand(this));
    }

    /**
     * Reloads the config and listener
     */
    public void reload() {
        reloadConfig();

        if (packetListener != null) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                PacketManager.getInstance().removeListener(packetListener, player);
            }
        }
        if (playerListener != null) {
            HandlerList.unregisterAll(playerListener);
        }

        messageReplacer = new MessageReplacer();
        {
            ConfigParser parser = new ConfigParser(getConfig().getConfigurationSection("replacer"));
            List<Replacer> replacerList = parser.parse();
            if (replacerList.isEmpty()) {
                getLogger().warning("Could not parse any replacer from the config! This plugin won't do anything now ;)");
            }
            for (Replacer replacer : replacerList) {
                messageReplacer.addReplacer(replacer);
            }
        }

        packetListener = new ChatPacketListener(this);

        getServer().getPluginManager().registerEvents((playerListener = new PlayerListener(packetListener)), this);

        for (Player player : Bukkit.getOnlinePlayers()) {
            PacketManager.getInstance().addListener(packetListener, player);
        }
    }

    /**
     * @return The {@link MessageReplacer}
     */
    public MessageReplacer getMessageReplacer() {
        return messageReplacer;
    }
}
