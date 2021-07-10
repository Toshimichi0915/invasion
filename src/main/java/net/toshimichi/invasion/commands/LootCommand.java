/*
 * Copyright (C) 2021 Toshimichi0915
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
