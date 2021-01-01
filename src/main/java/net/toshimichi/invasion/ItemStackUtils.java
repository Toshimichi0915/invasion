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
