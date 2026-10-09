package com.trtr.item;

import com.trtr.TRTRItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/** Hold on a dry, unlit campfire for four seconds, with tinder in the other hand. */
public final class FireDrillItem extends Item {
    public static final int IGNITION_TICKS = 80;
    private static final String TARGET = "trtr:fire_drill_target";

    public FireDrillItem(Properties properties) {
        super(properties);
    }

    private static ItemStack tinder(Player player, InteractionHand hand) {
        return player.getItemInHand(hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
    }

    public static boolean canIgnite(Level level, BlockPos pos) {
        var state = level.getBlockState(pos);
        return state.is(Blocks.CAMPFIRE) && !state.getValue(CampfireBlock.LIT)
                && !state.getValue(CampfireBlock.WATERLOGGED) && !level.isRainingAt(pos.above());
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || !player.getAbilities().mayBuild
                || !context.getLevel().mayInteract(player, context.getClickedPos())) {
            return InteractionResult.FAIL;
        }
        if (!canIgnite(context.getLevel(), context.getClickedPos())) {
            if (!context.getLevel().isClientSide()) {
                player.displayClientMessage(Component.translatable("message.trtr.fire_needs_dry_campfire"), true);
            }
            return InteractionResult.FAIL;
        }
        if (!tinder(player, context.getHand()).is(TRTRItems.GRASS_BUNDLE.get())) {
            if (!context.getLevel().isClientSide()) {
                player.displayClientMessage(Component.translatable("message.trtr.fire_needs_tinder"), true);
            }
            return InteractionResult.FAIL;
        }
        player.getPersistentData().putLong(TARGET, context.getClickedPos().asLong());
        player.startUsingItem(context.getHand());
        return InteractionResult.CONSUME;
    }

    private static boolean validTarget(Player player, ItemStack stack, BlockPos pos) {
        return player.getAbilities().mayBuild && player.level().mayInteract(player, pos)
                && player.mayUseItemAt(pos, Direction.UP, stack)
                && canIgnite(player.level(), pos)
                && tinder(player, player.getUsedItemHand()).is(TRTRItems.GRASS_BUNDLE.get())
                && player.pick(player.blockInteractionRange(), 1.0F, false) instanceof BlockHitResult hit
                && hit.getType() == HitResult.Type.BLOCK
                && hit.getBlockPos().equals(pos);
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remaining) {
        if (!(level instanceof ServerLevel server) || !(entity instanceof Player player)) {
            return;
        }
        if (!player.getPersistentData().contains(TARGET)) {
            player.stopUsingItem();
            return;
        }
        BlockPos pos = BlockPos.of(player.getPersistentData().getLong(TARGET));
        if (!validTarget(player, stack, pos)) {
            player.getPersistentData().remove(TARGET);
            player.stopUsingItem();
            return;
        }
        if (remaining % 20 == 0) {
            server.sendParticles(ParticleTypes.SMOKE, pos.getX() + 0.5, pos.getY() + 0.4,
                    pos.getZ() + 0.5, 2, 0.1, 0.05, 0.1, 0.01);
            level.playSound(null, pos, SoundEvents.WOOD_HIT, SoundSource.BLOCKS, 0.4F, 1.3F);
            player.displayClientMessage(Component.translatable("message.trtr.fire_progress",
                    (IGNITION_TICKS - remaining) * 100 / IGNITION_TICKS), true);
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (level instanceof ServerLevel && entity instanceof Player player && player.getPersistentData().contains(TARGET)) {
            BlockPos pos = BlockPos.of(player.getPersistentData().getLong(TARGET));
            player.getPersistentData().remove(TARGET);
            if (validTarget(player, stack, pos)) {
                level.setBlock(pos, level.getBlockState(pos).setValue(CampfireBlock.LIT, true), 3);
                tinder(player, player.getUsedItemHand()).consume(1, player);
                stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(player.getUsedItemHand()));
                player.awardStat(Stats.ITEM_USED.get(this));
                level.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 0.7F, 1.0F);
                level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
                player.displayClientMessage(Component.translatable("message.trtr.fire_lit"), true);
            }
        }
        return stack;
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int remaining) {
        entity.getPersistentData().remove(TARGET);
    }

    @Override public int getUseDuration(ItemStack stack, LivingEntity entity) { return IGNITION_TICKS; }
    @Override public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.BRUSH; }
    @Override public boolean isEnchantable(ItemStack stack) { return false; }
    @Override public boolean isBookEnchantable(ItemStack stack, ItemStack book) { return false; }
}
