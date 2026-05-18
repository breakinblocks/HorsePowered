---
navigation:
  title: Drying Rack
  position: 7
item_ids:
  - horsepowered:drying_rack
---

# Drying Rack

The Drying Rack is a passive wooden rack that slowly converts wet or raw items into a dried form. It holds **8 items at once** in a 4x2 grid laid across the slats, and each slot ticks its own progress timer independently, so you can load the rack one slot at a time without resetting anyone else's progress.

<Row>
<ItemImage id="horsepowered:drying_rack" scale="4" />
</Row>

## Usage

1. Place the Drying Rack on the ground (it can face any direction).
2. **Right-click** with an input item in hand to place it on the slot you clicked. Only one item per slot.
3. Wait for the recipe's drying time to elapse. The slot's item is replaced with the dried output on completion.
4. **Right-click with an empty hand** on a filled slot to take the item back. Works for both drying-in-progress items and finished outputs.

## Automation

Hoppers and pipes can interact with the rack from any side:

- **Insert**: only items with a registered drying recipe are accepted. Items go one at a time, one per empty slot.
- **Extract**: only **finished** items can be pulled out. Items still drying are locked, so a hopper underneath won't snatch a half-dried kelp.

This means you can run a fully-automated pipeline: hopper above feeding raw input, hopper below pulling finished output.

## Recipe

<RecipeFor id="horsepowered:drying_rack" />

## Notes

- The rack itself does not require power, fuel, or a worker. Drying happens passively over time.
- Each slot stores both the current item *and* its progress, so partial-progress items survive a chunk reload.
- Modpack makers can add custom drying recipes at `data/<namespace>/recipes/drying/<recipe>.json`. See the README for the recipe schema.
