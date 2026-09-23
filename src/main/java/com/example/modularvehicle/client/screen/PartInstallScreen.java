package com.example.modularvehicle.client.screen;

import com.example.modularvehicle.menu.PartInstallMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * 部件安装界面。原交接代码引用了从未创建的 PartInstallScreen，这里补齐最小实现。
 */
public class PartInstallScreen extends AbstractContainerScreen<PartInstallMenu> {

    public PartInstallScreen(PartInstallMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }


    @Override
    protected void renderBg(net.minecraft.client.gui.GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        // 纯色占位背景：使用 1x1 白色纹理平铺太复杂，这里仅绘制暗色矩形
        guiGraphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFF2B2B2B);
    }
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x404040, false);
    }
}
