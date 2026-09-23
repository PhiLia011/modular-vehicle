package com.example.modularvehicle.client;

import com.example.modularvehicle.client.screen.CarScreen;
import com.example.modularvehicle.client.screen.CarStatusScreen;
import com.example.modularvehicle.client.screen.ConfigScreen;
import com.example.modularvehicle.client.screen.PartInstallScreen;
import com.example.modularvehicle.client.screen.WorkbenchScreen;
import com.example.modularvehicle.menu.CarMenu;
import com.example.modularvehicle.menu.CarStatusMenu;
import com.example.modularvehicle.menu.ConfigMenu;
import com.example.modularvehicle.menu.ModMenuTypes;
import com.example.modularvehicle.menu.PartInstallMenu;
import com.example.modularvehicle.menu.WorkbenchMenu;
import net.minecraft.client.gui.screens.MenuScreens;

/**
 * 屏幕（GUI）注册表。仅可在客户端调用（见 CarClientEvents#onClientSetup）。
 */
public class ModScreens {

    public static void registerScreens() {
        MenuScreens.register(ModMenuTypes.WORKBENCH_MENU.get(), WorkbenchScreen::new);
        MenuScreens.register(ModMenuTypes.CAR_MENU.get(), CarScreen::new);
        MenuScreens.register(ModMenuTypes.PART_INSTALL_MENU.get(), PartInstallScreen::new);
        MenuScreens.register(ModMenuTypes.CAR_STATUS_MENU.get(), CarStatusScreen::new);
        MenuScreens.register(ModMenuTypes.CONFIG_MENU.get(), ConfigScreen::new);
    }
}
