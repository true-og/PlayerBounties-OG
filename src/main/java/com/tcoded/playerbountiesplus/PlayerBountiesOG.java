package com.tcoded.playerbountiesplus;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.collect.ImmutableList;
import com.tcoded.playerbountiesplus.command.BountyCommand;
import com.tcoded.playerbountiesplus.command.PlayerBountiesPlusAdminCmd;
import com.tcoded.playerbountiesplus.hook.currency.DiamondBankOGHook;
import com.tcoded.playerbountiesplus.hook.currency.EconomyHook;
import com.tcoded.playerbountiesplus.hook.placeholder.PlaceholderHook;
import com.tcoded.playerbountiesplus.hook.team.TeamHook;
import com.tcoded.playerbountiesplus.listener.DeathListener;
import com.tcoded.playerbountiesplus.listener.GuiListener;
import com.tcoded.playerbountiesplus.manager.BountyDataManager;
import com.tcoded.playerbountiesplus.util.LangUtil;

import net.trueog.diamondbankog.api.DiamondBankAPIJava;
import net.trueog.utilitiesog.UtilitiesOG;

public final class PlayerBountiesOG extends JavaPlugin {

    // Plugin instance declaration.
    private static PlayerBountiesOG instance;

    public static PlayerBountiesOG getInstance() {

        return instance;

    }

    public static String getPrefix() {

        return "&7[&cPlayerBounties&f-&4OG&7] ";

    }

    // Utilities.
    private LangUtil langUtil;

    // Managers.
    private BountyDataManager bountyDataManager;

    // Hooks.
    private EconomyHook ecoHook;
    private TeamHook teamHook;
    private PlaceholderHook placeholderHook;

    // DiamondBank-OG Economy.
    private DiamondBankAPIJava diamondBankAPI;

    public PlayerBountiesOG() {

        instance = this;

    }

    @Override
    public void onEnable() {

        // Config
        saveDefaultConfig();

        // Initialize the DiamondBank-OG API.
        final RegisteredServiceProvider<DiamondBankAPIJava> provider = getServer().getServicesManager()
                .getRegistration(DiamondBankAPIJava.class);

        // If the DiamondBank-OG API failed to initialize, do this...
        if (provider == null) {

            // Tell Bukkit to disable this plugin, and inform the console.
            disableSelf("DiamondBank-OG API is null – disabling " + getPluginMeta().getName() + "!");
            return;

        }

        // Assign the active instance of DiamondBank-OG to the API handler.
        this.diamondBankAPI = provider.getProvider();

        // Utils.
        this.reloadLang();

        // Managers.
        this.bountyDataManager = new BountyDataManager(this);
        this.bountyDataManager.init();

        // Economy Hooks.
        registerDefaultEcoHooks();

        // Team Hooks.
        this.teamHook = TeamHook.findTeamHook(this);
        if (this.teamHook == null) {

            getLogger().warning(
                    "There is no supported team/clan/party plugin on the server! Feel free to request support for the plugin you use on GitHub or Discord!");

        }

        // Placeholder Hooks.
        this.placeholderHook = PlaceholderHook.findPlaceholderHook(this);
        this.placeholderHook.enable();

        // Commands.
        final PluginCommand bountyCmd = this.getCommand("bounty");
        if (bountyCmd != null) {

            final BountyCommand bountyExec = new BountyCommand(this);
            bountyCmd.setExecutor(bountyExec);
            bountyCmd.setTabCompleter(bountyExec);

        }

        final PluginCommand adminCmd = this.getCommand("playerbountiesplusadmin");
        if (adminCmd != null) {

            final PlayerBountiesPlusAdminCmd adminExec = new PlayerBountiesPlusAdminCmd(this);
            adminCmd.setExecutor(adminExec);
            adminCmd.setTabCompleter(adminExec);

        }

        // Listeners.
        this.getServer().getPluginManager().registerEvents(new DeathListener(this), this);
        this.getServer().getPluginManager().registerEvents(new GuiListener(), this);

        final List<Plugin> plugins = ImmutableList.copyOf(this.getServer().getPluginManager().getPlugins());
        findPluginWithQuery(plugins, "team");
        findPluginWithQuery(plugins, "teams");
        findPluginWithQuery(plugins, "clan");
        findPluginWithQuery(plugins, "clans");
        findPluginWithQuery(plugins, "party", "voteparty");
        findPluginWithQuery(plugins, "parties");
        findPluginWithQuery(plugins, "guild");
        findPluginWithQuery(plugins, "guilds");

    }

    public void reloadLang() {

        this.langUtil = new LangUtil(this, this.getConfig().getString("lang", "en_us").toLowerCase());

    }

    @Override
    public void onDisable() {

        HandlerList.unregisterAll(this);
        this.placeholderHook.disable();

    }

    public EconomyHook getEcoHook() {

        return this.ecoHook;

    }

    public TeamHook getTeamHook() {

        return teamHook;

    }

    public PlaceholderHook getPlaceholderHook() {

        return placeholderHook;

    }

    public BountyDataManager getBountyDataManager() {

        return bountyDataManager;

    }

    public LangUtil getLang() {

        return this.langUtil;

    }

    // Utils.
    private static void findPluginWithQuery(List<Plugin> plugins, String pluginNameQuery, String... excludeStrings) {

        final String firstPluginFound = plugins.stream().filter(p -> {

            final String lowerName = p.getName().toLowerCase();
            // If the plugin name doesn't contain the query, skip.
            if (!lowerName.contains(pluginNameQuery)) {

                return false;

            }

            // If the plugin name contains any of the exclude strings, skip.
            for (String excludeString : excludeStrings) {

                if (lowerName.contains(excludeString)) {

                    return false;

                }

            }

            return true;

        }).findFirst().map(p -> {

            final List<String> authors = p.getPluginMeta().getAuthors();

            return p.getName() + " (" + (authors.isEmpty() ? "N/A" : authors.get(0)) + ")";

        }).orElse(null);

        if (firstPluginFound == null) {

            return;

        }

    }

    private void registerDefaultEcoHooks() {

        final ServicesManager servicesManager = getServer().getServicesManager();

        // De-register old hook.
        servicesManager.unregisterAll(this);

        // Initialize the DiamondBank-OG hook with the active plugin instance.
        final DiamondBankOGHook diamondBankOGHook = new DiamondBankOGHook(getInstance(), diamondBankAPI);

        // Register the DiamondBank-OG hook as the active economy hook.
        servicesManager.register(EconomyHook.class, diamondBankOGHook, this, ServicePriority.Low);

    }

    // Helps this plugin kill itself gracefully (in minecraft).
    public static void disableSelf(String reason) {

        // Run a meta-task on the Bukkit server outside of the context of this plugin.
        Bukkit.getScheduler().runTask(Bukkit.getServer().getPluginManager().getPlugin("PlayerBounties-OG"), () -> {

            // Attempt to fetch the active instance of this plugin.
            final PlayerBountiesOG pluginInstance = getInstance();

            // If this plugin is already disabled, do this...
            if (!pluginInstance.isEnabled()) {

                // Do nothing, task already completed.
                return;

            }

            // Inform console of this plugin being disabled.
            UtilitiesOG.logToConsole(getPrefix(), reason);

            // Commit sudoku.
            Bukkit.getPluginManager().disablePlugin(pluginInstance);

        });

    }

}