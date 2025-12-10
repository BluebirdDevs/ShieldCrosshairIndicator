package bluebird.shieldindicator.client;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@Config(name = "shieldindicator")
public class ModConfig implements ConfigData {
    @ConfigEntry.Gui.Excluded
    public static ModConfig INSTANCE;

    public static void init()
    {
        AutoConfig.register(ModConfig.class, JanksonConfigSerializer::new);
        INSTANCE = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
    }

    public enum IndicatorMode {
        OFF,
        PLAYERS,
        ENTITIES
    }

    public enum TextureMode {
        LAYERED,
        OVERRIDE
    }

    // Enum selector as a button
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    @ConfigEntry.Gui.Tooltip
    public IndicatorMode indicatorMode = IndicatorMode.PLAYERS;

    @ConfigEntry.Gui.Tooltip()
    @Comment("Enabled Player Shield Indicator")
    public boolean shieldIndicator = true;

    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    @ConfigEntry.Gui.Tooltip
    public TextureMode textureMode = TextureMode.LAYERED;
}
