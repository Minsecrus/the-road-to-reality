package com.trtr.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.trtr.TRTRItems;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

/** Only natural animal loot tables opt in. Death inventories and pack-animal cargo remain intact. */
public final class RawHideLootModifier extends LootModifier {
    public static final MapCodec<RawHideLootModifier> CODEC = RecordCodecBuilder.mapCodec(
            instance -> codecStart(instance).apply(instance, RawHideLootModifier::new));

    public RawHideLootModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext context) {
        for (int i = 0; i < loot.size(); i++) {
            if (loot.get(i).is(Items.LEATHER)) {
                loot.set(i, new ItemStack(TRTRItems.HIDE.get(), loot.get(i).getCount()));
            }
        }
        return loot;
    }

    @Override public MapCodec<? extends IGlobalLootModifier> codec() { return CODEC; }
}
