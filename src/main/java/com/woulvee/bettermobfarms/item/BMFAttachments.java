package com.woulvee.bettermobfarms.item;

import com.woulvee.bettermobfarms.BetterMobFarms;

import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class BMFAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, BetterMobFarms.MODID);

    // Entity id of the mob a player takes DNA from with a syringe, -1 if none. Not saved.
    // Both sides of the sampling player set it themselves. It is also synced, so other players can show the stab animation.
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Integer>> SAMPLING_TARGET = ATTACHMENT_TYPES.register("sampling_target",
            () -> AttachmentType.builder(() -> -1).sync(ByteBufCodecs.VAR_INT).build());
}
