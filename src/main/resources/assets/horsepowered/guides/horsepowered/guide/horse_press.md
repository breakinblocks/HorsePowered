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
2. The attached creature will press items automatically
3. Fluids are stored internally or output to adjacent tanks
4. Right-click to extract any solid outputs

## Recipe

<RecipeFor id="horsepowered:press" />

## Fluid Output

The Horse Press can produce fluids from certain recipes:
- Fluids are stored in an internal tank
- Connect a fluid pipe or tank to extract fluids
- Hold a bucket and right-click to manually extract fluids

## Bottling

Buckets work because the game gives them a fluid-handler capability. Glass bottles have none, so they cannot take fluid out of a tank on their own. The press covers that with a bottling recipe list.

Right-click the press with a bottle (or whatever container a recipe names) and it drains that recipe's fluid from the output tank and hands back the filled item. Right-click with the filled item and the reverse happens: the fluid goes into the input tank and you get the empty container back.

One bottling recipe ships by default: a glass bottle takes 250 mB of water out of the press and becomes a water bottle. Press ice, snow, or dripstone for the water first.

Modpack makers can add more at `data/<namespace>/recipes/bottling/<recipe>.json`:

```json
{
  "type": "horsepowered:bottling",
  "container": { "item": "minecraft:glass_bottle" },
  "fluid": { "id": "minecraft:water", "amount": 250 },
  "result": { "item": "minecraft:potion", "count": 1, "nbt": { "Potion": "minecraft:water" } }
}
```

* `container`: Ingredient. The empty container the player holds.
* `fluid`: the fluid and the amount in mB moved per click.
* `result`: the filled item handed back. NBT is respected, so potions and other data-carrying items work.
* `priority`: optional, sort order in JEI and EMI. Lower numbers display first.

This is the way to make a fluid that only exists in bottled form (fruit juices and the like) removable from the press by hand. JEI and EMI both list every bottling recipe under a Bottling category.

## Notes

- The press is two blocks tall
- Some recipes require multiple input items
- The creature requires a clear path around the press; the floor blocks under that path scale how fast it works. See [Horse Path & Speed](horse_path.md)
- Right-click with an empty hand to release the attached creature

## Example Recipes

Common pressing operations:
- Seeds to Seed Oil (requires mod support)
- Leaves to Water
- Various fruits to juice (with mod support)
