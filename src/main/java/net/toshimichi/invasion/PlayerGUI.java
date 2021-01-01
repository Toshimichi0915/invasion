package net.toshimichi.invasion;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public class PlayerGUI implements State, Listener {

    private final Plugin plugin;
    private final HashMap<Player, PlayerGUIData> opened = new HashMap<>();

    public PlayerGUI(Plugin plugin) {
        this.plugin = plugin;
    }

    public void openGUI(Player player, List<Player> list, Consumer<Player> consumer) {
        Inventory inventory = Bukkit.createInventory(player, 54, "蘇生するプレイヤーを選択してください");
        for (Player option : list) {
            ItemStack itemStack = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
            SkullMeta meta = (SkullMeta) itemStack.getItemMeta();
            meta.setDisplayName(ChatColor.RESET + option.getDisplayName());
            meta.setOwningPlayer(option);
            itemStack.setItemMeta(meta);
            inventory.addItem(itemStack);
        }
        player.openInventory(inventory);
        opened.put(player, new PlayerGUIData(player, list, consumer));
    }

    @Override
    public void enable() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void disable() {
        HandlerList.unregisterAll(this);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        PlayerGUIData data = opened.get(e.getWhoClicked());
        if (data == null) return;
        Consumer<Player> consumer = data.consumer;
        if (e.getSlot() < 0 || e.getSlot() >= data.list.size()) return;
        e.setCancelled(true);
        e.getWhoClicked().closeInventory();
        opened.remove(e.getWhoClicked());
        Player selected = data.list.get(e.getSlot());
        consumer.accept(selected);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        PlayerGUIData data = opened.get(e.getPlayer());
        if (data == null) return;
        data.consumer.accept(null);
        opened.remove(e.getPlayer());
    }

    private static class PlayerGUIData {
        public Player player;
        public List<Player> list;
        public Consumer<Player> consumer;

        public PlayerGUIData(Player player, List<Player> list, Consumer<Player> consumer) {
            this.player = player;
            this.list = list;
            this.consumer = consumer;
        }
    }
}
