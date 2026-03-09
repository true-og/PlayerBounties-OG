# PlayerBounties-OG

## Overview
PlayerBounties-OG is a hard fork of [PlayerBountiesPlus](https://github.com/TechnicallyCoded/PlayerBountiesPlus) that lets players set and claim bounties while blocking teammates from claiming rewards. Hooks into [DiamondBank-OG](https://github.com/true-og/DiamondBank-OG) for economy handling and supports [SimpleClans](https://github.com/true-og/SimpleClans). Built for Purpur 1.19.4.

## Features
- Clan-aware bounty system that prevents teammates from claiming each other's bounties.
- Integrates with [DiamondBank-OG](https://github.com/true-og/DiamondBank-OG) and supports [SimpleClans](https://github.com/true-og/SimpleClans) for team detection.
- Inventory GUI for viewing bounties.
- Customizable odds for receiving player head from bounty victim.
- Localized messages in multiple languages.
- Uses [LuckPerms](https://github.com/true-og/LuckPerms) for player name formatting.

## Commands
- `/bounty set <player> <amount>` – set a bounty on a player
- `/bounty top` – list the top bounties
- `/bounty check <player>` – check a player's bounty
- `/pbp reload` – reload configuration and messages
- `/pbp version` – display plugin version
- `/pbp bounty set <player> <amount>` – admin set a bounty
- `/pbp bounty add <player> <amount>` – add diamonds to a bounty
- `/pbp bounty remove <player> <amount>` – remove diamonds from a bounty
- `/pbp bounty delete <player>` – delete a bounty
- `/pbp bounty get <player>` – view a bounty

## Permissions
| Permission | Default |
| --- | --- |
| `playerbountiesplus.command.bounty` | all |
| `playerbountiesplus.command.bounty.set` | all |
| `playerbountiesplus.event.claim` | all |
| `playerbountiesplus.command.admin` | op |
| `playerbountiesplus.command.admin.version` | op |
| `playerbountiesplus.command.admin.reload` | op |
| `playerbountiesplus.command.admin.bounty.set` | op |
| `playerbountiesplus.command.admin.bounty.add` | op |
| `playerbountiesplus.command.admin.bounty.remove` | op |
| `playerbountiesplus.command.admin.bounty.delete` | op |
| `playerbountiesplus.command.admin.bounty.get` | op |

## Developer Notes
- Commands reside in `com.tcoded.playerbountiesplus.command` with subpackages for bounty and admin actions.
- Event listeners like `DeathListener` handle bounty claim logic.
- Hooks under `com.tcoded.playerbountiesplus.hook` integrate with DiamondBank-OG and SimpleClans.
- Inventory GUIs in `com.tcoded.playerbountiesplus.gui` present bounty data.
- `BountyDataManager` manages persistence of bounties.

