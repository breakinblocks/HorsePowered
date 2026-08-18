<p align="center">
  <img src="metadata/images/logo.png" alt="Horse Powered Logo" width="400">
</p>

<p align="center">
  <a href="https://discord.gg/TRxaXaYy42"><img src="https://img.shields.io/discord/123194979509207040?style=flat&logo=discord&logoColor=white&label=Discord&color=5865F2" alt="Discord"></a>
</p>

# Horse Powered

A Minecraft NeoForge mod that adds horse-powered machinery for grinding, chopping, and pressing items. Process materials the old-fashioned way — with animal power!

## Features

### Manual Machines

- **Hand Grindstone** — Grind items by hand. Right-click to turn the wheel and process materials.
- **Chopping Block** — Chop logs into planks using an axe. A simple early-game wood processing solution.

### Passive Machines

- **Drying Rack** — A wooden rack that dries 8 items at once. Each slot ticks independently — load one or fill all eight, the rack doesn't care. Right-click to place / take items. Hoppers and pipes can feed input from any side and extract only finished outputs, never half-dried items.

### Horse-Powered Machines

- **Horse Grindstone** — An automated grindstone powered by a horse walking in circles. Continuously grinds items without manual intervention.
- **Horse Chopper** — An automated chopping machine. Attach a horse to chop logs into planks automatically.
- **Horse Press** — Press items to extract fluids or produce other outputs. Squeeze flowers for dye, press seeds for oil, and more. Fluids come out by bucket, by pipe, or by bottle where a bottling recipe covers them.
- **Horse Powered Generator** — Converts horse labor into Forge Energy (FE). Generates 80 FE/tick while a worker walks, stores up to 100,000 FE, and pushes power to any adjacent energy handler. Requires a redstone signal to run.

### Tools

- **Work Saddle** — Right-click a valid worker (horse, donkey, mule, llama, trader llama) to capture it into the saddle, then right-click any block to release it elsewhere. The carried mob is rendered as an overlay on the saddle, monster-spawner style. Crafted from a saddle and a lead.

### Creative-Only

- **Creative Battery** — A creative-mode test block for energy setups. Holds up to `Integer.MAX_VALUE` FE and accepts/outputs power at the same rate. Pushes stored energy to adjacent blocks every tick.

### Seed Oil

Pressing seeds (wheat, melon, pumpkin, beetroot, or torchflower) in the Horse Press produces **Seed Oil** — a viscous, flammable plant-based fluid.

- Flows slowly and doesn't spread far (half the range of water)
- Can be picked up with a bucket
- **Burns as furnace fuel** — a bucket of seed oil lasts nearly as long as a bucket of lava
- **Flammable** — contact with lava or fire ignites it, and flames spread rapidly through connected oil
- Tagged as `c:plantoil` for cross-mod compatibility

## Getting Started

1. Craft a **Hand Grindstone** or **Chopping Block** for manual processing
2. Upgrade to horse-powered machines for automation
3. Lead a horse (or other valid creature) to the machine with a lead
4. Right-click the machine while holding the lead to attach the creature
5. Insert items by right-clicking the machine while holding the input material
6. Extract finished products by right-clicking with an empty hand

**Tip:** Shift+Right-click a horse-powered machine with an empty hand to visualize the required 7x7 working area. The walking ring must be clear and the floor beneath it must be sturdy. Green = clear, Red = obstructed. Levers placed inside the ring are allowed, so you can wire redstone toggles right onto the machine's floor.

## Included Recipes

Horse Powered ships with a comprehensive set of vanilla recipes out of the box. All recipes are viewable in-game with JEI or EMI.

### Chopping (25 recipes)
- **All log types to planks** — oak, birch, spruce, jungle, acacia, dark oak, mangrove, cherry, crimson, warped, bamboo
- **Planks to sticks** — works with all plank types
- **Food** — melon to slices, pumpkin to seeds
- **Wood recycling** — doors, trapdoors, fences, gates, slabs, stairs, buttons, signs, ladders all break down into sticks

### Grinding (22 recipes)
- **Bone processing** — bone to bone meal (with 25% bonus chance), bone block to 9 bone meal
- **Flour** — wheat to flour (craft dough with a water bucket, then smelt into bread)
- **Stone chain** — stone to cobblestone, cobblestone to gravel (with flint chance), gravel to sand (with flint chance)
- **Blaze/Breeze** — blaze rod to 4 powder, breeze rod to 5 wind charges
- **Block decomposition** — bricks, clay, glowstone, honeycomb block, dripstone, prismarine, quartz block all break into their components
- **Ore processing** — raw iron/gold to nuggets with 25% bonus, raw copper to ingots with 25% bonus
- **Miscellaneous** — wool to string, sandstone to sand, flint to gunpowder, sugar cane to sugar, soul soil to soul sand

