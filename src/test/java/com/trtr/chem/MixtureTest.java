package com.trtr.chem;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MixtureTest {

    @Test
    void 纯度视图() {
        // 粗铜场景：96 mol Cu + 4 mol S 杂质
        Mixture crude = Mixture.of(1400, TRTRSubstances.COPPER, 96)
                .plus(new Substance("sulfur", Formula.parse("S"), 388, 718, 0xd8c84a), 4);
        assertEquals(0.96, crude.purityOf(TRTRSubstances.COPPER), 1e-9);
    }

    @Test
    void 总质量按摩尔质量加权() {
        // 2 mol H2O = 36.03 g
        Mixture m = Mixture.of(300, TRTRSubstances.WATER, 2);
        assertEquals(36.03, m.totalMass(), 1e-3);
    }

    @Test
    void 不可变性() {
        Mixture a = Mixture.of(300, TRTRSubstances.WATER, 1);
        Mixture b = a.plus(TRTRSubstances.SILICA, 1);
        assertEquals(1, a.components().size());
        assertEquals(2, b.components().size());
        // 减到 0 的成分不出现在结果中
        Mixture c = b.plus(TRTRSubstances.WATER, -1);
        assertEquals(1, c.components().size());
    }
}
