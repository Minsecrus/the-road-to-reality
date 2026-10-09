package com.trtr.interaction;

import java.util.Set;
import com.trtr.TRTR;
import com.trtr.TRTRItems;
import com.trtr.crafting.KnappingMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;

/** Opening materials and hand processes; loot changes are made only after a successful break. */
@EventBusSubscriber(modid = TRTR.MODID)
public final class SurvivalInteractions {
    private static final Set<Block> FIBER_PLANTS = Set.of(
            Blocks.SHORT_GRASS, Blocks.TALL_GRASS, Blocks.FERN, Blocks.LARGE_FERN);
    private static final int SCRAPE_COOLDOWN = 10;

    @SubscribeEvent
    public static void onPlantDrops(BlockDropsEvent event) {
        if (!(event.getBreaker() instanceof Player player) || player.isCreative()) return;
        ServerLevel level = event.getLevel();
        BlockState state = event.getState();
        ItemStack tool = event.getTool();
        boolean silkTouch = tool.getEnchantmentLevel(
                level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH)) > 0;
        if (state.is(Blocks.GRAVEL) && !silkTouch) {
            int fortune = tool.getEnchantmentLevel(
                    level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FORTUNE));
            for (ItemEntity drop : event.getDrops()) {
                if (drop.getItem().is(Items.GRAVEL) || drop.getItem().is(Items.FLINT)) {
                    var result = level.random.nextFloat() < 1.0F / Math.max(1, 4 - fortune) ? TRTRItems.FLINT.get()
                            : level.random.nextFloat() < 0.3F ? TRTRItems.LOOSE_STONE.get() : Items.GRAVEL;
                    drop.setItem(new ItemStack(result, drop.getItem().getCount()));
                }
            }
            return;
        }
        if (tool.getItem() instanceof ShearsItem || silkTouch) return;
        if (state.is(BlockTags.LEAVES) && level.random.nextFloat() < 0.3F) addDrop(event, new ItemStack(Items.STICK));
        if (FIBER_PLANTS.contains(state.getBlock()) && level.random.nextFloat() < 0.75F) {
            addDrop(event, new ItemStack(TRTRItems.PLANT_FIBER.get()));
        }
        // Reuse vanilla farming as the renewable fiber crop.
        if (state.is(Blocks.WHEAT) && state.getValue(CropBlock.AGE) == 7) {
            addDrop(event, new ItemStack(TRTRItems.PLANT_FIBER.get(), 2));
        }
    }

    private static void addDrop(BlockDropsEvent event, ItemStack stack) {
        var pos = event.getPos();
        event.getDrops().add(new ItemEntity(event.getLevel(), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack));
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        ItemStack tool = event.getItemStack();
        if (KnappingMenu.isFlint(tool)) {
            KnappingMenu.open(player, event.getHand());
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
        } else if (tool.is(TRTRItems.FLINT_KNIFE.get())) {
            ItemStack hide = player.getItemInHand(event.getHand() == InteractionHand.MAIN_HAND
                    ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
            if (!hide.is(TRTRItems.HIDE.get())) return;
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
            if (!player.level().isClientSide() && !player.getCooldowns().isOnCooldown(tool.getItem())) {
                hide.consume(1, player);
                KnappingMenu.give(player, new ItemStack(TRTRItems.SCRAPED_HIDE.get()));
                tool.hurtAndBreak(1, player, LivingEntity.getSlotForHand(event.getHand()));
                player.getCooldowns().addCooldown(TRTRItems.FLINT_KNIFE.get(), SCRAPE_COOLDOWN);
            }
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        ItemStack tool = event.getItemStack();
        BlockState state = event.getLevel().getBlockState(event.getPos());
        if (!player.getAbilities().mayBuild || !event.getLevel().mayInteract(player, event.getPos())) return;
        if (tool.is(TRTRItems.FLINT_KNIFE.get()) && state.is(BlockTags.LOGS)) {
            var stripped = state.getToolModifiedState(new UseOnContext(player, event.getHand(), event.getHitVec()),
                    ItemAbilities.AXE_STRIP, false);
            if (stripped == null) return; // An already stripped log cannot produce another set of sticks.
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide()));
            if (!(event.getLevel() instanceof ServerLevel level) || player.getCooldowns().isOnCooldown(tool.getItem())) return;
            level.setBlock(event.getPos(), stripped, 3);
            if (!player.hasInfiniteMaterials()) KnappingMenu.give(player, new ItemStack(Items.STICK, 2));
            tool.hurtAndBreak(1, player, LivingEntity.getSlotForHand(event.getHand()));
            player.getCooldowns().addCooldown(TRTRItems.FLINT_KNIFE.get(), SCRAPE_COOLDOWN);
            level.playSound(null, event.getPos(), SoundEvents.AXE_STRIP, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent(player, GameEvent.BLOCK_CHANGE, event.getPos());
        } else if (tool.is(Items.STICK) && state.is(Blocks.CAMPFIRE) && state.getValue(CampfireBlock.LIT)) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide()));
            if (event.getLevel().isClientSide() || player.getCooldowns().isOnCooldown(Items.STICK)) return;
            tool.consume(1, player);
            KnappingMenu.give(player, new ItemStack(Items.TORCH));
            player.getCooldowns().addCooldown(Items.STICK, 10);
        }
    }

    @SubscribeEvent
    public static void tooltips(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        String key = null;
        if (KnappingMenu.isFlint(stack)) key = "flint";
        else if (stack.is(TRTRItems.PLANT_FIBER.get())) key = "fiber";
        else if (stack.is(TRTRItems.FLINT_KNIFE.get())) key = "knife";
        else if (stack.is(TRTRItems.FIRE_DRILL.get())) key = "fire_drill";
        else if (stack.is(TRTRItems.HIDE.get())) key = "hide";
        else if (stack.is(TRTRItems.SCRAPED_HIDE.get()) || stack.is(TRTRItems.DRYING_RACK.get())) key = "drying";
        if (key != null) event.getToolTip().add(Component.translatable("tooltip.trtr." + key));
    }

    private SurvivalInteractions() {}
}
