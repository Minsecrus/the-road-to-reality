package com.trtr.mining;

import java.util.Map;

import com.trtr.TRTRItems;
import com.trtr.TRTRBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 硬度数据（003 标尺，莫氏式连续值）。M0 以代码常量为种子数据，
 * 后续迁移 JSON（同 chem 的既定路线，ADR-003 数据化）。
 *
 * 岩石默认规则（可被 OVERRIDES 覆盖）：
 *   镐类  h = 1 + 2×原版破坏耗时（石头 1.5→4，与 003 表一致）
 *   斧类（木） h = 1.0 —— 手(h 1.5)可捶击取木，保底开局
 *   锹/锄类（土沙） h = 0.5
 */
public final class RockHardness {

    // —— 工具硬度（003：燧石 6.5；粗石镐取整块散石约 5）——
    public static final double H_HAND = 1.5;
    public static final double H_FLINT = 6.5;
    public static final double H_CRUDE_STONE = 5.0;

    /** 原版 tier → trtr 硬度（ADR-005 数据映射；铁 4.5 与 003 一致）。 */
    private static final Map<Tier, Double> VANILLA_TIERS = Map.of(
            Tiers.WOOD, 2.0,
            Tiers.STONE, 4.0,
            Tiers.IRON, 4.5,
            Tiers.DIAMOND, 8.0,
            Tiers.GOLD, 3.0,
            Tiers.NETHERITE, 9.0);

    /** 003 明确标定的岩石（其余按默认规则推算）。 */
    private static final Map<ResourceLocation, Double> OVERRIDES = Map.ofEntries(
            Map.entry(loc("granite"), 7.0),
            Map.entry(loc("polished_granite"), 7.0),
            Map.entry(loc("deepslate"), 7.0),
            Map.entry(loc("iron_ore"), 5.5),
            Map.entry(loc("deepslate_iron_ore"), 5.5),
            Map.entry(loc("copper_ore"), 5.5),
            Map.entry(loc("deepslate_copper_ore"), 5.5),
            Map.entry(loc("gold_ore"), 5.5),
            Map.entry(loc("deepslate_gold_ore"), 5.5),
            Map.entry(loc("diamond_ore"), 5.5),
            Map.entry(loc("deepslate_diamond_ore"), 5.5),
            Map.entry(loc("emerald_ore"), 5.5),
            Map.entry(loc("deepslate_emerald_ore"), 5.5));

    public static double of(BlockState state, BlockGetter level, BlockPos pos) {
        if (state.is(TRTRBlocks.SOFT_ROCK.get())) {
            return 2.0;
        }
        Double override = OVERRIDES.get(BuiltInRegistries.BLOCK.getKey(state.getBlock()));
        if (override != null) {
            return override;
        }
        if (state.is(BlockTags.MINEABLE_WITH_PICKAXE)) {
            return 1.0 + 2.0 * state.getDestroySpeed(level, pos);
        }
        if (state.is(BlockTags.MINEABLE_WITH_AXE)) {
            return 1.0;
        }
        return 0.5;
    }

    public static double ofTool(ItemStack tool) {
        if (tool.isEmpty()) {
            return H_HAND;
        }
        if (tool.is(TRTRItems.FLINT_AXE.get()) || tool.is(TRTRItems.FLINT_KNIFE.get())
                || tool.is(TRTRItems.FLINT_SHOVEL.get()) || tool.is(TRTRItems.FLINT_HOE.get())) {
            return H_FLINT;
        }
        if (tool.is(TRTRItems.CRUDE_STONE_PICKAXE.get())) {
            return H_CRUDE_STONE;
        }
        if (tool.getItem() instanceof TieredItem tiered) {
            return VANILLA_TIERS.getOrDefault(tiered.getTier(), 2.0);
        }
        return H_HAND;
    }

    /** 硬度差：h_tool − h_rock（003：效率 = f(diff)）。 */
    public static double diff(ItemStack tool, BlockState state, BlockGetter level, BlockPos pos) {
        return ofTool(tool) - of(state, level, pos);
    }

    private static ResourceLocation loc(String path) {
        return ResourceLocation.withDefaultNamespace(path);
    }

    private RockHardness() {}
}
