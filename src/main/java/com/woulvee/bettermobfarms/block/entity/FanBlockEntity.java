package com.woulvee.bettermobfarms.block.entity;

import com.woulvee.bettermobfarms.block.FanBlock;
import com.woulvee.bettermobfarms.item.BMFItems;
import com.woulvee.bettermobfarms.menu.FanMenu;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

public class FanBlockEntity extends BaseContainerBlockEntity {
    public static final int SLOT_WIDTH = 0;
    public static final int SLOT_HEIGHT = 1;
    public static final int SLOT_LENGTH = 2;
    public static final int SLOT_POWER = 3;
    public static final int CONTAINER_SIZE = 4;

    private static final Component DEFAULT_NAME = Component.translatable("block.bettermobfarms.fan");

    private NonNullList<ItemStack> items = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);

    private static final float SPIN_SPEED = 0.3f;

    // How many blocks in front of the fan the air reaches, 0 while unpowered or blocked
    private int airflowLength;

    // Client only: propeller angle in radians this tick and the last one, for smooth interpolation.
    // It runs on its own instead of following the game time, which the server corrects every second.
    private float spin;
    private float prevSpin;

    public FanBlockEntity(BlockPos pos, BlockState state) {
        super(BMFBlockEntities.FAN.get(), pos, state);
    }

    // The upgrade item that belongs into a slot
    public static Item upgradeForSlot(int slot) {
        return switch (slot) {
            case SLOT_WIDTH -> BMFItems.UPGRADE_WIDTH.get();
            case SLOT_HEIGHT -> BMFItems.UPGRADE_HEIGHT.get();
            case SLOT_LENGTH -> BMFItems.UPGRADE_LENGTH.get();
            default -> BMFItems.UPGRADE_POWER.get();
        };
    }

    public static void tick(Level level, BlockPos pos, BlockState state, FanBlockEntity fan) {
        if (level.isClientSide()) {
            fan.tickSpin(state.getValue(FanBlock.POWERED));
        }

        if (!state.getValue(FanBlock.POWERED)) {
            fan.airflowLength = 0;
            return;
        }

        Direction facing = state.getValue(FanBlock.FACING);
        if (level.getGameTime() % 4 == 0) {
            fan.airflowLength = computeAirflowLength(level, pos, state, facing);
        }

        AABB airflow = fan.getAirflowBox();
        if (airflow == null) {
            return;
        }

        float power = 0.1f + (float) Math.pow(state.getValue(FanBlock.POWER) - 1, 1.5) * 0.1f;
        double pushX = power * facing.getStepX();
        double pushZ = power * facing.getStepZ();
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, airflow)) {
            // Players move on their own client, everything else is moved by the server
            boolean push = entity instanceof Player ? level.isClientSide() && !entity.isSpectator() : !level.isClientSide();
            if (push) {
                entity.push(pushX, 0, pushZ);
            }
        }
    }

    private void tickSpin(boolean powered) {
        prevSpin = spin;
        if (powered) {
            spin += SPIN_SPEED;
        }
        // Keep the angle small so float precision never degrades, shifting both values keeps the interpolation intact
        if (spin >= Mth.TWO_PI) {
            spin -= Mth.TWO_PI;
            prevSpin -= Mth.TWO_PI;
        }
    }

    // The propeller angle between the last and the current tick
    public float getSpin(float partialTicks) {
        return Mth.lerp(partialTicks, prevSpin, spin);
    }

    // The air stops at the first block that has a full face towards or away from the fan
    private static int computeAirflowLength(Level level, BlockPos pos, BlockState state, Direction facing) {
        int maxLength = state.getValue(FanBlock.LENGTH);
        int width = state.getValue(FanBlock.WIDTH) - 1;
        int height = state.getValue(FanBlock.HEIGHT) - 1;
        Direction right = facing.getClockWise();

        for (int forward = 1; forward <= maxLength; forward++) {
            for (int side = -width; side <= width; side++) {
                for (int up = -height; up <= height; up++) {
                    BlockPos checkPos = pos.relative(facing, forward).relative(right, side).above(up);
                    BlockState checkState = level.getBlockState(checkPos);
                    if (checkState.isFaceSturdy(level, checkPos, facing) || checkState.isFaceSturdy(level, checkPos, facing.getOpposite())) {
                        return forward - 1;
                    }
                }
            }
        }
        return maxLength;
    }

    // The area the fan blows through, or null if there is no airflow
    public @Nullable AABB getAirflowBox() {
        if (airflowLength <= 0) {
            return null;
        }
        BlockState state = getBlockState();
        Direction facing = state.getValue(FanBlock.FACING);
        Direction right = facing.getClockWise();
        int width = state.getValue(FanBlock.WIDTH) - 1;
        int height = state.getValue(FanBlock.HEIGHT) - 1;
        BlockPos start = worldPosition.relative(facing).relative(right, -width).below(height);
        BlockPos end = worldPosition.relative(facing, airflowLength).relative(right, width).above(height);
        return AABB.encapsulatingFullBlocks(start, end);
    }

    // The upgrade counts are mirrored into the block state, so both sides know the fan's size
    private void updateUpgradeState() {
        if (level == null || level.isClientSide()) {
            return;
        }
        BlockState state = getBlockState();
        BlockState upgraded = state
                .setValue(FanBlock.WIDTH, Math.min(3, items.get(SLOT_WIDTH).getCount() + 1))
                .setValue(FanBlock.HEIGHT, Math.min(3, items.get(SLOT_HEIGHT).getCount() + 1))
                .setValue(FanBlock.LENGTH, Math.min(8, items.get(SLOT_LENGTH).getCount() + 3))
                .setValue(FanBlock.POWER, Math.min(4, items.get(SLOT_POWER).getCount() + 1));
        if (upgraded != state) {
            level.setBlock(worldPosition, upgraded, Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public void setChanged() {
        super.setChanged();
        updateUpgradeState();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack itemStack) {
        return itemStack.is(upgradeForSlot(slot));
    }

    @Override
    public int getContainerSize() {
        return CONTAINER_SIZE;
    }

    @Override
    protected Component getDefaultName() {
        return DEFAULT_NAME;
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new FanMenu(containerId, inventory, this);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, items);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, items);
    }
}
