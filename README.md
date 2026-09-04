<p align="center">
  <img src="metadata/images/logo.png" alt="Horse Powered Logo" width="400">
</p>

[![Discord](https://img.shields.io/discord/123194979509207040?style=flat&logo=discord&logoColor=white&label=Discord&color=5865F2)](https://discord.gg/TRxaXaYy42)

# Horse Powered

A Minecraft Forge mod that adds horse-powered machinery for grinding, chopping, and pressing items. Process materials the old-fashioned way, with animal power!

## Features

### Manual Machines

- **Hand Grindstone** - Grind items by hand. Right-click to turn the wheel and process materials.
- **Chopping Block** - Chop logs into planks using an axe. A simple early-game wood processing solution.

### Horse-Powered Machines

- **Horse Grindstone** - An automated grindstone powered by a horse walking in circles. Continuously grinds items without manual intervention.
- **Horse Chopper** - An automated chopping machine. Attach a horse to chop logs into planks automatically.
- **Horse Press** - Press items to extract fluids or produce other outputs. Perfect for making oils, juices, and other liquids. Fluids come out by bucket, by pipe, or by bottle where a bottling recipe covers them.
- **Horse Powered Generator** - Converts horse labor directly into Forge Energy. Generates 80 FE/tick while a worker walks, buffers 100,000 FE, and pushes power to any adjacent FE consumer. Requires a redstone signal to run (toggle with a lever inside the working ring).
- **Horse Engine** (needs Create): converts horse labor into Create rotational force. Spins at 16 RPM while a worker walks and provides 40 SU for every 0.1 of the worker's jump strength (a 0.7 horse gives 280 SU). Connect a shaft below it or cogwheels beside it. Speed, SU per jump point and the fallback jump strength are configurable, and a datapack can set a fixed value per mob.

### Passive Machines

- **Drying Rack** - 2x2 multiblock that dries 8 items in a 4x2 grid. Each slot ticks independently. Right-click empty-handed to retrieve an item, or with an item to insert. Hoppers can insert from any side and only extract finished items. Default recipes include kelp to dried kelp, wet sponge to sponge, rotten flesh to leather, mud to dirt, clay to terracotta, and saplings to dead bush.

### Tools

- **Work Saddle** - Captures any valid worker (horse, donkey, mule, llama, trader llama) into a portable item and releases it elsewhere on right-click. Carrying saddle shows a tooltip with the stored mob.

### Creative-Only

- **Creative Battery** - Infinite-capacity FE source/sink for testing energy setups. Holds `Integer.MAX_VALUE` FE and pushes stored energy to adjacent FE consumers every tick.

## Getting Started

1. Craft a **Hand Grindstone** or **Chopping Block** for manual processing
2. Upgrade to horse-powered machines for automation
3. Lead a horse (or other valid creature) to the machine with a lead
4. Right-click the machine while holding the lead to attach the creature
5. The creature will walk in circles, powering the machine automatically

## Working Area

Horse-powered machines require a clear 7x7 area around them for the animal to walk.

**Tip:** Shift+Right-click a horse-powered machine with an empty hand to visualize the required working area:

- **Green boxes** indicate clear areas
- **Red boxes** indicate obstructed blocks that need to be removed

## Recipes

Recipes can be viewed in-game using JEI (Just Enough Items). The mod includes recipes for:

- Grinding wheat into flour
- Grinding bones into bone meal
- Chopping logs into planks
- Pressing various items into fluids

These recipes exist as placeholder examples since this mod is primarily aimed at modpack makers. Additional recipes can be added via datapacks.

## For Modpack Makers

### Custom Recipes

Horse Powered uses data-driven JSON recipes that can be added or modified via datapacks. Recipe types include:

- `horsepowered:grinding` - Grindstone recipes
- `horsepowered:chopping` - Chopping block recipes
- `horsepowered:pressing` - Press recipes (supports item and fluid outputs)
- `horsepowered:drying` - Drying rack recipes (1 input -> 1 output over time)
- `horsepowered:trapping` - Animal trap recipes (bait + entity, with optional biome/waterlog gates, per-recipe bait consumption, and an overridable display name and icon)
- `horsepowered:bottling` - Press bottle-filling recipes (fluid to item, and the reverse)

Example grinding recipe (`data/yourpack/recipes/grinding/custom_recipe.json`):
```json
{
  "type": "horsepowered:grinding",
  "ingredient": { "item": "minecraft:wheat" },
  "result": { "item": "yourmod:flour", "count": 1 },
  "time": 12,
  "tier": 1,
  "priority": 0,
  "hungerCost": 0.1
}
```

Example trapping recipe (`data/yourpack/recipes/trapping/custom_recipe.json`):
```json
{
  "type": "horsepowered:trapping",
  "bait": { "item": "minecraft:kelp" },
  "entity": "minecraft:salmon",
  "time": 1500,
  "priority": 0,
  "biome": "#horsepowered:fish_habitat",
  "waterlogged": true,
  "baitConsumed": true,
  "baitConsumeChance": 10.0,
  "title": "River Salmon",
  "icon": "minecraft:salmon_bucket"
}
```
- `bait` - Ingredient. The item that triggers the recipe.
- `entity` - ResourceLocation of the entity to capture. Any mob can be specified.
- `time` - Optional minimum wait in ticks before the catch dice roll begins (default `1200`).
- `priority` - Optional integer; lower values display first in JEI/EMI (default `0`).
- `biome` - Optional biome tag the trap must sit inside before the dice roll counts.
- `waterlogged` - Optional boolean; when `true` the trap must be waterlogged before the dice roll counts.
- `baitConsumed` - Optional boolean (default `false`). When `true` the trap requires bait to generate each drop cycle after capture. When the bait slot is empty the drop timer pauses until matching bait is supplied. Hoppers may continue feeding bait into a captured trap.
- `baitConsumeChance` - Optional percent (`0.01`-`100.00`, default `100.0`). Only applies when `baitConsumed` is `true`. Rolled each time drops are generated; on success one bait is consumed. All built-in trapping recipes ship with `baitConsumed: true` and `baitConsumeChance: 10.0`.
- `title` - Optional string naming the catch in the JEI and EMI recipe panels. Accepts a translation key (resolved against the active language) or a plain string (shown as-is). When omitted the entity's own name is used, so the panel identifies the animal even in packs that hide spawn eggs.
- `icon` - Optional item ID shown in the recipe panel's output slot. Use it for mobs that have no spawn egg, or whose mod registers one the game cannot resolve. When omitted the icon is resolved in order: the entity's registered spawn egg, then an item named `<namespace>:<entity_path>_spawn_egg`, then `minecraft:egg` as a last resort.

The trap's drops come from the captured entity's vanilla loot table, so no drop list is declared on the trap recipe itself.

Example bottling recipe (`data/yourpack/recipes/bottling/custom_recipe.json`):
```json
{
  "type": "horsepowered:bottling",
  "container": { "item": "minecraft:glass_bottle" },
  "fluid": { "id": "minecraft:water", "amount": 250 },
  "result": { "item": "minecraft:potion", "count": 1, "nbt": { "Potion": "minecraft:water" } },
  "priority": 0
}
```
- `container` - Ingredient. The empty container the player holds. Usually a glass bottle, but any item works.
- `fluid` - The fluid and amount in mB moved per click.
- `result` - The filled item handed back. NBT is respected, so potions, juices, and other data-carrying items all work.
- `priority` - Optional integer; lower values display first in JEI/EMI (default `0`).

Right-clicking the Press with the `container` item drains `fluid` from the output tank and gives the `result`. Right-clicking with the `result` item does the reverse: it fills the input tank and returns the empty `container`. Both directions only fire when the tank has room or holds enough of the matching fluid.

This exists because glass bottles carry no fluid-handler capability in Forge, so bottles cannot interact with a tank the way buckets do. Mods whose fluids only have a bottled form (juices, for example) should ship a bottling recipe so their fluid can be taken out of the Press by hand. One recipe ships by default: a glass bottle takes 250 mB of water out of the press and becomes a water bottle.

#### Optional Recipe Fields

All three recipe types accept the following optional fields:

- **`tier`** (int, default `0`) - Restricts which stations can run the recipe. Higher tiers require the corresponding higher-tier station upgrade.
- **`priority`** (int, default `0`) - Sort order for recipe selection and JEI / EMI display. Lower values sort first.
- **`hungerCost`** (float, default `0.0`) - On manual stations (Hand Grindstone, Chopping Block), this value is added to the player's food exhaustion every chop or turn, on top of the existing config baseline. Has no effect on horse-powered stations. JEI and EMI display a "Hunger" line on the manual category when the value is greater than zero.

### Custom Worker Mobs

By default, the following vanilla mobs can power horse-powered machines:
- Horse
- Donkey
- Mule
- Llama
- Trader Llama

To add additional mobs as valid workers, create an entity type tag file at:
`data/horsepowered/tags/entity_types/valid_worker.json`

Example (adding modded horses):
```json
{
  "replace": false,
  "values": [
    "alexsmobs:elephant",
    "somemod:custom_horse"
  ]
}
```

**Note:** Only PathfinderMob entities (entities with AI that can navigate) will work properly with the pathing system.

## Mod Integrations

- **JEI** - Recipe viewing support
- **Jade** - Block information tooltips showing machine status, inventory, and worker info
- **GuideME** - In-game documentation (when installed, not required)

## Configuration

The mod includes several configuration options:

- Hunger exhaustion rates for manual machines
- Whether axes take damage when using the chopping block
- Points required for various machine operations
- Item rendering options

## Requirements

- Minecraft 1.20.1
- Forge 47.2.0+

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details.

## Credits

- **Saereth** - Current development and 1.20.1 port
- **GoryMoon** - Original [HorsePower](https://www.curseforge.com/minecraft/mc-mods/horse-power) mod concept and design

This mod is a spiritual successor and reimplementation of GoryMoon's original HorsePower mod, updated for modern Minecraft versions with new features and improvements. We thank GoryMoon for the original inspiration and concept that made this mod possible.
