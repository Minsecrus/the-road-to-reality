package com.trtr.crafting;

import java.util.Set;
import java.util.stream.Collectors;
import com.mojang.logging.LogUtils;
import com.trtr.TRTR;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

/** Runs after the vanilla recipe loader on startup and /reload, before client sync. */
@EventBusSubscriber(modid = TRTR.MODID)
public final class RecipeTakeover {
    private static final Set<ResourceLocation> DISABLED = Set.of(
            "wooden_pickaxe", "wooden_axe", "wooden_shovel", "wooden_hoe", "wooden_sword",
            "stone_pickaxe", "stone_axe", "stone_shovel", "stone_hoe", "stone_sword",
            "campfire", "furnace", "blast_furnace", "smoker", "stonecutter", "brewing_stand", "tnt")
            .stream().map(ResourceLocation::withDefaultNamespace).collect(Collectors.toUnmodifiableSet());

    @SubscribeEvent
    public static void addReloadListener(AddReloadListenerEvent event) {
        RecipeManager manager = event.getServerResources().getRecipeManager();
        event.addListener(new SimplePreparableReloadListener<Boolean>() {
            @Override
            protected Boolean prepare(ResourceManager resources, ProfilerFiller profiler) {
                return Boolean.TRUE;
            }

            @Override
            protected void apply(Boolean prepared, ResourceManager resources, ProfilerFiller profiler) {
                LogUtils.getLogger().info("TRTR: removed {} vanilla recipes for stone-age progression", strip(manager));
            }
        });
    }

    public static boolean isDisabled(RecipeHolder<?> recipe) {
        if (!recipe.id().getNamespace().equals("minecraft")) {
            return false;
        }
        RecipeType<?> type = recipe.value().getType();
        return DISABLED.contains(recipe.id()) || type == RecipeType.SMELTING
                || type == RecipeType.BLASTING || type == RecipeType.SMOKING;
    }

    public static int strip(RecipeManager manager) {
        var recipes = manager.getOrderedRecipes().stream().filter(recipe -> !isDisabled(recipe)).toList();
        int removed = manager.getRecipes().size() - recipes.size();
        manager.replaceRecipes(recipes);
        return removed;
    }

    private RecipeTakeover() {}
}
