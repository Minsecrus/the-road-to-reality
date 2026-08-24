package com.trtr;

import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * M0 物品。燧石器无镐（采掘走散石→粗石路线，003）。
 */
public final class TRTRItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TRTR.MODID);

    // —— 原料 ——
    public static final DeferredItem<Item> FLINT = register("flint_shard");
    public static final DeferredItem<Item> STICK = register("wood_stick");
    public static final DeferredItem<Item> PLANT_FIBER = register("plant_fiber");

    // —— 燧石器 ——
    public static final DeferredItem<Item> FLINT_AXE = ITEMS.register("flint_axe",
            () -> new AxeItem(Tiers.WOOD, new Item.Properties().durability(55)
                    .attributes(DiggerItem.createAttributes(Tiers.WOOD, 5.0F, -3.2F))));
    public static final DeferredItem<Item> FLINT_KNIFE = ITEMS.register("flint_knife",
            () -> new SwordItem(Tiers.WOOD, new Item.Properties().durability(45)
                    .attributes(SwordItem.createAttributes(Tiers.WOOD, 2, -1.8F))));
    public static final DeferredItem<Item> FLINT_SHOVEL = ITEMS.register("flint_shovel",
            () -> new ShovelItem(Tiers.WOOD, new Item.Properties().durability(55)
                    .attributes(DiggerItem.createAttributes(Tiers.WOOD, 1.5F, -3.0F))));
    public static final DeferredItem<Item> FLINT_HOE = ITEMS.register("flint_hoe",
            () -> new HoeItem(Tiers.WOOD, new Item.Properties().durability(45)
                    .attributes(DiggerItem.createAttributes(Tiers.WOOD, 0.0F, -2.0F))));

    // —— 粗石器 ——
    public static final DeferredItem<Item> CRUDE_STONE_PICKAXE = ITEMS.register("crude_stone_pickaxe",
            () -> new PickaxeItem(Tiers.STONE, new Item.Properties().durability(100)
                    .attributes(DiggerItem.createAttributes(Tiers.STONE, 1.0F, -2.8F))));

    // —— 方块物品 ——
    public static final DeferredItem<BlockItem> LOOSE_STONE =
            ITEMS.registerSimpleBlockItem(TRTRBlocks.LOOSE_STONE);

    private static DeferredItem<Item> register(String name) {
        return ITEMS.registerItem(name, Item::new);
    }

    private TRTRItems() {}
}
