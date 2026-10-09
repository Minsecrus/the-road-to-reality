package com.trtr.mining;

import com.trtr.TRTR;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;

/** Pay in time and wear; preserve the actual tool, enchantments and block entity in the normal loot pipeline. */
@EventBusSubscriber(modid = TRTR.MODID)
public final class MiningHooks {
    private static boolean manages(BlockState state) {
        String namespace = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getNamespace();
        return namespace.equals("minecraft") || namespace.equals(TRTR.MODID);
    }

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (!manages(event.getState()) || event.getEntity().isCreative()) {
            return;
        }
        event.getPosition().ifPresent(pos -> {
            double diff = RockHardness.diff(event.getEntity().getMainHandItem(), event.getState(),
                    event.getEntity().level(), pos);
            event.setNewSpeed(event.getNewSpeed() * HardnessModel.efficiency(diff));
        });
    }

    @SubscribeEvent
    public static void onHarvestCheck(PlayerEvent.HarvestCheck event) {
        if (manages(event.getTargetBlock())
                && event.getTargetBlock().getDestroySpeed(event.getLevel(), event.getPos()) >= 0) {
            event.setCanHarvest(true);
        }
    }

    // A BreakEvent may subsequently be cancelled. Wear belongs to an actual successful harvest.
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onDrops(BlockDropsEvent event) {
        if (!(event.getBreaker() instanceof Player player) || player.isCreative() || !manages(event.getState())) {
            return;
        }
        ItemStack tool = player.getMainHandItem();
        if (!tool.isDamageableItem() || !tool.is(event.getTool().getItem())) {
            return; // Normal mining wear may already have broken the tool.
        }
        int extra = HardnessModel.extraWear(RockHardness.diff(event.getTool(), event.getState(), event.getLevel(), event.getPos()));
        if (extra > 0) {
            tool.hurtAndBreak(extra, player, EquipmentSlot.MAINHAND);
        }
    }

    private MiningHooks() {}
}
