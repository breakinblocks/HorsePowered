---
navigation:
  title: Horse Press
  position: 5
item_ids:
  - horsepowered:press
---

# Horse Press

The Horse Press is a horse-powered machine that can press items to extract fluids or produce other outputs. It's particularly useful for extracting oils, juices, and other liquids.

<Row>
<ItemImage id="horsepowered:press" scale="4" />
</Row>

## Setup

1. Place the Horse Press in an area with at least a 7x7 clear space around it
2. Lead a horse (or other valid creature) with a lead
3. Right-click the press while holding the lead to attach the creature
4. The creature will automatically walk in circles, powering the press

## Checking the Working Area

**Shift+Right-click** the press with an empty hand to visualize the required working area:
- **Green blocks**: Clear - the area is suitable for the horse to walk
- **Red blocks**: Obstructed - remove these blocks for the press to function

## Usage

1. Right-click with items to insert them into the press
2. Right-click with a bucket of fluid to fill the internal tank, or with an empty bucket to drain it
3. The attached creature will press items automatically
4. Right-click to extract any solid outputs

## Recipe

<RecipeFor id="horsepowered:press" />

## Fluid Tank

The Horse Press has an internal fluid tank used for both producing and consuming fluids:

- **Producing fluids** — recipes like seed pressing fill the tank with the result
- **Consuming fluids** — recipes can also require a fluid input that is drained from the tank as part of the press cycle
- Right-click with a filled bucket to fill the tank, or with an empty bucket to drain it
- Connect a fluid pipe or tank for automated fluid transfer

## Notes

- The press is two blocks tall
- Some recipes require multiple input items
- Some recipes require a specific fluid in the tank in addition to the item input
- The creature requires a clear path around the press
- Right-click with an empty hand to release the attached creature

## Example Recipes

Common pressing operations:
- Seeds to Seed Oil — fills the tank
- Bone Meal + 1000 mB Milk to Slime Ball — consumes fluid from the tank, produces an item
- Sugar Cane to Paper, Honey Bottle to Sugar
- Ice / Snow to Water — fills the tank with water
- Flowers to Dye
