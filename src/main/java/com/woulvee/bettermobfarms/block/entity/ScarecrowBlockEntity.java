package com.woulvee.bettermobfarms.block.entity;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import com.woulvee.bettermobfarms.Config;
import com.woulvee.bettermobfarms.block.ScarecrowBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

// Holds the player head of a scarecrow. Pests and hostile mobs act on it through the goals in entity.ai,
// which look up the scarecrows around them here. Only phantoms have to be handled by the scarecrow itself.
public class ScarecrowBlockEntity extends BlockEntity {
    // The scarecrows of each level on the server, so mobs don't have to scan for them
    private static final Map<Level, Set<ScarecrowBlockEntity>> LOADED = new WeakHashMap<>();

    private static final int PHANTOM_CHECK_INTERVAL = 10;
    // Phantoms circle high above their target
    private static final double PHANTOM_CHECK_HEIGHT = 48;

    private ItemStack head = ItemStack.EMPTY;

    public ScarecrowBlockEntity(BlockPos pos, BlockState state) {
        super(BMFBlockEntities.SCARECROW.get(), pos, state);
    }

    public boolean hasHead() {
        return !head.isEmpty();
    }

    public ItemStack getHead() {
        return head;
    }

    public void setHead(ItemStack head) {
        this.head = head;
        setChanged();
        if (level != null) {
            BlockState state = getBlockState().setValue(ScarecrowBlock.PLAYER_HEAD, hasHead());
            level.setBlock(worldPosition, state, Block.UPDATE_ALL);
            // Replacing one head with another doesn't change the block state, so the client needs an extra update
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }

    // The middle of the scarecrow, at the height of its torso
    public Vec3 getCenter() {
        return Vec3.atBottomCenterOf(worldPosition.above());
    }

    // The nearest scarecrow within the radius that lures mobs (with a head) or repels them (without one)
    public static @Nullable ScarecrowBlockEntity findNearest(Level level, Vec3 pos, boolean luring, double radius, @Nullable BlockPos ignored) {
        ScarecrowBlockEntity nearest = null;
        double nearestDistance = radius * radius;
        for (ScarecrowBlockEntity scarecrow : LOADED.getOrDefault(level, Set.of())) {
            double distance = scarecrow.getCenter().distanceToSqr(pos);
            if (distance < nearestDistance && scarecrow.hasHead() == luring && !scarecrow.worldPosition.equals(ignored)) {
                nearest = scarecrow;
                nearestDistance = distance;
            }
        }
        return nearest;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ScarecrowBlockEntity scarecrow) {
        if (scarecrow.hasHead() || level.getGameTime() % PHANTOM_CHECK_INTERVAL != 0) {
            return;
        }
        double radius = Config.SCARECROW_REPEL_RADIUS.getAsInt();
        AABB area = new AABB(pos).inflate(radius, PHANTOM_CHECK_HEIGHT, radius);
        Vec3 center = scarecrow.getCenter();
        List<Phantom> phantoms = level.getEntitiesOfClass(Phantom.class, area, phantom -> phantom.getTarget() != null);
        for (Phantom phantom : phantoms) {
            LivingEntity target = phantom.getTarget();
            if (target.position().distanceToSqr(center) < radius * radius) {
                phantom.setTarget(null);
            }
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level instanceof ServerLevel) {
            LOADED.computeIfAbsent(level, key -> new HashSet<>()).add(this);
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        Set<ScarecrowBlockEntity> scarecrows = LOADED.get(level);
        if (scarecrows != null) {
            scarecrows.remove(this);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.storeNullable("Head", ItemStack.CODEC, hasHead() ? head : null);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        head = input.read("Head", ItemStack.CODEC).orElse(ItemStack.EMPTY);
    }

    // The item of a scarecrow keeps its head. Dropping the head separately would leave the player with
    // the pumpkin they got for it on top of the one inside the scarecrow.
    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        head = components.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyOne();
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        if (hasHead()) {
            components.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(List.of(head)));
        }
    }

    @Override
    public void removeComponentsFromTag(ValueOutput output) {
        super.removeComponentsFromTag(output);
        output.discard("Head");
    }

    // The client draws the head
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveCustomOnly(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
