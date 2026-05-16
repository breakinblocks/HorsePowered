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

### Horse-Powered Machines

- **Horse Grindstone** — An automated grindstone powered by a horse walking in circles. Continuously grinds items without manual intervention.
- **Horse Chopper** — An automated chopping machine. Attach a horse to chop logs into planks automatically.
- **Horse Press** — Press items to extract fluids or produce other outputs. Recipes can also consume fluids from the press's internal tank to make new items. Right-click with a bucket to fill or drain the tank.
- **Horse Powered Generator** — Converts horse labor into Forge Energy (FE). Generates 80 FE/tick while a worker walks, stores up to 100,000 FE, and pushes power to any adjacent energy handler. Requires a redstone signal to run.

### Tools

- **Work Saddle** — Right-click any creature in the `horsepowered:valid_worker` tag (tamed or untamed) to capture it onto the saddle, then right-click a block to release it. Lets you transport workers across long distances without leashing them home.

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

**Tip:** Shift+Right-click a horse-powered machine with an empty hand to visualize the required 7x7 working area. The walking ring must be clear and the floor beneath it must be sturdy. Green = clear, Red = obstructed. Levers placed inside the ring are allowed, so you can wire redstone toggles right onto the machine's floor (the Generator uses this for its on/off control).

## Included Recipes

Horse Powered ships with a comprehensive set of vanilla recipes out of the box. All recipes are viewable in-game with JEI.

### Chopping (26 recipes)
- **All log types to planks** — oak, birch, spruce, jungle, acacia, dark oak, mangrove, cherry, pale oak, crimson, warped, bamboo
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
- **Flowers to dye** — all 20 flower types, small flowers yield 4 dye, tall flowers yield 8
- **Water extraction** — ice, snow, snowball, packed ice, wet sponge, mud, pointed dripstone
- **Other** — sugar cane to paper, honey bottle to sugar, honeycomb to honey, cactus to green dye, magma block to magma cream, kelp to dried kelp

### Farmer's Delight Compat (11 recipes)
When Farmer's Delight is installed, the chopper gains meat-cutting recipes:
- Beef, porkchop, chicken, cod, salmon, mutton (raw and cooked variants)

## Automation

All Horse Powered machines support item automation via hoppers, pipes, and other modded item transport systems. Insert items into the input slot from the top or sides, and extract finished products from the bottom.

## Mod Integrations

- **JEI** — Recipe viewing support for all machine types
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

**Optional fields available on every grinding and chopping recipe:**
- `tier` — restricts which station can run the recipe. `"any"` (default) runs on both manual and horse-powered stations, `"hand"` is manual-only, `"horse"` is horse-powered-only.
- `priority` — integer that controls display order in JEI. Lower values appear first; ties keep their natural order.
- `hungerCost` — float (default `0.0`). On manual stations, this is added to the player's food exhaustion every chop or every turn, on top of the config baseline. Has no effect on horse-powered stations. JEI shows it as a "Hunger" line on the manual category only when greater than zero.

#### Grinding Recipe
```json
{
  "type": "horsepowered:grinding",
  "ingredient": "minecraft:bone",
  "result": { "id": "minecraft:bone_meal", "count": 3 },
  "secondary": { "id": "minecraft:bone_meal", "count": 1 },
  "secondaryChance": 25,
  "time": 12,
  "tier": "any",
  "priority": 0,
  "hungerCost": 0.5
}
```
- `ingredient` — Item or `#tag` reference
- `result` — Primary output (required)
- `secondary` — Optional secondary output
- `secondaryChance` — Percent chance for secondary (0-100)
- `time` — Processing cycles required

#### Chopping Recipe
```json
{
  "type": "horsepowered:chopping",
  "ingredient": "#minecraft:oak_logs",
  "result": { "id": "minecraft:oak_planks", "count": 4 },
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
  "ingredient": "minecraft:sugar_cane",
  "inputCount": 3,
  "result": { "id": "minecraft:paper", "count": 3 }
}
```

#### Pressing Recipe (Fluid Output)
```json
{
  "type": "horsepowered:pressing",
  "ingredient": "minecraft:wheat_seeds",
  "inputCount": 12,
  "fluidResult": { "id": "horsepowered:seed_oil", "amount": 250 }
}
```

#### Pressing Recipe (Fluid Input + Item Output)
```json
{
  "type": "horsepowered:pressing",
  "ingredient": "minecraft:bone_meal",
  "inputCount": 1,
  "fluidInput": { "ingredient": "minecraft:milk", "amount": 1000 },
  "result": { "id": "minecraft:slime_ball" }
}
```
- `inputCount` — Number of items consumed per press (default: 1)
- `fluidInput` — Optional `SizedFluidIngredient` drained from the tank each press
- `result` — Item output (omit for fluid-only)
- `fluidResult` — Fluid output with `id` and `amount` in mB (omit for item-only)
- A recipe can combine `fluidInput` with either `result`, `fluidResult`, or both

### Conditional Recipes (Mod Compat)

Use NeoForge conditions to add recipes that only load when a specific mod is present:
```json
{
  "neoforge:conditions": [
    { "type": "neoforge:mod_loaded", "modid": "farmersdelight" }
  ],
  "type": "horsepowered:chopping",
  "ingredient": "minecraft:beef",
  "result": { "id": "farmersdelight:minced_beef", "count": 2 },
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

- Minecraft 26.1.2
- NeoForge 26.1.2+
- Java 25+

## License

This project is licensed under the MIT License — see the [LICENSE.md](LICENSE.md) file for details.

## Credits

- **Saereth** — Current development and port to modern NeoForge
- **GoryMoon** — Original [HorsePower](https://www.curseforge.com/minecraft/mc-mods/horse-power) mod concept and design

This mod is a spiritual successor and reimplementation of GoryMoon's original HorsePower mod, updated for modern Minecraft with new features and improvements. We thank GoryMoon for the original inspiration that made this mod possible.
