package com.trtr.chem;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FormulaTest {

    @Test
    void 摩尔质量_水() {
        // 2*1.008 + 15.999 = 18.015
        assertEquals(18.015, Formula.parse("H2O").molarMass(), 1e-9);
    }

    @Test
    void 摩尔质量_碳酸钙() {
        // 40.078 + 12.011 + 3*15.999
        assertEquals(100.086, Formula.parse("CaCO3").molarMass(), 1e-3);
    }

    @Test
    void 括号嵌套_氢氧化铁() {
        // Fe(OH)3: 55.845 + 3*(1.008+15.999)
        assertEquals(106.866, Formula.parse("Fe(OH)3").molarMass(), 1e-3);
    }

    @Test
    void 多层括号() {
        Formula f = Formula.parse("Fe2((OH)2SO4)2");
        assertEquals(2, f.atoms().get(Element.FE));
        assertEquals(4, f.atoms().get(Element.H));
        assertEquals(2, f.atoms().get(Element.S));
        assertEquals(12, f.atoms().get(Element.O));
    }

    @Test
    void 默认下标为1() {
        Formula f = Formula.parse("FeO");
        assertEquals(1, f.atoms().get(Element.FE));
        assertEquals(1, f.atoms().get(Element.O));
    }

    @Test
    void 非法输入抛异常() {
        assertThrows(IllegalArgumentException.class, () -> Formula.parse("fe2o3"));
        assertThrows(IllegalArgumentException.class, () -> Formula.parse("Fe2(o3"));
        assertThrows(IllegalArgumentException.class, () -> Formula.parse("2Fe"));
    }

    @Test
    void 状态随温度() {
        Substance water = TRTRSubstances.WATER;
        assertTrue(water.phaseAt(250) == Substance.Phase.SOLID);
        assertTrue(water.phaseAt(300) == Substance.Phase.LIQUID);
        assertTrue(water.phaseAt(400) == Substance.Phase.GAS);
    }
}
