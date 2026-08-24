package com.trtr.chem;

/**
 * 基准物质（种子数据）。后续改为 JSON 数据加载（ADR-003）。
 */
public final class TRTRSubstances {
    private TRTRSubstances() {}

    // —— 单质与基础 ——
    public static final Substance CARBON = def("carbon", "C", 3800, 4300, 0x1a1a1a);
    public static final Substance IRON = def("iron", "Fe", 1811, 3134, 0xd8d8d8);
    public static final Substance COPPER = def("copper", "Cu", 1358, 2835, 0xc87d4a);

    // —— 气体 ——
    public static final Substance OXYGEN = def("oxygen", "O2", 54, 90, 0xaaccff);
    public static final Substance HYDROGEN = def("hydrogen", "H2", 14, 20, 0xdddddd);
    public static final Substance CARBON_MONOXIDE = def("carbon_monoxide", "CO", 68, 81, 0x99aacc);
    public static final Substance CARBON_DIOXIDE = def("carbon_dioxide", "CO2", Double.NaN, 195, 0xcccccc);

    // —— 水 ——
    public static final Substance WATER = def("water", "H2O", 273, 373, 0x3a6ea5);

    // —— 石灰循环（004#13）——
    public static final Substance CALCIUM_CARBONATE = def("calcium_carbonate", "CaCO3", 1612, Double.NaN, 0xf2f0e6);
    public static final Substance CALCIUM_OXIDE = def("calcium_oxide", "CaO", 2854, 3125, 0xf5f5f5);
    public static final Substance CALCIUM_HYDROXIDE = def("calcium_hydroxide", "Ca(OH)2", 851, Double.NaN, 0xf0f0f0);
    public static final Substance SILICA = def("silica", "SiO2", 1986, 2503, 0xe8e4d8);
    public static final Substance CALCIUM_SILICATE = def("calcium_silicate", "CaSiO3", 1817, Double.NaN, 0xdcd8cc);

    // —— 铁矿与铜矿 ——
    public static final Substance HEMATITE = def("hematite", "Fe2O3", 1838, Double.NaN, 0x9c4a3a);
    public static final Substance MAGNETITE = def("magnetite", "Fe3O4", 1870, Double.NaN, 0x2e2e34);
    public static final Substance COPPER_OXIDE = def("copper_oxide", "CuO", 1599, Double.NaN, 0x3a3a3a);

    // —— 造渣与还原 ——
    public static final Substance SLAG = CALCIUM_SILICATE; // 造渣产物别名

    private static Substance def(String id, String formula, double meltK, double boilK, int color) {
        return new Substance(id, Formula.parse(formula), meltK, boilK, color);
    }
}
