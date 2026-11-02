package com.woulvee.bettermobfarms.entity.ai;

import java.util.EnumSet;

import com.woulvee.bettermobfarms.Config;
import com.woulvee.bettermobfarms.block.entity.ScarecrowBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

// Sends idle hostile mobs to scarecrows with a player head. Once a mob reaches one and hits it, it notices
// the fake and ignores that scarecrow for a while. A real target always comes first.
public class ScarecrowLureGoal extends Goal {
    private static final double SPEED = 1;
    private static final double HIT_REACH_SQR = 4;
    private static final int GIVE_UP_TICKS = 600;

    private final PathfinderMob mob;
    private @Nullable ScarecrowBlockEntity scarecrow;
    private @Nullable BlockPos ignored;
    private long ignoredUntil;
    private int ticksLeft;

    public ScarecrowLureGoal(PathfinderMob mob) {
        this.mob = mob;
        setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (mob.getRandom().nextInt(reducedTickDelay(20)) != 0 || mob.getTarget() != null || mob.isLeashed() || mob.isPassenger()) {
            return false;
        }
        if (mob.level().getGameTime() >= ignoredUntil) {
            ignored = null;
        }
        scarecrow = ScarecrowBlockEntity.findNearest(mob.level(), mob.position(), true, Config.SCARECROW_LURE_RADIUS.getAsInt(), ignored);
        return scarecrow != null;
    }

    @Override
    public void start() {
        BlockPos pos = scarecrow.getBlockPos();
        ticksLeft = GIVE_UP_TICKS;
        mob.setAggressive(true);
        mob.getNavigation().moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 1, SPEED);
    }

    @Override
    public boolean canContinueToUse() {
        return scarecrow != null && !scarecrow.isRemoved() && scarecrow.hasHead() && mob.getTarget() == null;
    }

    @Override
    public void tick() {
        Vec3 center = scarecrow.getCenter();
        mob.getLookControl().setLookAt(center);
        if (mob.distanceToSqr(center) <= HIT_REACH_SQR) {
            hit(center);
        } else if (--ticksLeft <= 0 || mob.getNavigation().isDone()) {
            // No way to get there, don't keep trying
            ignoreScarecrow();
        }
    }

    private void hit(Vec3 center) {
        mob.swing(InteractionHand.MAIN_HAND);
        mob.level().playSound(null, center.x, center.y, center.z, SoundEvents.ARMOR_STAND_HIT, SoundSource.HOSTILE, 1f, 1f);
        if (mob.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.CRIT, center.x, center.y + 0.5, center.z, 8, 0.2, 0.3, 0.2, 0.1);
        }
        ignoreScarecrow();
    }

    private void ignoreScarecrow() {
        ignored = scarecrow.getBlockPos();
        ignoredUntil = mob.level().getGameTime() + Config.SCARECROW_IGNORE_TIME.getAsInt() * 20L;
        scarecrow = null;
    }

    @Override
    public void stop() {
        scarecrow = null;
        mob.setAggressive(false);
        mob.getNavigation().stop();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}
