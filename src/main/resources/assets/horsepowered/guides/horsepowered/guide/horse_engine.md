---
navigation:
  title: Horse Engine
  position: 7
item_ids:
  - horsepowered:horse_engine
---

# Horse Engine

The Horse Engine turns a walking worker into Create rotational force. It only exists when the Create mod is installed. A worker walking around the engine spins the cog on top, and the engine feeds stress units into any Create network connected to it.

<Row>
<ItemImage id="horsepowered:horse_engine" scale="4" />
</Row>

## Specifications

- **Speed**: 16 RPM while a worker is walking
- **Stress capacity**: 40 SU for every 0.1 of the worker's jump strength. A typical horse (0.7) gives 280 SU, the best horses (1.0) give 400 SU, donkeys, mules and llamas (0.5) give 200 SU
- **Output**: a shaft placed directly under the engine, or cogwheels placed beside it

Only horses, donkeys, mules and llamas carry a jump strength attribute in this version, so any other worker uses the configured default of 0.4 (160 SU) unless a datapack says otherwise.

## Setup

1. Place the Engine in an area with at least a 7x7 clear space around it and a sturdy floor below the walking ring
2. Lead a horse (or other valid creature) with a lead
3. Right-click the engine while holding the lead to attach the creature
4. Connect a shaft below the engine or a cogwheel beside it. Both spots sit inside the centre of the ring, so they do not block the worker
5. The engine runs as long as a worker is attached and the ring is clear. Use a clutch or gearshift to control it, or release the worker

## Checking the Working Area

**Shift+Right-click** the engine with an empty hand to visualize the required working area:
- **Green blocks**: Clear, the area is suitable for the worker to walk
- **Red blocks**: Obstructed, remove or replace these blocks for the engine to function

## Recipe

<RecipeFor id="horsepowered:horse_engine" />

## Configuration

These options are in `config/horsepowered-common.toml` under `horse_engine`:

- `horseEngineRpm`: rotation speed while working (default 16)
- `horseEngineStressPerJumpPoint`: stress units per 0.1 jump strength at that speed (default 40)
- `horseEngineDefaultJumpStrength`: jump strength used for workers that have no jump strength attribute (default 0.4)

## Overriding jump strength per mob

Pack makers can set a fixed jump strength for any entity type with a datapack file at `data/<namespace>/horse_engine_jump_strength/<name>.json`:

```json
{
  "values": {
    "minecraft:cow": 0.6,
    "#horsepowered:valid_worker": 0.5
  }
}
```

A listed value replaces the mob's own attribute. Entries starting with `#` match an entity type tag. The value is read when the worker is attached, so re-attach a worker after changing the file.

## Notes

- Engineer's Goggles show the engine's current speed and stress capacity
- The floor blocks under the worker's path change walking speed only. Rotation speed stays constant, so a faster path does not produce more stress
- Right-click with an empty hand and no shift to release the attached worker
- Breaking the engine, or picking it up with a wrench, releases the worker and drops a lead
