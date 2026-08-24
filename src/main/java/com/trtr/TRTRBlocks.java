package com.trtr;

import com.trtr.block.LooseStoneBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * M0 方块。散石：地表可拾取的石料，粗石器原料（003 保底通道）。
 */
public final class TRTRBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(TRTR.MODID);

    /** 散石：无工具要求，任何人可采（hardness 低）。 */
    public static final DeferredBlock<LooseStoneBlock> LOOSE_STONE = BLOCKS.register("loose_stone",
            () -> new LooseStoneBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)
                    .strength(1.2F)
                    .noOcclusion()
                    .mapColor(net.minecraft.world.level.material.MapColor.STONE)));

    private TRTRBlocks() {}
}
