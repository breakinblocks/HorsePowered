---
navigation:
  title: Creative Battery
  position: 11
item_ids:
  - horsepowered:creative_battery
---

# Creative Battery

The Creative Battery is a creative-mode test block for prototyping energy setups. It holds an effectively infinite amount of Forge Energy and accepts or outputs power at the same rate, making it a quick way to stand in for either a power source or a power sink while you wire up machines.

<Row>
<ItemImage id="horsepowered:creative_battery" scale="4" />
</Row>

## Specifications

- **Capacity**: `Integer.MAX_VALUE` FE (about 2.1 billion)
- **Receive rate**: `Integer.MAX_VALUE` FE/tick
- **Extract rate**: `Integer.MAX_VALUE` FE/tick
- **Auto-push**: every tick, any stored energy is pushed to adjacent FE consumers

## Usage

- As a **source**: insert it into the build, fill it once from a Generator (or `/setblock`), and it will keep pushing power until something consumes it. Once drained it stops pushing — wire a generator in if you want a perpetual supply.
- As a **sink**: connect the output of a machine and watch the buffer fill. The battery accepts as much as anything offers.

## Restrictions

- Creative-only: not craftable, only available in the creative inventory.
- Unbreakable in survival (bedrock-grade hardness) and drops nothing when broken in creative.
- Cannot be pushed by pistons.
