package com.mostlycloudy.cloudy.hud;

import com.mostlycloudy.cloudy.Cloudy;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;

/**
 * "Cloudy v1 / made by Mostly Cloudy" watermark. Add it from the HUD editor
 * (Meteor > HUD > Add > Cloudy > watermark). Text, colours and size are all editable.
 */
public class CloudyWatermark extends HudElement {
    public static final HudElementInfo<CloudyWatermark> INFO = new HudElementInfo<>(
        Cloudy.HUD_GROUP, "watermark", "Cloudy v1 - made by Mostly Cloudy.", CloudyWatermark::new
    );

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgColors = settings.createGroup("Colours");

    private final Setting<String> title = sgGeneral.add(new StringSetting.Builder()
        .name("title")
        .description("Main line.")
        .defaultValue(Cloudy.NAME + " " + Cloudy.VERSION)
        .build()
    );

    private final Setting<Boolean> showCredit = sgGeneral.add(new BoolSetting.Builder()
        .name("show-credit")
        .description("Show the 'made by' line.")
        .defaultValue(true)
        .build()
    );

    private final Setting<String> credit = sgGeneral.add(new StringSetting.Builder()
        .name("credit")
        .description("Second line.")
        .defaultValue("made by " + Cloudy.AUTHOR)
        .visible(showCredit::get)
        .build()
    );

    private final Setting<Double> scale = sgGeneral.add(new DoubleSetting.Builder()
        .name("scale")
        .description("Size of the title.")
        .defaultValue(1.6)
        .range(0.5, 5)
        .sliderRange(0.5, 3)
        .build()
    );

    private final Setting<Boolean> shadow = sgGeneral.add(new BoolSetting.Builder()
        .name("shadow")
        .description("Text shadow.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> background = sgGeneral.add(new BoolSetting.Builder()
        .name("background")
        .description("Draw a cloudy gradient behind the text.")
        .defaultValue(true)
        .build()
    );

    private final Setting<SettingColor> titleColor = sgColors.add(new ColorSetting.Builder()
        .name("title-color")
        .description("Title colour.")
        .defaultValue(new SettingColor(255, 255, 255))
        .build()
    );

    private final Setting<SettingColor> creditColor = sgColors.add(new ColorSetting.Builder()
        .name("credit-color")
        .description("Credit line colour.")
        .defaultValue(new SettingColor(191, 227, 255))
        .build()
    );

    private final Setting<SettingColor> bgTop = sgColors.add(new ColorSetting.Builder()
        .name("background-left")
        .description("Gradient colour on the left.")
        .defaultValue(new SettingColor(79, 168, 232, 170))
        .visible(background::get)
        .build()
    );

    private final Setting<SettingColor> bgBottom = sgColors.add(new ColorSetting.Builder()
        .name("background-right")
        .description("Gradient colour on the right.")
        .defaultValue(new SettingColor(22, 49, 79, 120))
        .visible(background::get)
        .build()
    );

    private static final double PAD = 4;

    public CloudyWatermark() {
        super(INFO);
    }

    @Override
    public void render(HudRenderer renderer) {
        double s = scale.get();
        double creditScale = Math.max(0.5, s * 0.55);
        boolean sh = shadow.get();

        double titleW = renderer.textWidth(title.get(), sh, s);
        double titleH = renderer.textHeight(sh, s);

        double creditW = showCredit.get() ? renderer.textWidth(credit.get(), sh, creditScale) : 0;
        double creditH = showCredit.get() ? renderer.textHeight(sh, creditScale) : 0;

        double w = Math.max(titleW, creditW) + PAD * 2;
        double h = titleH + creditH + PAD * 2 + (showCredit.get() ? 1 : 0);

        setSize(w, h);

        if (background.get()) {
            // left -> right gradient (top-left, top-right, bottom-right, bottom-left)
            renderer.quad(x, y, w, h, bgTop.get(), bgBottom.get(), bgBottom.get(), bgTop.get());
        }

        renderer.text(title.get(), x + PAD, y + PAD, titleColor.get(), sh, s);

        if (showCredit.get()) {
            renderer.text(credit.get(), x + PAD, y + PAD + titleH + 1, creditColor.get(), sh, creditScale);
        }
    }
}
