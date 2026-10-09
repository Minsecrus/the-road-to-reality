package com.trtr;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(TRTR.MODID)
public class TRTR {
    public static final String MODID = "trtr";

    public TRTR(IEventBus modBus) {
        TRTRBlocks.BLOCKS.register(modBus);
        TRTRItems.ITEMS.register(modBus);
        TRTRCreativeTabs.TABS.register(modBus);
        TRTRMenus.MENUS.register(modBus);
        TRTRLootModifiers.SERIALIZERS.register(modBus);
    }
}
