package com.trtr;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import com.mojang.authlib.GameProfile;
import com.trtr.block.DryingRackBlock;
import com.trtr.crafting.KnappingMenu;
import com.trtr.crafting.KnappingPattern;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(TRTR.MODID)
@PrefixGameTestTemplate(false)
public class M0GameTests {
    private static final BlockPos POS = new BlockPos(3, 2, 3);

    private static FakePlayer player(GameTestHelper helper) {
        FakePlayer player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "M0-test"));
        player.setGameMode(GameType.SURVIVAL);
        Vec3 origin = Vec3.atCenterOf(helper.absolutePos(POS.offset(0, 0, 2)));
        player.setPos(origin.x, origin.y, origin.z);
        return player;
    }

    private static BlockHitResult hit(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(POS);
        return new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
    }

    private static ItemStack crafted(GameTestHelper helper, Item... items) {
        List<ItemStack> slots = new ArrayList<>();
        for (Item item : items) slots.add(item == null ? ItemStack.EMPTY : new ItemStack(item));
        CraftingInput input = CraftingInput.of(2, 2, slots);
        var matches = helper.getLevel().getRecipeManager().getAllRecipesFor(RecipeType.CRAFTING).stream()
                .filter(recipe -> recipe.value().matches(input, helper.getLevel())).toList();
        helper.assertTrue(matches.size() == 1, "Opening recipe must have exactly one match, got " + matches.stream().map(r -> r.id().toString()).toList());
        return matches.getFirst().value().assemble(input, helper.getLevel().registryAccess());
    }

    @GameTest(template = "empty")
    public static void openingRecipesAreUniqueAndHandCraftable(GameTestHelper helper) {
        Item blade = TRTRItems.FLINT_BLADE.get(), rope = TRTRItems.ROPE.get(), stick = Items.STICK;
        helper.assertTrue(crafted(helper, blade, blade, rope, stick).is(TRTRItems.FLINT_AXE.get()), "Axe recipe");
        helper.assertTrue(crafted(helper, blade, blade, stick, rope).is(TRTRItems.FLINT_AXE.get()), "Mirrored axe recipe");
        helper.assertTrue(crafted(helper, blade, stick, rope, stick).is(TRTRItems.FLINT_HOE.get()), "Hoe recipe");
        helper.assertTrue(crafted(helper, blade, null, rope, stick).is(TRTRItems.FLINT_KNIFE.get()), "Knife recipe");
        helper.assertTrue(crafted(helper, blade, rope, null, stick).is(TRTRItems.FLINT_SHOVEL.get()), "Shovel recipe");
        helper.assertTrue(crafted(helper, TRTRItems.LOOSE_STONE.get(), TRTRItems.LOOSE_STONE.get(), rope, stick)
                .is(TRTRItems.CRUDE_STONE_PICKAXE.get()), "Pickaxe recipe");
        Item fiber = TRTRItems.PLANT_FIBER.get();
        helper.assertTrue(crafted(helper, fiber, fiber, fiber, null).is(rope), "Fiber to rope");
        helper.assertTrue(crafted(helper, fiber, fiber, fiber, fiber).is(TRTRItems.GRASS_BUNDLE.get()), "Fiber to tinder");
        helper.assertTrue(crafted(helper, stick, rope, stick, null).is(TRTRItems.FIRE_DRILL.get()), "Fire drill recipe");
        helper.succeed();
    }

    private static void assertRecipeTakeover(GameTestHelper helper) {
        var manager = helper.getLevel().getRecipeManager();
        for (String name : List.of("wooden_pickaxe", "wooden_axe", "stone_pickaxe", "stone_axe", "campfire", "furnace")) {
            helper.assertTrue(manager.byKey(ResourceLocation.withDefaultNamespace(name)).isEmpty(), "Disabled recipe returned: " + name);
        }
        helper.assertTrue(manager.getAllRecipesFor(RecipeType.SMELTING).isEmpty(), "Vanilla smelting bypass");
        helper.assertFalse(manager.getAllRecipesFor(RecipeType.CAMPFIRE_COOKING).isEmpty(), "Campfire food must remain available");
        helper.assertTrue(manager.byKey(ResourceLocation.withDefaultNamespace("oak_planks")).isPresent(), "Woodworking recipe must remain");
        helper.assertTrue(manager.byKey(ResourceLocation.fromNamespaceAndPath("trtr", "campfire")).isPresent(), "M0 campfire recipe missing");
    }

    @GameTest(template = "empty")
    public static void recipeTakeoverAppliesAtWorldLoad(GameTestHelper helper) {
        assertRecipeTakeover(helper);
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "zz_reload", timeoutTicks = 1200)
    public static void recipeTakeoverSurvivesResourceReload(GameTestHelper helper) {
        var server = helper.getLevel().getServer();
        var reloaded = server.reloadResources(server.getPackRepository().getSelectedIds());
        helper.succeedWhen(() -> {
            helper.assertTrue(reloaded.isDone(), "Waiting for resource reload");
            reloaded.join();
            assertRecipeTakeover(helper);
        });
    }

    @GameTest(template = "empty")
    public static void craftedCampfirePlacesUnlit(GameTestHelper helper) {
        FakePlayer player = player(helper);
        ItemStack campfire = crafted(helper, Items.OAK_LOG, TRTRItems.GRASS_BUNDLE.get(), Items.OAK_LOG, Items.OAK_LOG);
        helper.assertTrue(campfire.get(DataComponents.BLOCK_STATE) != null, "Campfire must carry its unlit state");
        helper.setBlock(POS, Blocks.STONE);
        player.setItemInHand(InteractionHand.MAIN_HAND, campfire);
        var context = new BlockPlaceContext(player, InteractionHand.MAIN_HAND, campfire, hit(helper));
        ((net.minecraft.world.item.BlockItem) campfire.getItem()).place(context);
        helper.assertBlockProperty(POS.above(), CampfireBlock.LIT, false);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void cancelledMiningCannotGiveLootOrWear(GameTestHelper helper) {
        FakePlayer player = player(helper);
        ItemStack pick = new ItemStack(TRTRItems.CRUDE_STONE_PICKAXE.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, pick);
        helper.setBlock(POS, Blocks.IRON_ORE);
        Consumer<BlockEvent.BreakEvent> cancel = event -> {
            if (event.getPlayer() == player) event.setCanceled(true);
        };
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, false, BlockEvent.BreakEvent.class, cancel);
        try {
            helper.assertFalse(player.gameMode.destroyBlock(helper.absolutePos(POS)), "Break must be cancelled");
        } finally {
            NeoForge.EVENT_BUS.unregister(cancel);
        }
        helper.assertBlockPresent(Blocks.IRON_ORE, POS);
        helper.assertTrue(pick.getDamageValue() == 0, "Cancelled mining must not damage tools");
        helper.assertItemEntityNotPresent(Items.RAW_IRON, POS, 3);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void miningDropsOnceAndChargesWear(GameTestHelper helper) {
        FakePlayer player = player(helper);
        ItemStack pick = new ItemStack(TRTRItems.CRUDE_STONE_PICKAXE.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, pick);
        helper.setBlock(POS, Blocks.IRON_ORE);
        helper.assertTrue(player.gameMode.destroyBlock(helper.absolutePos(POS)), "Ore should break");
        helper.assertItemEntityCountIs(Items.RAW_IRON, POS, 3, 1);
        helper.assertTrue(pick.getDamageValue() == 2, "Base wear plus hardness wear must be charged exactly once");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void harvestingWithWrongToolPreservesSilkTouch(GameTestHelper helper) {
        FakePlayer player = player(helper);
        ItemStack axe = new ItemStack(Items.WOODEN_AXE);
        axe.enchant(helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH), 1);
        player.setItemInHand(InteractionHand.MAIN_HAND, axe);
        helper.setBlock(POS, Blocks.STONE);
        player.gameMode.destroyBlock(helper.absolutePos(POS));
        helper.assertItemEntityCountIs(Items.STONE, POS, 3, 1);
        helper.assertItemEntityNotPresent(Items.COBBLESTONE, POS, 3);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void matureWheatSuppliesRenewableFiber(GameTestHelper helper) {
        FakePlayer player = player(helper);
        helper.setBlock(POS.below(), Blocks.FARMLAND);
        helper.setBlock(POS, Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE, 7));
        player.gameMode.destroyBlock(helper.absolutePos(POS));
        helper.assertItemEntityCountIs(TRTRItems.PLANT_FIBER.get(), POS, 3, 2);
        helper.assertItemEntityPresent(Items.WHEAT_SEEDS, POS, 3);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void gravelUsesFortuneButPreservesSilkTouch(GameTestHelper helper) {
        FakePlayer player = player(helper);
        var enchantments = helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        ItemStack shovel = new ItemStack(Items.DIAMOND_SHOVEL);
        shovel.enchant(enchantments.getOrThrow(Enchantments.FORTUNE), 3);
        player.setItemInHand(InteractionHand.MAIN_HAND, shovel);
        helper.setBlock(POS, Blocks.GRAVEL);
        player.gameMode.destroyBlock(helper.absolutePos(POS));
        helper.assertItemEntityCountIs(TRTRItems.FLINT.get(), POS, 3, 1);
        helper.assertItemEntityNotPresent(Items.FLINT, POS, 3);
        helper.assertItemEntityNotPresent(Items.GRAVEL, POS, 3);

        shovel = new ItemStack(Items.DIAMOND_SHOVEL);
        shovel.enchant(enchantments.getOrThrow(Enchantments.SILK_TOUCH), 1);
        player.setItemInHand(InteractionHand.MAIN_HAND, shovel);
        helper.setBlock(POS, Blocks.GRAVEL);
        player.gameMode.destroyBlock(helper.absolutePos(POS));
        helper.assertItemEntityCountIs(TRTRItems.FLINT.get(), POS, 3, 1);
        helper.assertItemEntityCountIs(Items.GRAVEL, POS, 3, 1);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void looseStoneNeedsSupportAndSoftRockYieldsStarterMaterial(GameTestHelper helper) {
        FakePlayer player = player(helper);
        var stone = TRTRBlocks.LOOSE_STONE.get().defaultBlockState();
        helper.assertFalse(stone.canSurvive(helper.getLevel(), helper.absolutePos(POS)), "Floating stone must fail placement");
        helper.setBlock(POS.below(), Blocks.DIRT);
        helper.assertTrue(stone.canSurvive(helper.getLevel(), helper.absolutePos(POS)), "Supported stone must be placeable");
        helper.setBlock(POS, stone);
        helper.setBlock(POS.below(), Blocks.AIR);
        helper.assertBlockPresent(Blocks.AIR, POS);
        helper.assertItemEntityCountIs(TRTRItems.LOOSE_STONE.get(), POS, 3, 1);
        helper.setBlock(POS, TRTRBlocks.SOFT_ROCK.get());
        player.gameMode.destroyBlock(helper.absolutePos(POS));
        helper.assertItemEntityCountIs(TRTRItems.LOOSE_STONE.get(), POS, 3, 3);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void breakingDryingRackPreservesItsContents(GameTestHelper helper) {
        FakePlayer player = player(helper);
        helper.setBlock(POS, TRTRBlocks.DRYING_RACK.get().defaultBlockState().setValue(DryingRackBlock.STAGE, 2));
        player.gameMode.destroyBlock(helper.absolutePos(POS));
        helper.assertItemEntityCountIs(TRTRItems.DRYING_RACK.get(), POS, 3, 1);
        helper.assertItemEntityCountIs(TRTRItems.SCRAPED_HIDE.get(), POS, 3, 1);
        helper.assertItemEntityNotPresent(Items.LEATHER, POS, 3);
        helper.setBlock(POS, TRTRBlocks.DRYING_RACK.get().defaultBlockState().setValue(DryingRackBlock.STAGE, 3));
        player.gameMode.destroyBlock(helper.absolutePos(POS));
        helper.assertItemEntityCountIs(TRTRItems.DRYING_RACK.get(), POS, 3, 2);
        helper.assertItemEntityCountIs(TRTRItems.SCRAPED_HIDE.get(), POS, 3, 1);
        helper.assertItemEntityCountIs(Items.LEATHER, POS, 3, 1);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void naturalHidesDoNotReplaceDeathInventoryOrCargo(GameTestHelper helper) {
        var cow = helper.spawn(EntityType.COW, POS);
        var params = new LootParams.Builder(helper.getLevel())
                .withParameter(LootContextParams.THIS_ENTITY, cow)
                .withParameter(LootContextParams.ORIGIN, cow.position())
                .withParameter(LootContextParams.DAMAGE_SOURCE, helper.getLevel().damageSources().generic())
                .create(LootContextParamSets.ENTITY);
        for (String animal : List.of("cow", "donkey", "hoglin", "horse", "llama", "mooshroom", "mule", "trader_llama")) {
            var key = ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.withDefaultNamespace("entities/" + animal));
            var table = helper.getLevel().getServer().reloadableRegistries().getLootTable(key);
            int hides = 0;
            for (int seed = 1; seed <= 16; seed++) {
                for (ItemStack stack : table.getRandomItems(params, seed)) {
                    helper.assertFalse(stack.is(Items.LEATHER), "Natural leather bypass in " + animal);
                    if (stack.is(TRTRItems.HIDE.get())) hides += stack.getCount();
                }
            }
            helper.assertTrue(hides > 0, "Natural raw hides missing for " + animal);
        }
        for (var entity : List.of(player(helper), helper.spawn(EntityType.DONKEY, POS))) {
            ItemStack leather = new ItemStack(Items.LEATHER, 4);
            var drops = new ArrayList<ItemEntity>();
            drops.add(new ItemEntity(helper.getLevel(), entity.getX(), entity.getY(), entity.getZ(), leather));
            NeoForge.EVENT_BUS.post(new LivingDropsEvent(entity, helper.getLevel().damageSources().generic(), drops, false));
            helper.assertTrue(drops.getFirst().getItem().is(Items.LEATHER)
                    && drops.getFirst().getItem().getCount() == 4, "Existing inventory/cargo leather must remain leather");
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void scrapingCannotRepeatOnOneLog(GameTestHelper helper) {
        FakePlayer player = player(helper);
        ItemStack knife = new ItemStack(TRTRItems.FLINT_KNIFE.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, knife);
        helper.setBlock(POS, Blocks.OAK_LOG);
        NeoForge.EVENT_BUS.post(new PlayerInteractEvent.RightClickBlock(player, InteractionHand.MAIN_HAND, helper.absolutePos(POS), hit(helper)));
        player.getCooldowns().removeCooldown(knife.getItem());
        NeoForge.EVENT_BUS.post(new PlayerInteractEvent.RightClickBlock(player, InteractionHand.MAIN_HAND, helper.absolutePos(POS), hit(helper)));
        helper.assertBlockPresent(Blocks.STRIPPED_OAK_LOG, POS);
        helper.assertTrue(player.getInventory().countItem(Items.STICK) == 2, "A log only yields one set of sticks");
        helper.assertTrue(knife.getDamageValue() == 1, "One scrape costs one durability");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void knappingEscrowAndPacketReplayAreSafe(GameTestHelper helper) {
        FakePlayer player = player(helper);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.FLINT, 2));
        KnappingMenu abandoned = new KnappingMenu(1, player.getInventory(), InteractionHand.MAIN_HAND);
        abandoned.removed(player);
        helper.assertTrue(player.getInventory().countItem(Items.FLINT) == 2, "Unworked material must be refunded");
        KnappingMenu menu = new KnappingMenu(2, player.getInventory(), InteractionHand.MAIN_HAND);
        helper.assertFalse(menu.clickMenuButton(player, -1), "Negative action must fail");
        helper.assertFalse(menu.clickMenuButton(player, 100), "Unknown action must fail");
        for (int pass = 0; pass < 25 && menu.hasMaterial(); pass++) {
            for (int cell = 0; cell < 25; cell++) {
                if ((KnappingPattern.BLADE & (1 << cell)) == 0) menu.clickMenuButton(player, cell);
            }
        }
        helper.assertTrue(player.getInventory().countItem(TRTRItems.FLINT_BLADE.get()) == 1, "One flint yields one blade");
        for (int cell = 0; cell < 25; cell++) menu.clickMenuButton(player, cell);
        menu.removed(player);
        helper.assertTrue(player.getInventory().countItem(TRTRItems.FLINT_BLADE.get()) == 1, "Replay must not duplicate output");
        helper.assertTrue(player.getInventory().countItem(Items.FLINT) == 1, "Worked material must not be refunded");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 660)
    public static void hideCanBeScrapedDriedAndCollected(GameTestHelper helper) {
        FakePlayer player = player(helper);
        ItemStack knife = new ItemStack(TRTRItems.FLINT_KNIFE.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, knife);
        player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(TRTRItems.HIDE.get()));
        NeoForge.EVENT_BUS.post(new PlayerInteractEvent.RightClickItem(player, InteractionHand.MAIN_HAND));
        helper.assertTrue(player.getInventory().countItem(TRTRItems.SCRAPED_HIDE.get()) == 1, "Scraping output missing");
        helper.assertTrue(player.getInventory().countItem(TRTRItems.HIDE.get()) == 0, "Hide must be consumed");
        player.getInventory().clearContent();
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(TRTRItems.SCRAPED_HIDE.get()));
        helper.setBlock(POS, TRTRBlocks.DRYING_RACK.get());
        helper.useBlock(POS, player);
        helper.assertBlockProperty(POS, DryingRackBlock.STAGE, 1);
        player.lookAt(EntityAnchorArgument.Anchor.EYES, Vec3.atCenterOf(helper.absolutePos(POS)));
        helper.assertTrue(player.pick(player.blockInteractionRange(), 1.0F, false) instanceof BlockHitResult target
                && target.getBlockPos().equals(helper.absolutePos(POS)), "The visible hide must be targetable for collection");
        helper.setBlock(POS.above(2), Blocks.STONE); // Roof keeps this test independent of the world's weather.
        helper.runAfterDelay(610, () -> {
            helper.assertBlockProperty(POS, DryingRackBlock.STAGE, 3);
            helper.useBlock(POS, player);
            helper.assertBlockProperty(POS, DryingRackBlock.STAGE, 0);
            helper.assertTrue(player.getInventory().countItem(Items.LEATHER) == 1, "Finished leather missing");
            helper.useBlock(POS, player);
            helper.assertTrue(player.getInventory().countItem(Items.LEATHER) == 1, "Rack must not duplicate leather");
            helper.succeed();
        });
    }

    @GameTest(template = "empty")
    public static void fireDrillRejectsWetCampfireAndInterruptedUse(GameTestHelper helper) {
        FakePlayer player = player(helper);
        ItemStack drill = new ItemStack(TRTRItems.FIRE_DRILL.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, drill);
        player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(TRTRItems.GRASS_BUNDLE.get(), 2));
        helper.setBlock(POS, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, false).setValue(CampfireBlock.WATERLOGGED, true));
        var context = new UseOnContext(player, InteractionHand.MAIN_HAND, hit(helper));
        helper.assertFalse(drill.getItem().useOn(context).consumesAction(), "Wet campfire must reject ignition");
        helper.setBlock(POS, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, false));
        helper.setBlock(POS.above(2), Blocks.STONE);
        player.lookAt(EntityAnchorArgument.Anchor.EYES, Vec3.atCenterOf(helper.absolutePos(POS)));
        helper.assertTrue(drill.getItem().useOn(context).consumesAction(), "Dry campfire must accept starting");
        drill.getItem().releaseUsing(drill, helper.getLevel(), player, 60);
        player.stopUsingItem();
        drill.getItem().finishUsingItem(drill, helper.getLevel(), player);
        helper.assertBlockProperty(POS, CampfireBlock.LIT, false);
        helper.assertTrue(drill.getDamageValue() == 0 && player.getOffhandItem().getCount() == 2, "Interrupted use consumes nothing");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 120)
    public static void holdingFireDrillCompletesIgnition(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        Vec3 origin = Vec3.atBottomCenterOf(helper.absolutePos(POS.offset(0, 0, 2)));
        player.setPos(origin.x, origin.y, origin.z);
        player.setNoGravity(true);
        player.lookAt(EntityAnchorArgument.Anchor.EYES, Vec3.atCenterOf(helper.absolutePos(POS)).add(0, -0.25, 0));
        ItemStack drill = new ItemStack(TRTRItems.FIRE_DRILL.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, drill);
        player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(TRTRItems.GRASS_BUNDLE.get(), 2));
        helper.setBlock(POS, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, false));
        helper.setBlock(POS.above(2), Blocks.STONE);
        drill.getItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit(helper)));
        helper.startSequence().thenExecuteFor(85, player::tick).thenExecute(() -> {
            helper.assertBlockProperty(POS, CampfireBlock.LIT, true);
            helper.assertTrue(player.getOffhandItem().getCount() == 1, "One tinder bundle must be consumed");
            helper.assertTrue(drill.getDamageValue() == 1, "One ignition costs one durability");
        }).thenSucceed();
    }
}