### Pressing (33 recipes)
- **Seeds to Seed Oil** — all seed types (wheat, melon, pumpkin, beetroot, torchflower)
- **Flowers to dye** — all 19 flower types, small flowers yield 4 dye, tall flowers yield 8
- **Water extraction** — ice, snow, snowball, packed ice, wet sponge, mud, pointed dripstone
- **Other** — sugar cane to paper, honey bottle to sugar, honeycomb to honey, cactus to green dye, magma block to magma cream, kelp to dried kelp

### Drying (6 recipes)
The Drying Rack is passive — slower than other machines because no worker drives it.
- **Kelp** → Dried Kelp (1000 ticks / 50s)
- **Wet Sponge** → Sponge (2000 ticks / 100s)
- **Rotten Flesh** → Leather (1000 ticks / 50s)
- **Mud** → Dirt (1000 ticks / 50s)
- **Clay** → Terracotta (4000 ticks / 200s)
- **Saplings** (any) → Dead Bush (1500 ticks / 75s)

Jade tooltip shows per-slot progress and remaining time when you look at a specific slot.

### Bottling (1 recipe)
Bottling recipes let a container that has no fluid-handler capability of its own move fluid in and out of the Horse Press.
- **Glass Bottle** + 250 mB Water → **Water Bottle** (and the reverse, emptying a water bottle back into the press)

Press ice, snow, snowballs, or pointed dripstone for the water first. Packs and mods add their own entries for fluids that only exist in bottled form.

### Farmer's Delight Compat (11 recipes)
When Farmer's Delight is installed, the chopper gains meat-cutting recipes:
- Beef, porkchop, chicken, cod, salmon, mutton (raw and cooked variants)

## Automation

All Horse Powered machines support item automation via hoppers, pipes, and other modded item transport systems. Insert items into the input slot from the top or sides, and extract finished products from the bottom.

The Horse Press exposes a fluid handler to adjacent blocks, so pipes and tanks can fill its input tank and drain its output tank. By hand, fluids move with a bucket, or with a bottle where a bottling recipe covers that fluid.

## Mod Integrations

- **JEI** — Recipe viewing support for all machine types
- **EMI** — Recipe viewing support for all machine types
- **Jade** — Block tooltips showing machine status, inventory, worker info, and progress
- **GuideME** — In-game guidebook (when installed, not required)
- **Farmer's Delight** — Conditional meat-cutting recipes for the chopper
- **Forge Energy (FE)** — The Horse Powered Generator exposes the standard FE capability and works with any mod that accepts FE (cables, machines, batteries)

## Configuration

The mod includes several configuration options:

- Hunger exhaustion rates for manual machines
- Whether axes take damage when using the chopping block
- Points required for various machine operations
- Press fluid tank capacity
- Item rendering options

---

## For Modpack Makers & Datapack Authors

### Custom Recipes

Horse Powered uses data-driven JSON recipes that can be added or modified via datapacks. All built-in recipes are data-generated and can be overridden.

**Recipe types:**
- `horsepowered:grinding` — Grindstone recipes (supports secondary output with chance)
- `horsepowered:chopping` — Chopping recipes
- `horsepowered:pressing` — Press recipes (supports item OR fluid output)
- `horsepowered:drying` — Drying Rack recipes (item-in, item-out, with a time in ticks)
- `horsepowered:bottling` — Press bottle-filling recipes (fluid to item, and the reverse)
- `horsepowered:trapping` — Animal Trap recipes (bait + entity, with optional biome/waterlog gates, per-recipe bait consumption, and an overridable display name and icon)

**Optional fields available on every grinding and chopping recipe:**
- `tier` — restricts which station can run the recipe. `"any"` (default) runs on both manual and horse-powered stations, `"hand"` is manual-only, `"horse"` is horse-powered-only.
- `priority` — integer that controls display order in JEI/EMI. Lower values appear first; ties keep their natural order.
- `hungerCost` — float (default `0.0`). On manual stations, this is added to the player's food exhaustion every chop or every turn, on top of the config baseline. Has no effect on horse-powered stations. EMI/JEI show it as a "Hunger" line on the manual category only when greater than zero.

#### Grinding Recipe
```json
{
  "type": "horsepowered:grinding",
  "ingredient": {"item": "minecraft:bone"},
  "result": {"id": "minecraft:bone_meal", "count": 3},
  "secondary": {"id": "minecraft:bone_meal", "count": 1},
  "secondaryChance": 25,
  "time": 12,
  "tier": "any",
  "priority": 0,
  "hungerCost": 0.5
}
```

#### Chopping Recipe
```json
{
  "type": "horsepowered:chopping",
  "ingredient": {"tag": "minecraft:oak_logs"},
  "result": {"id": "minecraft:oak_planks", "count": 4},
  "time": 1,
  "tier": "any",
  "priority": 0,
  "hungerCost": 0.0
}
```

#### Pressing Recipe (Item Output)
```json
{
  "type": "horsepowered:pressing",
  "ingredient": {"item": "minecraft:sugar_cane"},
  "inputCount": 3,
  "result": {"id": "minecraft:paper", "count": 3}
}
```

