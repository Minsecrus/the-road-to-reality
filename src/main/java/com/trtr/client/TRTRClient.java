package com.trtr.client;

import com.trtr.TRTR;
import com.trtr.TRTRMenus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = TRTR.MODID, value = Dist.CLIENT)
public final class TRTRClient {
    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(TRTRMenus.KNAPPING.get(), KnappingScreen::new);
    }
    private TRTRClient() {}
}
