package com.trtr.chem;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 化学式：元素 → 原子数。支持括号嵌套与默认下标 1，如 Fe2(SO4)3。
 */
public final class Formula {

    private final Map<Element, Integer> atoms;
    private final String text;
    private final double molarMass;

    private Formula(String text, Map<Element, Integer> atoms) {
        this.text = text;
        this.atoms = Collections.unmodifiableMap(atoms);
        this.molarMass = atoms.entrySet().stream()
                .mapToDouble(e -> e.getKey().atomicMass() * e.getValue())
                .sum();
    }

    public static Formula parse(String formula) {
        String s = formula.replace(" ", "");
        Deque<Map<Element, Integer>> stack = new ArrayDeque<>();
        Map<Element, Integer> current = new LinkedHashMap<>();
        int i = 0;
        while (i < s.length()) {
            char c = s.charAt(i);
            if (c == '(') {
                stack.push(current);
                current = new LinkedHashMap<>();
                i++;
            } else if (c == ')') {
                i++;
                int mult = readNumber(s, i);
                i = skipDigits(s, i);
                Map<Element, Integer> group = current;
                Map<Element, Integer> target = stack.pop();
                current = target;
                group.forEach((el, n) -> target.merge(el, n * mult, Integer::sum));
            } else if (Character.isUpperCase(c)) {
                int start = i++;
                while (i < s.length() && Character.isLowerCase(s.charAt(i))) {
                    i++;
                }
                Element el = Element.valueOf(s.substring(start, i).toUpperCase(java.util.Locale.ROOT));
                int n = readNumber(s, i);
                i = skipDigits(s, i);
                current.merge(el, n, Integer::sum);
            } else {
                throw new IllegalArgumentException("非法化学式字符 '" + c + "' 于位置 " + i + ": " + formula);
            }
        }
        if (!stack.isEmpty()) {
            throw new IllegalArgumentException("括号不匹配: " + formula);
        }
        if (current.isEmpty()) {
            throw new IllegalArgumentException("空化学式: " + formula);
        }
        return new Formula(formula, current);
    }

    private static int readNumber(String s, int i) {
        if (i >= s.length() || !Character.isDigit(s.charAt(i))) {
            return 1;
        }
        int end = i;
        while (end < s.length() && Character.isDigit(s.charAt(end))) {
            end++;
        }
        return Integer.parseInt(s.substring(i, end));
    }

    private static int skipDigits(String s, int i) {
        while (i < s.length() && Character.isDigit(s.charAt(i))) {
            i++;
        }
        return i;
    }

    public Map<Element, Integer> atoms() {
        return atoms;
    }

    public double molarMass() {
        return molarMass;
    }

    @Override
    public String toString() {
        return text;
    }
}
