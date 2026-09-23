package com.example.modularvehicle.client.screen;

import com.example.modularvehicle.menu.ConfigMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ConfigScreen extends AbstractContainerScreen<ConfigMenu> {
    private static final ResourceLocation CONFIG_TEXTURE = new ResourceLocation("modular_vehicle", "textures/gui/config.png");

    public ConfigScreen(ConfigMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 222;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(CONFIG_TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        guiGraphics.drawString(font, "载具配置", leftPos + 60, topPos + 5, 0x404040);
        guiGraphics.drawString(font, "物品栏", leftPos + 8, topPos + 122, 0x404040);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);

        guiGraphics.drawString(font, title, leftPos + imageWidth / 2 - font.width(title) / 2, topPos + 5, 0x404040, false);
        String[] lines = menu.getCarConfig().split("\n");
        for (int i = 0; i < lines.length && i < 7; i++) {
            guiGraphics.drawString(font, lines[i], leftPos + 20, topPos + 25 + i * 20, 0x808080, false);
        }
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (isHovering(leftPos + 20, topPos + 25, 136, 150, mouseX, mouseY)) {
            guiGraphics.renderTooltip(font, Component.literal("载具配置信息"), mouseX, mouseY);
            return;
        }
        super.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
