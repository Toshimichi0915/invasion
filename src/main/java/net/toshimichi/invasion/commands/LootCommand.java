package net.toshimichi.invasion.commands;

import net.toshimichi.invasion.Loot;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public class LootCommand implements CommandExecutor {

    private final Consumer<Loot> consumer;

    public LootCommand(Consumer<Loot> consumer) {
        this.consumer = consumer;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "出現比を指定してください");
            return true;
        }
        ItemStack itemStack = ((Player)sender).getInventory().getItemInMainHand();
        int ratio;
        try {
            ratio = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "出現比は整数で指定してください");
            return true;
        }
        consumer.accept(new Loot(itemStack, ratio));
        sender.sendMessage("アイテムを追加しました");
        return true;
    }
}
