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
3. Right-click with an empty hand to turn the grindstone — each turn plays a grinding sound and spins the item on the stone
4. The grindstone will process the item over multiple turns; the output appears on the grinding surface next to the input
5. Right-click with an empty hand on the output to collect it
6. Sneak + right-click to pull un-milled input back out

## Display & Pickup Sides

The Hand Grindstone displays its contents based on the direction the block is facing:
- **Input on top**: spinning preview of the un-milled item
- **Player's left** (block's clockwise side relative to facing): extra input display
- **Player's right** (block's counter-clockwise side): output display — right-click here to pick up the finished product
- **Player's side / front**: secondary output display

## Recipe

<RecipeFor id="horsepowered:hand_grindstone" />

## Notes

- Each turn consumes a small amount of hunger (configurable). Recipes may also specify an extra `hungerCost` per turn that adds on top of the config baseline.
- Multiple items can be inserted for batch processing
- Works with the same recipes as the Horse Grindstone
- The number of turns required per recipe is shown in JEI / EMI on the manual category

## Example Recipes

Common items you can grind:
- Wheat to Flour
- Bone to Bone Meal
- Various ores to dusts (with mod support)
