package net.toshimichi.invasion;

import net.toshimichi.invasion.commands.ReviveCommand;
import net.toshimichi.invasion.commands.StartCommand;
import net.toshimichi.invasion.commands.StopCommand;
import org.bukkit.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

public class GamePlugin extends JavaPlugin {

    private final Holder<State> gameHolder = new Holder<>();

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
        getCommand("istart").setExecutor(new StartCommand(this, gameHolder, spawnLoc, getConfig().getString("tags"), reviveItem, playerGUI));
        getCommand("istop").setExecutor(new StopCommand(gameHolder));
        getCommand("irevive").setExecutor(new ReviveCommand(reviveItem));
    }

    public void onDisable() {
        if (gameHolder.get() != null)
            gameHolder.get().disable();
    }
}
