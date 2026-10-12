package com.mostlycloudy.cloudy.modules;

import com.mostlycloudy.cloudy.Cloudy;
import it.unimi.dsi.fastutil.Pair;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.AutoReconnect;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;

/**
 * Relogs once when you pass a Y level (default: below Y 0, i.e. into deepslate),
 * then joins the same server again after a delay.
 */
public class AutoRelog extends Module {
    public enum Trigger {
        Below,
        Above
    }

    public enum Repeat {
        OncePerToggle,
        EveryTime
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> yLevel = sgGeneral.add(new IntSetting.Builder()
        .name("y-level")
        .description("The Y level that triggers a relog.")
        .defaultValue(0)
        .range(-64, 320)
        .sliderRange(-64, 128)
        .build()
    );

    private final Setting<Trigger> trigger = sgGeneral.add(new EnumSetting.Builder<Trigger>()
        .name("trigger")
        .description("Relog when you go below or above the Y level.")
        .defaultValue(Trigger.Below)
        .build()
    );

    private final Setting<Double> delay = sgGeneral.add(new DoubleSetting.Builder()
        .name("rejoin-delay")
        .description("Seconds to wait before rejoining.")
        .defaultValue(3.0)
        .range(0.5, 60)
        .sliderRange(0.5, 15)
        .decimalPlaces(1)
        .build()
    );

    private final Setting<Repeat> repeat = sgGeneral.add(new EnumSetting.Builder<Repeat>()
        .name("repeat")
        .description("OncePerToggle: relog one time, then wait until you toggle the module again. EveryTime: re-arms once you're back past the Y level.")
        .defaultValue(Repeat.OncePerToggle)
        .build()
    );

    private final Setting<String> serverFilter = sgGeneral.add(new StringSetting.Builder()
        .name("server-filter")
        .description("Only works on servers whose address contains this text. Leave empty for any server.")
        .defaultValue("donutsmp")
        .build()
    );

    private final Setting<Boolean> feedback = sgGeneral.add(new BoolSetting.Builder()
        .name("chat-feedback")
        .description("Print a message when it rejoins.")
        .defaultValue(true)
        .build()
    );

    private boolean armed;
    private boolean waiting;
    private int ticksLeft;
    private Pair<ServerAddress, ServerData> target;

    public AutoRelog() {
        super(Cloudy.CATEGORY, "auto-relog", "Relogs when you pass a Y level and rejoins the same server.");
        runInMainMenu = true; // keep ticking on the disconnect screen so we can rejoin
    }

    @Override
    public void onActivate() {
        armed = true;
        waiting = false;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (Utils.canUpdate()) {
            if (waiting) {
                // We're back in a world - the rejoin worked.
                waiting = false;
                if (feedback.get()) info("Rejoined.");
            }
            tickInGame();
        } else if (waiting) {
            if (--ticksLeft <= 0) {
                waiting = false;
                if (target != null) {
                    ConnectScreen.startConnecting(new TitleScreen(), mc, target.left(), target.right(), false, null);
                }
            }
        }
    }

    private void tickInGame() {
        double y = mc.player.getY();
        boolean past = trigger.get() == Trigger.Below ? y < yLevel.get() : y > yLevel.get();

        if (!past) {
            if (repeat.get() == Repeat.EveryTime) armed = true;
            return;
        }

        if (!armed || !serverMatches()) return;

        AutoReconnect autoReconnect = Modules.get().get(AutoReconnect.class);
        target = autoReconnect.lastServerConnection;
        if (target == null) return;

        armed = false;

        // If Meteor's Auto Reconnect is on it will rejoin by itself; don't double-connect.
        if (!autoReconnect.isActive()) {
            waiting = true;
            ticksLeft = (int) Math.round(delay.get() * 20);
        }

        mc.player.connection.handleDisconnect(new ClientboundDisconnectPacket(
            Component.literal("[Cloudy] Auto Relog - rejoining in " + delay.get() + "s")
        ));
    }

    private boolean serverMatches() {
        String filter = serverFilter.get().trim().toLowerCase();
        if (filter.isEmpty()) return true;
        if (mc.getCurrentServer() == null) return false;
        return mc.getCurrentServer().ip.toLowerCase().contains(filter);
    }
}
