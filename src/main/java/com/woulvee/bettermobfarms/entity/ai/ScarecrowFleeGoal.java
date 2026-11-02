package com.woulvee.bettermobfarms.entity.ai;

import java.util.EnumSet;

import com.woulvee.bettermobfarms.Config;
import com.woulvee.bettermobfarms.block.entity.ScarecrowBlockEntity;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

// Makes mobs of the scarecrow_pests tag run from scarecrows without a player head. Tamed and leashed ones stay.
public class ScarecrowFleeGoal extends Goal {
    private static final double SPEED = 1.3;

    private final PathfinderMob mob;
    private @Nullable Vec3 fleePos;

    public ScarecrowFleeGoal(PathfinderMob mob) {
        this.mob = mob;
        setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (mob.getRandom().nextInt(reducedTickDelay(10)) != 0 || mob.isLeashed() || mob instanceof TamableAnimal animal && animal.isTame()) {
            return false;
        }
        ScarecrowBlockEntity scarecrow = ScarecrowBlockEntity.findNearest(mob.level(), mob.position(), false, Config.SCARECROW_REPEL_RADIUS.getAsInt(), null);
        if (scarecrow == null) {
            return false;
        }
        fleePos = DefaultRandomPos.getPosAway(mob, 16, 7, scarecrow.getCenter());
        return fleePos != null;
    }

    @Override
    public void start() {
        mob.getNavigation().moveTo(fleePos.x, fleePos.y, fleePos.z, SPEED);
    }

    @Override
    public boolean canContinueToUse() {
        return !mob.getNavigation().isDone();
    }
}
