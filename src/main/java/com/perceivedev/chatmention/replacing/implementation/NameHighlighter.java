package com.perceivedev.chatmention.replacing.implementation;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

import com.perceivedev.chatmention.ChatMention;
import com.perceivedev.chatmention.replacing.Replacer;

/**
 * Highlights the name, if prefixed by "@"
 */
public class NameHighlighter extends AbstractReplacer {

    /**
     * Highlights your name in chat, if it is prefixed by a "@"
     *
     * @param prefix The prefix for the name
     * @param suffix The suffix for the name
     * @param playerNamePrefix The prefix before the player name, for it to be
     *            recognized
     * @param playerNameSuffix The suffix after the player name, for it to be
     *            recognized
     */
    public NameHighlighter(String prefix, String suffix, String playerNamePrefix, String playerNameSuffix) {
        setMatcherFunction(
                (s, player) -> s.equalsIgnoreCase(playerNamePrefix + player.getDisplayName() + playerNameSuffix) || s.equalsIgnoreCase(playerNamePrefix + player.getName() + playerNameSuffix));

        setReplacerFunction((context, player) -> {
            String newWord = prefix + context.getCurrentWord();
            if (!suffix.isEmpty()) {
                return newWord + suffix;
            }

            String lastColors = ChatColor.getLastColors(context.getWholeText().substring(0, context.getIndex() + 1));

            return newWord + lastColors;
        });
    }

    @Override
    public Replacer fromSection(ConfigurationSection section) {
        String prefix;
        if (!section.contains("prefix") || !section.isString("prefix")) {
            ChatMention.getPlugin(ChatMention.class).getLogger().warning("Section: '" + section.getCurrentPath() + "' misses key 'prefix'");
            return null;
        }
        prefix = section.getString("prefix");

        String suffix;
        if (!section.contains("suffix") || !section.isString("suffix")) {
            ChatMention.getPlugin(ChatMention.class).getLogger().warning("Section: '" + section.getCurrentPath() + "' misses key 'suffix'");
            return null;
        }
        suffix = section.getString("suffix");

        String playerNamePrefix;
        if (!section.contains("playerNamePrefix") || !section.isString("playerNamePrefix")) {
            ChatMention.getPlugin(ChatMention.class).getLogger().warning("Section: '" + section.getCurrentPath() + "' misses key 'playerNamePrefix'");
            return null;
        }
        playerNamePrefix = section.getString("playerNamePrefix");

        String playerNameSuffix;
        if (!section.contains("playerNameSuffix") || !section.isString("playerNameSuffix")) {
            ChatMention.getPlugin(ChatMention.class).getLogger().warning("Section: '" + section.getCurrentPath() + "' misses key 'playerNameSuffix'");
            return null;
        }
        playerNameSuffix = section.getString("playerNameSuffix");

        return new NameHighlighter(prefix, suffix, playerNamePrefix, playerNameSuffix);
    }
}
