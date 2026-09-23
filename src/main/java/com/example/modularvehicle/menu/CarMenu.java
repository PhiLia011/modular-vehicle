package com.example.modularvehicle.menu;

import com.example.modularvehicle.entity.CarEntity;
import com.example.modularvehicle.item.CarPartItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.SimpleContainer;

/**
 * 载具部件 GUI 菜单。
 * TODO: 需求文档要求基于 ItemStackHandler（IItemHandler）的 9 槽位（引擎/车轮x4/车身/座椅/油箱/电池），
 * 当前先用 SimpleContainer 占位 6 槽，后续接入 Capability 体系。
 */
public class CarMenu extends AbstractContainerMenu {
    private final Level level;
    private final CarEntity carEntity;
    private final SimpleContainer partContainer = new SimpleContainer(6);

    public CarMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, resolveCar(extraData, playerInventory));
    }

    private static CarEntity resolveCar(FriendlyByteBuf buf, Inventory playerInventory) {
        int entityId = buf.readInt();
        return playerInventory.player.level().getEntity(entityId) instanceof CarEntity car ? car : null;
    }

    public CarMenu(int containerId, Inventory playerInventory, CarEntity carEntity) {
        super(ModMenuTypes.CAR_MENU.get(), containerId);
        this.level = playerInventory.player.level();
        this.carEntity = carEntity;

        // 添加部件安装槽位（0-5）
        addPartSlots();

        // 添加玩家物品栏（6-32）与快捷栏（33-41）
        addPlayerInventorySlots(playerInventory);
    }

    private void addPartSlots() {
        int[][] slotPositions = {
            {30, 20},   // 引擎
            {30, 50},   // 轮子
            {30, 80},   // 座位
            {80, 20},   // 电池
            {80, 50},   // 行李箱
            {80, 80}    // 灯光
        };

        for (int i = 0; i < 6; i++) {
            final int slotIndex = i;
            this.addSlot(new Slot(partContainer, i, slotPositions[i][0], slotPositions[i][1]) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return isPartValid(stack, slotIndex);
                }

                @Override
                public void setChanged() {
                    super.setChanged();
                    // TODO: 部件槽位变化同步到 CarEntity（当前容器仅作占位）
                }
            });
        }
    }

    private void addPlayerInventorySlots(Inventory playerInventory) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

    private boolean isPartValid(ItemStack stack, int partIndex) {
        if (stack.getItem() instanceof CarPartItem partItem) {
            return partItem.getPartType().equals(getExpectedPartType(partIndex));
        }
        return false;
    }

    private String getExpectedPartType(int partIndex) {
        switch (partIndex) {
            case 0: return "engine";
            case 1: return "wheel";
            case 2: return "seat";
            case 3: return "battery";
            case 4: return "trunk";
            case 5: return "light";
            default: return "";
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return carEntity != null && carEntity.isAlive() && player.distanceTo(carEntity) <= 8.0;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = slots.get(index);

        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            itemStack = stack.copy();

            if (index < 6) {
                // 部件槽位 -> 玩家物品栏
                if (!moveItemStackTo(stack, 6, slots.size(), false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // 玩家物品栏 -> 部件槽位
                if (!moveItemStackTo(stack, 0, 6, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (stack.getCount() == itemStack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, stack);
        }

        return itemStack;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        // TODO: 玩家离开载具时停止引擎等收尾逻辑
    }

    /** 载具状态文本（仅在有实体引用时有效）。 */
    public String getCarStatus() {
        if (carEntity == null) {
            return "载具状态不可用";
        }
        StringBuilder status = new StringBuilder("载具状态:\n");
        status.append("引擎: ").append(carEntity.isEngineRunning() ? "运行中" : "停止").append("\n");
        status.append("燃料: ").append(carEntity.getFuel()).append("/100\n");
        for (int i = 0; i < 6; i++) {
            var part = carEntity.getPart(i);
            status.append(part.getType()).append(": ")
                  .append(part.isInstalled() ? (part.isBroken() ? "损坏" : "正常") : "未安装")
                  .append(" (").append(part.getDurability()).append("/").append(part.getMaxDurability()).append(")\n");
        }
        return status.toString();
    }
}
