package com.trtr.chem;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/** M1 基准反应（002：CaCO₃ 分解、Fe₂O₃ 还原、燃烧窗口）。电解水待电位维度接入后补测。 */
class ReactionTest {

    @Test
    void calcinationDecomposesAbove1170K() {
        Mixture cold = Mixture.of(1000, TRTRSubstances.CALCIUM_CARBONATE, 1);
        assertFalse(TRTRReactions.LIME_CALCINATION.solve(cold).occurred());

        Mixture hot = Mixture.of(1200, TRTRSubstances.CALCIUM_CARBONATE, 1);
        Reaction.Result r = TRTRReactions.LIME_CALCINATION.solve(hot);
        assertTrue(r.occurred());
        assertEquals(1, r.output().amountOf(TRTRSubstances.CALCIUM_OXIDE), 1e-9);
        assertEquals(1, r.output().amountOf(TRTRSubstances.CARBON_DIOXIDE), 1e-9);
        assertFalse(r.output().contains(TRTRSubstances.CALCIUM_CARBONATE));
    }

    @Test
    void ironReductionIsLimitingReactantComplete() {
        // 1 Fe2O3 + 2 CO → CO 限制，extent = 2/3，余 1/3 Fe2O3，产 4/3 Fe
        Mixture m = Mixture.of(1200, TRTRSubstances.HEMATITE, 1)
                .plus(TRTRSubstances.CARBON_MONOXIDE, 2);
        Reaction.Result r = TRTRReactions.IRON_REDUCTION.solve(m);
        assertTrue(r.occurred());
        assertEquals(2.0 / 3.0, r.extent(), 1e-9);
        assertEquals(4.0 / 3.0, r.output().amountOf(TRTRSubstances.IRON), 1e-9);
        assertEquals(1.0 / 3.0, r.output().amountOf(TRTRSubstances.HEMATITE), 1e-9);
    }

    @Test
    void combustionWindowSelectsByTemperature() {
        // 低温篝火：C + O2 → CO2
        Mixture low = Mixture.of(1000, TRTRSubstances.CARBON, 1)
                .plus(TRTRSubstances.OXYGEN, 1);
        assertEquals("carbon_combustion",
                TRTRReactions.seedRegistry().tick(low).orElseThrow().reaction().id());
        // 高温竖炉：2C + O2 → 2CO
        Mixture high = Mixture.of(1300, TRTRSubstances.CARBON, 2)
                .plus(TRTRSubstances.OXYGEN, 1);
        assertEquals("co_formation",
                TRTRReactions.seedRegistry().tick(high).orElseThrow().reaction().id());
    }

    @Test
    void slagFormationNeedsBothReactants() {
        Mixture onlyLime = Mixture.of(1500, TRTRSubstances.CALCIUM_OXIDE, 1);
        assertFalse(TRTRReactions.SLAG_FORMATION.solve(onlyLime).occurred());
    }
}
