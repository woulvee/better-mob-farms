package com.woulvee.bettermobfarms.block.entity;

import com.woulvee.bettermobfarms.BetterMobFarms;
import com.woulvee.bettermobfarms.block.BMFBlocks;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class BMFBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, BetterMobFarms.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FanBlockEntity>> FAN = BLOCK_ENTITY_TYPES.register("fan",
            () -> new BlockEntityType<>(FanBlockEntity::new, BMFBlocks.FAN.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<IncubatorBlockEntity>> INCUBATOR = BLOCK_ENTITY_TYPES.register("incubator",
            () -> new BlockEntityType<>(IncubatorBlockEntity::new, BMFBlocks.INCUBATOR.get()));
}
