package com.mostlycloudy.cloudy.modules;

import com.mostlycloudy.cloudy.Cloudy;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;

import java.util.ArrayList;
import java.util.List;

/**
 * Turn it on: every active module is switched off and remembered.
 * Turn it off: exactly those modules come back on.
 * Bind it to a key in the click GUI like any other module.
 */
public class KillSwitch extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<List<Module>> exclude = sgGeneral.add(new ModuleListSetting.Builder()
        .name("never-disable")
        .description("Modules the kill switch leaves running.")
        .build()
    );

    private final Setting<Boolean> hideHud = sgGeneral.add(new BoolSetting.Builder()
        .name("hide-hud")
        .description("Also hides the Meteor HUD while the kill switch is on.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> feedback = sgGeneral.add(new BoolSetting.Builder()
        .name("chat-feedback")
        .description("Print how many modules were switched off / restored.")
        .defaultValue(false)
        .build()
    );

    // Hidden: the saved set is stored as a setting so it survives a game restart.
    private final Setting<List<Module>> saved = sgGeneral.add(new ModuleListSetting.Builder()
        .name("saved-modules")
        .description("Internal - modules that will be restored.")
        .visible(() -> false)
        .build()
    );

    private final Setting<Boolean> hudWasOn = sgGeneral.add(new BoolSetting.Builder()
        .name("hud-was-on")
        .description("Internal.")
        .defaultValue(true)
        .visible(() -> false)
        .build()
    );

    public KillSwitch() {
        super(Cloudy.CATEGORY, "kill-switch", "Turns every module off, then turns the same ones back on.");
        runInMainMenu = true;
    }

    @Override
    public void onActivate() {
        // If the game was closed while the kill switch was on, the saved list
        // was loaded from config before this runs - keep it instead of wiping it.
        boolean fresh = saved.get().isEmpty();
        List<Module> turnedOff = new ArrayList<>(saved.get());

        for (Module module : new ArrayList<>(Modules.get().getActive())) {
            if (module == this) continue;
            if (module instanceof CloudyTheme) continue;
            if (exclude.get().contains(module)) continue;

            turnedOff.add(module);
            module.toggle();
        }

        saved.set(turnedOff);

        if (hideHud.get()) {
            if (fresh) hudWasOn.set(Hud.get().active);
            Hud.get().active = false;
        }

        if (feedback.get()) info("Switched off %d modules.", turnedOff.size());
    }

    @Override
    public void onDeactivate() {
        int restored = 0;

        for (Module module : new ArrayList<>(saved.get())) {
            if (module != null && !module.isActive()) {
                module.toggle();
                restored++;
            }
        }

        saved.set(new ArrayList<>());

        if (hideHud.get()) Hud.get().active = hudWasOn.get();

        if (feedback.get()) info("Restored %d modules.", restored);
    }
}
