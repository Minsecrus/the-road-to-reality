package com.trtr.crafting;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class KnappingPatternTest {
    @Test
    void bladeCanBeMadeEntirelyFromExposedEdges() {
        int mask = KnappingPattern.FULL;
        for (int pass = 0; pass < 25 && mask != KnappingPattern.BLADE; pass++) {
            boolean progressed = false;
            for (int cell = 0; cell < 25; cell++) {
                if ((KnappingPattern.BLADE & (1 << cell)) == 0 && KnappingPattern.canChip(mask, cell)) {
                    mask &= ~(1 << cell);
                    progressed = true;
                }
            }
            assertTrue(progressed, "The target must be reachable without destroying the blade");
            assertFalse(KnappingPattern.ruined(mask));
        }
        assertEquals(KnappingPattern.BLADE, mask);
    }

    @Test
    void rejectsInteriorMissingAndOutOfRangeCells() {
        assertFalse(KnappingPattern.canChip(KnappingPattern.FULL, 12));
        assertFalse(KnappingPattern.canChip(KnappingPattern.FULL, -1));
        assertFalse(KnappingPattern.canChip(KnappingPattern.FULL, 25));
        assertTrue(KnappingPattern.canChip(KnappingPattern.FULL, 0));
        assertFalse(KnappingPattern.canChip(KnappingPattern.FULL & ~1, 0));
        assertTrue(KnappingPattern.ruined(KnappingPattern.FULL & ~(1 << 2)));
    }
}