#### Pressing Recipe (Fluid Output)
```json
{
  "type": "horsepowered:pressing",
  "ingredient": {"item": "minecraft:wheat_seeds"},
  "inputCount": 12,
  "fluidResult": {"id": "horsepowered:seed_oil", "amount": 250}
}
```

#### Drying Recipe
```json
{
  "type": "horsepowered:drying",
  "ingredient": {"item": "minecraft:kelp"},
  "result": {"id": "minecraft:dried_kelp", "count": 1},
  "time": 400
}
```

#### Bottling Recipe
```json
{
  "type": "horsepowered:bottling",
  "container": {"item": "minecraft:glass_bottle"},
  "fluid": {"id": "minecraft:water", "amount": 250},
  "result": {"id": "minecraft:potion", "count": 1, "components": {"minecraft:potion_contents": {"potion": "minecraft:water"}}},
  "priority": 0
}
```
- `container` — Ingredient. The empty container the player holds. Usually a glass bottle, but any item works.
- `fluid` — The fluid and amount in mB moved per click.
- `result` — The filled item handed back. Components are respected, so potions, juices, and other data-carrying items all work.
- `priority` — Optional integer; lower values display first in JEI/EMI (default `0`).

Right-clicking the Press with the `container` item drains `fluid` from the output tank and gives the `result`. Right-clicking with the `result` item does the reverse: it fills the input tank and returns the empty `container`. Both directions only fire when the tank has room or holds enough of the matching fluid.

This exists because glass bottles carry no fluid-handler capability in NeoForge, so bottles cannot interact with a tank the way buckets do. Mods whose fluids only have a bottled form (juices, for example) should ship a bottling recipe so their fluid can be taken out of the Press by hand.

#### Trapping Recipe
```json
{
  "type": "horsepowered:trapping",
  "bait": {"item": "minecraft:kelp"},
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
- `bait` — Ingredient. The item that triggers the recipe.
- `entity` — ResourceLocation of the entity to capture. Any mob can be specified.
- `time` — Optional minimum wait in ticks before the catch dice roll begins (default `1200`).
- `priority` — Optional integer; lower values display first in JEI/EMI (default `0`).
- `biome` — Optional biome tag the trap must sit inside before the dice roll counts.
- `waterlogged` — Optional boolean; when `true` the trap must be waterlogged before the dice roll counts.
- `baitConsumed` — Optional boolean (default `false`). When `true` the trap requires bait to generate each drop cycle after capture. When the bait slot is empty the drop timer pauses at the threshold until matching bait is supplied. Hoppers may continue feeding bait into a captured trap.
- `baitConsumeChance` — Optional percent (`0.01`-`100.00`, default `100.0`). Only applies when `baitConsumed` is `true`. Rolled each time drops are generated; on success one bait is consumed. Lower values let one bait fuel multiple drop cycles on average. All built-in trapping recipes ship with `baitConsumed: true` and `baitConsumeChance: 10.0`.
- `title` — Optional string naming the catch in the JEI and EMI recipe panels. Accepts a translation key (resolved against the active language) or a plain string (shown as-is). When omitted the entity's own name is used, so the panel identifies the animal even in packs that hide spawn eggs.
- `icon` — Optional item ID shown in the recipe panel's output slot. Use it for mobs that have no spawn egg, or whose mod registers one the game cannot resolve. When omitted the icon is resolved in order: the entity's registered spawn egg, then an item named `<namespace>:<entity_path>_spawn_egg`, then `minecraft:egg` as a last resort.

The trap's drops come from the captured entity's vanilla loot table, so no drop list is declared on the trap recipe itself.

### Conditional Recipes (Mod Compat)

Use NeoForge conditions to add recipes that only load when a specific mod is present:
```json
{
  "neoforge:conditions": [
    {"type": "neoforge:mod_loaded", "modid": "farmersdelight"}
  ],
  "type": "horsepowered:chopping",
  "ingredient": {"item": "minecraft:beef"},
  "result": {"id": "farmersdelight:minced_beef", "count": 2},
  "time": 1
}
```

### Custom Worker Mobs

By default, horses, donkeys, mules, llamas, and trader llamas can power machines and can be picked up by the Work Saddle. To add more, create an entity type tag at `data/horsepowered/tags/entity_type/valid_worker.json`:

```json
{
  "replace": false,
  "values": [
    "alexsmobs:elephant",
    "somemod:custom_horse"
  ]
}
```

**Note:** Only `PathfinderMob` entities (mobs with navigation AI) work properly with the walking system.

## Requirements

- Minecraft 1.21.1
- NeoForge 21.1.0+
- Java 21+

## License

This project is licensed under the MIT License — see the [LICENSE.md](LICENSE.md) file for details.

## Credits

- **Saereth** — Current development and port to modern NeoForge
- **GoryMoon** — Original [HorsePower](https://www.curseforge.com/minecraft/mc-mods/horse-power) mod concept and design

This mod is a spiritual successor and reimplementation of GoryMoon's original HorsePower mod, updated for modern Minecraft with new features and improvements. We thank GoryMoon for the original inspiration that made this mod possible.
