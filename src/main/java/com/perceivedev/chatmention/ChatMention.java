package com.perceivedev.chatmention;

import static com.perceivedev.perceivecore.reflection.ReflectionUtil.$;

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

import com.perceivedev.perceivecore.packet.Packet;
import com.perceivedev.perceivecore.packet.PacketEvent;
import com.perceivedev.perceivecore.packet.PacketInjector;
import com.perceivedev.perceivecore.packet.PacketListener;
import com.perceivedev.perceivecore.reflection.ReflectionUtil;
import com.perceivedev.perceivecore.reflection.ReflectionUtil.ReflectResponse;

public class ChatMention extends JavaPlugin implements PacketListener, Listener {

    private static final String IDENTIFIER = hiddenText("ChatMention");
    private Class<?>            targetClass;

    private Logger              logger;

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
        logger.info("Chat packet found!");

        Packet packet = packetEvent.getPacket();
        System.out.println("Packet class: " + packet.getPacketClass().getSimpleName());
        ReflectResponse<Object> type = packet.get("b");
        System.out.println(type.isSuccessful() + ", " + type.isValuePresent() + ", " + ((byte) type.getValue()));
        if (!type.isSuccessful() || !type.isValuePresent() || ((byte) type.getValue()) != 1) {
            return;
        }

        logger.info("Correct type found!");

        ReflectResponse<Object> chatObject = packet.get("a");
        System.out.println("Packet class: " + listFields(packet.getPacketClass(), packet.getNMSPacket()));
        if (!chatObject.isSuccessful() || !chatObject.isValuePresent()) {
            logger.severe("Failed to get chatObject! (" + chatObject.getResultType().toString() + ", " + chatObject.getValue() + ")");
            return;
        }

        String text = $(chatObject.getValue()).getMethod("toPlainText").invoke().getValue().toString();

        System.out.println("Chat message sending: " + text);
        
        text = $(chatObject.getValue()).getMethod("getText").invoke().getValue().toString();
        
        System.out.println("Chat message sending 2: " + text);

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent e) {
        PacketInjector injector = new PacketInjector(e.getPlayer());
        injector.addPacketListener(this);
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
