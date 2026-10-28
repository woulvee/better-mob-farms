package com.woulvee.bettermobfarms.block;

import com.mojang.serialization.MapCodec;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

// Never placed in the world. It only exists so the fan renderer can look up the propeller model per facing.
public class FanPropellerBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<FanPropellerBlock> CODEC = simpleCodec(FanPropellerBlock::new);

    public FanPropellerBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends FanPropellerBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
}
