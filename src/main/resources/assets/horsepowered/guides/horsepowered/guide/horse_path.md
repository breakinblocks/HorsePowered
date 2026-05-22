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
- Levers placed inside the ring are always allowed, useful for hosting redstone toggles right on the floor (handy for the Generator).
- Shift + right-click any horse-powered block with an empty hand to visualize the area. Green = clear, Red = obstructed.

## Obstruction Tolerance

A small number of non-replaceable blocks (chests, hoppers, pipes, gears, etc.) can sit inside the ring without breaking validation. The default tolerance is **2 blocks**, configurable in `config/horsepowered-common.toml` under `[horse_path]`.

## Path Speed

The floor blocks under the worker's circular path each contribute a speed multiplier. The final multiplier is the average across the unique blocks along the path, and it scales how fast the worker moves, which directly scales how fast recipes run.

Defaults: dirt, grass, coarse dirt, and rooted dirt run at 0.5x; dirt paths run at 1.0x; packed ice runs at 2.0x. Anything else defaults to 1.0x. Mixed paths average out.

Speed entries and the default multiplier are configurable in `config/horsepowered-common.toml` under `[horse_path]`.

## Notes

- The speed multiplier is recomputed on each validation pass (roughly every 11 seconds), so you can swap blocks under a running worker without re-attaching the horse.
- Only the floor blocks directly under the path are sampled.
