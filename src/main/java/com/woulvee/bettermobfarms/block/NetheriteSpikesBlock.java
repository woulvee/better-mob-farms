package com.woulvee.bettermobfarms.block;

import com.mojang.serialization.MapCodec;
import com.woulvee.bettermobfarms.damage.BMFDamageTypes;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class NetheriteSpikesBlock extends SpikeBlock {
    public static final MapCodec<NetheriteSpikesBlock> CODEC = simpleCodec(NetheriteSpikesBlock::new);

    public NetheriteSpikesBlock(Properties properties) {
        super(6.5f, BMFDamageTypes.SPIKE, properties);
    }

    @Override
    protected MapCodec<? extends NetheriteSpikesBlock> codec() {
        return CODEC;
    }

    // Using a magma block on the spikes heats them up
    @Override
    protected InteractionResult useItemOn(ItemStack itemStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!itemStack.is(Items.MAGMA_BLOCK)) {
            return super.useItemOn(itemStack, state, level, pos, player, hand, hitResult);
        }

        if (!level.isClientSide()) {
            level.setBlockAndUpdate(pos, BMFBlocks.HOT_NETHERITE_SPIKES.get().defaultBlockState());
            itemStack.consume(1, player);
            level.playSound(null, pos, SoundEvents.BUCKET_FILL_LAVA, SoundSource.BLOCKS, 1f, 1f);
        }
        return InteractionResult.SUCCESS;
    }
}
