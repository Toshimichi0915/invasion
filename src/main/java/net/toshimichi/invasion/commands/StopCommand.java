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
