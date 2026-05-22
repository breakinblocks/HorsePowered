---
navigation:
  title: Hand Grindstone
  position: 1
item_ids:
  - horsepowered:hand_grindstone
---

# Hand Grindstone

The Hand Grindstone is a manual processing block that lets you grind items by hand. A great early-game option before you have horses for automation.

<Row>
<ItemImage id="horsepowered:hand_grindstone" scale="4" />
</Row>

## Usage

1. Place the Hand Grindstone.
2. Right-click with an item to insert it into the input slot.
3. Right-click with an empty hand to turn the grindstone. Each turn plays a grinding sound and spins the item on the stone.
4. The grindstone processes the item over multiple turns. The output appears on the grinding surface next to the input.
5. Right-click with an empty hand on the output to collect it.
6. Sneak + right-click to pull un-milled input back out.

## Display & Pickup Sides

Items rendered on the grindstone surface line up with the side that picks them up, based on which way the block was placed:

- **Top (spinning)**: the input, processed each turn
- **Player's left side**: the input slot, sneak + right-click here to pull back unprocessed input
- **Player's right side**: the main output, right-click here to collect
- **Front (player's side)**: the secondary output (when a recipe produces one)

## Recipe

<RecipeFor id="horsepowered:hand_grindstone" />

## Hunger Cost

Each turn costs a small baseline amount of hunger (configurable). Some recipes also add their own extra hunger cost on top of the baseline, and only the Hand Grindstone pays that cost. The Horse Grindstone ignores it. JEI shows a "Hunger" line on recipes that have one, so you can spot the expensive ones at a glance.

## Notes

- Multiple items can be inserted for batch processing.
- Works with the same recipes as the Horse Grindstone unless the recipe is tagged horse-only.
- Available grinding recipes can be found in JEI/EMI.
