package com.example.modularvehicle.registry;

import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "modular_vehicle")
public class PartReloadListener {
    
    @SubscribeEvent
    public static void onAddReloadListener(AddReloadListenerEvent event) {
        // 添加部件定义重载监听器
        event.addListener(PartRegistry.getInstance());
    }
}
