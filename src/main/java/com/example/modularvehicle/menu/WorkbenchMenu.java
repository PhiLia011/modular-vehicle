package com.example.modularvehicle.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** 工作台菜单（3x3 合成占位）。TODO: 接入实际合成逻辑。 */
public class WorkbenchMenu extends AbstractContainerMenu {
    private final Level level;
    private final BlockPos blockPos;
    private final SimpleContainer craftContainer = new SimpleContainer(9);

    public WorkbenchMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, extraData.readBlockPos());
    }

    public WorkbenchMenu(int containerId, Inventory playerInventory, BlockPos blockPos) {
        super(ModMenuTypes.WORKBENCH_MENU.get(), containerId);
        this.level = playerInventory.player.level();
        this.blockPos = blockPos;

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                this.addSlot(new Slot(craftContainer, i * 3 + j, 30 + j * 18, 17 + i * 18));
            }
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
        return blockPos.distToCenterSqr(player.position()) < 64.0;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            itemStack = stack.copy();
            if (index < 9) {
                if (!moveItemStackTo(stack, 9, slots.size(), false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!moveItemStackTo(stack, 0, 9, false)) {
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
        // TODO: 决定关闭时是否掉落合成格物品（当前直接清空）
        for (int i = 0; i < 9; i++) {
            slots.get(i).set(ItemStack.EMPTY);
        }
    }
}
