package com.woulvee.bettermobfarms.item;

import com.woulvee.bettermobfarms.BetterMobFarms;
import com.woulvee.bettermobfarms.block.IncubatorEgg;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class BMFDataComponents {
    public static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, BetterMobFarms.MODID);

    // The mob a syringe took its DNA from
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<EntityType<?>>> DNA = DATA_COMPONENTS.registerComponentType("dna",
            builder -> builder.persistent(EntityType.CODEC).networkSynchronized(EntityType.STREAM_CODEC));

    // Set next to DNA (minecraft:player) when the sample was taken from a player
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<PlayerDna>> PLAYER_DNA = DATA_COMPONENTS.registerComponentType("player_dna",
            builder -> builder.persistent(PlayerDna.CODEC).networkSynchronized(PlayerDna.STREAM_CODEC));

    // The vanilla egg a mutated egg was made from, picks its item model
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<IncubatorEgg>> ORIGIN_EGG = DATA_COMPONENTS.registerComponentType("origin_egg",
            builder -> builder.persistent(IncubatorEgg.CODEC).networkSynchronized(IncubatorEgg.STREAM_CODEC));
}
