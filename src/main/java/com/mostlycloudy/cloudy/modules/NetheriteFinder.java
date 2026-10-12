package com.mostlycloudy.cloudy.modules;

import com.mostlycloudy.cloudy.Cloudy;
import com.mostlycloudy.cloudy.utils.Notify;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.BlockUpdateEvent;
import meteordevelopment.meteorclient.events.world.ChunkDataEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;

import java.util.HashSet;
import java.util.Set;

/**
 * Highlights ancient debris in loaded chunks and tells you where it is.
 * Only finds what the server actually sends - with anti-xray on, buried
 * debris is disguised until a face is exposed.
 */
public class NetheriteFinder extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgNotify = settings.createGroup("Notifications");
    private final SettingGroup sgRender = settings.createGroup("Render");

    // General

    private final Setting<Boolean> netherOnly = sgGeneral.add(new BoolSetting.Builder()
        .name("nether-only")
        .description("Only scan in the Nether.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> minY = sgGeneral.add(new IntSetting.Builder()
        .name("min-y")
        .description("Lowest Y to scan.")
        .defaultValue(8)
        .range(-64, 320)
        .sliderRange(0, 128)
        .build()
    );

    private final Setting<Integer> maxY = sgGeneral.add(new IntSetting.Builder()
        .name("max-y")
        .description("Highest Y to scan.")
        .defaultValue(119)
        .range(-64, 320)
        .sliderRange(0, 128)
        .build()
    );

    // Notifications

    private final Setting<Boolean> notify = sgNotify.add(new BoolSetting.Builder()
        .name("notify")
        .description("Alert with coordinates when debris is found.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Notify.Mode> notifyMode = sgNotify.add(new EnumSetting.Builder<Notify.Mode>()
        .name("notify-mode")
        .description("How you get told.")
        .defaultValue(Notify.Mode.Chat)
        .visible(notify::get)
        .build()
    );

    private final Setting<Boolean> sound = sgNotify.add(new BoolSetting.Builder()
        .name("sound")
        .description("Play a chime with the alert.")
        .defaultValue(true)
        .visible(notify::get)
        .build()
    );

    private final Setting<Integer> groupRadius = sgNotify.add(new IntSetting.Builder()
        .name("group-radius")
        .description("Debris within this many blocks of an already-alerted one won't alert again (stops spam from clusters).")
        .defaultValue(4)
        .range(0, 32)
        .sliderRange(0, 16)
        .visible(notify::get)
        .build()
    );

    // Render

    private final Setting<Integer> renderRange = sgRender.add(new IntSetting.Builder()
        .name("render-range")
        .description("Only draw debris within this many blocks.")
        .defaultValue(128)
        .range(8, 512)
        .sliderRange(16, 256)
        .build()
    );

    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("shape-mode")
        .description("How the boxes are drawn.")
        .defaultValue(ShapeMode.Both)
        .build()
    );

    private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder()
        .name("side-color")
        .description("Box fill colour.")
        .defaultValue(new SettingColor(79, 168, 232, 40))
        .build()
    );

    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
        .name("line-color")
        .description("Box outline colour.")
        .defaultValue(new SettingColor(191, 227, 255, 255))
        .build()
    );

    private final Setting<Boolean> tracers = sgRender.add(new BoolSetting.Builder()
        .name("tracers")
        .description("Draw a line from your crosshair to each debris.")
        .defaultValue(false)
        .build()
    );

    private final Setting<SettingColor> tracerColor = sgRender.add(new ColorSetting.Builder()
        .name("tracer-color")
        .description("Tracer colour.")
        .defaultValue(new SettingColor(79, 168, 232, 200))
        .visible(tracers::get)
        .build()
    );

    private final Set<BlockPos> found = new HashSet<>();
    private final Set<BlockPos> alerted = new HashSet<>();

    public NetheriteFinder() {
        super(Cloudy.CATEGORY, "netherite-finder", "Highlights ancient debris and alerts you with coordinates.");
    }

    @Override
    public void onActivate() {
        clear();
        if (!Utils.canUpdate() || !shouldScan()) return;
        for (ChunkAccess chunk : Utils.chunks()) scanChunk(chunk);
    }

    @Override
    public void onDeactivate() {
        clear();
    }

    @EventHandler
    private void onGameLeft(GameLeftEvent event) {
        clear();
    }

    @EventHandler
    private void onChunkData(ChunkDataEvent event) {
        if (!shouldScan()) return;
        scanChunk(event.chunk());
    }

    @EventHandler
    private void onBlockUpdate(BlockUpdateEvent event) {
        if (!shouldScan()) return;
        boolean isDebris = event.newState.is(Blocks.ANCIENT_DEBRIS);
        BlockPos pos = event.pos.immutable();

        if (isDebris) add(pos);
        else found.remove(pos);
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (mc.player == null) return;
        double rangeSq = (double) renderRange.get() * renderRange.get();

        for (BlockPos pos : found) {
            if (mc.player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > rangeSq) continue;

            event.renderer.box(pos, sideColor.get(), lineColor.get(), shapeMode.get(), 0);

            if (tracers.get()) {
                event.renderer.line(
                    RenderUtils.center.x, RenderUtils.center.y, RenderUtils.center.z,
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    tracerColor.get()
                );
            }
        }
    }

    private boolean shouldScan() {
        if (mc.level == null) return false;
        return !netherOnly.get() || mc.level.dimension() == Level.NETHER;
    }

    private void scanChunk(ChunkAccess chunk) {
        int startX = chunk.getPos().getMinBlockX();
        int startZ = chunk.getPos().getMinBlockZ();
        int bottom = Math.max(minY.get(), mc.level.getMinY());
        int top = Math.min(maxY.get(), mc.level.getMinY() + mc.level.getHeight() - 1);

        BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = bottom; y <= top; y++) {
                    mpos.set(startX + x, y, startZ + z);
                    if (chunk.getBlockState(mpos).is(Blocks.ANCIENT_DEBRIS)) add(mpos.immutable());
                }
            }
        }
    }

    private void add(BlockPos pos) {
        if (!found.add(pos)) return;
        if (!notify.get()) return;

        int r = groupRadius.get();
        for (BlockPos other : alerted) {
            if (Math.abs(other.getX() - pos.getX()) <= r
                && Math.abs(other.getY() - pos.getY()) <= r
                && Math.abs(other.getZ() - pos.getZ()) <= r) return;
        }

        alerted.add(pos);
        Notify.send(this, notifyMode.get(), sound.get(), Items.ANCIENT_DEBRIS, "Netherite Finder",
            String.format("Ancient debris at %d, %d, %d", pos.getX(), pos.getY(), pos.getZ()));
    }

    private void clear() {
        found.clear();
        alerted.clear();
    }
}
