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

import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class Loot {
    private ItemStack itemStack;
    private int ratio;

    public Loot() {
    }

    public Loot(ItemStack itemStack, int ratio) {
        this.itemStack = itemStack;
        this.ratio = ratio;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public int getRatio() {
        return ratio;
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public void setRatio(int ratio) {
        this.ratio = ratio;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("itemStack", itemStack);
        result.put("ratio", ratio);
        return result;
    }

    public void fromMap(Map<String, Object> map) {
        itemStack = (ItemStack) map.get("itemStack");
        ratio = (int) map.get("ratio");
    }
}
