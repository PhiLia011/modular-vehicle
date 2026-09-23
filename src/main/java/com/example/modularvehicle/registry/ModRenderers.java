package com.example.modularvehicle.registry;

import com.example.modularvehicle.ModularVehicle;
import com.example.modularvehicle.entity.CarEntity;
import com.example.modularvehicle.render.CarRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;

@Mod.EventBusSubscriber(modid = ModularVehicle.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ModRenderers {
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // 注册载具渲染器
        event.registerEntityRenderer(ModEntities.CAR.get(), CarRenderer::new);
    }

    public static void registerClientReloadListeners(RegisterClientReloadListenersEvent event) {
        // 注册模型重新加载监听器
        event.registerReloadListener(new CarModelReloadListener());
    }
}
