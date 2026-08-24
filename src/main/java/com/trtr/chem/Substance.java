package com.trtr.chem;

/**
 * 物质：由数据定义（ADR-003）。等价性按 id 判定。
 *
 * @param id          注册 id，如 "caco3"
 * @param formula     化学式
 * @param meltK       熔点 K（未知为 Double.NaN）
 * @param boilK       沸点 K（未知为 Double.NaN）
 * @param colorTint   染色（通用物品/流体贴图用），0xRRGGBB
 */
public record Substance(String id, Formula formula, double meltK, double boilK, int colorTint) {

    public double molarMass() {
        return formula.molarMass();
    }

    public enum Phase { SOLID, LIQUID, GAS }

    public Phase phaseAt(double temperatureK) {
        if (!Double.isNaN(boilK) && temperatureK >= boilK) {
            return Phase.GAS;
        }
        if (!Double.isNaN(meltK) && temperatureK >= meltK) {
            return Phase.LIQUID;
        }
        return Phase.SOLID;
    }
}
