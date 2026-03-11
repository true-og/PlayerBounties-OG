package com.tcoded.playerbountiesplus.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.tcoded.playerbountiesplus.PlayerBountiesOG;

import net.trueog.diamondbankog.api.DiamondBankAPIJava;
import net.trueog.gxui.GUIBase;
import net.trueog.gxui.GUIButton;
import net.trueog.gxui.GUIItem;
import net.trueog.utilitiesog.UtilitiesOG;

public class MainBountyGui extends GUIBase {

    private static final List<Integer> PYRAMID_SLOTS = List.of(4, 12, 13, 14, 20, 21, 22, 23, 24, 28, 29, 30, 31, 32,
            33, 34, 36, 37, 38, 39, 40, 41, 42, 43, 44);

    private static final int PREVIOUS_PAGE_SLOT = 47;
    private static final int PAGE_INFO_SLOT = 49;
    private static final int NEXT_PAGE_SLOT = 51;
    private static final int CLOSE_SLOT = 53;

    private final PlayerBountiesOG plugin;
    private final Player viewer;
    private final Supplier<List<BountyGuiEntry>> bountySupplier;
    private final DiamondBankAPIJava diamondBankAPI;
    private final int page;

    public MainBountyGui(PlayerBountiesOG plugin, Player viewer, Supplier<List<BountyGuiEntry>> bountySupplier,
            DiamondBankAPIJava diamondBankAPI)
    {

        this(plugin, viewer, bountySupplier, 0, diamondBankAPI);

    }

    private MainBountyGui(PlayerBountiesOG plugin, Player viewer, Supplier<List<BountyGuiEntry>> bountySupplier,
            int page, DiamondBankAPIJava diamondBankAPITransporter)
    {

        super(plugin, viewer, plugin.getLang().getColored("gui.main.title"), 54, true);

        this.plugin = plugin;
        this.viewer = viewer;
        this.bountySupplier = bountySupplier;
        this.diamondBankAPI = diamondBankAPITransporter;
        this.page = Math.max(0, page);

    }

    public void open() {

        super.open(false);

    }

    @Override
    public void setupItems() {

        final List<BountyGuiEntry> sortedBounties = getSortedBounties();
        final int maxPage = getMaxPage(sortedBounties);
        final int currentPage = Math.min(page, maxPage);

        addNavigationItems(currentPage, maxPage, sortedBounties.size());

        if (sortedBounties.isEmpty()) {

            addItem(22,
                    createStaticItem(Material.WRITABLE_BOOK, "&6&lNo Active Bounties",
                            List.of("&7Nobody currently has a bounty.", "&7Place one with your bounty command to get",
                                    "&7this leaderboard populated again.")));
            return;

        }

        final int startIndex = currentPage * PYRAMID_SLOTS.size();
        final int endIndex = Math.min(startIndex + PYRAMID_SLOTS.size(), sortedBounties.size());

        int pyramidIndex = 0;
        for (int i = startIndex; i < endIndex; i++) {

            final BountyGuiEntry entry = sortedBounties.get(i);
            final int globalRank = i + 1;

            addItem(PYRAMID_SLOTS.get(pyramidIndex), createBountyItem(entry, globalRank));

            pyramidIndex++;

        }

    }

    private void addNavigationItems(int currentPage, int maxPage, int totalBounties) {

        if (currentPage > 0) {

            addItem(PREVIOUS_PAGE_SLOT, createButtonItem(Material.ARROW, "&a&lPrevious Page",
                    List.of("&7Go to page &f" + currentPage), new GUIButton()
                    {

                        @Override
                        public boolean leftClick() {

                            return openPage(currentPage - 1);

                        }

                        @Override
                        public boolean leftClickShift() {

                            return openPage(currentPage - 1);

                        }

                        @Override
                        public boolean rightClick() {

                            return openPage(currentPage - 1);

                        }

                        @Override
                        public boolean rightClickShift() {

                            return openPage(currentPage - 1);

                        }

                    }));

        } else {

            addItem(PREVIOUS_PAGE_SLOT,
                    createStaticItem(Material.GRAY_DYE, "&7Previous Page", List.of("&8Already on the first page.")));

        }

        addItem(PAGE_INFO_SLOT, createStaticItem(Material.BOOK, "&3&lPage " + (currentPage + 1) + "/" + (maxPage + 1),
                List.of("&6Active bounties: &c" + totalBounties)));

        if (currentPage < maxPage) {

            addItem(NEXT_PAGE_SLOT, createButtonItem(Material.ARROW, "&a&lNext Page",
                    List.of("&7Go to page &f" + (currentPage + 2)), new GUIButton()
                    {

                        @Override
                        public boolean leftClick() {

                            return openPage(currentPage + 1);

                        }

                        @Override
                        public boolean leftClickShift() {

                            return openPage(currentPage + 1);

                        }

                        @Override
                        public boolean rightClick() {

                            return openPage(currentPage + 1);

                        }

                        @Override
                        public boolean rightClickShift() {

                            return openPage(currentPage + 1);

                        }

                    }));

        } else {

            addItem(NEXT_PAGE_SLOT,
                    createStaticItem(Material.GRAY_DYE, "&7Next Page", List.of("&8Already on the last page.")));

        }

        addItem(CLOSE_SLOT,
                createButtonItem(Material.BARRIER, "&c&lClose", List.of("&7Close the bounty menu."), new GUIButton()
                {

                    @Override
                    public boolean leftClick() {

                        viewer.closeInventory();

                        return true;

                    }

                    @Override
                    public boolean leftClickShift() {

                        viewer.closeInventory();

                        return true;

                    }

                    @Override
                    public boolean rightClick() {

                        viewer.closeInventory();

                        return true;

                    }

                    @Override
                    public boolean rightClickShift() {

                        viewer.closeInventory();

                        return true;

                    }

                }));

    }

