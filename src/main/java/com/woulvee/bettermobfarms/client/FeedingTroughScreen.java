package com.woulvee.bettermobfarms.client;

import com.woulvee.bettermobfarms.BetterMobFarms;
import com.woulvee.bettermobfarms.menu.FeedingTroughMenu;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class FeedingTroughScreen extends AbstractContainerScreen<FeedingTroughMenu> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(BetterMobFarms.MODID, "textures/gui/container/feeding_trough.png");

    public FeedingTroughScreen(FeedingTroughMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 133);
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = (imageWidth - font.width(title)) / 2;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractBackground(graphics, mouseX, mouseY, a);
        int xo = (width - imageWidth) / 2;
        int yo = (height - imageHeight) / 2;
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, xo, yo, 0f, 0f, imageWidth, imageHeight, 256, 256);
    }
}
