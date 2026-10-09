package com.trtr;

import com.mojang.serialization.MapCodec;
import com.trtr.loot.RawHideLootModifier;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class TRTRLootModifiers {
    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> SERIALIZERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, TRTR.MODID);

    static {
        SERIALIZERS.register("raw_hides", () -> RawHideLootModifier.CODEC);
    }

    private TRTRLootModifiers() {}
}
