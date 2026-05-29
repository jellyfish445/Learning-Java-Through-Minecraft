# DMMC — Upgraded Addon

A NeoForge 1.21.1 addon bridging **Upgraded**, **DivineRPG**, and **Occultism** into a unified combat progression system.

## What it does

- **Kill DivineRPG bosses** → drops a `boss_upgrade` item (type + tier encoded via data components)
- **Occultism rituals** → craft slot tokens and apply upgrade tokens
- **Right-click tokens onto gear** → adds slots or applies stat upgrades as attribute modifiers

See the [design document](docs/design.md) for full details.

## Setup

1. Clone the repo
2. Copy mod jars into `libs/` — see `libs/README.md`
3. `./gradlew build`
4. Output jar is in `build/libs/`

## Dependencies

| Mod | Required? |
|---|---|
| Upgraded | ✅ Required |
| DivineRPG | ⬜ Optional (boss drops disabled without it) |
| Occultism | ⬜ Optional (rituals disabled without it) |
| Apothic Attributes | ⬜ Optional (crit/lifesteal/armor shred disabled) |
| AoA3 | ⬜ Optional (ritual ingredients only) |
| Modern Industrialization | ⬜ Optional (ritual ingredients only) |

## Textures needed

Place 16×16 PNGs at `src/main/resources/assets/dmmc/textures/item/`:
- `boss_upgrade.png`
- `armor_slot_token.png`
- `weapon_slot_token.png`
- `apply_upgrade_token.png`
