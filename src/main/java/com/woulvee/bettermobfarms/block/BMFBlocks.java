package com.woulvee.bettermobfarms.block;

import com.woulvee.bettermobfarms.BetterMobFarms;
import com.woulvee.bettermobfarms.damage.BMFDamageTypes;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class BMFBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(BetterMobFarms.MODID);

    public static final DeferredBlock<Block> WITHERPROOF_BLOCK = BLOCKS.registerSimpleBlock("netheriteforged_obsidian",
            p -> p.mapColor(MapColor.COLOR_BLACK).strength(52f, 1200f).requiresCorrectToolForDrops().sound(SoundType.NETHERITE_BLOCK));

    public static final DeferredBlock<SpikeBlock> IRON_SPIKES = BLOCKS.registerBlock("iron_spikes",
            p -> new SpikeBlock(3f, BMFDamageTypes.SPIKE, p),
            p -> p.mapColor(MapColor.METAL).strength(1.5f, 2.8f).requiresCorrectToolForDrops().noOcclusion().sound(SoundType.METAL));
    public static final DeferredBlock<SpikeBlock> DIAMOND_SPIKES = BLOCKS.registerBlock("diamond_tipped_spikes",
            p -> new SpikeBlock(5f, BMFDamageTypes.SPIKE, p),
            p -> p.mapColor(MapColor.DIAMOND).strength(2.3f, 5f).requiresCorrectToolForDrops().noOcclusion().sound(SoundType.METAL));
    public static final DeferredBlock<NetheriteSpikesBlock> NETHERITE_SPIKES = BLOCKS.registerBlock("netherite_coated_spikes",
            NetheriteSpikesBlock::new,
            p -> p.mapColor(MapColor.COLOR_BLACK).strength(3.5f, 500f).requiresCorrectToolForDrops().noOcclusion().sound(SoundType.NETHERITE_BLOCK));
    public static final DeferredBlock<HotNetheriteSpikesBlock> HOT_NETHERITE_SPIKES = BLOCKS.registerBlock("hot_netherite_coated_spikes",
            HotNetheriteSpikesBlock::new,
            p -> p.mapColor(MapColor.COLOR_ORANGE).strength(3.5f, 500f).requiresCorrectToolForDrops().noOcclusion().lightLevel(state -> 3).sound(SoundType.NETHERITE_BLOCK));
    public static final DeferredBlock<IceSpikesBlock> ICE_SPIKES = BLOCKS.registerBlock("ice_spikes",
            IceSpikesBlock::new,
            p -> p.mapColor(MapColor.ICE).friction(0.98f).strength(0.6f).requiresCorrectToolForDrops().noOcclusion().sound(SoundType.GLASS));
    public static final DeferredBlock<PunjiSticksBlock> BAMBOO_SPIKES = BLOCKS.registerBlock("punji_sticks",
            PunjiSticksBlock::new,
            p -> p.mapColor(MapColor.PLANT).strength(0.5f).noOcclusion().sound(SoundType.BAMBOO).pushReaction(PushReaction.DESTROY));

    public static final DeferredBlock<FanBlock> FAN = BLOCKS.registerBlock("fan",
            FanBlock::new,
            p -> p.mapColor(DyeColor.GRAY).strength(3.5f).requiresCorrectToolForDrops().noOcclusion());
    public static final DeferredBlock<FanPropellerBlock> FAN_PROPELLER = BLOCKS.registerBlock("fan_propeller",
            FanPropellerBlock::new,
            p -> p.noCollision().noOcclusion().noLootTable());

    public static final DeferredBlock<BearTrapBlock> BEAR_TRAP = BLOCKS.registerBlock("bear_trap",
            BearTrapBlock::new,
            p -> p.mapColor(MapColor.METAL).strength(2f, 6f).requiresCorrectToolForDrops().noOcclusion().sound(SoundType.METAL)
                    .pushReaction(PushReaction.DESTROY));

    // Placed with a soap bar, has no item of its own and drops nothing
    public static final DeferredBlock<SoapBlock> SOAP = BLOCKS.registerBlock("soap",
            SoapBlock::new,
            p -> p.mapColor(MapColor.SNOW).instabreak().noOcclusion().noLootTable().replaceable().sound(SoundType.HONEY_BLOCK)
                    .pushReaction(PushReaction.DESTROY));

    public static final DeferredBlock<IncubatorBlock> INCUBATOR = BLOCKS.registerBlock("incubator",
            IncubatorBlock::new,
            p -> p.mapColor(DyeColor.WHITE).strength(3.5f).requiresCorrectToolForDrops().noOcclusion());
}
