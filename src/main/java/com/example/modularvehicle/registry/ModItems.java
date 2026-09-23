package com.example.modularvehicle.registry;

import com.example.modularvehicle.ModularVehicle;
import com.example.modularvehicle.item.CarPartItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * 部件物品注册表（需求：引擎/车轮x4/车身外壳/座椅/油箱/电池）。
 */
public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
        DeferredRegister.create(ForgeRegistries.ITEMS, ModularVehicle.MOD_ID);

    public static final RegistryObject<CarPartItem> ENGINE_ITEM = ITEMS.register("engine",
        () -> new CarPartItem("engine", new Item.Properties().stacksTo(1)));

    public static final RegistryObject<CarPartItem> WHEEL_ITEM = ITEMS.register("wheel",
        () -> new CarPartItem("wheel", new Item.Properties().stacksTo(4)));

    public static final RegistryObject<CarPartItem> SHELL_ITEM = ITEMS.register("shell",
        () -> new CarPartItem("shell", new Item.Properties().stacksTo(1)));

    public static final RegistryObject<CarPartItem> SEAT_ITEM = ITEMS.register("seat",
        () -> new CarPartItem("seat", new Item.Properties().stacksTo(1)));

    public static final RegistryObject<CarPartItem> FUEL_TANK_ITEM = ITEMS.register("fuel_tank",
        () -> new CarPartItem("fuel_tank", new Item.Properties().stacksTo(1)));

    public static final RegistryObject<CarPartItem> BATTERY_ITEM = ITEMS.register("battery",
        () -> new CarPartItem("battery", new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> WORKBENCH_ITEM = ITEMS.register("workbench",
        () -> new BlockItem(ModBlocks.WORKBENCH.get(), new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
        PartRegistry.register();
    }
}
