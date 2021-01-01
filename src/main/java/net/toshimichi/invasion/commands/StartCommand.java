package net.toshimichi.invasion.commands;

import net.toshimichi.invasion.GameState;
import net.toshimichi.invasion.Holder;
import net.toshimichi.invasion.PlayerGUI;
import net.toshimichi.invasion.State;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class StartCommand implements CommandExecutor {

    private final Plugin plugin;
    private final Holder<State> holder;
    private final Location spawnLoc;
    private final String tags;
    private final ItemStack reviveItem;
    private final PlayerGUI playerGUI;

    public StartCommand(Plugin plugin, Holder<State> holder, Location spawnLoc, String tags, ItemStack reviveItem, PlayerGUI playerGUI) {
        this.plugin = plugin;
        this.holder = holder;
        this.spawnLoc = spawnLoc;
        this.tags = tags;
        this.reviveItem = reviveItem;
        this.playerGUI = playerGUI;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        GameState state = new GameState(plugin, spawnLoc, tags, reviveItem, playerGUI);
        holder.set(state);
        state.enable();
        return true;
    }
}
