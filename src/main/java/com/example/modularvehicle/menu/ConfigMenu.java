package com.example.modularvehicle.menu;

import com.example.modularvehicle.entity.CarEntity;
import com.example.modularvehicle.entity.CarPart;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** 载具配置查看菜单（只读槽位）。 */
public class ConfigMenu extends AbstractContainerMenu {
    private final Level level;
    private final CarEntity carEntity;
    private final SimpleContainer displayContainer = new SimpleContainer(4);

    public ConfigMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, resolveCar(extraData, playerInventory));
    }

    private static CarEntity resolveCar(FriendlyByteBuf buf, Inventory playerInventory) {
        int entityId = buf.readInt();
        return playerInventory.player.level().getEntity(entityId) instanceof CarEntity car ? car : null;
    }

    public ConfigMenu(int containerId, Inventory playerInventory, CarEntity carEntity) {
        super(ModMenuTypes.CONFIG_MENU.get(), containerId);
        this.level = playerInventory.player.level();
        this.carEntity = carEntity;

        for (int i = 0; i < 4; i++) {
            this.addSlot(new Slot(displayContainer, i, 30 + i * 20, 35) {
                @Override
                public boolean mayPlace(ItemStack stack) { return false; }

                @Override
                public boolean mayPickup(Player player) { return false; }
            });
        }

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
        if (index < 4) {
            return ItemStack.EMPTY;
        }
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            itemStack = stack.copy();
            if (!moveItemStackTo(stack, 4, slots.size(), false)) {
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

    public String getCarConfig() {
        if (carEntity == null) return "载具配置不可用";
        StringBuilder config = new StringBuilder("载具配置:\n");
        config.append("载具类型: 模块化载具\n");
        config.append("座位数量: ").append(6).append("\n");
        config.append("燃料容量: ").append("100").append("\n");
        config.append("\n部件配置:\n");
        for (int i = 0; i < 6; i++) {
            CarPart part = carEntity.getPart(i);
            var definition = carEntity.getPartDefinition(i);
            config.append(part.getType()).append(": ")
                  .append(part.isInstalled() ? (part.isBroken() ? "损坏" : "正常") : "未安装")
                  .append(" (").append(part.getDurability()).append("/").append(part.getMaxDurability()).append(")\n");
            if (definition != null) {
                config.append("  显示名称: ").append(definition.getDisplayName()).append("\n");
                config.append("  重量: ").append(definition.getWeight()).append("\n");
            }
        }
        return config.toString();
    }

    public String getCompatibilityInfo() {
        if (carEntity == null) return "兼容性信息不可用";
        StringBuilder compatibility = new StringBuilder("兼容性信息:\n");
        compatibility.append("\n部件兼容性:\n");
        for (int i = 0; i < 6; i++) {
            var definition = carEntity.getPartDefinition(i);
            if (definition != null) {
                compatibility.append(definition.getDisplayName()).append(": ");
                for (String vehicle : definition.getCompatibleVehicles()) {
                    compatibility.append(vehicle).append(", ");
                }
                compatibility.setLength(compatibility.length() - 2);
                compatibility.append("\n");
            }
        }
        compatibility.append("\n支持载具类型: 基础载具, 运动载具, SUV\n");
        return compatibility.toString();
    }
}
