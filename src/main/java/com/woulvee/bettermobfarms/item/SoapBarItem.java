package com.woulvee.bettermobfarms.item;

import com.woulvee.bettermobfarms.block.BMFBlocks;
import com.woulvee.bettermobfarms.effect.BMFEffects;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

// Right click a player or mob to soap it for 15 seconds, or a floor or wall to apply a layer of soap.
// Every use wears the bar down by one.
public class SoapBarItem extends Item {
    private static final int SOAPED_TICKS = 300;

    public SoapBarItem(Properties properties) {
        super(properties);
    }

    // Called from BetterMobFarms#onEntityInteract on both sides, before the target can handle the click itself
    public InteractionResult soap(ItemStack itemStack, Player player, InteractionHand hand, LivingEntity target) {
        if (!player.level().isClientSide()) {
            target.addEffect(new MobEffectInstance(BMFEffects.SOAPED, SOAPED_TICKS, 0, false, false, true), player);
            player.level().playSound(null, target.blockPosition(), SoundEvents.HONEY_BLOCK_SLIDE, SoundSource.PLAYERS, 1f, 1.2f);
            itemStack.hurtAndBreak(1, player, hand);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
        BlockState soap = BMFBlocks.SOAP.get().stateFor(context.getClickedFace());
        if (soap == null || !level.getBlockState(pos).canBeReplaced() || !soap.canSurvive(level, pos)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            level.setBlockAndUpdate(pos, soap);
            level.playSound(null, pos, SoundEvents.HONEY_BLOCK_PLACE, SoundSource.BLOCKS, 1f, 1.2f);
            if (context.getPlayer() != null) {
                context.getItemInHand().hurtAndBreak(1, context.getPlayer(), context.getHand());
            }
        }
        return InteractionResult.SUCCESS;
    }
}
