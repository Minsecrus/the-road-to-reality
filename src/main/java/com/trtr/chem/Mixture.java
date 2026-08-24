package com.trtr.chem;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * 混合物：成分 → 摩尔量（ADR-006 成分列表模型）。
 * 不可变；"纯度"是本类的视图。温度随混合物整体携带。
 */
public final class Mixture {

    private final Map<Substance, Double> moles;
    private final double temperatureK;

    private Mixture(Map<Substance, Double> moles, double temperatureK) {
        this.moles = Collections.unmodifiableMap(moles);
        this.temperatureK = temperatureK;
    }

    public static Mixture empty(double temperatureK) {
        return new Mixture(new LinkedHashMap<>(), temperatureK);
    }

    public static Mixture of(double temperatureK, Substance substance, double amount) {
        Mixture m = Mixture.empty(temperatureK);
        return m.plus(substance, amount);
    }

    /** 加入/移除物质；归零或负值的成分被移除。 */
    public Mixture plus(Substance substance, double amount) {
        if (amount == 0) {
            return this;
        }
        double next = amountOf(substance) + amount;
        Map<Substance, Double> copy = new LinkedHashMap<>(moles);
        if (next <= 1e-12) {
            copy.remove(substance);
        } else {
            copy.put(substance, next);
        }
        return new Mixture(copy, temperatureK);
    }

    public Mixture atTemperature(double temperatureK) {
        return new Mixture(new LinkedHashMap<>(moles), temperatureK);
    }

    public double amountOf(Substance substance) {
        return moles.getOrDefault(substance, 0.0);
    }

    public boolean contains(Substance substance) {
        return amountOf(substance) > 0;
    }

    public double totalMoles() {
        return moles.values().stream().mapToDouble(Double::doubleValue).sum();
    }

    public double totalMass() {
        return moles.entrySet().stream()
                .mapToDouble(e -> e.getKey().molarMass() * e.getValue())
                .sum();
    }

    /** 纯度：目标物质摩尔占比（ADR-006）。 */
    public double purityOf(Substance substance) {
        double total = totalMoles();
        return total == 0 ? 0 : amountOf(substance) / total;
    }

    public double temperatureK() {
        return temperatureK;
    }

    public Set<Substance> components() {
        return moles.keySet();
    }

    public Map<Substance, Double> asMap() {
        return moles;
    }
}
