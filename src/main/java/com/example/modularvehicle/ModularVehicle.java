package com.example.modularvehicle;

import com.example.modularvehicle.client.ModScreens;
import com.example.modularvehicle.collision.CarCollisionEvents;
import com.example.modularvehicle.command.CarDebugCommand;
import com.example.modularvehicle.command.PartDebugCommand;
import com.example.modularvehicle.config.CarCollisionConfig;
import com.example.modularvehicle.registry.ModBlocks;
import com.example.modularvehicle.menu.ModMenuTypes;
import com.example.modularvehicle.registry.ModCreativeTabs;
import com.example.modularvehicle.registry.ModEntities;
import com.example.modularvehicle.registry.ModEntityAttributes;
import com.example.modularvehicle.registry.ModItems;
import com.example.modularvehicle.registry.ModRecipes;
import com.example.modularvehicle.registry.ModRenderers;
import com.example.modularvehicle.registry.PartReloadListener;
import com.example.modularvehicle.network.ModNetwork;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;

@Mod(ModularVehicle.MOD_ID)
public class ModularVehicle {
    public static final String MOD_ID = "modular_vehicle";

    public ModularVehicle() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // 注册实体、物品、方块
        ModEntities.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModRecipes.register(modEventBus);

        // 注册菜单类型
        ModCreativeTabs.register(modEventBus);
        ModMenuTypes.register(modEventBus);

        // 注册客户端渲染器
        modEventBus.addListener(ModRenderers::registerEntityRenderers);

        // 注册事件监听
        modEventBus.addListener(this::commonSetup);
        // 实体属性注册（MOD 总线）
        modEventBus.addListener(ModEntityAttributes::register);

        MinecraftForge.EVENT_BUS.register(this);
        
        // 注册配置
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CarCollisionConfig.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // 初始化逻辑
        event.enqueueWork(() -> {
            // 注册实体属性
            // ModEntityAttributes.register();
            
            // 注册网络包
            ModNetwork.register();
        });
    }

        @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        CarDebugCommand.register(event.getDispatcher());
        PartDebugCommand.register(event.getDispatcher());
    }

    // 交互事件处理
    private void onPlayerInteract(PlayerInteractEvent event) {
        // TODO: 处理玩家与载具的交互
    }
}
