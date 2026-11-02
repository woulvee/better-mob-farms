package com.woulvee.bettermobfarms.block;

import java.util.List;

import com.mojang.serialization.MapCodec;
import com.woulvee.bettermobfarms.item.BMFItems;
import com.woulvee.bettermobfarms.particle.BMFParticles;

import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

// A cauldron of lye, made by using bone meal on a water cauldron. Rotten flesh and leather thrown in
// boil down into a soap bar, which uses up one level of lye. It does not fill up with rain.
public class LyeCauldronBlock extends LayeredCauldronBlock {
    public static final MapCodec<LyeCauldronBlock> CODEC = simpleCodec(LyeCauldronBlock::new);

    // Nothing can be done to lye with an item in hand, it is only used up by making soap
    public static final CauldronInteraction.Dispatcher INTERACTIONS = new CauldronInteraction.Dispatcher();

    public LyeCauldronBlock(Properties properties) {
        super(Biome.Precipitation.NONE, INTERACTIONS, properties);
    }

    // LayeredCauldronBlock pins the codec type to its own
    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public MapCodec<LayeredCauldronBlock> codec() {
        return (MapCodec) CODEC;
    }

    // Registered on the water cauldron: bone meal turns the water into lye, keeping its level
    public static InteractionResult makeLye(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, ItemStack itemInHand) {
        if (!level.isClientSide()) {
            level.setBlockAndUpdate(pos, BMFBlocks.LYE_CAULDRON.get().defaultBlockState().setValue(LEVEL, state.getValue(LEVEL)));
            player.awardStat(Stats.USE_CAULDRON);
            player.awardStat(Stats.ITEM_USED.get(itemInHand.getItem()));
            itemInHand.consume(1, player);
            level.playSound(null, pos, SoundEvents.BONE_MEAL_USE, SoundSource.BLOCKS, 1f, 1f);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean isPrecise) {
        super.entityInside(state, level, pos, entity, effectApplier, isPrecise);
        if (level instanceof ServerLevel serverLevel && entity instanceof ItemEntity item && isIngredient(item.getItem())) {
            makeSoap(state, serverLevel, pos);
        }
    }

    private static boolean isIngredient(ItemStack stack) {
        return stack.is(Items.ROTTEN_FLESH) || stack.is(Items.LEATHER);
    }

    private static void makeSoap(BlockState state, ServerLevel level, BlockPos pos) {
        AABB inside = new AABB(pos.getX() + 2 / 16.0, pos.getY() + 4 / 16.0, pos.getZ() + 2 / 16.0,
                pos.getX() + 14 / 16.0, pos.getY() + 1, pos.getZ() + 14 / 16.0);
        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, inside, item -> item.isAlive() && isIngredient(item.getItem()));
        ItemEntity flesh = find(items, Items.ROTTEN_FLESH);
        ItemEntity leather = find(items, Items.LEATHER);
        if (flesh == null || leather == null) {
            return;
        }
        flesh.getItem().shrink(1);
        leather.getItem().shrink(1);

        ItemEntity soap = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, new ItemStack(BMFItems.SOAP_BAR.get()));
        soap.setDeltaMovement(0, 0.2, 0);
        level.addFreshEntity(soap);
        level.playSound(null, pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1f, 1f);
        level.sendParticles(BMFParticles.SOAP_BUBBLE.get(), pos.getX() + 0.5, pos.getY() + 0.9, pos.getZ() + 0.5, 12, 0.25, 0.1, 0.25, 0.02);
        lowerFillLevel(state, level, pos);
    }

    private static @Nullable ItemEntity find(List<ItemEntity> items, Item item) {
        for (ItemEntity entity : items) {
            if (entity.getItem().is(item)) {
                return entity;
            }
        }
        return null;
    }

    // Now and then a bubble rises from the lye, so it can be told apart from water
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(4) == 0) {
            double surface = pos.getY() + getContentHeight(state);
            level.addParticle(BMFParticles.SOAP_BUBBLE.get(), pos.getX() + 0.25 + random.nextDouble() * 0.5, surface,
                    pos.getZ() + 0.25 + random.nextDouble() * 0.5, 0, 0.02, 0);
        }
    }
}
