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

package net.toshimichi.invasion;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ItemStackUtils {
    public static int countItemStack(Inventory inventory, ItemStack itemStack) {
        if (itemStack == null) {
            return 0;
        }

        int count = 0;
        for (ItemStack content : inventory.getContents()) {
            if (itemStack.isSimilar(content)) {
                count += itemStack.getAmount();
            }
        }
        return count;
    }
}
