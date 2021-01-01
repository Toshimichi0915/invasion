package net.toshimichi.invasion.commands;

import net.toshimichi.invasion.Holder;
import net.toshimichi.invasion.State;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class StopCommand implements CommandExecutor {

    private final Holder<State> holder;

    public StopCommand(Holder<State> holder) {
        this.holder = holder;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        holder.get().disable();
        return true;
    }
}
