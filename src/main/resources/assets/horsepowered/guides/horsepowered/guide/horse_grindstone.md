---
navigation:
  title: Horse Grindstone
  position: 2
item_ids:
  - horsepowered:grindstone
---

# Horse Grindstone

The Horse Grindstone is an automated grinding machine powered by a horse or other creature walking in circles around it.

<Row>
<ItemImage id="horsepowered:grindstone" scale="4" />
</Row>

## Setup

1. Place the Horse Grindstone in an area with at least a 7x7 clear space around it.
2. Lead a horse (or other valid creature) with a lead.
3. Right-click the grindstone while holding the lead to attach the creature.
4. The creature walks in circles, powering the grindstone.

## Checking the Working Area

**Shift+Right-click** the grindstone with an empty hand to visualize the required working area. Green = clear, Red = obstructed.

## Usage

1. Right-click with items to insert them into the grindstone.
2. The attached creature grinds items automatically.
3. Right-click to extract output items.

## Recipe

<RecipeFor id="horsepowered:grindstone" />

## Notes

- Right-click with an empty hand to release the attached creature back to a lead.
- If the creature dies or escapes, a lead is dropped.
- The floor under the path scales work speed, and a small number of obstructions (chests, hoppers, etc.) are tolerated. See [Horse Path & Speed](horse_path.md).
- Valid workers by default: horses, donkeys, mules, and llamas. Additional creatures can be added via config.
