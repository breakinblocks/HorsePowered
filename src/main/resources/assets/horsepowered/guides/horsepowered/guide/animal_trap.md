---
navigation:
  title: Animal Trap
  position: 8
item_ids:
  - horsepowered:animal_trap
---

# Animal Trap

The Animal Trap is a passive wooden cage that lures and captures passive animals using bait. Once an animal is caught it stays inside the trap as stored NBT (not as a live entity) and produces its normal death drops every couple of minutes into the trap's internal inventory. Hoppers below the trap pull the drops out for processing, automation, or storage.

<Row>
<ItemImage id="horsepowered:animal_trap" scale="4" />
</Row>

## Usage

1. Place the Animal Trap on the ground. It can be waterlogged for fish recipes.
2. Right-click the trap with a valid bait item (wheat for a cow, kelp for salmon, and so on). The bait drops into the trap's bait slot and begins spinning inside the cage.
3. Wait. Every recipe has a minimum trapping time. Jade shows the countdown.
4. Once the minimum time has elapsed the trap rolls a 5% chance per second to capture the target animal. Average extra wait is around 20 seconds.
5. On a successful capture the bait is consumed and the animal appears spinning inside the cage.
6. Keep matching bait in the trap. The trap produces drops every 2.5 minutes, and each cycle has a small chance to consume one bait (built-in recipes consume around one bait per ten drops). When the bait slot is empty the drop timer pauses until matching bait is supplied, so a hopper feed keeps things running unattended.
7. Pull the drops out with a hopper below the trap, or break the trap and take everything with you.

## Bait Recipes

Default trapping recipes that ship with the mod can be found in JEI/EMI

## Drops

When the captured animal is set, the trap rolls the entity's vanilla death loot table every 2.5 minutes. Drops land in the five output slots inside the trap. If those slots fill up the next roll's items pop out on top of the trap as item entities.

Examples of what each captured animal produces over time can be found in:

* Cow: beef and leather
* Pig: porkchop
* Chicken: chicken and feathers
* Sheep: mutton plus wool when shorn
* Rabbit: rabbit, rabbit hide, occasionally rabbit's foot
* Goat: nothing on its loot table by default (vanilla goats only drop horns when ramming a block, which the trap cannot reproduce)
* Mooshroom: beef and leather, same as cow
* Strider: string and the occasional saddle reroll on the loot table
* Salmon: raw salmon

## Player Interactions

* Right-click with bait: insert bait into the bait slot.
* Right-click with an empty hand: drop every item in the trap on the ground (the bait if any, plus all output slots). The captured animal stays inside.
* Sneak + right-click: release the captured animal back into the world. The trap returns to empty.
* Mine the trap with a stone-tier or better axe: the trap drops as an item with its full NBT preserved, including any captured animal and any items still in the output slots. Place it again anywhere and it picks up exactly where it left off, including the drop timer.

## Hopper Automation

* Hoppers feeding into the trap can only insert valid bait into the bait slot. Random items are rejected by the slot's validity check. Captured traps keep accepting matching bait so a hopper can sustain the drop cycles unattended.
* Hoppers below the trap pull only from the five drop slots. The bait slot is extraction-locked even while empty, so an automated bait-pulling setup is not possible.
* The block is fully waterloggable, so a hopper or pipe can sit in flowing water under a fish trap without breaking the water column.

## Biome and Waterlog Conditions

When a recipe specifies a biome tag or a waterlogged requirement, the trap only counts dice rolls when those conditions are met. The bait stays, the timer keeps running, but no animal is ever caught until you relocate or waterlog the trap.

Jade displays in red as `Wrong biome` or `Needs water` when bait is loaded but the conditions are not satisfied.

## Crafting Recipe

<RecipeFor id="horsepowered:animal_trap" />

## Notes

* The captured animal renders inside the cage like a vanilla mob spawner mob, spinning slowly. The same renderer is used for the bait item before capture.
* Mining preserves the captured entity
* Jade shows live progress, the captured animal's name, and a countdown until the next drop. It also lists the contents of the trap's inventory.
* JEI has a Trapping category showing every registered recipe along with its bait, time, conditions, and the expected animal, named in text and rendered as the matching spawn egg. Recipes can override both with the optional `title` and `icon` fields, which is useful in packs that hide spawn eggs from the item list.
