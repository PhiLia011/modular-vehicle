package com.example.modularvehicle.client.screen;

import com.example.modularvehicle.menu.CarStatusMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class CarStatusScreen extends AbstractContainerScreen<CarStatusMenu> {
    private static final ResourceLocation CAR_STATUS_TEXTURE = new ResourceLocation("modular_vehicle", "textures/gui/car_status.png");

    public CarStatusScreen(CarStatusMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 222;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(CAR_STATUS_TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        guiGraphics.drawString(font, "载具状态", leftPos + 60, topPos + 5, 0x404040);
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
        for (int i = 0; i < lines.length && i < 6; i++) {
            guiGraphics.drawString(font, lines[i], leftPos + 20, topPos + 25 + i * 20, 0x808080, false);
        }
        String performance = menu.getCarPerformance();
        String[] perfLines = performance.split("\n");
        for (int i = 0; i < perfLines.length && i < 4; i++) {
            guiGraphics.drawString(font, perfLines[i], leftPos + 20, topPos + 145 + i * 20, 0x808080, false);
        }
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (isHovering(leftPos + 20, topPos + 25, 136, 120, mouseX, mouseY)) {
            guiGraphics.renderTooltip(font, Component.literal("载具状态信息"), mouseX, mouseY);
            return;
        }
        super.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
