package net.toshimichi.invasion.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ReviveCommand implements CommandExecutor {

    private final ItemStack reviveItem;

    public ReviveCommand(ItemStack reviveItem) {
        this.reviveItem = reviveItem;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player) {
            ((Player)sender).getInventory().addItem(reviveItem);
        }
        return true;
    }
}
