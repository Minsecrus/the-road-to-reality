package com.trtr.chem;

/**
 * 基准反应（种子数据）。后续改为 JSON 数据加载（ADR-003）。
 */
public final class TRTRReactions {
    private TRTRReactions() {}

    /** CaCO3 → CaO + CO2（煅烧，约 1170 K 起明显分解）。 */
    public static final Reaction LIME_CALCINATION = Reaction.of("lime_calcination")
            .reactant(TRTRSubstances.CALCIUM_CARBONATE, 1)
            .product(TRTRSubstances.CALCIUM_OXIDE, 1)
            .product(TRTRSubstances.CARBON_DIOXIDE, 1)
            .condition(new Reaction.Condition(1170, Double.MAX_VALUE, null))
            .build();

    /** CaO + SiO2 → CaSiO3（造渣，003/M3 主反应）。 */
    public static final Reaction SLAG_FORMATION = Reaction.of("slag_formation")
            .reactant(TRTRSubstances.CALCIUM_OXIDE, 1)
            .reactant(TRTRSubstances.SILICA, 1)
            .product(TRTRSubstances.CALCIUM_SILICATE, 1)
            .condition(new Reaction.Condition(1400, Double.MAX_VALUE, null))
            .build();

    /** Fe2O3 + 3CO → 2Fe + 3CO2（竖炉还原）。 */
    public static final Reaction IRON_REDUCTION = Reaction.of("iron_reduction")
            .reactant(TRTRSubstances.HEMATITE, 1)
            .reactant(TRTRSubstances.CARBON_MONOXIDE, 3)
            .product(TRTRSubstances.IRON, 2)
            .product(TRTRSubstances.CARBON_DIOXIDE, 3)
            .condition(new Reaction.Condition(1000, Double.MAX_VALUE, null))
            .build();

    /**
     * C + O2 → CO2（完全燃烧，低温窗口：篝火/柴堆）。
     * 高温富碳环境走 CO_FORMATION（Boudouard 平衡的确定性简化）。
     */
    public static final Reaction CARBON_COMBUSTION = Reaction.of("carbon_combustion")
            .reactant(TRTRSubstances.CARBON, 1)
            .reactant(TRTRSubstances.OXYGEN, 1)
            .product(TRTRSubstances.CARBON_DIOXIDE, 1)
            .condition(new Reaction.Condition(700, 1200, null))
            .build();

    /** 2C + O2 → 2CO（高温富碳：竖炉/鼓风炉生成还原剂）。 */
    public static final Reaction CO_FORMATION = Reaction.of("co_formation")
            .reactant(TRTRSubstances.CARBON, 2)
            .reactant(TRTRSubstances.OXYGEN, 1)
            .product(TRTRSubstances.CARBON_MONOXIDE, 2)
            .condition(new Reaction.Condition(1200, Double.MAX_VALUE, null))
            .build();

    public static ReactionRegistry seedRegistry() {
        ReactionRegistry registry = new ReactionRegistry();
        registry.register(CARBON_COMBUSTION);
        registry.register(CO_FORMATION);
        registry.register(LIME_CALCINATION);
        registry.register(SLAG_FORMATION);
        registry.register(IRON_REDUCTION);
        return registry;
    }
}
