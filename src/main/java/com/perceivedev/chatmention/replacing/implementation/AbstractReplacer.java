package com.perceivedev.chatmention.replacing.implementation;

import java.util.function.BiFunction;

import org.bukkit.entity.Player;

import com.perceivedev.chatmention.replacing.Replacer;
import com.perceivedev.perceivecore.util.TextUtils;

/**
 * Highlights the name
 */
public abstract class AbstractReplacer implements Replacer {

    private BiFunction<ReplacerContext, Player, String> replacerFunction;
    private BiFunction<String, Player, String>          matcherFunction;

    /**
     * @param replacerFunction The replacer function
     * @param matcherFunction The matching function. The returned String is either null for no match or the match
     */
    public AbstractReplacer(BiFunction<ReplacerContext, Player, String> replacerFunction, BiFunction<String, Player, String> matcherFunction) {
        this.replacerFunction = replacerFunction;
        this.matcherFunction = matcherFunction;
    }

    /**
     * Set functions!
     */
    protected AbstractReplacer() {
    }

    /**
     * Sets the matcher function
     *
     * @param matcherFunction The new matcher function
     */
    protected void setMatcherFunction(BiFunction<String, Player, String> matcherFunction) {
        this.matcherFunction = matcherFunction;
    }

    /**
     * Sets the replacer function
     *
     * @param replacerFunction The new replacer function
     */
    protected void setReplacerFunction(BiFunction<ReplacerContext, Player, String> replacerFunction) {
        this.replacerFunction = replacerFunction;
    }

    @Override
    public String apply(String message, Player receivingPlayer) {
        // String[] words = message.split("[\\s,.!?;:]+");

        String newMessage = message;

        String matched = matcherFunction.apply(message, receivingPlayer);
        if (matched != null) {
            String replaced = replacerFunction.apply(new ReplacerContext(matched, message, message.indexOf(matched)), receivingPlayer);
            newMessage = TextUtils.colorize(newMessage.replace(matched, replaced));
        }

        return newMessage;
    }

    /**
     * The context for replacing
     */
    protected static class ReplacerContext {
        private String currentWord;
        private String wholeText;
        private int    index;

        /**
         * @param currentWord The current word
         * @param wholeText The whole text
         * @param index The current index
         */
        private ReplacerContext(String currentWord, String wholeText, int index) {
            this.currentWord = currentWord;
            this.wholeText = wholeText;
            this.index = index;
        }

        /**
         * Returns the current index
         *
         * @return The index
         */
        protected int getIndex() {
            return index;
        }

        /**
         * Sets the new index
         *
         * @param index The new index
         */
        protected void setIndex(int index) {
            this.index = index;
        }

        /**
         * Returns the current word
         *
         * @return The current word
         */
        protected String getCurrentWord() {
            return currentWord;
        }

        /**
         * Sets the new current word
         *
         * @param currentWord The nee current word
         */
        protected void setCurrentWord(String currentWord) {
            this.currentWord = currentWord;
        }

        /**
         * Returns the whole text
         *
         * @return The whole text
         */
        protected String getWholeText() {
            return wholeText;
        }

        /**
         * Sets the new whole text
         *
         * @param wholeText The new whole text
         */
        protected void setWholeText(String wholeText) {
            this.wholeText = wholeText;
        }
    }
}
