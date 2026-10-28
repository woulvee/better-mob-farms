package com.woulvee.bettermobfarms.client;

import java.util.OptionalInt;

import com.woulvee.bettermobfarms.block.entity.IncubatorBlockEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

// Tint index 0 of the incubator models: the mutated egg inside takes the DNA color of its mob
public class IncubatorEggTint implements BlockTintSource {
    @Override
    public int color(BlockState state) {
        return -1;
    }

    @Override
    public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
        if (!(level.getBlockEntity(pos) instanceof IncubatorBlockEntity incubator) || incubator.displayDna() == null) {
            return -1;
        }
        EntityType<?> dna = incubator.displayDna();
        OptionalInt color = DnaColors.getCached(dna);
        if (color.isPresent()) {
            return ARGB.opaque(color.getAsInt());
        }
        // Chunks are built off the render thread, where the color can not be computed. Do it there and redraw the block.
        BlockPos redrawPos = pos.immutable();
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.execute(() -> {
            DnaColors.get(dna);
            if (minecraft.level != null) {
                minecraft.levelRenderer.blockChanged(minecraft.level, redrawPos, state, state, Block.UPDATE_IMMEDIATE);
            }
        });
        return -1;
    }
}
