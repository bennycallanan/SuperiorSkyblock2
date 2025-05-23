package com.bgsoftware.superiorskyblock.core.menu.impl;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.menu.Menu;
import com.bgsoftware.superiorskyblock.api.menu.layout.MenuLayout;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.api.menu.view.ViewArgs;
import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.io.MenuParserImpl;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.menu.AbstractMenu;
import com.bgsoftware.superiorskyblock.core.menu.MenuIdentifiers;
import com.bgsoftware.superiorskyblock.core.menu.MenuParseResult;
import com.bgsoftware.superiorskyblock.core.menu.MenuPatternSlots;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.IslandCreationButton;
import com.bgsoftware.superiorskyblock.core.menu.converter.MenuConverter;
import com.bgsoftware.superiorskyblock.core.menu.layout.AbstractMenuLayout;
import com.bgsoftware.superiorskyblock.core.menu.view.AbstractMenuView;
import com.bgsoftware.superiorskyblock.core.serialization.Serializers;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Locale;

public class MenuIslandCreation extends AbstractMenu<MenuIslandCreation.View, MenuIslandCreation.Args> {

    private MenuIslandCreation(MenuParseResult<View> parseResult) {
        super(MenuIdentifiers.MENU_ISLAND_CREATION, parseResult);
    }

    @Override
    protected View createViewInternal(SuperiorPlayer superiorPlayer, Args args,
                                      @Nullable MenuView<?, ?> previousMenuView) {
        return new View(superiorPlayer, previousMenuView, this, args);
    }

    @Nullable
    public static MenuIslandCreation createInstance() {
        MenuParseResult<View> menuParseResult = MenuParserImpl.getInstance().loadMenu("island-creation.yml",
                MenuIslandCreation::convertOldGUI);

        if (menuParseResult == null) {
            return null;
        }

        MenuPatternSlots menuPatternSlots = menuParseResult.getPatternSlots();
        YamlConfiguration cfg = menuParseResult.getConfig();
        MenuLayout.Builder<View> patternBuilder = menuParseResult.getLayoutBuilder();

        if (cfg.isConfigurationSection("items")) {
            for (String itemSectionName : cfg.getConfigurationSection("items").getKeys(false)) {
                ConfigurationSection itemSection = cfg.getConfigurationSection("items." + itemSectionName);

                if (!itemSection.isString("schematic"))
                    continue;

                Schematic schematic = plugin.getSchematics().getSchematic(itemSection.getString("schematic"));

                if (schematic == null) {
                    Log.warnFromFile("island-creation.yml", "Invalid schematic for item ", itemSectionName);
                    continue;
                }

                IslandCreationButton.Builder buttonBuilder = new IslandCreationButton.Builder(schematic);

                {
                    String biomeName = itemSection.getString("biome", "PLAINS");
                    try {
                        Biome biome = Biome.valueOf(biomeName.toUpperCase(Locale.ENGLISH));
                        buttonBuilder.setBiome(biome);
                    } catch (IllegalArgumentException error) {
                        Log.warnFromFile("island-creation.yml", "Invalid biome name for item ",
                                itemSectionName, ": ", biomeName);
                        continue;
                    }
                }

                {
                    Object bonusWorth = itemSection.get("bonus", itemSection.get("bonus-worth", 0D));
                    if (bonusWorth instanceof Double) {
                        buttonBuilder.setBonusWorth(BigDecimal.valueOf((double) bonusWorth));
                    } else if (bonusWorth instanceof String) {
                        buttonBuilder.setBonusWorth(new BigDecimal((String) bonusWorth));
                    }
                }

                {
                    Object bonusLevel = itemSection.get("bonus-level", 0D);
                    if (bonusLevel instanceof Double) {
                        buttonBuilder.setBonusLevel(BigDecimal.valueOf((double) bonusLevel));
                    } else if (bonusLevel instanceof String) {
                        buttonBuilder.setBonusLevel(new BigDecimal((String) bonusLevel));
                    }
                }

                ConfigurationSection soundSection = cfg.getConfigurationSection("sounds." + itemSectionName);
                if (soundSection != null) {
                    buttonBuilder.setAccessSound(MenuParserImpl.getInstance().getSound(soundSection.getConfigurationSection("access")));
                    buttonBuilder.setNoAccessSound(MenuParserImpl.getInstance().getSound(soundSection.getConfigurationSection("no-access")));
                }

                ConfigurationSection commandSection = cfg.getConfigurationSection("commands." + itemSectionName);
                if (commandSection != null) {
                    buttonBuilder.setAccessCommands(commandSection.getStringList("access"));
                    buttonBuilder.setNoAccessCommands(commandSection.getStringList("no-access"));
                }

                buttonBuilder.setOffset(itemSection.getBoolean("offset", false));

                if (itemSection.isString("spawn-offset"))
                    buttonBuilder.setSpawnOffset(Serializers.OFFSET_SPACED_SERIALIZER.deserialize(itemSection.getString("spawn-offset")));

                buttonBuilder.setAccessItem(MenuParserImpl.getInstance().getItemStack("menus/island-creation.yml",
                        itemSection.getConfigurationSection("access")));
                buttonBuilder.setNoAccessItem(MenuParserImpl.getInstance().getItemStack("menus/island-creation.yml",
                        itemSection.getConfigurationSection("no-access")));

                patternBuilder.mapButtons(menuPatternSlots.getSlots(itemSectionName), buttonBuilder);
            }
        }

        return new MenuIslandCreation(menuParseResult);
    }