    private boolean openPage(int targetPage) {

        new MainBountyGui(plugin, viewer, bountySupplier, targetPage, diamondBankAPI).open();

        return true;

    }

    private GUIItem createBountyItem(BountyGuiEntry entry, int rank) {

        final String displayName = safeDisplayName(entry.displayName());
        final ArrayList<String> lore = new ArrayList<>();

        lore.add("&eBounty: &b" + formatBounty(entry.bountyDiamonds()));
        lore.add("&7Click to increase this player's bounty.");
        lore.add("&8You will be prompted in chat for the amount.");
        lore.add("");
        lore.add("&6Claiming the bounty will give you a 50% chance of beheading your victim!");

        final GUIItem item;
        if (entry.targetName() != null && !entry.targetName().isBlank()) {

            item = new GUIItem(Material.PLAYER_HEAD, 1, " &f#" + rank + " " + displayName, entry.targetName());

        } else {

            item = new GUIItem(Material.PAPER, 1, "&6&l#" + rank + " " + displayName);

        }

        item.lore(lore);
        item.setButton(new GUIButton() {

            @Override
            public boolean leftClick() {

                return promptIncrease(entry);

            }

            @Override
            public boolean leftClickShift() {

                return promptIncrease(entry);

            }

            @Override
            public boolean rightClick() {

                return promptIncrease(entry);

            }

            @Override
            public boolean rightClickShift() {

                return promptIncrease(entry);

            }

        });
        item.setPlayErrorSound(false);

        return item;

    }

    private boolean promptIncrease(BountyGuiEntry entry) {

        if (entry.targetUuid() == null) {

            UtilitiesOG.trueogMessage(viewer, plugin.getLang().getColored("command.bounty.add.player-not-found"));
            return true;

        }

        final String targetName = StringUtils.defaultIfBlank(entry.targetName(), "Unknown Player");
        plugin.getBountyIncreasePromptListener().prompt(viewer.getUniqueId(), entry.targetUuid(), targetName);

        final String promptMessage = plugin.getLang().getColored("command.bounty.add.prompt").replace("{target}",
                safeDisplayName(entry.displayName()));
        UtilitiesOG.trueogMessage(viewer, promptMessage);

        viewer.closeInventory();

        return true;

    }

    private String formatBounty(double bountyDiamonds) {

        final long shards = diamondBankAPI.diamondsToShards(bountyDiamonds);

        return diamondBankAPI.shardsToDiamonds(shards) + " Diamonds";

    }

    private GUIItem createStaticItem(Material material, String displayName, List<String> lore) {

        final GUIItem item = new GUIItem(material, 1, displayName);
        item.lore(new ArrayList<>(lore));
        item.setPlayErrorSound(false);

        return item;

    }

    private GUIItem createButtonItem(Material material, String displayName, List<String> lore, GUIButton button) {

        final GUIItem item = new GUIItem(material, 1, displayName);
        item.lore(new ArrayList<>(lore));
        item.setButton(button);
        item.setPlayErrorSound(false);

        return item;

    }

    private List<BountyGuiEntry> getSortedBounties() {

        final List<BountyGuiEntry> supplied = bountySupplier.get();
        if (supplied == null || supplied.isEmpty()) {

            return Collections.emptyList();

        }

        final List<BountyGuiEntry> sorted = new ArrayList<>(supplied);
        sorted.removeIf(entry -> entry == null || entry.bountyDiamonds() <= 0D);
        sorted.sort(Comparator.comparingDouble(BountyGuiEntry::bountyDiamonds).reversed()
                .thenComparing(entry -> safeName(entry.targetName()), String.CASE_INSENSITIVE_ORDER));

        return sorted;

    }

    private int getMaxPage(List<BountyGuiEntry> bounties) {

        if (bounties.isEmpty()) {

            return 0;

        }

        return (bounties.size() - 1) / PYRAMID_SLOTS.size();

    }

    private String safeName(String name) {

        if (name == null || name.isBlank()) {

            return "Unknown Player";

        }

        return name;

    }

    private String safeDisplayName(String displayName) {

        if (displayName == null || displayName.isBlank()) {

            return "&fUnknown Player";

        }

        return displayName;

    }

    public static record BountyGuiEntry(UUID targetUuid, String targetName, String displayName, double bountyDiamonds) {
    }

}