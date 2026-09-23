package com.example.modularvehicle.client;

import com.example.modularvehicle.entity.CarEntity;
import com.example.modularvehicle.menu.CarMenu;
import com.example.modularvehicle.menu.CarStatusMenu;
import com.example.modularvehicle.menu.ConfigMenu;
import com.example.modularvehicle.menu.PartInstallMenu;
import com.example.modularvehicle.menu.WorkbenchMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

/**
 * 仅客户端加载的 GUI 打开工具。
 * 由 ModNetwork 通过 DistExecutor 调用， Dedicated 服务器上绝不加载此类。
 */
public final class ClientGuiHandler {

    private ClientGuiHandler() {}

    public static void openWorkbench(BlockPos pos) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        player.openMenu(new SimpleMenuProvider(
            (id, inv, p) -> new WorkbenchMenu(id, inv, pos), Component.literal("工作台")));
    }

    public static void openCar(int entityId) {
        openCarMenu(entityId, (id, inv, car) -> new CarMenu(id, inv, car), "载具");
    }

    public static void openPartInstall(BlockPos pos, String partType) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        player.openMenu(new SimpleMenuProvider(
            (id, inv, p) -> new PartInstallMenu(id, inv, pos, partType), Component.literal("部件安装")));
    }

    public static void openCarStatus(int entityId) {
        openCarMenu(entityId, (id, inv, car) -> new CarStatusMenu(id, inv, car), "载具状态");
    }

    public static void openConfig(int entityId) {
        openCarMenu(entityId, (id, inv, car) -> new ConfigMenu(id, inv, car), "载具配置");
    }

    private interface CarMenuFactory {
        Object create(int id, net.minecraft.world.entity.player.Inventory inv, CarEntity car);
    }

    private static void openCarMenu(int entityId, CarMenuFactory factory, String title) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        Entity entity = player.level().getEntity(entityId);
        if (entity instanceof CarEntity car) {
            player.openMenu(new SimpleMenuProvider(
                (id, inv, p) -> { Object m = factory.create(id, inv, car); return (net.minecraft.world.inventory.AbstractContainerMenu) m; },
                Component.literal(title)));
        }
    }
}
