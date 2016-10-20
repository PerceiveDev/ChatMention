package com.perceivedev.chatmention.replacing;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.Player;

/**
 * Replaces the message
 */
public class MessageReplacer {

    private Set<Replacer> replacerSet = new HashSet<>();

    /**
     * Adds an replacer
     *
     * @param replacer The replacer to add
     */
    public void addReplacer(Replacer replacer) {
        replacerSet.add(replacer);
    }

    /**
     * Replaces the message
     *
     * @param message The Message to replace
     * @param player The player to replace it for
     *
     * @return The replaced message
     */
    public String replaceAll(String message, Player player) {
        String replacementMessage = message;
        for (Replacer replacer : replacerSet) {
            replacementMessage = replacer.apply(message, player);
        }
        return replacementMessage;
    }
}
