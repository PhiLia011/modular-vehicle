package com.example.modularvehicle.registry;

import com.example.modularvehicle.ModularVehicle;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/** 创造模式物品栏：部件物品 + 工作台。 */
public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ModularVehicle.MOD_ID);

    public static final RegistryObject<CreativeModeTab> MAIN_TAB = TABS.register("main",
        () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.modular_vehicle"))
            .icon(() -> new ItemStack(ModItems.ENGINE_ITEM.get()))
            .displayItems((params, output) -> {
                output.accept(ModItems.ENGINE_ITEM.get());
                output.accept(ModItems.WHEEL_ITEM.get());
                output.accept(ModItems.SHELL_ITEM.get());
                output.accept(ModItems.SEAT_ITEM.get());
                output.accept(ModItems.FUEL_TANK_ITEM.get());
                output.accept(ModItems.BATTERY_ITEM.get());
                output.accept(ModItems.WORKBENCH_ITEM.get());
            })
            .build());

    public static void register(IEventBus eventBus) {
        TABS.register(eventBus);
    }
}
