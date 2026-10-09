package com.trtr;

import com.trtr.block.LooseStoneBlock;
import com.trtr.block.DryingRackBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * M0 方块。散石：地表可拾取的石料，粗石器原料（003 保底通道）。
 */
public final class TRTRBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(TRTR.MODID);

    /**
     * 散石：地表可拾取的石料，粗石器原料（003 保底通道）。
     * ADR-005"没有挖不动，只有代价"——徒手可采、必掉自身，不走 requiresCorrectToolForDrops。
     * 不挂 mineable/pickaxe 标签，避免被采掘接管的非镐 3% 惩罚误伤。
     */
    public static final DeferredBlock<LooseStoneBlock> LOOSE_STONE = BLOCKS.register("loose_stone",
            () -> new LooseStoneBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(0.4F)
                    .sound(SoundType.GRAVEL)
                    .noOcclusion()
                    .pushReaction(PushReaction.DESTROY)));

    public static final DeferredBlock<Block> SOFT_ROCK = BLOCKS.registerSimpleBlock("soft_rock",
            BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(0.8F).sound(SoundType.TUFF));

    public static final DeferredBlock<DryingRackBlock> DRYING_RACK = BLOCKS.register("drying_rack",
            () -> new DryingRackBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD)
                    .strength(1.0F).sound(SoundType.WOOD).noOcclusion()));

    private TRTRBlocks() {}
}
