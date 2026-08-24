package com.trtr.chem;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReactionTest {

    @Test
    void 煅烧石灰石_按计量比完全转化() {
        // 3 mol CaCO3 → 3 mol CaO + 3 mol CO2
        Mixture charge = Mixture.of(1200, TRTRSubstances.CALCIUM_CARBONATE, 3);
        Reaction.Result r = TRTRReactions.LIME_CALCINATION.solve(charge);
        assertTrue(r.occurred());
        assertEquals(3, r.extent(), 1e-9);
        assertEquals(3, r.output().amountOf(TRTRSubstances.CALCIUM_OXIDE), 1e-9);
        assertEquals(3, r.output().amountOf(TRTRSubstances.CARBON_DIOXIDE), 1e-9);
        assertEquals(0, r.output().amountOf(TRTRSubstances.CALCIUM_CARBONATE), 1e-9);
    }

    @Test
    void 温度门控_低温不反应() {
        Mixture charge = Mixture.of(298, TRTRSubstances.CALCIUM_CARBONATE, 3);
        assertFalse(TRTRReactions.LIME_CALCINATION.solve(charge).occurred());
    }

    @Test
    void 限制反应物_铁还原() {
        // 1 mol Fe2O3 + 2 mol CO：CO 限制，extent = 2/3
        Mixture charge = Mixture.of(1300,
                TRTRSubstances.HEMATITE, 1).plus(TRTRSubstances.CARBON_MONOXIDE, 2);
        Reaction.Result r = TRTRReactions.IRON_REDUCTION.solve(charge);
        assertTrue(r.occurred());
        assertEquals(2.0 / 3.0, r.extent(), 1e-9);
        // 产物铁 = 2 * extent
        assertEquals(4.0 / 3.0, r.output().amountOf(TRTRSubstances.IRON), 1e-9);
        // 剩余 Fe2O3 = 1 - extent
        assertEquals(1.0 / 3.0, r.output().amountOf(TRTRSubstances.HEMATITE), 1e-9);
        assertEquals(0, r.output().amountOf(TRTRSubstances.CARBON_MONOXIDE), 1e-9);
    }

    @Test
    void 造渣_杂质SiO2被消耗() {
        // 粗石灰（CaO 为主 + SiO2 杂质）在炉温下自行造渣
        Mixture charge = Mixture.of(1500,
                TRTRSubstances.CALCIUM_OXIDE, 10).plus(TRTRSubstances.SILICA, 2);
        Mixture out = TRTRReactions.SLAG_FORMATION.solve(charge).output();
        assertEquals(2, out.amountOf(TRTRSubstances.CALCIUM_SILICATE), 1e-9);
        assertEquals(8, out.amountOf(TRTRSubstances.CALCIUM_OXIDE), 1e-9);
        assertEquals(0, out.amountOf(TRTRSubstances.SILICA), 1e-9);
    }

    @Test
    void 温度选择_低温燃烧高温产CO() {
        ReactionRegistry registry = TRTRReactions.seedRegistry();
        // 低温（1000K，篝火）：完全燃烧
        Mixture cold = Mixture.of(1000,
                TRTRSubstances.CARBON, 3).plus(TRTRSubstances.OXYGEN, 1);
        Reaction.Result first = registry.tick(cold).orElseThrow();
        assertEquals(TRTRReactions.CARBON_COMBUSTION.id(), first.reaction().id());
        // 剩 2C，无氧，链式终止
        assertFalse(registry.tick(first.output()).isPresent());

        // 高温（1500K，竖炉）：产 CO（还原剂）
        Mixture hot = Mixture.of(1500,
                TRTRSubstances.CARBON, 3).plus(TRTRSubstances.OXYGEN, 1);
        assertEquals(TRTRReactions.CO_FORMATION.id(),
                registry.tick(hot).orElseThrow().reaction().id());
    }

    @Test
    void 竖炉全流程_碳氧平衡到铁() {
        // 模拟一次竖炉加料：6C + 3O2 + 2Fe2O3（石灰造渣另计）
        Mixture charge = Mixture.of(1500,
                TRTRSubstances.CARBON, 6)
                .plus(TRTRSubstances.OXYGEN, 6)
                .plus(TRTRSubstances.HEMATITE, 2);
        ReactionRegistry registry = TRTRReactions.seedRegistry();
        boolean progressed = true;
        while (progressed) {
            var opt = registry.tick(charge);
            if (opt.isEmpty()) {
                progressed = false;
            } else {
                charge = opt.get().output();
            }
        }
        // 全部铁被还原
        assertEquals(4, charge.amountOf(TRTRSubstances.IRON), 1e-9);
        assertEquals(0, charge.amountOf(TRTRSubstances.HEMATITE), 1e-9);
    }
}
