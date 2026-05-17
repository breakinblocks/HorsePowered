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

## Specifications

- **Generation rate**: 80 FE/tick while a worker is walking
- **Internal buffer**: 100,000 FE
- **Output**: pushes up to the full buffer per tick to any adjacent energy handler
- **Input**: none (output-only, will not accept energy from external sources)

## Setup

1. Place the Generator in an area with at least a 7x7 clear space around it and a sturdy floor below the walking ring
2. Lead a horse (or other valid creature) with a lead
3. Right-click the generator while holding the lead to attach the creature
4. Apply a redstone signal (for example a lever on top) to enable generation
5. The redstone torch on top lights up red while the generator is actively producing power

## Checking the Working Area

**Shift+Right-click** the generator with an empty hand to visualize the required working area:
- **Green blocks**: Clear, the area is suitable for the worker to walk
- **Red blocks**: Obstructed, remove or replace these blocks for the generator to function

Levers placed inside the working area do not invalidate it, so the generator can be redstone-toggled with a wall lever on a nearby floor block.

## Recipe

<RecipeFor id="horsepowered:generator" />

## Notes

- Connect the sides, top, or bottom to any FE-compatible cable or machine to extract power
- The generator only runs while it has redstone power AND its buffer is below capacity
- Right-click with an empty hand and no shift to release the attached worker
