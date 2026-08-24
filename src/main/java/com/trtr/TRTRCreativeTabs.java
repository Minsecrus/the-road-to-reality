package com.trtr;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class TRTRCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TRTR.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN =
            TABS.register("main", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.trtr.main"))
                    .icon(() -> TRTRItems.FLINT.get().getDefaultInstance())
                    .displayItems((params, output) -> {
                        output.accept(TRTRItems.FLINT.get());
                        output.accept(TRTRItems.STICK.get());
                        output.accept(TRTRItems.PLANT_FIBER.get());
                        output.accept(TRTRItems.FLINT_AXE.get());
                        output.accept(TRTRItems.FLINT_KNIFE.get());
                        output.accept(TRTRItems.FLINT_SHOVEL.get());
                        output.accept(TRTRItems.FLINT_HOE.get());
                        output.accept(TRTRItems.CRUDE_STONE_PICKAXE.get());
                        output.accept(TRTRItems.LOOSE_STONE.get());
                    })
                    .build());

    private TRTRCreativeTabs() {}
}
