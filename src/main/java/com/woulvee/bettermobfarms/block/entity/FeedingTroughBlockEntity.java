package com.woulvee.bettermobfarms.block.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.woulvee.bettermobfarms.Config;
import com.woulvee.bettermobfarms.block.FeedingTroughBlock;
import com.woulvee.bettermobfarms.menu.FeedingTroughMenu;
import com.woulvee.bettermobfarms.util.BMFTags;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

// Lures the livestock around it (the bettermobfarms:livestock entity tag) and, at a regular interval, feeds
// the grown-up ones next to it with food from its inventory, so they fall in love and breed.
// Animals are only fed in pairs of the same kind, so no food goes to an animal without a partner,
// and a kind is no longer fed once there are enough of it around.
public class FeedingTroughBlockEntity extends BaseContainerBlockEntity {
    public static final int CONTAINER_SIZE = 3;

    private static final int LURE_INTERVAL = 40;
    // How close an animal has to be to eat from the trough, and how close lured animals walk up to it
    private static final double FEEDING_REACH = 4;

    private static final Component DEFAULT_NAME = Component.translatable("block.bettermobfarms.feeding_trough");

    private NonNullList<ItemStack> items = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);
    private int feedCooldown;

    public FeedingTroughBlockEntity(BlockPos pos, BlockState state) {
        super(BMFBlockEntities.FEEDING_TROUGH.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FeedingTroughBlockEntity trough) {
        // Spread the work of many troughs over different ticks
        if ((level.getGameTime() + pos.asLong()) % LURE_INTERVAL == 0) {
            trough.lureAnimals(level);
        }
        if (--trough.feedCooldown <= 0) {
            trough.feedCooldown = Config.FEEDING_TROUGH_INTERVAL.get() * 20;
            if (trough.feedAnimals(level)) {
                trough.setChanged();
            }
        }
    }

    private Vec3 center() {
        return Vec3.atBottomCenterOf(worldPosition);
    }

    private List<Animal> livestockWithin(Level level, double horizontal, double vertical) {
        AABB area = new AABB(worldPosition).inflate(horizontal, vertical, horizontal);
        return level.getEntitiesOfClass(Animal.class, area, animal -> animal.isAlive() && animal.is(BMFTags.Entities.LIVESTOCK));
    }

    // Idle animals in range walk over to the trough. Leashed and ridden ones stay where they are,
    // and others that are busy (panicking, following a player) only get sent sometimes, so their own goals win.
    private void lureAnimals(Level level) {
        Vec3 center = center();
        int range = Config.FEEDING_TROUGH_RANGE.get();
        for (Animal animal : livestockWithin(level, range, 4)) {
            if (animal.isLeashed() || animal.isVehicle() || animal.isPassenger()
                    || animal.position().closerThan(center, FEEDING_REACH)) {
                continue;
            }
            if (animal.getNavigation().isDone() || animal.getRandom().nextInt(3) == 0) {
                animal.getNavigation().moveTo(center.x, center.y, center.z, 1.0);
            }
        }
    }

    // Returns whether any food was eaten
    private boolean feedAnimals(Level level) {
        if (isEmpty()) {
            return false;
        }
        Map<EntityType<?>, List<Animal>> hungry = new HashMap<>();
        for (Animal animal : livestockWithin(level, FEEDING_REACH, 1.5)) {
            if (animal.getAge() == 0 && animal.canFallInLove()) {
                hungry.computeIfAbsent(animal.getType(), type -> new ArrayList<>()).add(animal);
            }
        }

        boolean fed = false;
        int maxAnimals = Config.FEEDING_TROUGH_MAX_ANIMALS.get();
        int range = Config.FEEDING_TROUGH_RANGE.get();
        for (List<Animal> animals : hungry.values()) {
            EntityType<?> type = animals.getFirst().getType();
            long nearby = livestockWithin(level, range, 4).stream().filter(animal -> animal.getType() == type).count();
            if (nearby >= maxAnimals) {
                continue;
            }
            for (int i = 0; i + 1 < animals.size(); i += 2) {
                Animal first = animals.get(i);
                Animal second = animals.get(i + 1);
                if (countFood(first) < 2) {
                    break;
                }
                feed(level, first);
                feed(level, second);
                fed = true;
            }
        }
        return fed;
    }

    private int countFood(Animal animal) {
        int count = 0;
        for (ItemStack stack : items) {
            if (!stack.isEmpty() && animal.isFood(stack)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private void feed(Level level, Animal animal) {
        for (ItemStack stack : items) {
            if (!stack.isEmpty() && animal.isFood(stack)) {
                stack.shrink(1);
                break;
            }
        }
        animal.setInLove(null);
        level.playSound(null, animal.getX(), animal.getY(), animal.getZ(), SoundEvents.GENERIC_EAT, SoundSource.NEUTRAL, 0.5f, 1f);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (level != null && !level.isClientSide()) {
            BlockState state = getBlockState();
            BlockState updated = state.setValue(FeedingTroughBlock.FULL, !isEmpty());
            if (updated != state) {
                level.setBlock(worldPosition, updated, Block.UPDATE_CLIENTS);
            }
        }
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
        return new FeedingTroughMenu(containerId, inventory, this);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, items);
        feedCooldown = input.getIntOr("FeedCooldown", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, items);
        output.putInt("FeedCooldown", feedCooldown);
    }
}
