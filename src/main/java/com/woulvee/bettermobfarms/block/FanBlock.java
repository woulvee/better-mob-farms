package com.woulvee.bettermobfarms.block;

import com.mojang.serialization.MapCodec;
import com.woulvee.bettermobfarms.block.entity.BMFBlockEntities;
import com.woulvee.bettermobfarms.block.entity.FanBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

// While powered by redstone, the fan pushes living entities in front of it.
// Its reach and strength come from the upgrades in its inventory, see FanBlockEntity.
public class FanBlock extends BaseEntityBlock {
    public static final MapCodec<FanBlock> CODEC = simpleCodec(FanBlock::new);

    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final IntegerProperty WIDTH = IntegerProperty.create("width", 1, 3);
    public static final IntegerProperty HEIGHT = IntegerProperty.create("height", 1, 3);
    public static final IntegerProperty LENGTH = IntegerProperty.create("length", 1, 8);
    public static final IntegerProperty POWER = IntegerProperty.create("fan_power", 1, 4);

    public FanBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState()
                .setValue(FACING, Direction.NORTH).setValue(POWERED, false)
                .setValue(WIDTH, 1).setValue(HEIGHT, 1).setValue(LENGTH, 3).setValue(POWER, 1));
    }

    @Override
    protected MapCodec<? extends FanBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED, WIDTH, HEIGHT, LENGTH, POWER);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(POWERED, isPowered(context.getLevel(), context.getClickedPos()));
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean movedByPiston) {
        boolean powered = isPowered(level, pos);
        if (powered != state.getValue(POWERED)) {
            level.setBlock(pos, state.setValue(POWERED, powered), Block.UPDATE_CLIENTS);
        }
    }

    private static boolean isPowered(Level level, BlockPos pos) {
        return level.hasNeighborSignal(pos) || level.hasNeighborSignal(pos.above());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof FanBlockEntity fan) {
            player.openMenu(fan);
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
        return new FanBlockEntity(pos, state);
    }

    // Ticks on both sides: the client needs the airflow for players and the renderer
    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, BMFBlockEntities.FAN.get(), FanBlockEntity::tick);
    }
}
