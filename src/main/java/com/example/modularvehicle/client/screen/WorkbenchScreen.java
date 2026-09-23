package com.example.modularvehicle.client.screen;

import com.example.modularvehicle.menu.WorkbenchMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class WorkbenchScreen extends AbstractContainerScreen<WorkbenchMenu> {
    private static final ResourceLocation WORKBENCH_TEXTURE = new ResourceLocation("modular_vehicle", "textures/gui/workbench.png");

    public WorkbenchScreen(WorkbenchMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(WORKBENCH_TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        guiGraphics.drawString(font, "合成区域", leftPos + 60, topPos + 5, 0x404040);
        guiGraphics.drawString(font, "物品栏", leftPos + 8, topPos + 72, 0x404040);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
        guiGraphics.drawString(font, title, leftPos + imageWidth / 2 - font.width(title) / 2, topPos + 5, 0x404040, false);
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (isHovering(leftPos + 30, topPos + 17, 54, 54, mouseX, mouseY)) {
            guiGraphics.renderTooltip(font, Component.literal("工作台合成区域"), mouseX, mouseY);
            return;
        }
        super.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
