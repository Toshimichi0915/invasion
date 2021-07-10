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

import net.toshimichi.invasion.commands.LootCommand;
import net.toshimichi.invasion.commands.ReviveCommand;
import net.toshimichi.invasion.commands.StartCommand;
import net.toshimichi.invasion.commands.StopCommand;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;

public class GamePlugin extends JavaPlugin implements Listener {

    private final Holder<State> gameHolder = new Holder<>();
    private final ArrayList<Loot> loots = new ArrayList<>();
    private final Holder<Lottery<ItemStack>> lottery = new Holder<>();

    private void newLottery() {
        Lottery<ItemStack> lottery = new Lottery<>();
        for (Loot loot : loots) {
            lottery.add(loot.getRatio(), loot.getItemStack());
        }
        this.lottery.set(lottery);
    }

    @Override
    public void onEnable() {
        ItemStack reviveItem = new ItemStack(Material.MAGMA_CREAM);
        ItemMeta meta = reviveItem.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "ANOTHER LIFE");
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "死を回避する「王」の持ち物");
        lore.add(ChatColor.GRAY + "「市民」の蘇生にも使用可能");
        meta.setLore(lore);
        reviveItem.setItemMeta(meta);

        saveDefaultConfig();
        World world = Bukkit.getWorld(getConfig().getString("spawn.world"));
        int x = getConfig().getInt("spawn.x");
        int y = getConfig().getInt("spawn.y");
        int z = getConfig().getInt("spawn.z");
        Location spawnLoc = new Location(world, x, y, z);

        PlayerGUI playerGUI = new PlayerGUI(this);
        playerGUI.enable();
        ConfigurationSection section = getConfig().getConfigurationSection("lottery");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                Loot loot = new Loot();
                HashMap<String, Object> map = new HashMap<>();
                for (String key1 : section.getConfigurationSection(key).getKeys(false)) {
                    map.put(key1, section.getConfigurationSection(key).get(key1));
                }
                loot.fromMap(map);
                loots.add(loot);
            }
        }
        newLottery();
        Bukkit.getPluginManager().registerEvents(this, this);
        ArrayList<Border> borders = new ArrayList<>();
        for (String key : getConfig().getConfigurationSection("border").getKeys(false)) {
            Border border = new Border(Integer.parseInt(key),
                    Integer.parseInt(getConfig().getString("border." + key + ".peace")),
                    Double.parseDouble(getConfig().getString("border." + key + ".to")),
                    Integer.parseInt(getConfig().getString("border." + key + ".time")));
            borders.add(border);
        }
        getCommand("istart").setExecutor(new StartCommand(this, gameHolder, spawnLoc,
                getConfig().getString("tags"), reviveItem, playerGUI, lottery, getConfig().getInt("maxItems"),
                getConfig().getInt("borderRange"), borders));
        getCommand("istop").setExecutor(new StopCommand(gameHolder));
        getCommand("irevive").setExecutor(new ReviveCommand(reviveItem));
        getCommand("iloot").setExecutor(new LootCommand(l -> {
            loots.add(l);
            newLottery();
            ConfigurationSection savedSection = getConfig().createSection("lottery");
            for (int i = 0; i < loots.size(); i++) {
                savedSection.set(Integer.toString(i), loots.get(i).toMap());
            }
            saveConfig();
        }));
    }

    public void onDisable() {
        if (gameHolder.get() != null)
            gameHolder.get().disable();
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Bukkit.getScheduler().runTaskLater(this, () -> e.getEntity().spigot().respawn(), 1);
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        if (e.getPlayer().getGameMode() == GameMode.CREATIVE) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        if (e.getPlayer().getGameMode() == GameMode.CREATIVE) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        if (!((Player) e.getEntity()).getScoreboard().equals(Bukkit.getScoreboardManager().getMainScoreboard())) return;
        e.setCancelled(true);
    }
}
