package com.perceivedev.chatmention.replacing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bukkit.entity.Player;

import com.perceivedev.chatmention.replacing.implementation.NameHighlighter;

/**
 * Replaces the message
 */
public class MessageReplacer {

    private Map<String, Replacer> registeredReplacer = new HashMap<>();

    private List<Replacer> replacerList = new ArrayList<>();

    {
        // add defaults
        registerReplacer("name highlighter", new NameHighlighter("", "", "", ""));
    }

    /**
     * Registers the Replacer
     *
     * @param name The name of the replacer
     * @param replacer The replacer
     */
    public void registerReplacer(String name, Replacer replacer) {
        registeredReplacer.put(name, replacer);
    }

    /**
     * Gets a replacer by it's name
     *
     * @param name The name of the replacer
     *
     * @return The replacer with that name
     */
    public Optional<Replacer> getReplacer(String name) {
        return Optional.ofNullable(registeredReplacer.get(name));
    }

    /**
     * Adds an replacer
     *
     * @param replacer The replacer to add
     */
    public void addReplacer(Replacer replacer) {
        replacerList.add(replacer);
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
        for (Replacer replacer : replacerList) {
            replacementMessage = replacer.apply(replacementMessage, player);
        }
        return replacementMessage;
    }
}
