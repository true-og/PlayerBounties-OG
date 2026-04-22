# Changelog

## 1.2

- Unified bounty head formatting across item names, armor stand labels, and action bar messaging into a single canonical line showing victim, slayer, and diamond reward.
- Removed redundant bounty head lore now that the item display name carries full claim information.
- `/bounty check` now resolves offline players who still have active bounties, and tab completion suggests their names.
- Bounty check output now formats the amount through the DiamondBank shards-to-diamonds formatter for consistency with head labels and announcements.
- Bounty leaderboard GUI now reads the beheading chance from the `bounty-head-drop-chance` config value instead of hardcoding 50%.
- Added the missing `playerbountiesog.command.admin.bounty.get` permission entry and Italian translation listing; removed dead `death.announce-claimed` lang key.
- Replaced raw stack traces in the bounty data manager with logger output and pruned dead plugin-discovery code from plugin startup.

## 1.1

- Added bounty amounts to bounty head item titles so the selected hotbar item text shows the reward without using the action bar.
- Updated placed bounty head hover labels to render on separate lines for clearer in-world head information.
- Added right-click action bar messaging for bounty heads that shows who claimed the bounty for both hotbar items and placed heads.
- Stored claimant metadata on bounty heads so claim information survives item drops and place/break cycles.
