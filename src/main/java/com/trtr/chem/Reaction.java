package com.trtr.chem;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 反应：计量比 + 条件。确定性求解（ADR-002）：条件满足即按限制反应物完全转化。
 */
public final class Reaction {

    /**
     * 反应条件：温度窗口 + 可选催化剂（null 表示无需催化剂）。
     * 机器 = 条件提供者（ADR-003）。
     */
    public record Condition(double minTempK, double maxTempK, Substance catalyst) {
        public boolean satisfiedBy(Mixture mixture) {
            if (mixture.temperatureK() < minTempK || mixture.temperatureK() > maxTempK) {
                return false;
            }
            return catalyst == null || mixture.contains(catalyst);
        }
    }

    /** 求解结果：extent = 实际转化的"反应次数"。 */
    public record Result(Reaction reaction, double extent, Mixture output) {
        public boolean occurred() {
            return extent > 0;
        }
    }

    private final String id;
    private final Map<Substance, Integer> reactants;
    private final Map<Substance, Integer> products;
    private final Condition condition;

    private Reaction(String id, Map<Substance, Integer> reactants,
                     Map<Substance, Integer> products, Condition condition) {
        this.id = id;
        this.reactants = reactants;
        this.products = products;
        this.condition = condition;
    }

    public static Builder of(String id) {
        return new Builder(id);
    }

    public String id() {
        return id;
    }

    public Map<Substance, Integer> reactants() {
        return reactants;
    }

    public Map<Substance, Integer> products() {
        return products;
    }

    public Condition condition() {
        return condition;
    }

    public boolean applicableTo(Mixture mixture) {
        return condition.satisfiedBy(mixture)
                && reactants.keySet().stream().allMatch(mixture::contains);
    }

    /**
     * 按限制反应物完全转化，返回新混合物（温度不变）。
     * 未满足条件或缺少反应物时 extent = 0，原样返回。
     */
    public Result solve(Mixture mixture) {
        if (!applicableTo(mixture)) {
            return new Result(this, 0, mixture);
        }
        double extent = reactants.entrySet().stream()
                .mapToDouble(e -> mixture.amountOf(e.getKey()) / e.getValue())
                .min().orElse(0);
        if (extent <= 0) {
            return new Result(this, 0, mixture);
        }
        Mixture out = mixture;
        for (Map.Entry<Substance, Integer> e : reactants.entrySet()) {
            out = out.plus(e.getKey(), -extent * e.getValue());
        }
        for (Map.Entry<Substance, Integer> e : products.entrySet()) {
            out = out.plus(e.getKey(), extent * e.getValue());
        }
        return new Result(this, extent, out);
    }

    @Override
    public String toString() {
        return id + ": " + reactants + " -> " + products + " @ " + condition;
    }

    public static final class Builder {
        private final String id;
        private final Map<Substance, Integer> reactants = new LinkedHashMap<>();
        private final Map<Substance, Integer> products = new LinkedHashMap<>();
        private Condition condition = new Condition(0, Double.MAX_VALUE, null);

        private Builder(String id) {
            this.id = id;
        }

        public Builder reactant(Substance s, int coefficient) {
            reactants.put(s, coefficient);
            return this;
        }

        public Builder product(Substance s, int coefficient) {
            products.put(s, coefficient);
            return this;
        }

        public Builder condition(Condition condition) {
            this.condition = condition;
            return this;
        }

        public Reaction build() {
            if (reactants.isEmpty() || products.isEmpty()) {
                throw new IllegalStateException("反应 " + id + " 缺少反应物或产物");
            }
            return new Reaction(id, reactants, products, condition);
        }
    }
}
