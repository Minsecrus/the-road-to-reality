package com.trtr.chem;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class FormulaTest {

    @Test
    void parsesSimpleFormula() {
        Formula f = Formula.parse("H2O");
        assertEquals(2, f.atoms().get(Element.H));
        assertEquals(1, f.atoms().get(Element.O));
        // 2×1.008 + 15.999 = 18.015
        assertEquals(18.015, f.molarMass(), 1e-9);
    }

    @Test
    void parsesNestedGroups() {
        Formula f = Formula.parse("Fe2(SO4)3");
        assertEquals(2, f.atoms().get(Element.FE));
        assertEquals(3, f.atoms().get(Element.S));
        assertEquals(12, f.atoms().get(Element.O));
        // 2×55.845 + 3×32.06 + 12×15.999 = 399.858
        assertEquals(399.858, f.molarMass(), 1e-9);
    }

    @Test
    void defaultSubscriptIsOne() {
        Formula f = Formula.parse("SiO2");
        assertEquals(1, f.atoms().get(Element.SI));
        assertEquals(2, f.atoms().get(Element.O));
    }

    @Test
    void rejectsMalformedInput() {
        assertThrows(IllegalArgumentException.class, () -> Formula.parse("Fe2(SO4"));
        assertThrows(IllegalArgumentException.class, () -> Formula.parse("h2o"));
        assertThrows(IllegalArgumentException.class, () -> Formula.parse(""));
    }
}
