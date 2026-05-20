"""Build animal_trap.png from the vanilla spawner base.

Maps the vanilla spawner's dark blue-purple cage palette to warm wood tones,
and recolors the magenta "eyes" to iron-grey to suggest the iron chain
component of the trap's crafting recipe.
"""
from pathlib import Path
import sys

from PIL import Image


# Vanilla spawner palette -> wooden trap palette.
PALETTE_MAP = {
    (24, 44, 57):   (45, 30, 15),    # dark blue -> very dark brown (outline)
    (30, 24, 39):   (35, 22, 10),    # dark purple -> bark shadow
    (42, 36, 53):   (75, 50, 25),    # mid purple -> wood bark
    (42, 68, 85):   (110, 75, 40),   # mid blue -> oak plank
    (64, 98, 120):  (155, 110, 65),  # light blue -> light oak
    (110, 4, 83):   (180, 180, 180), # magenta eye -> iron chain grey
}


def main(src: Path, dst: Path) -> None:
    img = Image.open(src).convert("RGBA")
    if img.size != (16, 16):
        raise SystemExit(f"unexpected size {img.size}, expected (16, 16)")

    pixels = img.load()
    unmapped = set()
    for y in range(img.height):
        for x in range(img.width):
            r, g, b, a = pixels[x, y]
            if a == 0:
                continue
            key = (r, g, b)
            if key in PALETTE_MAP:
                nr, ng, nb = PALETTE_MAP[key]
                pixels[x, y] = (nr, ng, nb, a)
            else:
                unmapped.add(key)

    if unmapped:
        print(f"warning: unmapped colors left untouched: {sorted(unmapped)}")

    dst.parent.mkdir(parents=True, exist_ok=True)
    img.save(dst, "PNG")
    print(f"wrote {dst} ({img.size[0]}x{img.size[1]})")


if __name__ == "__main__":
    src = Path(sys.argv[1])
    dst = Path(sys.argv[2])
    main(src, dst)
