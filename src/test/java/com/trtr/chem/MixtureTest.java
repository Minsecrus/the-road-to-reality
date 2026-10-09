package com.trtr.chem;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class MixtureTest {

    @Test
    void purityIsMolarFractionView() {
        // 粗铜：9 mol Cu + 1 mol Fe → 纯度 0.9（ADR-006）
        Mixture crude = Mixture.of(1358, TRTRSubstances.COPPER, 9)
                .plus(TRTRSubstances.IRON, 1);
        assertEquals(0.9, crude.purityOf(TRTRSubstances.COPPER), 1e-12);
    }

    @Test
    void massSumsComponents() {
        // 1 mol H2O + 1 mol Fe = 18.015 + 55.845
        Mixture m = Mixture.of(300, TRTRSubstances.WATER, 1)
                .plus(TRTRSubstances.IRON, 1);
        assertEquals(73.86, m.totalMass(), 1e-9);
    }

    @Test
    void zeroOrNegativeAmountsRemoveComponent() {
        Mixture m = Mixture.of(300, TRTRSubstances.IRON, 1)
                .plus(TRTRSubstances.IRON, -1);
        assertFalse(m.contains(TRTRSubstances.IRON));
        assertEquals(0, m.totalMoles());
    }

    @Test
    void immutableByConstruction() {
        Mixture base = Mixture.of(300, TRTRSubstances.IRON, 1);
        base.plus(TRTRSubstances.COPPER, 1);
        assertFalse(base.contains(TRTRSubstances.COPPER));
    }

    @Test
    void phaseFollowsTemperature() {
        assertEquals(Substance.Phase.SOLID, TRTRSubstances.WATER.phaseAt(250));
        assertEquals(Substance.Phase.LIQUID, TRTRSubstances.WATER.phaseAt(300));
        assertEquals(Substance.Phase.GAS, TRTRSubstances.WATER.phaseAt(400));
    }
}
