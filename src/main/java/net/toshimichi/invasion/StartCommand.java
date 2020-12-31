package net.toshimichi.invasion;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

public class StartCommand implements CommandExecutor {

    private final Plugin plugin;
    private final Holder<State> holder;
    private final Location spawnLoc;
    private final String tags;

    public StartCommand(Plugin plugin, Holder<State> holder, Location spawnLoc, String tags) {
        this.plugin = plugin;
        this.holder = holder;
        this.spawnLoc = spawnLoc;
        this.tags = tags;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        GameState state = new GameState(plugin, spawnLoc, tags);
        holder.set(state);
        state.enable();
        return true;
    }
}
