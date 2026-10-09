package com.trtr;

import com.trtr.crafting.KnappingMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class TRTRMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, TRTR.MODID);
    public static final DeferredHolder<MenuType<?>, MenuType<KnappingMenu>> KNAPPING =
            MENUS.register("knapping", () -> new MenuType<>(KnappingMenu::new, FeatureFlags.DEFAULT_FLAGS));
    private TRTRMenus() {}
}
