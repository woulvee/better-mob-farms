package com.woulvee.bettermobfarms.item;

import com.woulvee.bettermobfarms.BetterMobFarms;
import java.util.Comparator;
import java.util.List;

import com.woulvee.bettermobfarms.block.BMFBlocks;
import com.woulvee.bettermobfarms.block.IncubatorEgg;
import com.woulvee.bettermobfarms.util.BMFTags;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.alchemy.PotionContents;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class BMFItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(BetterMobFarms.MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, BetterMobFarms.MODID);

    // BLOCK ITEMS
    public static final DeferredItem<BlockItem> WITHERPROOF_BLOCK = ITEMS.registerSimpleBlockItem(BMFBlocks.WITHERPROOF_BLOCK);
    public static final DeferredItem<BlockItem> IRON_SPIKES = ITEMS.registerSimpleBlockItem(BMFBlocks.IRON_SPIKES);
    public static final DeferredItem<BlockItem> DIAMOND_SPIKES = ITEMS.registerSimpleBlockItem(BMFBlocks.DIAMOND_SPIKES);
    public static final DeferredItem<BlockItem> NETHERITE_SPIKES = ITEMS.registerSimpleBlockItem(BMFBlocks.NETHERITE_SPIKES);
    public static final DeferredItem<BlockItem> HOT_NETHERITE_SPIKES = ITEMS.registerSimpleBlockItem(BMFBlocks.HOT_NETHERITE_SPIKES);
    public static final DeferredItem<BlockItem> ICE_SPIKES = ITEMS.registerSimpleBlockItem(BMFBlocks.ICE_SPIKES);
    public static final DeferredItem<BlockItem> BAMBOO_SPIKES = ITEMS.registerSimpleBlockItem(BMFBlocks.BAMBOO_SPIKES);
    public static final DeferredItem<BlockItem> FAN = ITEMS.registerSimpleBlockItem(BMFBlocks.FAN);
    public static final DeferredItem<BlockItem> INCUBATOR = ITEMS.registerSimpleBlockItem(BMFBlocks.INCUBATOR);
    public static final DeferredItem<BlockItem> BEAR_TRAP = ITEMS.registerSimpleBlockItem(BMFBlocks.BEAR_TRAP);

    // MATERIALS
    public static final DeferredItem<Item> ICE_SHARD = ITEMS.registerSimpleItem("ice_shard");
    public static final DeferredItem<Item> NETHER_STAR_SHARD = ITEMS.registerSimpleItem("nether_star_shard",
            p -> p.rarity(Rarity.UNCOMMON).component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true));
    public static final DeferredItem<Item> CORRUPTED_STAR = ITEMS.registerSimpleItem("corrupted_star",
            p -> p.rarity(Rarity.UNCOMMON).component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true));
    public static final DeferredItem<Item> DEAD_ENDERMITE = ITEMS.registerSimpleItem("endermite_cadaver", p -> p.stacksTo(1));
    public static final DeferredItem<SoapBarItem> SOAP_BAR = ITEMS.registerItem("soap_bar", SoapBarItem::new, p -> p.durability(16));

    // SYRINGES
    public static final DeferredItem<SyringeItem> SYRINGE = ITEMS.registerItem("syringe", SyringeItem::new, p -> p.stacksTo(1));
    public static final DeferredItem<PotionSyringeItem> POTION_SYRINGE = ITEMS.registerItem("potion_syringe", PotionSyringeItem::new,
            p -> p.craftRemainder(SYRINGE.get()).stacksTo(1));
    public static final DeferredItem<Item> SYRINGE_WARP_NUCLEUS = ITEMS.registerSimpleItem("syringe_warp_nucleus",
            p -> p.craftRemainder(SYRINGE.get()).stacksTo(1));
    public static final DeferredItem<DnaItem> SYRINGE_DNA = ITEMS.registerItem("syringe_with_dna", DnaItem::new,
            p -> p.craftRemainder(SYRINGE.get()).stacksTo(1));

    // INCUBATOR ITEMS
    public static final DeferredItem<DnaItem> MUTATED_EGG = ITEMS.registerItem("mutated_egg", DnaItem::new, p -> p.stacksTo(16));
    public static final DeferredItem<Item> LIQUID_NUTRIENTS = ITEMS.registerSimpleItem("liquid_nutrients", p -> p.stacksTo(16));

    // FAN UPGRADES, their stack size is the maximum upgrade level
    public static final DeferredItem<Item> UPGRADE_WIDTH = ITEMS.registerSimpleItem("upgrade_width", p -> p.stacksTo(2));
    public static final DeferredItem<Item> UPGRADE_HEIGHT = ITEMS.registerSimpleItem("upgrade_height", p -> p.stacksTo(2));
    public static final DeferredItem<Item> UPGRADE_LENGTH = ITEMS.registerSimpleItem("upgrade_length", p -> p.stacksTo(4));
    public static final DeferredItem<Item> UPGRADE_POWER = ITEMS.registerSimpleItem("upgrade_power", p -> p.stacksTo(3));

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> BMF_TAB = CREATIVE_MODE_TABS.register("bettermobfarms", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.bettermobfarms"))
            .icon(() -> IRON_SPIKES.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(WITHERPROOF_BLOCK.get());
                output.accept(IRON_SPIKES.get());
                output.accept(DIAMOND_SPIKES.get());
                output.accept(NETHERITE_SPIKES.get());
                output.accept(HOT_NETHERITE_SPIKES.get());
                output.accept(ICE_SPIKES.get());
                output.accept(BAMBOO_SPIKES.get());
                output.accept(BEAR_TRAP.get());
                output.accept(FAN.get());
                output.accept(INCUBATOR.get());
                output.accept(ICE_SHARD.get());
                output.accept(NETHER_STAR_SHARD.get());
                output.accept(CORRUPTED_STAR.get());
                output.accept(SYRINGE.get());
                output.accept(SYRINGE_WARP_NUCLEUS.get());
                output.accept(DEAD_ENDERMITE.get());
                output.accept(LIQUID_NUTRIENTS.get());
                output.accept(SOAP_BAR.get());
                output.accept(UPGRADE_LENGTH.get());
                output.accept(UPGRADE_WIDTH.get());
                output.accept(UPGRADE_HEIGHT.get());
                output.accept(UPGRADE_POWER.get());
            }).build());

    // A DNA syringe and a mutated egg for every mob that has a spawn egg and may be sampled, mobs of other mods included
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> DNA_TAB = CREATIVE_MODE_TABS.register("dna_types", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.bettermobfarms.dna_types"))
            .withTabsBefore(BMF_TAB.getKey())
            .icon(() -> dnaSyringe(EntityType.ZOMBIE))
            .displayItems((parameters, output) -> {
                List<EntityType<?>> types = BuiltInRegistries.ENTITY_TYPE.stream()
                        .filter(type -> type.isEnabled(parameters.enabledFeatures())
                                && SpawnEggItem.byId(type).isPresent()
                                && !BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(type).is(BMFTags.Entities.SYRINGE_DNA_BLACKLIST))
                        .sorted(Comparator.comparing(type -> BuiltInRegistries.ENTITY_TYPE.getKey(type).toString()))
                        .toList();
                types.forEach(type -> output.accept(dnaSyringe(type)));
                types.forEach(type -> output.accept(mutatedEgg(type)));
            })
            .build());

    public static ItemStack dnaSyringe(EntityType<?> type) {
        return withDna(SYRINGE_DNA.get(), type);
    }

    public static ItemStack mutatedEgg(EntityType<?> type) {
        return withDna(MUTATED_EGG.get(), type);
    }

    public static ItemStack mutatedEgg(EntityType<?> type, IncubatorEgg origin) {
        ItemStack stack = mutatedEgg(type);
        stack.set(BMFDataComponents.ORIGIN_EGG.get(), origin);
        return stack;
    }

    private static ItemStack withDna(Item item, EntityType<?> type) {
        ItemStack stack = new ItemStack(item);
        stack.set(BMFDataComponents.DNA.get(), type);
        return stack;
    }

    // A filled potion syringe for every potion with effects, listed after the vanilla potions in Food & Drinks
    public static void addToVanillaTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() != CreativeModeTabs.FOOD_AND_DRINKS) {
            return;
        }
        CreativeModeTab.ItemDisplayParameters parameters = event.getParameters();
        parameters.holders().lookup(Registries.POTION).ifPresent(potions -> potions.listElements()
                .filter(potion -> potion.value().isEnabled(parameters.enabledFeatures()) && !potion.value().getEffects().isEmpty())
                .forEach(potion -> event.accept(PotionContents.createItemStack(POTION_SYRINGE.get(), potion),
                        CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS)));
    }
}
