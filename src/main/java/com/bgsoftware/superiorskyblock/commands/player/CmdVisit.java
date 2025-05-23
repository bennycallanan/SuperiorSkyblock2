package com.bgsoftware.superiorskyblock.commands.player;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.core.IslandWorlds;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.island.privilege.IslandPrivileges;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

public class CmdVisit implements ISuperiorCommand {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("visit");
    }

    @Override
    public String getPermission() {
        return "superior.island.visit";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "visit <" +
                Message.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_VISIT.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return 2;
    }

    @Override
    public int getMaxArgs() {
        return 2;
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return false;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        Island targetIsland = CommandArguments.getIsland(plugin, sender, args[1]).getIsland();

        if (targetIsland == null)
            return;

        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(sender);
        Dimension dimension = plugin.getSettings().getWorlds().getDefaultWorldDimension();

        IslandWorlds.accessIslandWorldAsync(targetIsland, dimension, islandWorldResult -> {
            islandWorldResult.ifLeft(world -> teleportPlayerInternal(targetIsland, superiorPlayer));
        });
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(sender);
        return args.length == 2 ? CommandTabCompletes.getOnlinePlayersWithIslands(plugin, args[1],
                plugin.getSettings().isTabCompleteHideVanished(),
                (onlinePlayer, onlineIsland) -> onlineIsland != null && (
                        (!plugin.getSettings().getVisitorsSign().isRequiredForVisit() ||
                                onlineIsland.getVisitorsLocation((Dimension) null /* unused */) != null) ||
                                superiorPlayer.hasBypassModeEnabled()) && (!onlineIsland.isLocked() ||
                        onlineIsland.hasPermission(superiorPlayer, IslandPrivileges.CLOSE_BYPASS))) : Collections.emptyList();
    }

    private static void teleportPlayerInternal(Island targetIsland, SuperiorPlayer superiorPlayer) {
        Location visitLocation = plugin.getSettings().getVisitorsSign().isRequiredForVisit() ?
                targetIsland.getVisitorsLocation((Dimension) null /* unused */) :
                targetIsland.getIslandHome(plugin.getSettings().getWorlds().getDefaultWorldDimension());

        if (visitLocation == null) {
            Message.INVALID_VISIT_LOCATION.send(superiorPlayer);

            if (!superiorPlayer.hasBypassModeEnabled())
                return;

            visitLocation = targetIsland.getIslandHome(plugin.getSettings().getWorlds().getDefaultWorldDimension());
            Message.INVALID_VISIT_LOCATION_BYPASS.send(superiorPlayer);
        }

        if (targetIsland.isLocked() && !targetIsland.hasPermission(superiorPlayer, IslandPrivileges.CLOSE_BYPASS)) {
            Message.NO_CLOSE_BYPASS.send(superiorPlayer);
            return;
        }

        if (plugin.getSettings().getVisitWarmup() > 0 && !superiorPlayer.hasBypassModeEnabled()) {
            Message.TELEPORT_WARMUP.send(superiorPlayer, Formatters.TIME_FORMATTER.format(
                    Duration.ofMillis(plugin.getSettings().getVisitWarmup()), superiorPlayer.getUserLocale()));

            Location finalVisitLocation = visitLocation;

            superiorPlayer.setTeleportTask(BukkitExecutor.sync(() ->
                            teleportPlayerNoWarmup(superiorPlayer, targetIsland, finalVisitLocation, true),
                    plugin.getSettings().getHomeWarmup() / 50));
        } else {
            teleportPlayerNoWarmup(superiorPlayer, targetIsland, visitLocation, false);
        }
    }

    private static void teleportPlayerNoWarmup(SuperiorPlayer superiorPlayer, Island island,
                                               Location visitLocation, boolean checkIslandLock) {
        superiorPlayer.setTeleportTask(null);

        if (checkIslandLock && island.isLocked() && !island.hasPermission(superiorPlayer, IslandPrivileges.CLOSE_BYPASS)) {
            Message.NO_CLOSE_BYPASS.send(superiorPlayer);
            return;
        }

        superiorPlayer.teleport(visitLocation);
    }

}
