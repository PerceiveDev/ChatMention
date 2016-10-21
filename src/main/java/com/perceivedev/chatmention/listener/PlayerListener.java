package com.perceivedev.chatmention.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.perceivedev.perceivecore.packet.PacketListener;
import com.perceivedev.perceivecore.packet.PacketManager;

/**
 * Listens for Player related events
 */
public class PlayerListener implements Listener {

    private PacketListener listener;

    /**
     * @param listener The PacketListener to use
     */
    public PlayerListener(PacketListener listener) {
        this.listener = listener;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent e) {
        PacketManager.getInstance().addListener(listener, e.getPlayer());
    }
}
