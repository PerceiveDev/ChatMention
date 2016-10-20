package com.perceivedev.chatmention;

import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.perceivedev.chatmention.replacing.MessageReplacer;
import com.perceivedev.chatmention.replacing.implementation.NameHighlighter;
import com.perceivedev.perceivecore.packet.Packet;
import com.perceivedev.perceivecore.packet.PacketEvent;
import com.perceivedev.perceivecore.packet.PacketListener;
import com.perceivedev.perceivecore.packet.PacketManager;
import com.perceivedev.perceivecore.reflection.ReflectionUtil;
import com.perceivedev.perceivecore.reflection.ReflectionUtil.ReflectResponse;

public class ChatMention extends JavaPlugin implements PacketListener, Listener {

    private static final String IDENTIFIER = hiddenText("ChatMention");
    private Class<?> targetClass;

    private Logger logger;

    private MessageReplacer replacer;

    @Override
    public void onEnable() {
        logger = getLogger();

        Optional<Class<?>> target = ReflectionUtil.getClass("{nms}.PacketPlayOutChat");
        if (!target.isPresent()) {
            logger.severe("Could not find chat packet class! The plugin will now be disabled.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        targetClass = target.get();
        replacer = new MessageReplacer();
        replacer.addReplacer(new NameHighlighter("&b&l", ""));

        getServer().getPluginManager().registerEvents(this, this);

        logger.info(versionText() + " enabled");
    }

    @Override
    public void onDisable() {
        logger.info(versionText() + " disabled");
    }

    public String versionText() {
        return getName() + " v" + getDescription().getVersion();
    }

    private static String hiddenText(String text) {
        char[] chars = text.toCharArray();
        StringBuilder sb = new StringBuilder();
        for (char c : chars) {
            sb.append(ChatColor.COLOR_CHAR).append(c);
        }
        return sb.toString();
    }

    @Override
    public void onPacketReceived(PacketEvent packetEvent) {
    }

    @Override
    public void onPacketSend(PacketEvent packetEvent) {
        if (packetEvent.getPacket().getPacketClass() != targetClass) {
            return;
        }
        System.out.println("\t>> Chat packet found!");

        Packet packet = packetEvent.getPacket();

        if (!isNormalChatPacket(packet)) {
            System.out.println("\t>> Is no normal chat packet");
            return;
        }

        System.out.println("\t>> Correct type found!");

        String text = TextExtractor.decode(packet);

        if (text == null) {
            System.out.println("\t>> Got null!");
            return;
        }

        Bukkit.getConsoleSender().sendMessage("\t>> Got text '" + text + "'");
        String newText = replacer.replaceAll(text, packetEvent.getPlayer());
        Bukkit.getConsoleSender().sendMessage("\t>> New text '" + newText + "'");

        // TODO: 20.10.2016 Replace the packet using packetEvent.setPacket or modify existing 
    }

    private boolean isNormalChatPacket(Packet packet) {
        ReflectResponse<Object> type = packet.get("b");
        System.out.println("\t>> " + type.isSuccessful() + ", " + type.isValuePresent() + ", " + type.getValue());

        return type.isSuccessful()
                  && type.isValuePresent()
                  && ((byte) type.getValue()) < 2; // 0: chat (chat box), 1: system message (chat box), 2: action bar
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent e) {
        PacketManager.getInstance().addListener(this, e.getPlayer());
        logger.info("Injected packet listener for " + e.getPlayer().getName());
    }

    private String listFields(Class<?> clazz, Object obj) {

        return Arrays.stream(clazz.getDeclaredFields()).map(field -> {
            try {
                field.setAccessible(true);
                return field.getType().getCanonicalName() + " " + field.getName() + " (" + field.get(obj) + ")";
            } catch (Exception e) {
                e.printStackTrace();
                return "ERR";
            }
        }).collect(Collectors.joining(", "));

    }

}
