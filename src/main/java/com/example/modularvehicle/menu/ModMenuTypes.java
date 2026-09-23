package com.example.modularvehicle.menu;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
        DeferredRegister.create(ForgeRegistries.MENU_TYPES, "modular_vehicle");

    public static final RegistryObject<MenuType<WorkbenchMenu>> WORKBENCH_MENU =
        MENU_TYPES.register("workbench_menu", () -> IForgeMenuType.create(WorkbenchMenu::new));

    public static final RegistryObject<MenuType<CarMenu>> CAR_MENU =
        MENU_TYPES.register("car_menu", () -> IForgeMenuType.create(CarMenu::new));

    public static final RegistryObject<MenuType<PartInstallMenu>> PART_INSTALL_MENU =
        MENU_TYPES.register("part_install_menu", () -> IForgeMenuType.create(PartInstallMenu::new));

    public static final RegistryObject<MenuType<CarStatusMenu>> CAR_STATUS_MENU =
        MENU_TYPES.register("car_status_menu", () -> IForgeMenuType.create(CarStatusMenu::new));

    public static final RegistryObject<MenuType<ConfigMenu>> CONFIG_MENU =
        MENU_TYPES.register("config_menu", () -> IForgeMenuType.create(ConfigMenu::new));

    public static void register(IEventBus eventBus) {
        MENU_TYPES.register(eventBus);
    }
}
