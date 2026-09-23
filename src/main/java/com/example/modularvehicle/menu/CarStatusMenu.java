package com.example.modularvehicle.menu;

import com.example.modularvehicle.entity.CarEntity;
import com.example.modularvehicle.entity.CarPart;
import com.example.modularvehicle.entity.CarPart;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** 载具状态查看菜单（只读槽位）。 */
public class CarStatusMenu extends AbstractContainerMenu {
    private final Level level;
    private final CarEntity carEntity;
    private final SimpleContainer displayContainer = new SimpleContainer(1);

    public CarStatusMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, resolveCar(extraData, playerInventory));
    }

    private static CarEntity resolveCar(FriendlyByteBuf buf, Inventory playerInventory) {
        int entityId = buf.readInt();
        return playerInventory.player.level().getEntity(entityId) instanceof CarEntity car ? car : null;
    }

    public CarStatusMenu(int containerId, Inventory playerInventory, CarEntity carEntity) {
        super(ModMenuTypes.CAR_STATUS_MENU.get(), containerId);
        this.level = playerInventory.player.level();
        this.carEntity = carEntity;

        // 只读显示槽位
        this.addSlot(new Slot(displayContainer, 0, 80, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) { return false; }

            @Override
            public boolean mayPickup(Player player) { return false; }
        });

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return carEntity != null && carEntity.isAlive() && player.distanceTo(carEntity) <= 8.0;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        // 只读菜单：仅允许在玩家背包内移动
        if (index == 0) {
            return ItemStack.EMPTY;
        }
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            itemStack = stack.copy();
            if (!moveItemStackTo(stack, 1, slots.size(), false)) {
                return ItemStack.EMPTY;
            }
            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return itemStack;
    }

    public String getCarStatus() {
        if (carEntity == null) return "载具状态不可用";
        StringBuilder status = new StringBuilder("载具状态:\n");
        status.append("引擎: ").append(carEntity.isEngineRunning() ? "运行中" : "停止").append("\n");
        status.append("燃料: ").append(carEntity.getFuel()).append("/100\n");
        for (int i = 0; i < CarPart.SLOT_COUNT; i++) {
            CarPart part = carEntity.getPart(i);
            status.append(part.getType()).append(": ")
                  .append(part.isInstalled() ? (part.isBroken() ? "损坏" : "正常") : "未安装")
                  .append(" (").append(part.getDurability()).append("/").append(part.getMaxDurability()).append(")\n");
        }
        return status.toString();
    }

    public String getCarPerformance() {
        if (carEntity == null) return "性能数据不可用";
        StringBuilder performance = new StringBuilder("载具性能:\n");
        double baseSpeed = 1.0;
        var cm = carEntity.getCollisionManager();
        baseSpeed *= cm.isPartEffective(CarPart.PartType.ENGINE) ? 1.2 : 0.3;
        baseSpeed *= cm.isPartEffective(CarPart.PartType.BATTERY) ? 1.0 : 0.7;
        baseSpeed *= cm.isPartEffective(CarPart.PartType.WHEEL) ? 1.0 : 0.5;
        performance.append("当前速度倍率: ").append(String.format("%.2f", baseSpeed)).append("\n");
        performance.append("转向能力: ").append(cm.isPartEffective(CarPart.PartType.WHEEL) ? "正常" : "困难").append("\n");
        performance.append("摩擦系数: ").append(String.format("%.2f",
            cm.isPartEffective(CarPart.PartType.WHEEL) ? 0.98 : 0.7)).append("\n");
        return performance.toString();
    }
}
