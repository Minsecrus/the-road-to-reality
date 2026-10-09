package com.trtr.mining;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/** 003 连续硬度曲线：连续、单调、区间端点与设计值吻合。 */
class HardnessModelTest {

    @Test
    void efficiencyMatches003Zones() {
        assertEquals(1.0F, HardnessModel.efficiency(1.0), 1e-9);   // 正常切削
        assertEquals(1.0F, HardnessModel.efficiency(3.0), 1e-9);
        assertEquals(0.1F, HardnessModel.efficiency(0.0), 1e-9);   // 零差：极慢起点
        assertEquals(0.03F, HardnessModel.efficiency(-1.0), 1e-9); // 负一差
        assertEquals(0.024F, HardnessModel.efficiency(-2.5), 1e-8); // 逐渐趋向下限
    }

    @Test
    void efficiencyIsMonotonic() {
        float prev = 0;
        for (double d = -3; d <= 2; d += 0.01) {
            float f = HardnessModel.efficiency(d);
            assertTrue(f >= prev - 1e-9, "非单调于 diff=" + d);
            prev = f;
        }
    }

    @Test
    void wearOnlyOnNegativeDiffAndScales() {
        assertEquals(0, HardnessModel.extraWear(0.5));
        assertEquals(0, HardnessModel.extraWear(0));
        assertEquals(1, HardnessModel.extraWear(-0.5));  // 燧石(6.5)凿花岗岩(7)
        assertEquals(3, HardnessModel.extraWear(-2.5));  // 徒手凿石头
    }

    @Test
    void efficiencyDoesNotJumpAtZoneBoundaries() {
        for (double boundary : new double[] {-1, 0, 1}) {
            assertEquals(HardnessModel.efficiency(boundary - 1e-6),
                    HardnessModel.efficiency(boundary + 1e-6), 2e-6);
        }
        assertTrue(HardnessModel.efficiency(-1000) >= 0.02F);
    }
}
