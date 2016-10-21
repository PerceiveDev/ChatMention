package com.perceivedev.chatmention.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

import org.bukkit.configuration.ConfigurationSection;

import com.perceivedev.chatmention.ChatMention;
import com.perceivedev.chatmention.replacing.Replacer;

/**
 * Parses the config
 */
public class ConfigParser {

    private ConfigurationSection section;

    /**
     * Creates a parser for the given section
     *
     * @param section The section to parse
     */
    public ConfigParser(ConfigurationSection section) {
        this.section = section;
    }

    /**
     * Parses the replacer and returns them
     *
     * @return The parsed replacer. Empty if none.
     */
    public List<Replacer> parse() {
        List<Replacer> replacerList = new ArrayList<>();

        for (String key : section.getKeys(false)) {
            ConfigurationSection replacerSection = section.getConfigurationSection(key);

            String type = replacerSection.getString("type");
            Optional<Replacer> replacerBase = ChatMention.getPlugin(ChatMention.class).getMessageReplacer().getReplacer(type);

            if (!replacerBase.isPresent()) {
                ChatMention.getPlugin(ChatMention.class).getLogger().log(Level.WARNING,
                          "Unknown replacer type: '" + type + "' in key " + key);
                continue;
            }

            Replacer replacer = replacerBase.get().fromSection(replacerSection);

            if (replacer != null) {
                replacerList.add(replacer);
            }
        }

        return replacerList;
    }
}
