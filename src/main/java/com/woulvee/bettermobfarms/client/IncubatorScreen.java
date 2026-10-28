package com.woulvee.bettermobfarms.client;

import com.woulvee.bettermobfarms.BetterMobFarms;
import com.woulvee.bettermobfarms.item.BMFDataComponents;
import com.woulvee.bettermobfarms.menu.IncubatorMenu;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;

public class IncubatorScreen extends AbstractContainerScreen<IncubatorMenu> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(BetterMobFarms.MODID, "textures/gui/container/incubator.png");
    private static final Identifier INJECT_SPRITE = sprite("inject_progress");
    private static final Identifier INJECT_FLUID_SPRITE = sprite("inject_progress_fluid");
    private static final Identifier MUTATING_SPRITE = sprite("mutating_progress");
    private static final Identifier INCUBATING_SPRITE = sprite("incubating_progress");

    public IncubatorScreen(IncubatorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    private static Identifier sprite(String name) {
        return Identifier.fromNamespaceAndPath(BetterMobFarms.MODID, "container/incubator/" + name);
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

        // Injecting: the arrow fills from the top while the DNA in the tube drains, colored like the DNA syringe
        float inject = menu.injectProgress();
        int arrowHeight = Mth.ceil(inject * 20);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, INJECT_SPRITE, 9, 20, 0, 0, xo + 34, yo + 32, 9, arrowHeight);
        EntityType<?> dna = menu.dnaSyringe().get(BMFDataComponents.DNA.get());
        if (dna != null) {
            int fluidHeight = 16 - Mth.floor(inject * 16);
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, INJECT_FLUID_SPRITE, 4, 16, 0, 16 - fluidHeight,
                    xo + 29, yo + 34 + 16 - fluidHeight, 4, fluidHeight, ARGB.opaque(DnaColors.get(dna)));
        }

        // Mutating and incubating fill from left to right
        int mutatingWidth = Mth.ceil(menu.mutateProgress() * 24);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, MUTATING_SPRITE, 24, 16, 0, 0, xo + 49, yo + 53, mutatingWidth, 16);
        int incubatingWidth = Mth.ceil(menu.incubateProgress() * 46);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, INCUBATING_SPRITE, 46, 18, 0, 0, xo + 100, yo + 53, incubatingWidth, 18);
    }
}
