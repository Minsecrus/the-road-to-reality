package com.trtr.mining;

/**
 * 003 连续硬度模型的曲线部分（纯函数，可单测）。
 * 效率与磨损均由硬度差 diff = h_tool − h_rock 决定，无等级 enum（ADR-005）：
 *   diff ≥ 1    正常切削（不干预，交还原版速度体系）
 *   0 ≤ diff <1 交界：缓慢滑入极慢区
 *   −1 ≤ diff <0 极慢 + 磨损急增
 *   diff < −1   速度趋零 + 崩刃加速（涌现式"挖不动"，非硬性禁止）
 */
public final class HardnessModel {

    private static final float FLOOR = 0.02F;

    /** 速度因子：1 = 正常，递减至趋零下限。连续、单调。 */
    public static float efficiency(double diff) {
        if (diff >= 1) {
            return 1.0F;
        }
        if (diff >= 0) {
            return (float) (0.1 + 0.9 * diff);
        }
        if (diff >= -1) {
            return (float) (0.03 + 0.07 * (diff + 1));
        }
        // Join the -1 endpoint continuously, approaching FLOOR for softer tools.
        return (float) (FLOOR + 0.01 / -diff);
    }

    /** 额外磨损点数：正差基础磨损（0 额外），负差急增（003 磨损模型）。 */
    public static int extraWear(double diff) {
        return diff >= 0 ? 0 : (int) Math.ceil(-diff);
    }

    private HardnessModel() {}
}
