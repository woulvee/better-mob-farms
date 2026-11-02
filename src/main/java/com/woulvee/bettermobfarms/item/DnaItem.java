package com.woulvee.bettermobfarms.item;

import java.util.function.Consumer;

import com.woulvee.bettermobfarms.BetterMobFarms;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

// An item that carries the DNA of a mob (BMFDataComponents#DNA), or of a player (also BMFDataComponents#PLAYER_DNA), like the DNA syringe and the mutated egg
public class DnaItem extends Item {
    public DnaItem(Properties properties) {
        super(properties);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void appendHoverText(ItemStack itemStack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag) {
        EntityType<?> dna = itemStack.get(BMFDataComponents.DNA.get());
        PlayerDna playerDna = itemStack.get(BMFDataComponents.PLAYER_DNA.get());
        Component type = playerDna != null
                ? Component.literal(playerDna.name())
                : dna != null
                ? dna.getDescription()
                : Component.translatable("tooltip." + BetterMobFarms.MODID + ".syringe.dna.missing").withStyle(ChatFormatting.DARK_RED);
        builder.accept(Component.translatable("tooltip." + BetterMobFarms.MODID + ".syringe.dna").withStyle(ChatFormatting.GRAY)
                .append(" ").append(type));
    }
}
