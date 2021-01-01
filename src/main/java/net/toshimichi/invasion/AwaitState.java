package net.toshimichi.invasion;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public class AwaitState implements State, Listener, Runnable {

    private final Plugin plugin;
    private final Holder<State> holder;
    private final State nextState;
    private final Location spawnLoc;
    private BukkitTask task;
    private int counter = 200;

    public AwaitState(Plugin plugin, Holder<State> holder, State nextState, Location spawnLoc) {
        this.plugin = plugin;
        this.holder = holder;
        this.nextState = nextState;
        this.spawnLoc = spawnLoc;
    }

    @Override
    public void enable() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        task = Bukkit.getScheduler().runTaskTimer(plugin, this, 0, 1);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.teleport(spawnLoc);
        }
    }

    @Override
    public void disable() {
        HandlerList.unregisterAll(this);
        if (task != null)
            task.cancel();
    }

    @Override
    public void run() {
        counter--;
        if (counter == 0) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                Location loc = player.getLocation();
                loc.add(0, -3, 0);
                player.teleport(loc);
                player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
                player.setFoodLevel(20);
                player.getInventory().setChestplate(new ItemStack(Material.ELYTRA));
            }
            holder.set(nextState);
            nextState.enable();
            disable();
        }
        if (counter % 20 == 0) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.sendTitle("", Integer.toString(counter / 20), 0, 30, 0);
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_PLING, 1, 1);
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        e.setCancelled(true);
    }
}
