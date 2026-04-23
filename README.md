# PlayerBounties-OG

## Overview
PlayerBounties-OG is a hard fork of [PlayerBountiesPlus](https://github.com/TechnicallyCoded/PlayerBountiesPlus) that lets players set and claim bounties while blocking teammates from claiming rewards. Hooks into [DiamondBank-OG](https://github.com/true-og/DiamondBank-OG) for economy handling and supports [SimpleClans](https://github.com/true-og/SimpleClans) and API-compatible forks such as Unions-OG. Built for Purpur 1.19.4.

## Features
- Clan-aware bounty system that prevents teammates from claiming each other's bounties.
- Inventory GUI for viewing bounties.
- Customizable odds for receiving player head from bounty victim.
- Localized messages in multiple languages.
- Integrates [CoreProtect-OG](https://github.com/true-og/CoreProtect-OG) for player head rollback injection.
- Integrates [DiamondBank-OG](https://github.com/true-og/DiamondBank-OG) for currency.
- Integrates [LuckPerms](https://github.com/true-og/LuckPerms) for player name and rank formatting.
- Integrates [SimpleClans](https://github.com/true-og/SimpleClans) and compatible forks such as Unions-OG for team detection.

## Commands
- `/bounty set <player> <amount>` – set a bounty on a player
- `/bounty add <player> <amount>` – add diamonds to an existing bounty
- `/bounty top` – list the top bounties
- `/bounty check <player>` – check a player's bounty (online or offline)
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
| `playerbountiesog.command.bounty` | all |
| `playerbountiesog.command.bounty.set` | all |
| `playerbountiesog.command.bounty.add` | all |
| `playerbountiesog.event.claim` | all |
| `playerbountiesog.command.admin` | op |
| `playerbountiesog.command.admin.version` | op |
| `playerbountiesog.command.admin.reload` | op |
| `playerbountiesog.command.admin.bounty.set` | op |
| `playerbountiesog.command.admin.bounty.add` | op |
| `playerbountiesog.command.admin.bounty.remove` | op |
| `playerbountiesog.command.admin.bounty.delete` | op |
| `playerbountiesog.command.admin.bounty.get` | op |

## Developer Notes
- Commands reside in `com.tcoded.playerbountiesplus.command` with subpackages for bounty and admin actions.
- Event listeners like `DeathListener` handle bounty claim logic.
- Hooks under `com.tcoded.playerbountiesplus.hook` integrate with DiamondBank-OG and supported clan/team plugins such as SimpleClans and Unions-OG.
- Inventory GUIs in `com.tcoded.playerbountiesplus.gui` present bounty data.
- `BountyDataManager` manages persistence of bounties.
