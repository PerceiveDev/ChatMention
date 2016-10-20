package com.perceivedev.chatmention.replacing;

import org.bukkit.entity.Player;

/**
 * Replaces the name in the message or colors it.
 */
public interface Replacer {

    /**
     * Modifies the message, colors it or something
     *
     * @param message The message to modify
     * @param receivingPlayer The player who receives the message
     *
     * @return The modified message
     */
    String apply(String message, Player receivingPlayer);
}
