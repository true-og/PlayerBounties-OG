package com.tcoded.playerbountiesplus.util;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.tcoded.playerbountiesplus.PlayerBountiesOG;

public class LangUtil {

    private static final String DEFAULT_LANG_CODE = "en_us";

    private final PlayerBountiesOG plugin;
    private final Logger logger;
    private final File overrideMessagesFile;
    private final FileConfiguration messages;
    private final String lang;
    private final boolean customLang;

    public enum SupportedLang {

        DE_DE, EN_GB, EN_US, EN_UWU, ES_ES, FR_FR, HI_IN, HU_HU, IT_IT, NL_NL, RU_RU, SE_SE, ZH_CN;

        public static SupportedLang find(String name) {

            if (name == null || name.isBlank()) {

                return null;

            }

            for (SupportedLang lang : SupportedLang.values()) {

                if (lang.name().equalsIgnoreCase(name)) {

                    return lang;

                }

            }

            return null;

        }

        public String fileName() {

            return this.name().toLowerCase(Locale.ROOT) + ".yml";

        }

    }

    public LangUtil(PlayerBountiesOG plugin, String configuredLang) {

        this.plugin = plugin;
        this.logger = plugin.getLogger();

        final SupportedLang resolvedLang = resolveSupportedLang(configuredLang);
        this.lang = resolvedLang.name().toLowerCase(Locale.ROOT);

        final String englishFilePath = "lang/" + SupportedLang.EN_US.fileName();
        final String langFilePath = "lang/" + resolvedLang.fileName();

        final FileConfiguration englishDefaults = loadBundledYaml(englishFilePath, true);
        final FileConfiguration selectedLangDefaults = resolvedLang == SupportedLang.EN_US ? englishDefaults
                : loadBundledYaml(langFilePath, false);

        if (resolvedLang != SupportedLang.EN_US) {

            selectedLangDefaults.setDefaults(englishDefaults);

        }

        this.overrideMessagesFile = new File(plugin.getDataFolder(), langFilePath);

        if (this.overrideMessagesFile.exists()) {

            this.messages = YamlConfiguration.loadConfiguration(this.overrideMessagesFile);
            this.messages.setDefaults(selectedLangDefaults);
            this.customLang = true;
            this.logger.warning("Using custom override language file: " + this.overrideMessagesFile.getPath());

        } else {

            this.messages = selectedLangDefaults;
            this.customLang = false;

        }

    }

    private SupportedLang resolveSupportedLang(String configuredLang) {

        final String normalized = configuredLang == null ? DEFAULT_LANG_CODE
                : configuredLang.trim().toUpperCase(Locale.ROOT);

        final SupportedLang supportedLang = SupportedLang.find(normalized);

        if (supportedLang != null) {

            return supportedLang;

        }

        this.logger.warning("Unsupported language in config.yml: " + configuredLang + ". Falling back to "
                + DEFAULT_LANG_CODE + ".");
        this.logger.info("You can contribute new languages by following the instructions at the top of config.yml.");

        return SupportedLang.EN_US;

    }

    private FileConfiguration loadBundledYaml(String path, boolean required) {

        final InputStream resource = this.plugin.getResource(path);

        if (resource == null) {

            if (required) {

                throw new IllegalStateException("Required bundled language file could not be found: " + path);

            }

            this.logger.warning("Bundled language file not found: " + path + ". Falling back to English.");
            return new YamlConfiguration();

        }

        final InputStream input = resource;
        final InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8);

        return YamlConfiguration.loadConfiguration(reader);

    }

    public String getColored(String key) {

        if (key == null || key.isBlank()) {

            return "";

        }

        final String value = this.messages.getString(key);

        if (value != null) {

            return value;

        }

        this.logger.warning("Missing language key: " + key + " (lang=" + this.lang + ")");

        return key;

    }

    public List<String> getColoredList(String key) {

        if (key == null || key.isBlank()) {

            return Collections.emptyList();

        }

        if (!this.messages.contains(key)) {

            this.logger.warning("Missing language list key: " + key + " (lang=" + this.lang + ")");
            return List.of(key);

        }

        final List<String> values = this.messages.getStringList(key);

        if (values == null || values.isEmpty()) {

            return Collections.emptyList();

        }

        return new ArrayList<>(values);

    }

    public boolean contains(String key) {

        return this.messages.contains(key);

    }

    public String getLang() {

        return this.lang;

    }

    public boolean isCustomLang() {

        return this.customLang;

    }

    public File getOverrideMessagesFile() {

        return this.overrideMessagesFile;

    }

}
