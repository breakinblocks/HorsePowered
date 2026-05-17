---
navigation:
  title: Chopping Block
  position: 3
item_ids:
  - horsepowered:chopping_block
---

# Chopping Block

The Chopping Block is a manual processing station for chopping logs and other items using an axe.

<Row>
<ItemImage id="horsepowered:chopping_block" scale="4" />
</Row>

## Usage

1. Place the Chopping Block
2. Right-click with a log or other choppable item to place it on the block
3. Right-click with an axe to chop the item
4. Right-click to collect the output

## Recipe

<RecipeFor id="horsepowered:chopping_block" />

## Hunger Cost

Every axe strike consumes a small baseline amount of hunger (configurable). Individual chopping recipes can also declare a `hungerCost` in their JSON, which is **added on top** of the baseline only on the Chopping Block. The Horse Chopper ignores it. When a recipe has a hunger cost, JEI and EMI show a "Hunger" line on the manual-chopping category.

## Notes

- Axes may take durability damage when chopping (configurable)
- Works with the same recipes as the Horse Chopper (unless the recipe is restricted with a `tier` of `"horse"`)
- Different logs can produce different amounts of planks
- Manual chopping takes more strikes per recipe than horse chopping by a configurable multiplier; JEI and EMI display the actual chop count

## Tips

- Keep a spare axe handy for extended chopping sessions
- The chopping block is great for early game wood processing
- Upgrade to the Horse Chopper for automated processing
