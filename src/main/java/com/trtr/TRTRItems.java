package com.trtr;

import com.trtr.item.FireDrillItem;

import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * M0 物品。燧石器无镐（采掘走散石→粗石路线，003）。
 * trtr 工具一律不可附魔（ADR-008：魔法线与 trtr 线互不渗透）。
 */
public final class TRTRItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TRTR.MODID);

    // —— 原料 ——
    public static final DeferredItem<Item> FLINT = register("flint_shard");
    public static final DeferredItem<Item> PLANT_FIBER = register("plant_fiber");
    public static final DeferredItem<Item> FLINT_BLADE = register("flint_blade");
    public static final DeferredItem<Item> ROPE = register("rope");
    public static final DeferredItem<Item> HIDE = register("hide");
    public static final DeferredItem<Item> SCRAPED_HIDE = register("scraped_hide");
    public static final DeferredItem<Item> GRASS_BUNDLE = register("grass_bundle");
    public static final DeferredItem<FireDrillItem> FIRE_DRILL = ITEMS.register("fire_drill",
            () -> new FireDrillItem(new Item.Properties().durability(32)));

    // —— 燧石器 ——
    public static final DeferredItem<Item> FLINT_AXE = ITEMS.register("flint_axe",
            () -> new AxeItem(Tiers.WOOD, new Item.Properties().durability(55)
                    .attributes(DiggerItem.createAttributes(Tiers.WOOD, 5.0F, -3.2F))) {
                @Override public boolean isEnchantable(ItemStack stack) { return false; }
                @Override public boolean isBookEnchantable(ItemStack stack, ItemStack book) { return false; }
            });
    public static final DeferredItem<Item> FLINT_KNIFE = ITEMS.register("flint_knife",
            () -> new SwordItem(Tiers.WOOD, new Item.Properties().durability(45)
                    .attributes(SwordItem.createAttributes(Tiers.WOOD, 2, -1.8F))) {
                @Override public boolean canPerformAction(ItemStack stack, ItemAbility ability) {
                    return ability == ItemAbilities.AXE_STRIP || super.canPerformAction(stack, ability);
                }
                @Override public boolean isEnchantable(ItemStack stack) { return false; }
                @Override public boolean isBookEnchantable(ItemStack stack, ItemStack book) { return false; }
            });
    public static final DeferredItem<Item> FLINT_SHOVEL = ITEMS.register("flint_shovel",
            () -> new ShovelItem(Tiers.WOOD, new Item.Properties().durability(55)
                    .attributes(DiggerItem.createAttributes(Tiers.WOOD, 1.5F, -3.0F))) {
                @Override public boolean isEnchantable(ItemStack stack) { return false; }
                @Override public boolean isBookEnchantable(ItemStack stack, ItemStack book) { return false; }
            });
    public static final DeferredItem<Item> FLINT_HOE = ITEMS.register("flint_hoe",
            () -> new HoeItem(Tiers.WOOD, new Item.Properties().durability(45)
                    .attributes(DiggerItem.createAttributes(Tiers.WOOD, 0.0F, -2.0F))) {
                @Override public boolean isEnchantable(ItemStack stack) { return false; }
                @Override public boolean isBookEnchantable(ItemStack stack, ItemStack book) { return false; }
            });

    // —— 粗石器 ——
    public static final DeferredItem<Item> CRUDE_STONE_PICKAXE = ITEMS.register("crude_stone_pickaxe",
            () -> new PickaxeItem(Tiers.STONE, new Item.Properties().durability(100)
                    .attributes(DiggerItem.createAttributes(Tiers.STONE, 1.0F, -2.8F))) {
                @Override public boolean isEnchantable(ItemStack stack) { return false; }
                @Override public boolean isBookEnchantable(ItemStack stack, ItemStack book) { return false; }
            });

    // —— 方块物品 ——
    public static final DeferredItem<BlockItem> LOOSE_STONE =
            ITEMS.registerSimpleBlockItem(TRTRBlocks.LOOSE_STONE);
    public static final DeferredItem<BlockItem> SOFT_ROCK = ITEMS.registerSimpleBlockItem(TRTRBlocks.SOFT_ROCK);
    public static final DeferredItem<BlockItem> DRYING_RACK = ITEMS.registerSimpleBlockItem(TRTRBlocks.DRYING_RACK);

    private static DeferredItem<Item> register(String name) {
        return ITEMS.registerItem(name, Item::new);
    }

    private TRTRItems() {}
}
