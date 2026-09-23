package com.example.modularvehicle.menu;

import com.example.modularvehicle.entity.CarEntity;
import com.example.modularvehicle.entity.CarPart;
import com.example.modularvehicle.item.CarPartItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * 载具部件 GUI 菜单（9 槽：引擎/车轮x4/车身外壳/座椅/油箱/电池）。
 * TODO: 槽位直接绑定 CarEntity 的 ItemStackHandler（当前为显示用 SimpleContainer 镜像）。
 */
public class CarMenu extends AbstractContainerMenu {
    private final Level level;
    private final CarEntity carEntity;
    private final SimpleContainer partContainer = new SimpleContainer(CarPart.SLOT_COUNT);

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

        addPartSlots();
        addPlayerInventorySlots(playerInventory);
    }

    private void addPartSlots() {
        // 3x3 布局：上排 引擎+轮x2，中排 轮x2+外壳，下排 座椅+油箱+电池
        int[][] slotPositions = {
            {30, 20},   // 0 引擎
            {30, 50},   // 1 左前轮
            {30, 80},   // 2 右前轮
            {80, 20},   // 3 左后轮
            {80, 50},   // 4 右后轮
            {80, 80},   // 5 车身外壳
            {130, 20},  // 6 座椅
            {130, 50},  // 7 油箱
            {130, 80}   // 8 电池
        };

        for (int i = 0; i < CarPart.SLOT_COUNT; i++) {
            final int slotIndex = i;
            this.addSlot(new Slot(partContainer, i, slotPositions[i][0], slotPositions[i][1]) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return isPartValid(stack, slotIndex);
                }

                @Override
                public void setChanged() {
                    super.setChanged();
                    // TODO: 镜像容器变化同步到 CarEntity 的 ItemStackHandler
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
            return CarPart.partTypeForSlot(partIndex).getJsonType().equals(partItem.getPartType());
        }
        return false;
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

            if (index < CarPart.SLOT_COUNT) {
                if (!moveItemStackTo(stack, CarPart.SLOT_COUNT, slots.size(), false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!moveItemStackTo(stack, 0, CarPart.SLOT_COUNT, false)) {
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

    /** 载具状态文本。 */
    public String getCarStatus() {
        if (carEntity == null) {
            return "载具状态不可用";
        }
        StringBuilder status = new StringBuilder("载具状态:\n");
        status.append("引擎: ").append(carEntity.isEngineRunning() ? "运行中" : "停止").append("\n");
        status.append("燃料: ").append(carEntity.getFuel()).append("/100\n");
        for (int i = 0; i < CarPart.SLOT_COUNT; i++) {
            status.append(CarEntity.slotName(i)).append(": ")
                  .append(carEntity.isPartInstalled(i) ? (carEntity.getPart(i).isBroken() ? "损坏" : "正常") : "未安装")
                  .append(" (").append(carEntity.getPart(i).getDurability())
                  .append("/").append(carEntity.getPart(i).getMaxDurability()).append(")\n");
        }
        return status.toString();
    }
}
