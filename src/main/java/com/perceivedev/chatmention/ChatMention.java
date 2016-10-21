package com.perceivedev.chatmention;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.perceivedev.chatmention.config.ConfigParser;
import com.perceivedev.chatmention.listener.ChatPacketListener;
import com.perceivedev.chatmention.listener.PlayerListener;
import com.perceivedev.chatmention.replacing.MessageReplacer;
import com.perceivedev.chatmention.replacing.Replacer;
import com.perceivedev.perceivecore.packet.PacketManager;

public class ChatMention extends JavaPlugin {

    private MessageReplacer messageReplacer;

    @Override
    public void onEnable() {
        messageReplacer = new MessageReplacer();

        // TODO: @rayzr - Add a reload command

        saveDefaultConfig();

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

        ChatPacketListener packetListener = new ChatPacketListener(this);

        getServer().getPluginManager().registerEvents(new PlayerListener(packetListener), this);

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
