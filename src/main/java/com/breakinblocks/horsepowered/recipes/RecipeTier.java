package com.breakinblocks.horsepowered.recipes;

public enum RecipeTier {
    ANY("any"),
    HAND_ONLY("hand_only"),
    HORSE_ONLY("horse_only");

    private final String name;

    RecipeTier(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean allowsHand() {
        return this != HORSE_ONLY;
    }

    public boolean allowsHorse() {
        return this != HAND_ONLY;
    }

    public static RecipeTier fromString(String s) {
        if (s == null || s.isEmpty()) return ANY;
        for (RecipeTier t : values()) {
            if (t.name.equalsIgnoreCase(s)) return t;
        }
        throw new IllegalArgumentException("Unknown recipe tier: " + s);
    }
}
