---
navigation:
  title: Granite Anvil
  position: 7
item_ids:
  - horsepowered:granite_anvil
---

# Granite Anvil

The Granite Anvil is a manual crushing station for breaking harder materials into smaller bits. Strike placed items with a stone-tier or better pickaxe to crush stone into cobblestone, cobblestone into gravel, and so on.

<Row>
<ItemImage id="horsepowered:granite_anvil" scale="4" />
</Row>

## Usage

1. Place the Granite Anvil
2. Right-click with a stone block or other crushable item to place it on the anvil
3. Right-click with a stone-tier (or better) pickaxe to crush the item — multiple strikes are needed per item
4. The finished output either drops at the anvil or stays in the internal slot (configurable)

## Recipe

<RecipeFor id="horsepowered:granite_anvil" />

## Tool Requirement

A wooden pickaxe is not strong enough — you need at least a **stone** pickaxe to operate the anvil. Iron, gold, diamond, and netherite pickaxes also work.

## Hunger Cost

Every strike consumes a small baseline amount of hunger (configurable). Individual crushing recipes can also declare a `hungerCost` in their JSON, which is added on top of the baseline. When a recipe has a hunger cost, JEI and EMI show a "Hunger" line on the crushing category.

## Notes

- Pickaxes may take durability damage when crushing (configurable)
- Each strike contributes one tick toward the recipe; the number of strikes shown in JEI / EMI is `time × crushingMultiplier` (default 4)
- Jade displays the remaining strikes while crushing is in progress
- Hover over the anvil with the Granite Anvil's item in JEI / EMI to see all available crushing recipes
