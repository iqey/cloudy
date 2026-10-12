package com.mostlycloudy.cloudy;

import com.mojang.logging.LogUtils;
import com.mostlycloudy.cloudy.hud.CloudyWatermark;
import com.mostlycloudy.cloudy.modules.*;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.render.DisplayItemUtils;
import net.minecraft.world.item.Items;
import org.slf4j.Logger;

/**
 * Cloudy v1 - made by Mostly Cloudy.
 */
public class Cloudy extends MeteorAddon {
    public static final Logger LOG = LogUtils.getLogger();

    public static final String NAME = "Cloudy";
    public static final String VERSION = "v1";
    public static final String AUTHOR = "Mostly Cloudy";

    public static final Category CATEGORY = new Category(NAME, () -> DisplayItemUtils.toStack(Items.PHANTOM_MEMBRANE));
    public static final HudGroup HUD_GROUP = new HudGroup(NAME);

    @Override
    public void onInitialize() {
        LOG.info("{} {} - made by {}", NAME, VERSION, AUTHOR);

        Modules modules = Modules.get();
        modules.add(new WeatherNotifier());
        modules.add(new KillSwitch());
        modules.add(new RenderToggle());
        modules.add(new AutoRelog());
        modules.add(new AutoLogPlus());
        modules.add(new NetheriteFinder());
        modules.add(new CloudyTheme());

        Hud.get().register(CloudyWatermark.INFO);
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(CATEGORY);
    }

    @Override
    public String getPackage() {
        return "com.mostlycloudy.cloudy";
    }
}
