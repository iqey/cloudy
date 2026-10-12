package com.mostlycloudy.cloudy.modules;

import com.mostlycloudy.cloudy.Cloudy;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.themes.meteor.MeteorGuiTheme;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;

/**
 * Re-skins Meteor's click GUI in Cloudy blue. Every colour is editable here,
 * changes show up live, and turning the module off puts Meteor's defaults back.
 */
public class CloudyTheme extends Module {
    private final SettingGroup sgMain = settings.getDefaultGroup();
    private final SettingGroup sgPanels = settings.createGroup("Panels");
    private final SettingGroup sgText = settings.createGroup("Text");
    private final SettingGroup sgSliders = settings.createGroup("Sliders");

    // Main

    private final Setting<SettingColor> accent = color(sgMain, "accent", "Main colour (replaces Meteor purple).", new SettingColor(79, 168, 232));
    private final Setting<SettingColor> checkbox = color(sgMain, "checkbox", "Ticked checkbox colour.", new SettingColor(191, 227, 255));
    private final Setting<SettingColor> separator = color(sgMain, "separator", "Divider line colour.", new SettingColor(191, 227, 255, 150));

    // Panels

    private final Setting<SettingColor> background = color(sgPanels, "panel-background", "Panel background.", new SettingColor(22, 49, 79, 200));
    private final Setting<SettingColor> moduleActive = color(sgPanels, "active-module", "Background of enabled modules.", new SettingColor(40, 85, 130));
    private final Setting<SettingColor> outline = color(sgPanels, "outline", "Panel outline.", new SettingColor(12, 28, 46));
    private final Setting<SettingColor> scrollbar = color(sgPanels, "scrollbar", "Scrollbar colour.", new SettingColor(40, 85, 130, 200));

    // Text

    private final Setting<SettingColor> text = color(sgText, "text", "Normal text.", new SettingColor(255, 255, 255));
    private final Setting<SettingColor> textSecondary = color(sgText, "secondary-text", "Descriptions and hints.", new SettingColor(170, 200, 225));
    private final Setting<SettingColor> titleText = color(sgText, "title-text", "Panel titles.", new SettingColor(255, 255, 255));

    // Sliders

    private final Setting<SettingColor> sliderLeft = color(sgSliders, "slider-filled", "Filled part of sliders.", new SettingColor(79, 168, 232));
    private final Setting<SettingColor> sliderRight = color(sgSliders, "slider-empty", "Empty part of sliders.", new SettingColor(22, 49, 79));
    private final Setting<SettingColor> sliderHandle = color(sgSliders, "slider-handle", "Slider knob.", new SettingColor(191, 227, 255));

    public CloudyTheme() {
        super(Cloudy.CATEGORY, "cloudy-theme", "Blue cloudy look for the Meteor menus. Every colour is customisable.");
        runInMainMenu = true;
    }

    @Override
    public void onActivate() {
        apply();
    }

    @Override
    public void onDeactivate() {
        MeteorGuiTheme theme = theme();
        if (theme == null) return;

        // Put back Meteor's own defaults for every colour we changed.
        for (SettingGroup group : theme.settings) {
            for (Setting<?> setting : group) {
                if (setting.get() instanceof SettingColor) setting.reset();
            }
        }
    }

    private void apply() {
        MeteorGuiTheme theme = theme();
        if (theme == null) {
            warning("Cloudy Theme only works with the Meteor GUI theme (Meteor > GUI > Theme).");
            return;
        }

        theme.accentColor.set(new SettingColor(accent.get()));
        theme.checkboxColor.set(new SettingColor(checkbox.get()));
        theme.moduleBackground.set(new SettingColor(moduleActive.get()));

        theme.separatorEdges.set(new SettingColor(separator.get()));
        theme.separatorCenter.set(new SettingColor(separator.get()));

        theme.textColor.set(new SettingColor(text.get()));
        theme.textSecondaryColor.set(new SettingColor(textSecondary.get()));
        theme.titleTextColor.set(new SettingColor(titleText.get()));

        theme.sliderLeft.set(new SettingColor(sliderLeft.get()));
        theme.sliderRight.set(new SettingColor(sliderRight.get()));

        threeState(theme.backgroundColor, background.get());
        threeState(theme.outlineColor, outline.get());
        threeState(theme.scrollbarColor, scrollbar.get());
        threeState(theme.sliderHandle, sliderHandle.get());
    }

    /** Sets normal / hovered / pressed, with hover and press a little lighter. */
    private void threeState(MeteorGuiTheme.ThreeStateColorSetting setting, SettingColor base) {
        setting.get(false, false, true).set(base);
        setting.get(false, true, true).set(lighten(base, 12));
        setting.get(true, false).set(lighten(base, 24));
    }

    private static SettingColor lighten(SettingColor c, int amount) {
        return new SettingColor(
            Math.min(255, c.r + amount),
            Math.min(255, c.g + amount),
            Math.min(255, c.b + amount),
            c.a
        );
    }

    private static MeteorGuiTheme theme() {
        GuiTheme theme = GuiThemes.get();
        return theme instanceof MeteorGuiTheme m ? m : null;
    }

    private Setting<SettingColor> color(SettingGroup group, String name, String description, SettingColor value) {
        return group.add(new ColorSetting.Builder()
            .name(name)
            .description(description)
            .defaultValue(value)
            .onChanged(c -> {
                if (isActive()) apply();
            })
            .build()
        );
    }
}
