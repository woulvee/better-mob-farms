package com.woulvee.bettermobfarms.menu;

import com.woulvee.bettermobfarms.BetterMobFarms;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class BMFMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, BetterMobFarms.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<FanMenu>> FAN = MENUS.register("fan",
            () -> new MenuType<>(FanMenu::new, FeatureFlags.VANILLA_SET));
    public static final DeferredHolder<MenuType<?>, MenuType<IncubatorMenu>> INCUBATOR = MENUS.register("incubator",
            () -> new MenuType<>(IncubatorMenu::new, FeatureFlags.VANILLA_SET));
}
