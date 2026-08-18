---
navigation:
  title: Horse Press
  position: 5
item_ids:
  - horsepowered:press
---

# Horse Press

The Horse Press is a horse-powered machine that presses items to extract fluids or produce other outputs. Useful for extracting oils, juices, and other liquids.

<Row>
<ItemImage id="horsepowered:press" scale="4" />
</Row>

## Setup

1. Place the Horse Press in an area with at least a 7x7 clear space around it.
2. Lead a horse (or other valid creature) with a lead.
3. Right-click the press while holding the lead to attach the creature.
4. The creature walks in circles, powering the press.

## Checking the Working Area

**Shift+Right-click** the press with an empty hand to visualize the required working area. Green = clear, Red = obstructed.

## Usage

1. Right-click with items to insert them.
2. Right-click with a filled bucket to fill the internal tank, or with an empty bucket to drain it.
3. The attached creature presses items automatically.
4. Right-click to extract any solid outputs.

## Recipe

<RecipeFor id="horsepowered:press" />

## Fluid Tank

The Horse Press has an internal fluid tank used for both producing and consuming fluids:

- Recipes can fill the tank (seed pressing produces oil), or drain it (a recipe may consume a fluid input).
- Right-click with a filled bucket to fill, or an empty bucket to drain.
- Connect a fluid pipe or tank for automated fluid transfer.

## Bottling

Buckets work because the game gives them a fluid-handler capability. Glass bottles have none, so they cannot take fluid out of a tank on their own. The press covers that with a bottling recipe list.

Right-click the press with a bottle (or whatever container a recipe names) and it drains that recipe's fluid from the output tank and hands back the filled item. Right-click with the filled item and the reverse happens: the fluid goes into the input tank and you get the empty container back.

One bottling recipe ships by default: a glass bottle takes 250 mB of water out of the press and becomes a water bottle. Press ice, snow, or dripstone for the water first.

Modpack makers can add more at `data/<namespace>/recipe/bottling/<recipe>.json`:

```json
{
  "type": "horsepowered:bottling",
  "container": "minecraft:glass_bottle",
  "fluid": { "id": "minecraft:water", "amount": 250 },
  "result": {
    "id": "minecraft:potion",
    "components": { "minecraft:potion_contents": { "potion": "minecraft:water" } }
  }
}
```

* `container`: Ingredient. The empty container the player holds.
* `fluid`: the fluid and the amount in mB moved per click.
* `result`: the filled item handed back. Components are respected, so potions and other data-carrying items work.
* `priority`: optional, sort order in JEI. Lower numbers display first.

This is the way to make a fluid that only exists in bottled form (fruit juices and the like) removable from the press by hand. JEI lists every bottling recipe under a Bottling category.

## Notes

- The press is two blocks tall.
- Some recipes require multiple input items or a specific fluid in the tank.
- Right-click with an empty hand to release the attached creature.
- The floor under the path scales work speed. See [Horse Path & Speed](horse_path.md).
- Available pressing recipes can be found in JEI/EMI.
