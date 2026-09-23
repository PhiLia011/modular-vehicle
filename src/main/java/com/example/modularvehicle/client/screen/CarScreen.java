package com.example.modularvehicle.client.screen;

import com.example.modularvehicle.menu.CarMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * 载具部件安装界面。
 * TODO: 需求文档要求分层渲染预览与耐久着色（>75% 正常 / 50-75% 裂纹 / 25-50% 偏暗 / <25% 冒烟）。
 */
public class CarScreen extends AbstractContainerScreen<CarMenu> {
    private static final ResourceLocation CAR_TEXTURE = new ResourceLocation("modular_vehicle", "textures/gui/car.png");

    public CarScreen(CarMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 222;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        // 注意：textures/gui/car.png 当前为占位文件，缺失时显示棋盘格
        guiGraphics.blit(CAR_TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);

        guiGraphics.drawString(font, "部件安装", leftPos + 60, topPos + 5, 0x404040);
        String[] partLabels = {"引擎", "轮子", "座位", "电池", "行李箱", "灯光"};
        for (int i = 0; i < partLabels.length; i++) {
            guiGraphics.drawString(font, partLabels[i], leftPos + 20, topPos + 25 + i * 30, 0x404040);
        }
        guiGraphics.drawString(font, "物品栏", leftPos + 8, topPos + 122, 0x404040);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);

        guiGraphics.drawString(font, title, leftPos + imageWidth / 2 - font.width(title) / 2, topPos + 5, 0x404040, false);
        String status = menu.getCarStatus();
        String[] lines = status.split("\n");
        for (int i = 0; i < lines.length && i < 4; i++) {
            guiGraphics.drawString(font, lines[i], leftPos + 100, topPos + 25 + i * 20, 0x808080, false);
        }
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        for (int i = 0; i < 6; i++) {
            int slotX = leftPos + 80;
            int slotY = topPos + 20 + i * 30;
            if (isHovering(slotX, slotY, 16, 16, mouseX, mouseY)) {
                guiGraphics.renderTooltip(font, Component.literal("部件安装槽"), mouseX, mouseY);
                return;
            }
        }
        super.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
