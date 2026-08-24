package com.trtr.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 散石：小石堆造型（非满格），007 碎石堆的前身。
 */
public class LooseStoneBlock extends Block {

    private static final VoxelShape SHAPE = Block.box(3, 0, 3, 13, 5, 13);

    public LooseStoneBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}
