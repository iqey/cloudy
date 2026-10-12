package com.mostlycloudy.cloudy.modules;

import com.mostlycloudy.cloudy.Cloudy;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.Keybind;

/**
 * Two keys: one drops render distance to a low value, the other puts it back.
 * Keep the module enabled for the keys to work. Both keys and both distances
 * are set in the module's settings.
 *
 * Note: the server's own view distance still caps what you actually receive.
 */
public class RenderToggle extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgKeys = settings.createGroup("Keys");

    private final Setting<Integer> lowDistance = sgGeneral.add(new IntSetting.Builder()
        .name("low-distance")
        .description("Render distance (chunks) the low key sets.")
        .defaultValue(2)
        .range(2, 32)
        .sliderRange(2, 32)
        .build()
    );

    private final Setting<Boolean> restorePrevious = sgGeneral.add(new BoolSetting.Builder()
        .name("restore-previous")
        .description("The high key restores whatever you had before going low. Turn off to always use high-distance.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Integer> highDistance = sgGeneral.add(new IntSetting.Builder()
        .name("high-distance")
        .description("Render distance (chunks) the high key sets.")
        .defaultValue(32)
        .range(2, 32)
        .sliderRange(2, 32)
        .visible(() -> !restorePrevious.get())
        .build()
    );

    private final Setting<Boolean> feedback = sgGeneral.add(new BoolSetting.Builder()
        .name("chat-feedback")
        .description("Print the new render distance in chat.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Keybind> lowKey = sgKeys.add(new KeybindSetting.Builder()
        .name("low-key")
        .description("Drops render distance to low-distance.")
        .defaultValue(Keybind.none())
        .action(this::goLow)
        .build()
    );

    private final Setting<Keybind> highKey = sgKeys.add(new KeybindSetting.Builder()
        .name("high-key")
        .description("Puts render distance back up.")
        .defaultValue(Keybind.none())
        .action(this::goHigh)
        .build()
    );

    private final Setting<Keybind> swapKey = sgKeys.add(new KeybindSetting.Builder()
        .name("swap-key")
        .description("Optional single key that flips between low and high.")
        .defaultValue(Keybind.none())
        .action(this::swap)
        .build()
    );

    private int previous = -1;
    private boolean isLow;

    public RenderToggle() {
        super(Cloudy.CATEGORY, "render-toggle", "Keys to drop render distance to 2 and put it back to 32.");
    }

    private void goLow() {
        int current = mc.options.renderDistance().get();
        if (!isLow) previous = current;
        set(lowDistance.get());
        isLow = true;
    }

    private void goHigh() {
        int target = restorePrevious.get() && previous > 0 ? previous : highDistance.get();
        set(target);
        isLow = false;
    }

    private void swap() {
        if (isLow) goHigh();
        else goLow();
    }

    private void set(int chunks) {
        mc.options.renderDistance().set(chunks);
        if (feedback.get()) info("Render distance: %d chunks.", chunks);
    }
}
