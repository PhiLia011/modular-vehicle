package com.example.modularvehicle.menu;

import com.example.modularvehicle.item.CarPartItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * 部件安装菜单（工作台/载具交互用）。
 * TODO: 关闭菜单时将部件安装到载具（当前仅占位）。
 */
public class PartInstallMenu extends AbstractContainerMenu {
    private final Level level;
    private final BlockPos blockPos;
    private final String partType;
    private final SimpleContainer partContainer = new SimpleContainer(1);

    public PartInstallMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, extraData.readBlockPos(), extraData.readUtf());
    }

    public PartInstallMenu(int containerId, Inventory playerInventory, BlockPos blockPos, String partType) {
        super(ModMenuTypes.PART_INSTALL_MENU.get(), containerId);
        this.level = playerInventory.player.level();
        this.blockPos = blockPos;
        this.partType = partType;

        this.addSlot(new Slot(partContainer, 0, 80, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return isPartValid(stack);
            }
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

    private boolean isPartValid(ItemStack stack) {
        if (stack.getItem() instanceof CarPartItem partItem) {
            return partItem.getPartType().equals(partType);
        }
        return false;
    }

    @Override
    public boolean stillValid(Player player) {
        // 距离校验（8 格）
        return blockPos.distToCenterSqr(player.position()) < 64.0;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            itemStack = stack.copy();
            if (index == 0) {
                if (!moveItemStackTo(stack, 1, slots.size(), false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!moveItemStackTo(stack, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            }
            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return itemStack;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        // TODO: 关闭菜单时尝试把部件安装到载具
    }

    public String getPartType() {
        return partType;
    }
}