    public static class Args implements ViewArgs {

        private final String islandName;

        public Args(String islandName) {
            this.islandName = islandName;
        }

    }

    public static class View extends AbstractMenuView<View, Args> {

        private final String islandName;

        View(SuperiorPlayer inventoryViewer, @Nullable MenuView<?, ?> previousMenuView,
             Menu<View, Args> menu, Args args) {
            super(inventoryViewer, previousMenuView, menu);
            this.islandName = args.islandName;
        }

        public String getIslandName() {
            return islandName;
        }

    }

    private static boolean convertOldGUI(SuperiorSkyblockPlugin plugin, YamlConfiguration newMenu) {
        File oldFile = new File(plugin.getDataFolder(), "guis/creation-gui.yml");

        if (!oldFile.exists())
            return false;

        //We want to reset the items of newMenu.
        ConfigurationSection itemsSection = newMenu.createSection("items");
        ConfigurationSection soundsSection = newMenu.createSection("sounds");
        ConfigurationSection commandsSection = newMenu.createSection("commands");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(oldFile);

        newMenu.set("title", cfg.getString("creation-gui.title"));

        int size = cfg.getInt("creation-gui.size");

        char[] patternChars = new char[size * 9];
        Arrays.fill(patternChars, '\n');

        int charCounter = 0;

        if (cfg.contains("creation-gui.fill-items")) {
            charCounter = MenuConverter.convertFillItems(cfg.getConfigurationSection("creation-gui.fill-items"),
                    charCounter, patternChars, itemsSection, commandsSection, soundsSection);
        }

        if (cfg.contains("creation-gui.schematics")) {
            for (String schemName : cfg.getConfigurationSection("creation-gui.schematics").getKeys(false)) {
                ConfigurationSection section = cfg.getConfigurationSection("creation-gui.schematics." + schemName);
                char itemChar = AbstractMenuLayout.BUTTON_SYMBOLS[charCounter++];
                section.set("schematic", schemName);
                MenuConverter.convertItemAccess(section, patternChars, itemChar, itemsSection, commandsSection, soundsSection);
            }
        }

        newMenu.set("pattern", MenuConverter.buildPattern(size, patternChars,
                AbstractMenuLayout.BUTTON_SYMBOLS[charCounter]));

        return true;
    }

}
