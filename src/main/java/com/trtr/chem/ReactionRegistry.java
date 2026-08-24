package com.trtr.chem;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 反应注册表。当前为代码注册；后续切换为 JSON 数据加载（ADR-003）。
 */
public final class ReactionRegistry {

    private final List<Reaction> reactions = new ArrayList<>();

    public ReactionRegistry register(Reaction reaction) {
        reactions.add(reaction);
        return this;
    }

    public List<Reaction> all() {
        return List.copyOf(reactions);
    }

    /** 找到第一个可发生的反应并求解（确定性：注册顺序即优先级）。 */
    public Optional<Reaction.Result> tick(Mixture mixture) {
        for (Reaction reaction : reactions) {
            if (reaction.applicableTo(mixture)) {
                Reaction.Result result = reaction.solve(mixture);
                if (result.occurred()) {
                    return Optional.of(result);
                }
            }
        }
        return Optional.empty();
    }
}
