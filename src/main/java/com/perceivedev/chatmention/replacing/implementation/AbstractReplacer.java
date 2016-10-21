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
    private BiFunction<String, Player, Boolean>         matcherFunction;

    /**
     * @param replacerFunction The replacer function
     * @param matcherFunction The matching function
     */
    public AbstractReplacer(BiFunction<ReplacerContext, Player, String> replacerFunction, BiFunction<String, Player, Boolean> matcherFunction) {
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
    protected void setMatcherFunction(BiFunction<String, Player, Boolean> matcherFunction) {
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
        String[] words = message.split(" ");

        String newMessage = message;

        ReplacerContext context = new ReplacerContext("", message, 0);
        for (String word : words) {
            context.setCurrentWord(word);
            if (matcherFunction.apply(word, receivingPlayer)) {
                newMessage = newMessage.replace(
                          word,
                          TextUtils.colorize(replacerFunction.apply(context, receivingPlayer))
                );
            }
            context.setWholeText(newMessage);
            context.setIndex(context.getIndex() + word.length());
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
        private void setIndex(int index) {
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
        private void setCurrentWord(String currentWord) {
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
        private void setWholeText(String wholeText) {
            this.wholeText = wholeText;
        }
    }
}
