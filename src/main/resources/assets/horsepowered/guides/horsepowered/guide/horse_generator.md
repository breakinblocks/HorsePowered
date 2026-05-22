---
navigation:
  title: Horse Powered Generator
  position: 6
item_ids:
  - horsepowered:generator
---

# Horse Powered Generator

The Horse Powered Generator converts horse labor directly into Forge Energy (FE). A walking worker spins the generator, charging an internal buffer that automatically pushes power to adjacent machines, cables, and energy storage.

<Row>
<ItemImage id="horsepowered:generator" scale="4" />
</Row>

## Setup

1. Place the Generator in an area with at least a 7x7 clear space around it.
2. Lead a horse (or other valid creature) with a lead.
3. Right-click the generator while holding the lead to attach the creature.
4. Apply a redstone signal (for example a lever on top) to enable generation.
5. The redstone torch on top lights up red while the generator is actively producing power.

## Checking the Working Area

**Shift+Right-click** the generator with an empty hand to visualize the required working area. Green = clear, Red = obstructed. Levers placed inside the ring do not invalidate it, so the generator can be toggled with a lever on a nearby floor block.

## Recipe

<RecipeFor id="horsepowered:generator" />

## Notes

- Connect any side, top, or bottom to an FE-compatible cable or machine to extract power.
- The generator only runs while it has redstone power and its buffer is below capacity.
- The floor under the path scales generation speed. See [Horse Path & Speed](horse_path.md).
- Mining the generator preserves its stored charge, so you can move it without losing power.
- Right-click with an empty hand and no shift to release the attached worker.
