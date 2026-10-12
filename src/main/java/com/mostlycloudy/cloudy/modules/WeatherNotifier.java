package com.mostlycloudy.cloudy.modules;

import com.mostlycloudy.cloudy.Cloudy;
import com.mostlycloudy.cloudy.utils.Notify;
import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.item.Items;

/**
 * Tells you when rain or thunder starts and stops, so you know when Riptide
 * (any rain) or Channeling (thunder) will work.
 */
public class WeatherNotifier extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgAlerts = settings.createGroup("Alerts");

    private final Setting<Boolean> rainStart = sgAlerts.add(new BoolSetting.Builder()
        .name("rain-start")
        .description("Alert when it starts raining.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> rainStop = sgAlerts.add(new BoolSetting.Builder()
        .name("rain-stop")
        .description("Alert when the rain stops.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> thunderStart = sgAlerts.add(new BoolSetting.Builder()
        .name("thunder-start")
        .description("Alert when a thunderstorm starts.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> thunderStop = sgAlerts.add(new BoolSetting.Builder()
        .name("thunder-stop")
        .description("Alert when a thunderstorm ends.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> thunderOnly = sgGeneral.add(new BoolSetting.Builder()
        .name("thunder-only")
        .description("Only alert for thunder (ignore plain rain).")
        .defaultValue(false)
        .build()
    );

    private final Setting<Notify.Mode> mode = sgGeneral.add(new EnumSetting.Builder<Notify.Mode>()
        .name("notify-mode")
        .description("How you get told.")
        .defaultValue(Notify.Mode.Both)
        .build()
    );

    private final Setting<Boolean> sound = sgGeneral.add(new BoolSetting.Builder()
        .name("sound")
        .description("Play a chime with the alert.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> alertOnJoin = sgGeneral.add(new BoolSetting.Builder()
        .name("alert-on-join")
        .description("Tell you the current weather when you join a server.")
        .defaultValue(false)
        .build()
    );

    private boolean wasRaining;
    private boolean wasThundering;
    private boolean primed;

    public WeatherNotifier() {
        super(Cloudy.CATEGORY, "weather-notifier", "Tells you when rain and thunder start and stop.");
    }

    @Override
    public void onActivate() {
        primed = false;
    }

    @EventHandler
    private void onGameJoined(GameJoinedEvent event) {
        primed = false;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.level == null || mc.player == null) return;

        boolean raining = mc.level.isRaining();
        boolean thundering = mc.level.isThundering();

        // First tick after join/enable: record the state without spamming alerts.
        if (!primed) {
            wasRaining = raining;
            wasThundering = thundering;
            primed = true;

            if (alertOnJoin.get()) {
                String now = thundering ? "Thunderstorm" : raining ? "Raining" : "Clear";
                alert("Weather: " + now);
            }
            return;
        }

        if (!thunderOnly.get()) {
            if (raining && !wasRaining && rainStart.get()) alert("It started raining - Riptide is on.");
            if (!raining && wasRaining && rainStop.get()) alert("The rain stopped.");
        }

        if (thundering && !wasThundering && thunderStart.get()) alert("Thunderstorm started - Channeling works.");
        if (!thundering && wasThundering && thunderStop.get()) alert("The thunderstorm ended.");

        wasRaining = raining;
        wasThundering = thundering;
    }

    private void alert(String message) {
        Notify.send(this, mode.get(), sound.get(), Items.TRIDENT, "Weather", message);
    }
}
