---
navigation:
  title: Work Saddle
  position: 10
item_ids:
  - horsepowered:work_saddle
---

# Work Saddle

The Work Saddle captures any creature in the `horsepowered:valid_worker` tag (horses, donkeys, mules, llamas, trader llamas) into a portable item, then releases it elsewhere on right-click.

<Row>
<ItemImage id="horsepowered:work_saddle" scale="4" />
</Row>

## Usage

1. **Capture**: hold an empty work saddle and right-click a valid worker. The mob is stored on the saddle.
2. **Release**: right-click any block face with the carrying saddle to spawn the worker onto that block. The saddle becomes empty again.
3. **Hover tooltip**: a carrying saddle shows which mob it currently holds.

## Restrictions

- Only mobs in `horsepowered:valid_worker` can be captured
- Captured mobs are dropped from any passenger seat and detached from any lead at capture time
- Releasing a worker fails if a mob with the same UUID is already in the world

## Recipe

<RecipeFor id="horsepowered:work_saddle" />
