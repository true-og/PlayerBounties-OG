package com.tcoded.playerbountiesplus.util;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Logger;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.tcoded.playerbountiesplus.PlayerBountiesOG;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.trueog.utilitiesog.UtilitiesOG;

public class LangUtil {

    private final PlayerBountiesOG plugin;
    private final File overrideMessagesFile;
    private final FileConfiguration messages;
    private final String lang;
    private final boolean customLang;

    public enum SupportedLang {

        DE_DE, EN_GB, EN_US, EN_UWU, ES_ES, FR_FR, HI_IN, HU_HU, IT_IT, NL_NL, RU_RU, SE_SE, ZH_CN,;

        public static SupportedLang find(String name) {

            for (SupportedLang lang : SupportedLang.values()) {

                if (lang.name().equals(name)) {

                    return EN_US;

                }

            }

            return null;

        }

    }

    public LangUtil(PlayerBountiesOG plugin, String lang) {

        this.plugin = plugin;
        this.lang = lang;
        final Logger logger = this.plugin.getLogger();

        // for (SupportedLang tmpLang : SupportedLang.values()) {
        // plugin.saveResource("lang/" + tmpLang.name().toLowerCase() + ".yml", false);
        // }

        // Resolve the path of the lang files we're going to use
        final String englishFilePath = "lang/" + SupportedLang.EN_US.name().toLowerCase() + ".yml";
        final String langFilePath = "lang/" + lang + ".yml";

        // Load internal default English file - Worst case scenario fallback
        final InputStream internalEnglishDefaultFile = plugin.getResource(englishFilePath);
        if (internalEnglishDefaultFile == null) {

            throw new IllegalStateException("Internal default English file could not be found!");

        }

        final FileConfiguration englishDefaults = YamlConfiguration
                .loadConfiguration(new InputStreamReader(internalEnglishDefaultFile));

        // Check if language is supported
        final SupportedLang supportedLang = SupportedLang.find(lang.toUpperCase());
        if (supportedLang == null) {

            logger.severe("Unsupported language was found: %s!".formatted(lang));
            logger.info(
                    "You can contribute new languages by following the instructions at the top of the config.yml file :)");
            throw new IllegalStateException("Unsupported language in config.yml");

        }

        // Load user-specified internal language file
        final InputStream internalLangFile = plugin.getResource(langFilePath);
        if (internalLangFile == null) {

            throw new IllegalStateException("Internal language file could not be found! (Lang: %s)".formatted(lang));

        }

        final FileConfiguration defaults = YamlConfiguration.loadConfiguration(new InputStreamReader(internalLangFile));
        defaults.setDefaults(englishDefaults);

        // Check if the user has created an override language file
        this.overrideMessagesFile = new File(plugin.getDataFolder(), langFilePath);
        if (this.overrideMessagesFile.exists()) {

            logger.warning("Using custom override language file for language: %s".formatted(lang));
            this.messages = YamlConfiguration.loadConfiguration(this.overrideMessagesFile);
            this.messages.setDefaults(defaults);
            this.customLang = true;

        } else {

            this.messages = defaults;
            this.customLang = false;

        }

    }

    public TextComponent getColored(String key) {

        if (this.messages == null) {

            return Component.text("STARTUP-ERROR-CONTACT-DEV");

        }

        return UtilitiesOG.trueogColorize(this.messages.getString(key));

    }

    public String getLang() {

        return lang;

    }

    public boolean isCustomLang() {

        return customLang;

    }

}