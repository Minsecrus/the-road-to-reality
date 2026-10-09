package com.trtr.crafting;

/** Pure shape rules. Each bit represents a retained flint cell; chips must start at an exposed edge. */
public final class KnappingPattern {
    public static final int SIZE = 5;
    public static final int FULL = (1 << (SIZE * SIZE)) - 1;
    public static final int BLADE = mask("00100", "01110", "01110", "00100", "00100");

    private static int mask(String... rows) {
        int bits = 0;
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                if (rows[y].charAt(x) == '1') bits |= 1 << (y * SIZE + x);
            }
        }
        return bits;
    }

    public static boolean canChip(int mask, int cell) {
        if (cell < 0 || cell >= SIZE * SIZE || (mask & (1 << cell)) == 0) return false;
        int x = cell % SIZE;
        int y = cell / SIZE;
        return x == 0 || x == SIZE - 1 || y == 0 || y == SIZE - 1
                || (mask & (1 << (cell - 1))) == 0 || (mask & (1 << (cell + 1))) == 0
                || (mask & (1 << (cell - SIZE))) == 0 || (mask & (1 << (cell + SIZE))) == 0;
    }

    public static boolean ruined(int mask) { return (mask & BLADE) != BLADE; }
    private KnappingPattern() {}
}
