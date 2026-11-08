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
- The 7x7 square one block below must be sturdy floor. Dirt paths, and any other block listed in the path speed config, also count as floor.
- Any block a mob can walk through (torches, levers, buttons, signs, flowers, redstone dust, open gates and so on) is allowed inside the ring, so you can light it or host redstone toggles right on the floor (handy for the Generator).
- Shift + right-click any horse-powered block with an empty hand to visualize the area. Green = clear, Red = obstructed.

## Obstruction Tolerance

A small number of non-replaceable blocks (chests, hoppers, pipes, gears, etc.) can sit inside the ring without breaking validation. The default tolerance is **2 blocks**, configurable in `config/horsepowered-common.toml` under `[horse_path]`.

## Path Speed

The floor blocks under the worker's circular path each contribute a speed multiplier. The final multiplier is the average across the unique blocks along the path, and it scales how fast the worker moves, which directly scales how fast recipes run.

Defaults: dirt, grass, coarse dirt, and rooted dirt run at 0.5x; dirt paths run at 1.0x; packed ice runs at 2.0x. Anything else defaults to 1.0x. Mixed paths average out.

Speed entries and the default multiplier are configurable in `config/horsepowered-common.toml` under `[horse_path]`.

## Worker Speed

The worker's jump strength also changes how fast it walks, on top of the path speed. Most mobs have a jump strength of 0.42 and walk at normal speed, and every point above or below that adds or removes the same amount of speed, so a 0.7 horse walks at about 1.3x and a 1.0 horse at about 1.6x.

The two multiply together: a 1.6x horse on a 2.0x path works at 3.2x. Jade shows both on any machine with a worker attached, and looking at a mob that can be a worker shows its jump strength and speed.

The baseline and scale are `workerJumpSpeedBaseline` (default 0.42) and `workerJumpSpeedScale` (default 1.0) under `[horse_path]`. Set the scale to `0` to make every worker walk at the same speed.

Pack makers can set a fixed jump strength for any entity type with the `horsepowered:horse_engine_jump_strength` data map, in a datapack file at `data/<namespace>/data_maps/entity_type/horse_engine_jump_strength.json`:

```json
{
  "values": {
    "minecraft:cow": 0.6,
    "#horsepowered:valid_worker": 0.5
  }
}
```

A listed value replaces the mob's own attribute. The value is read when the worker is attached, so re-attach a worker after changing the file.

## Notes

- The speed multiplier is recomputed on each validation pass (roughly every 11 seconds), so you can swap blocks under a running worker without re-attaching the horse.
- Only the floor blocks directly under the path are sampled.
