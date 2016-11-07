package com.perceivedev.chatmention.replacing;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

/**
 * Replaces the name in the message or colors it.
 */
public interface Replacer {

    /**
     * Builds a new Replacer from the configuration section
     *
     * @param section The {@link ConfigurationSection} to read from
     *
     * @return A new Replacer, built from the given ConfigurationSection. May be
     *         null, if an error occured.
     */
    Replacer fromSection(ConfigurationSection section);

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
