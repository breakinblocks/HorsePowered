---
navigation:
  title: Hand Grindstone
  position: 1
item_ids:
  - horsepowered:hand_grindstone
---

# Hand Grindstone

The Hand Grindstone is a manual processing block that allows you to grind items by hand. It's a great early-game option before you have access to horses for automation.

<Row>
<ItemImage id="horsepowered:hand_grindstone" scale="4" />
</Row>

## Usage

1. Place the Hand Grindstone
2. Right-click with an item to insert it into the input slot
3. Right-click with an empty hand to turn the grindstone. Each turn plays a grinding sound and spins the item on the stone
4. The grindstone will process the item over multiple turns; the output appears on the grinding surface next to the input
5. Right-click with an empty hand on the output to collect it
6. Sneak + right-click to pull un-milled input back out

## Display & Pickup Sides

Items rendered on the grindstone surface line up with the side that picks them up, based on which way the block was placed:

- **Top (spinning)**: the input, processed each turn
- **Player's left side**: the input slot. Sneak + right-click here to pull back unprocessed input
- **Player's right side**: the main output. Right-click here to collect
- **Front (player's side)**: the secondary output (when a recipe produces one)

## Recipe

<RecipeFor id="horsepowered:hand_grindstone" />

## Hunger Cost

Each turn consumes a small baseline amount of hunger (configurable). Individual grinding recipes can also declare a `hungerCost` in their JSON, which is **added on top** of the baseline only on the Hand Grindstone. Horse Grindstone recipes ignore it. When a recipe has a hunger cost, JEI and EMI show a "Hunger" line on the manual-grinding category.

## Notes

- Multiple items can be inserted for batch processing
- Works with the same recipes as the Horse Grindstone (unless the recipe is restricted with a `tier` of `"horse"`)
- Output items appear in dedicated slots on the grindstone surface

