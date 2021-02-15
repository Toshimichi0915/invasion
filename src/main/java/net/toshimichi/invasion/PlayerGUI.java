package net.toshimichi.invasion;

import org.apache.commons.lang.math.RandomUtils;
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
import org.bukkit.inventory.meta.ItemMeta;
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
        int counter = 0;
        for (Player option : list) {
            ItemStack placeholder = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
            SkullMeta meta = (SkullMeta) placeholder.getItemMeta();
            meta.setDisplayName(ChatColor.RESET + option.getDisplayName());
            placeholder.setItemMeta(meta);
            inventory.setItem(counter++, placeholder);
        }
        counter = 0;
        for (Player option : list) {
            int slot = counter;
            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
                ItemStack itemStack = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
                SkullMeta meta = (SkullMeta) itemStack.getItemMeta();
                meta.setDisplayName(ChatColor.RESET + option.getDisplayName());
                meta.setOwningPlayer(option);
                itemStack.setItemMeta(meta);
                Bukkit.getScheduler().runTask(plugin, () -> {
                    inventory.setItem(slot, itemStack);
                });
            }, 1);
            counter++;
        }
        ItemStack random = new ItemStack(Material.BOW, 1);
        ItemMeta meta = random.getItemMeta();
        meta.setDisplayName(ChatColor.RESET + "ランダムで蘇生させる");
        random.setItemMeta(meta);
        inventory.setItem(53, random);
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
        Player selected;
        if (e.getSlot() == 53 && !data.list.isEmpty()) {
            selected = data.list.get(RandomUtils.nextInt(data.list.size()));
        } else if (e.getSlot() >= 0 && e.getSlot() < data.list.size()) {
            selected = data.list.get(e.getSlot());
        } else {
            return;
        }
        e.setCancelled(true);
        e.getWhoClicked().closeInventory();
        opened.remove(e.getWhoClicked());
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
