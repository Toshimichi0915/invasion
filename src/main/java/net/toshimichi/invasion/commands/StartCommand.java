package net.toshimichi.invasion.commands;

import net.toshimichi.invasion.*;
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
    private final Holder<ItemStack> reviveItem;
    private final PlayerGUI playerGUI;
    private final Holder<Lottery<ItemStack>> lottery;

    public StartCommand(Plugin plugin, Holder<State> holder, Location spawnLoc, String tags, Holder<ItemStack> reviveItem, PlayerGUI playerGUI, Holder<Lottery<ItemStack>> lottery) {
        this.plugin = plugin;
        this.holder = holder;
        this.spawnLoc = spawnLoc;
        this.tags = tags;
        this.reviveItem = reviveItem;
        this.playerGUI = playerGUI;
        this.lottery = lottery;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        GameState gameState = new GameState(plugin, spawnLoc, tags, reviveItem.get(), playerGUI, lottery.get());
        AwaitState awaitState = new AwaitState(plugin, holder, gameState, spawnLoc);
        holder.set(awaitState);
        awaitState.enable();
        return true;
    }
}
