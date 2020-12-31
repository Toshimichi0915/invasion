package net.toshimichi.invasion;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

public class GamePlugin extends JavaPlugin {

    private final Holder<State> gameHolder = new Holder<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        World world = Bukkit.getWorld(getConfig().getString("spawn.world"));
        int x = getConfig().getInt("spawn.x");
        int y = getConfig().getInt("spawn.y");
        int z = getConfig().getInt("spawn.z");
        Location spawnLoc = new Location(world, x, y, z);

        getCommand("istart").setExecutor(new StartCommand(this, gameHolder, spawnLoc, getConfig().getString("tags")));
        getCommand("istop").setExecutor(new StopCommand(gameHolder));
    }

    public void onDisable() {
        if(gameHolder.getObject() != null)
            gameHolder.getObject().disable();
    }
}
