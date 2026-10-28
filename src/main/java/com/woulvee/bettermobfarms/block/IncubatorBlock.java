package com.woulvee.bettermobfarms.block;

import com.mojang.serialization.MapCodec;
import com.woulvee.bettermobfarms.block.entity.BMFBlockEntities;
import com.woulvee.bettermobfarms.block.entity.IncubatorBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

// Turns DNA and an egg into a spawn egg, see IncubatorBlockEntity for the processes.
// EGG and OPEN only pick the model: the egg inside, and whether the door is open.
// WORKING is true while a process runs, the client then puffs steam out of the vent.
public class IncubatorBlock extends BaseEntityBlock {
    public static final MapCodec<IncubatorBlock> CODEC = simpleCodec(IncubatorBlock::new);

    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<IncubatorEgg> EGG = EnumProperty.create("egg", IncubatorEgg.class);
    public static final BooleanProperty OPEN = BooleanProperty.create("open");
    public static final BooleanProperty WORKING = BooleanProperty.create("working");

    public IncubatorBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH).setValue(EGG, IncubatorEgg.NONE)
                .setValue(OPEN, false).setValue(WORKING, false));
    }

    @Override
    protected MapCodec<? extends IncubatorBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, EGG, OPEN, WORKING);
    }

    // Steam comes out between the vent slats, which sit on the right side of the model when looking at its glass front
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(WORKING) || random.nextInt(3) != 0) {
            return;
        }
        Direction vent = state.getValue(FACING).getClockWise();
        Direction along = vent.getClockWise();
        // The slats span 10 pixels along the side and 6 pixels in height
        double sideways = (random.nextDouble() * 10 - 5) / 16;
        double x = pos.getX() + 0.5 + vent.getStepX() * 0.52 + along.getStepX() * sideways;
        double y = pos.getY() + (5 + random.nextDouble() * 6) / 16;
        double z = pos.getZ() + 0.5 + vent.getStepZ() * 0.52 + along.getStepZ() * sideways;
        level.addParticle(ParticleTypes.WHITE_SMOKE, x, y, z, vent.getStepX() * 0.02, 0.01, vent.getStepZ() * 0.02);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof IncubatorBlockEntity incubator) {
            player.openMenu(incubator);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        Containers.updateNeighboursAfterDestroy(state, level, pos);
    }

    /* BLOCK ENTITY */

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new IncubatorBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : createTickerHelper(type, BMFBlockEntities.INCUBATOR.get(), IncubatorBlockEntity::serverTick);
    }
}
