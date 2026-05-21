---
navigation:
  title: Horse Path & Speed
  icon: minecraft:dirt_path
  position: 15
---

# Horse Path & Speed

Every horse-powered block (grindstone, chopper, press, generator) needs a 7x7 working area around it. The worker walks a circular path on the outer ring of that area, and the blocks that path crosses can speed the worker up or slow it down.

## Working Area

- A 7x7 ring (the center 3x3 is occupied by the block and a small buffer) must be clear at the block's level and one block above.
- The 7x7 square one block below must be sturdy floor.
- Levers placed inside the ring are always allowed, so you can host redstone toggles directly on the floor (useful for the Generator).
- Shift + right-click any horse-powered block with an empty hand to visualize the area. Green = clear, Red = obstructed.

## Obstruction Tolerance

A small number of non-replaceable blocks (chests, hoppers, pipes, Create gears, etc.) can sit inside the ring without breaking validation. The default tolerance is **2 blocks**.

Change it in `config/horsepowered-common.toml`:

```toml
[horse_path]
pathObstructionTolerance = 2
```

Range is `0` (strict, the original behavior) to `40` (the full ring size).

## Path Speed

The 24 floor blocks under the worker's circular path each contribute a speed multiplier. The final multiplier is the average across the unique blocks along the path, and it scales how fast the worker moves, which directly scales how fast recipes run.

Default multipliers:

| Block | Multiplier |
|---|---|
| `minecraft:grass_block` | 0.5 |
| `minecraft:dirt` | 0.5 |
| `minecraft:coarse_dirt` | 0.5 |
| `minecraft:rooted_dirt` | 0.5 |
| `minecraft:dirt_path` | 1.0 |
| `minecraft:packed_ice` | 2.0 |
| anything else | 1.0 (configurable as `pathSpeedDefault`) |

A multiplier of `0.0` stops the worker on that block; values above `1.0` accelerate it. Mixed paths average out, so a half-ice, half-dirt-path ring lands around 1.5x.

## Configuring Speeds

Add or override entries in `config/horsepowered-common.toml`:

```toml
[horse_path]
pathSpeedDefault = 1.0
pathSpeedEntries = [
    "minecraft:grass_block=0.5",
    "minecraft:dirt=0.5",
    "minecraft:coarse_dirt=0.5",
    "minecraft:rooted_dirt=0.5",
    "minecraft:dirt_path=1.0",
    "minecraft:packed_ice=2.0",
    "minecraft:blue_ice=3.0",
    "modid:custom_block=1.5"
]
```

Format is `namespace:block_id=multiplier`. Entries that fail to parse are skipped. Changes apply on world reload.

## Notes

- The speed multiplier is recomputed roughly every 11 seconds (each validation pass), so you can swap blocks underneath a running worker and the rate updates without re-attaching the horse.
- Only the floor blocks directly under the 24 path points are sampled. Blocks inside the inner ring or outside the 7x7 do not affect speed.
- Tolerance still respects sturdy-floor requirements: the floor under the entire 7x7 must remain solid even when ground-level obstructions are tolerated.
