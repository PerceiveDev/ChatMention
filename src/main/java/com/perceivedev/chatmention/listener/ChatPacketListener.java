package com.perceivedev.chatmention.listener;

import java.util.Optional;

import org.bukkit.Bukkit;

import com.perceivedev.chatmention.ChatMention;
import com.perceivedev.chatmention.ChatPacketTextUtils;
import com.perceivedev.perceivecore.packet.Packet;
import com.perceivedev.perceivecore.packet.PacketAdapter;
import com.perceivedev.perceivecore.packet.PacketEvent;
import com.perceivedev.perceivecore.reflection.ReflectionUtil;
import com.perceivedev.perceivecore.reflection.ReflectionUtil.ReflectResponse;

/**
 * A Listener for chat packets
 */
public class ChatPacketListener extends PacketAdapter {

    private Class<?>    targetClass;
    private ChatMention plugin;

    /**
     * @param plugin The Plugin to listen for
     */
    public ChatPacketListener(ChatMention plugin) {
        this.plugin = plugin;

        Optional<Class<?>> target = ReflectionUtil.getClass("{nms}.PacketPlayOutChat");
        if (!target.isPresent()) {
            plugin.getLogger().severe("Could not find chat packet class! The plugin will now be disabled.");
            Bukkit.getPluginManager().disablePlugin(plugin);
            throw new IllegalArgumentException("ChatPacket class not found!");
        }
        targetClass = target.get();
    }

    @Override
    public void onPacketSend(PacketEvent packetEvent) {
        if (packetEvent.getPacket().getPacketClass() != targetClass) {
            return;
        }

        Packet packet = packetEvent.getPacket();

        if (!isNormalChatPacket(packet)) {
            return;
        }

        String text = ChatPacketTextUtils.decode(packet);

        if (text == null) {
            return;
        }

        String newText = plugin.getMessageReplacer().replaceAll(text, packetEvent.getPlayer());

        if (newText.equals(text)) {
            return;
        }
        
        packet.set("a", ChatPacketTextUtils.encode(newText));
    }

    private boolean isNormalChatPacket(Packet packet) {
        ReflectResponse<Object> type = packet.get("b");

        return type.isSuccessful()
                  && type.isValuePresent()
                  && ((byte) type.getValue()) < 2; // 0: chat (chat box), 1: system message (chat box), 2: action bar
    }
}
