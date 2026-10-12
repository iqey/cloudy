package com.mostlycloudy.cloudy.utils;

import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.MeteorToast;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;

import static meteordevelopment.meteorclient.MeteorClient.mc;

/**
 * Shared notification helper so every Cloudy module notifies the same way.
 */
public final class Notify {
    public enum Mode {
        Chat,
        Toast,
        Both
    }

    private Notify() {}

    /**
     * @param module  module sending the alert (used for the chat prefix)
     * @param mode    chat, toast or both
     * @param sound   play a chime
     * @param icon    toast icon
     * @param title   toast title
     * @param message body text (also used for chat)
     */
    public static void send(Module module, Mode mode, boolean sound, Item icon, String title, String message) {
        if (mode == Mode.Chat || mode == Mode.Both) {
            module.info(message);
        }

        if (mode == Mode.Toast || mode == Mode.Both) {
            MeteorToast toast = new MeteorToast.Builder(title)
                .icon(icon)
                .text(message)
                .sound(sound ? SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_CHIME.value(), 1.2f, 1f) : null)
                .build();
                   mc.getToastManager().addToast(toast);
        } else if (sound) {
            // Chat-only mode still gets a sound if asked for.
            mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_CHIME.value(), 1.2f, 1f));
        }
    }
}
