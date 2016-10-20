package com.perceivedev.chatmention.replacing.implementation;

import org.bukkit.ChatColor;

/**
 * Highlights the name, if prefixed by "@"
 */
public class NameHighlighter extends AbstractReplacer {

    /**
     * Highlights your name in chat, if it is prefixed by a "@"
     *
     * @param prefix The prefix for the name
     * @param suffix The suffix for the name
     */
    public NameHighlighter(String prefix, String suffix) {

        setMatcherFunction((s, player) ->
                  s.equalsIgnoreCase("@" + player.getDisplayName())
                            || s.equalsIgnoreCase("@" + player.getName())
        );

        setReplacerFunction((context, player) -> {
            String newWord = prefix + context.getCurrentWord();
            if (!suffix.isEmpty()) {
                return newWord + suffix;
            }

            String lastColors = ChatColor.getLastColors(context.getWholeText().substring(0, context.getIndex() + 1));

            return newWord + lastColors;
        });
    }
}
