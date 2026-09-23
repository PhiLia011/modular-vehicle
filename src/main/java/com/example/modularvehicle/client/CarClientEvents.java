package com.example.modularvehicle.client;

import com.example.modularvehicle.ModularVehicle;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * 客户端初始化事件。
 * 屏幕注册必须只在物理客户端执行，因此从 commonSetup 移到这里。
 */
@Mod.EventBusSubscriber(modid = ModularVehicle.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CarClientEvents {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(ModScreens::registerScreens);
    }
}
