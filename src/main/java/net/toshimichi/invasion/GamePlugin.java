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
        getCommand("istart").setExecutor(new StartCommand(this, gameHolder, spawnLoc,
                getConfig().getString("tags"), reviveItem, playerGUI, lottery, getConfig().getInt("maxItems"),
                getConfig().getInt("border"), getConfig().getDouble("borderSpeed")));
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
