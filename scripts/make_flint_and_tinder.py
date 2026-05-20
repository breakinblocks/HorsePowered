"""Build flint_and_tinder.png from the vanilla flint_and_steel base."""
from pathlib import Path
import sys

from PIL import Image


STEEL_X_MAX = 7
STEEL_Y_MAX = 10

STEEL_TO_TINDER = {
    (0x47, 0x47, 0x47): (0x4a, 0x2e, 0x15),
    (0xb9, 0xb9, 0xb9): (0xd8, 0xa5, 0x58),
    (0x9e, 0x9e, 0x9e): (0xb8, 0x84, 0x3a),
    (0x71, 0x71, 0x71): (0x8a, 0x5d, 0x24),
    (0x32, 0x32, 0x32): (0x2a, 0x18, 0x08),
}


def main(src: Path, dst: Path) -> None:
    img = Image.open(src).convert("RGBA")
    if img.size != (16, 16):
        raise SystemExit(f"unexpected size {img.size}, expected (16, 16)")
    pixels = img.load()
    for y in range(img.height):
        for x in range(img.width):
            r, g, b, a = pixels[x, y]
            if a == 0:
                continue
            if x > STEEL_X_MAX or y > STEEL_Y_MAX:
                continue
            key = (r, g, b)
            if key in STEEL_TO_TINDER:
                nr, ng, nb = STEEL_TO_TINDER[key]
                pixels[x, y] = (nr, ng, nb, a)
    dst.parent.mkdir(parents=True, exist_ok=True)
    img.save(dst, "PNG")
    print(f"wrote {dst} ({img.size[0]}x{img.size[1]})")


if __name__ == "__main__":
    main(Path(sys.argv[1]), Path(sys.argv[2]))
