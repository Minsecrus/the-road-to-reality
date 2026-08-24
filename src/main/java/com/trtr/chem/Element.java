package com.trtr.chem;

/**
 * 元素周期表（trtr 工业线所需子集）。
 * 质量单位 g/mol，采用 IUPAC 常用值。
 */
public enum Element {
    H(1.008),
    LI(6.94),
    B(10.81),
    C(12.011),
    N(14.007),
    O(15.999),
    F(18.998),
    NA(22.990),
    MG(24.305),
    AL(26.982),
    SI(28.085),
    P(30.974),
    S(32.06),
    CL(35.45),
    K(39.098),
    CA(40.078),
    TI(47.867),
    V(50.942),
    CR(51.996),
    MN(54.938),
    FE(55.845),
    CO(58.933),
    NI(58.693),
    CU(63.546),
    ZN(65.38),
    AS(74.922),
    BR(79.904),
    AG(107.87),
    SN(118.71),
    SB(121.76),
    I(126.90),
    BA(137.33),
    W(183.84),
    PT(195.08),
    AU(196.97),
    HG(200.59),
    PB(207.2),
    BI(208.98),
    U(238.03);

    private final double atomicMass;

    Element(double atomicMass) {
        this.atomicMass = atomicMass;
    }

    public double atomicMass() {
        return atomicMass;
    }

    public String symbol() {
        return name();
    }
}
