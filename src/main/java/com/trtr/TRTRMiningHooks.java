package com.trtr;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * 采掘接管 v0（003 连续硬度模型的涌现式第一步）：
 * 岩石类方块（mineable/pickaxe）对非镐工具采掘速度压至 3%——
 * 不禁止、无掉落（原版规则），玩家自然放弃用燧石器凿岩。
 */
@EventBusSubscriber(modid = TRTR.MODID)
public final class TRTRMiningHooks {

    private static final float NON_PICK_ROCK_FACTOR = 0.03F;

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (!event.getState().is(BlockTags.MINEABLE_WITH_PICKAXE)) {
            return;
        }
        ItemStack tool = event.getEntity().getMainHandItem();
        if (tool.getItem() instanceof PickaxeItem) {
            return; // 镐正常切削
        }
        event.setNewSpeed(event.getNewSpeed() * NON_PICK_ROCK_FACTOR);
    }

    private TRTRMiningHooks() {}
}
