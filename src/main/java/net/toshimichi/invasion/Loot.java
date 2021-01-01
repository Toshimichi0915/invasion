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
