package com.trtr.block;

import com.trtr.TRTRItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** M0 abstracts hide finishing to scraping and drying. Chemical tanning belongs to later milestones. */
public class DryingRackBlock extends Block {
    public static final IntegerProperty STAGE = IntegerProperty.create("stage", 0, 3);
    public static final int DRYING_STEP_TICKS = 300;
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(1, 0, 6, 3, 16, 10), Block.box(13, 0, 6, 15, 16, 10),
            Block.box(1, 13, 6, 15, 16, 10));
    private static final VoxelShape LOADED_SHAPE = Shapes.or(SHAPE, Block.box(3, 4, 7, 13, 13, 9));

    public DryingRackBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(STAGE, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STAGE);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(STAGE) == 0 ? SHAPE : LOADED_SHAPE;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        if (!player.getAbilities().mayBuild || !level.mayInteract(player, pos)) {
            return ItemInteractionResult.FAIL;
        }
        int stage = state.getValue(STAGE);
        if (stage == 3) {
            if (!level.isClientSide()) {
                level.setBlock(pos, state.setValue(STAGE, 0), 3);
                if (!player.getInventory().add(new ItemStack(Items.LEATHER))) {
                    player.drop(new ItemStack(Items.LEATHER), false);
                }
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        }
        if (stage == 0 && stack.is(TRTRItems.SCRAPED_HIDE.get())) {
            if (!level.isClientSide()) {
                stack.consume(1, player);
                level.setBlock(pos, state.setValue(STAGE, 1), 3);
                level.scheduleTick(pos, this, DRYING_STEP_TICKS);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (state.getValue(STAGE) == 3) {
            return useItemOn(ItemStack.EMPTY, state, level, pos, player, InteractionHand.MAIN_HAND, hit).result();
        }
        return InteractionResult.PASS;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int stage = state.getValue(STAGE);
        if (stage > 0 && stage < 3) {
            if (!level.isRainingAt(pos.above()) && level.getFluidState(pos).isEmpty()) {
                stage++;
                level.setBlock(pos, state.setValue(STAGE, stage), 3);
            }
            if (stage < 3) {
                level.scheduleTick(pos, this, DRYING_STEP_TICKS);
            }
        }
    }
}
