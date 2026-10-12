package com.mostlycloudy.cloudy.modules;

import com.mostlycloudy.cloudy.Cloudy;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.AutoReconnect;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;

/**
 * Logs you out when things go wrong: low health, out of totems, a big hit,
 * or a non-friend getting too close. Every trigger can be switched off.
 */
public class AutoLogPlus extends Module {
    private final SettingGroup sgHealth = settings.getDefaultGroup();
    private final SettingGroup sgTotems = settings.createGroup("Totems");
    private final SettingGroup sgPlayers = settings.createGroup("Players");
    private final SettingGroup sgAfter = settings.createGroup("After Logging");

    // Health

    private final Setting<Boolean> healthCheck = sgHealth.add(new BoolSetting.Builder()
        .name("low-health")
        .description("Log out when your health is at or below the threshold.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Double> health = sgHealth.add(new DoubleSetting.Builder()
        .name("health")
        .description("Health threshold (20 = full, each heart is 2).")
        .defaultValue(6)
        .range(1, 19)
        .sliderRange(1, 19)
        .decimalPlaces(1)
        .visible(healthCheck::get)
        .build()
    );

    private final Setting<Boolean> countAbsorption = sgHealth.add(new BoolSetting.Builder()
        .name("count-absorption")
        .description("Include golden-apple absorption hearts in your health.")
        .defaultValue(true)
        .visible(healthCheck::get)
        .build()
    );

    private final Setting<Boolean> bigHit = sgHealth.add(new BoolSetting.Builder()
        .name("big-hit")
        .description("Log out if you lose a lot of health in one tick.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Double> bigHitAmount = sgHealth.add(new DoubleSetting.Builder()
        .name("big-hit-amount")
        .description("Health lost in one tick that counts as a big hit.")
        .defaultValue(8)
        .range(1, 20)
        .sliderRange(1, 20)
        .decimalPlaces(1)
        .visible(bigHit::get)
        .build()
    );

    // Totems

    private final Setting<Boolean> totemCheck = sgTotems.add(new BoolSetting.Builder()
        .name("low-totems")
        .description("Log out when you're down to a set number of totems.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Integer> totems = sgTotems.add(new IntSetting.Builder()
        .name("totems-left")
        .description("Log out when you have this many totems or fewer (inventory + offhand).")
        .defaultValue(0)
        .range(0, 36)
        .sliderRange(0, 10)
        .visible(totemCheck::get)
        .build()
    );

    // Players

    private final Setting<Boolean> playerCheck = sgPlayers.add(new BoolSetting.Builder()
        .name("nearby-player")
        .description("Log out when a non-friend gets within range.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Integer> playerRange = sgPlayers.add(new IntSetting.Builder()
        .name("range")
        .description("How close (blocks) a non-friend has to be.")
        .defaultValue(16)
        .range(1, 128)
        .sliderRange(1, 64)
        .visible(playerCheck::get)
        .build()
    );

    // After

    private final Setting<Boolean> toggleOff = sgAfter.add(new BoolSetting.Builder()
        .name("toggle-off")
        .description("Turn Auto Log+ off after it logs you out (stops instant re-logs).")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> stopReconnect = sgAfter.add(new BoolSetting.Builder()
        .name("stop-reconnect")
        .description("Turn off Meteor Auto Reconnect and Cloudy Auto Relog so you stay logged out.")
        .defaultValue(true)
        .build()
    );

    private float lastHealth = -1;

    public AutoLogPlus() {
        super(Cloudy.CATEGORY, "auto-log-plus", "Logs out on low health, low totems, a big hit or a nearby player.");
    }

    @Override
    public void onActivate() {
        lastHealth = -1;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null || mc.level == null) return;
        if (mc.player.isDeadOrDying()) return;

        float hp = mc.player.getHealth();
        float effective = hp + (countAbsorption.get() ? mc.player.getAbsorptionAmount() : 0);

        if (healthCheck.get() && effective <= health.get()) {
            logout(String.format("Health %.1f (limit %.1f)", effective, health.get()));
            return;
        }

        if (bigHit.get() && lastHealth > 0 && lastHealth - hp >= bigHitAmount.get()) {
            logout(String.format("Took %.1f damage in one hit", lastHealth - hp));
            return;
        }
        lastHealth = hp;

        if (totemCheck.get()) {
            int count = InvUtils.find(Items.TOTEM_OF_UNDYING).count();
            if (count <= totems.get()) {
                logout("Totems left: " + count);
                return;
            }
        }

        if (playerCheck.get()) {
            double rangeSq = (double) playerRange.get() * playerRange.get();
            for (Entity entity : mc.level.entitiesForRendering()) {
                if (!(entity instanceof Player player) || player == mc.player) continue;
                if (Friends.get().isFriend(player)) continue;
                if (player.distanceToSqr(mc.player) <= rangeSq) {
                    logout("Player nearby: " + player.getName().getString());
                    return;
                }
            }
        }
    }

    private void logout(String reason) {
        if (stopReconnect.get()) {
            AutoReconnect autoReconnect = Modules.get().get(AutoReconnect.class);
            if (autoReconnect.isActive()) autoReconnect.toggle();

            AutoRelog autoRelog = Modules.get().get(AutoRelog.class);
            if (autoRelog != null && autoRelog.isActive()) autoRelog.toggle();
        }

        if (toggleOff.get()) toggle();

        mc.player.connection.handleDisconnect(new ClientboundDisconnectPacket(
            Component.literal("[Cloudy] Auto Log+ - " + reason)
        ));
    }
}
