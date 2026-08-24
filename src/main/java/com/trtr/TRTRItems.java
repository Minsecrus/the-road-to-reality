package com.trtr;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * M0 开局原料。占位物品，贴图后补。
 */
public final class TRTRItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TRTR.MODID);

    public static final DeferredItem<Item> FLINT = register("flint_shard");
    public static final DeferredItem<Item> STICK = register("wood_stick");
    public static final DeferredItem<Item> PLANT_FIBER = register("plant_fiber");

    private static DeferredItem<Item> register(String name) {
        return ITEMS.registerItem(name, Item::new);
    }

    private TRTRItems() {}
}
