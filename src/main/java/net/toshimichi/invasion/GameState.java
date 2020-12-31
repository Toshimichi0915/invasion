package net.toshimichi.invasion;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class GameState implements State, Listener, Runnable {

    private final Plugin plugin;
    private final Location spawnLoc;
    private final Random random = new Random();
    private final ArrayList<GameTeam> teams = new ArrayList<>();
    private final HashMap<Player, Integer> killCount = new HashMap<>();
    private final String tags;
    private int tagCounter = 0;
    private BukkitTask task;

    public GameState(Plugin plugin, Location spawnLoc, String tags) {
        this.plugin = plugin;
        this.tags = tags;
        this.spawnLoc = spawnLoc;
    }

    private GameTeam getTeam(Player player) {
        for (GameTeam team : teams) {
            if (team.getOwner().equals(player) || team.getCitizens().contains(player)) {
                return team;
            }
        }
        return null;
    }

    @Override
    public void enable() {
        System.out.println("ゲームが開始しました");
        Bukkit.getPluginManager().registerEvents(this, plugin);
        task = Bukkit.getScheduler().runTaskTimer(plugin, this, 0, 1);
        for (Player player : Bukkit.getOnlinePlayers()) {
            GameTeam team = new GameTeam(player, Character.toString(tags.charAt(tagCounter++)));
            teams.add(team);
            killCount.put(player, 0);
            player.teleport(spawnLoc);
        }
    }

    @Override
    public void disable() {
        System.out.println("ゲームが終了しました");
        HandlerList.unregisterAll(this);
        if (task != null) {
            task.cancel();
        }
    }

    @Override
    public void run() {
        // ゲーム終了
        if (teams.size() == 1) {
            disable();
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        e.getPlayer().setGameMode(GameMode.SPECTATOR);
        e.getPlayer().teleport(spawnLoc);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        e.getPlayer().damage(1000);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        e.getPlayer().setGameMode(GameMode.SPECTATOR);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        GameTeam victimTeam = getTeam(e.getEntity());
        GameTeam killerTeam = getTeam(e.getEntity().getKiller());
        killCount.put(e.getEntity(), 0);

        /*
        チームの所有者が死亡:
            自然死: 市民に所有者の座を譲渡
            他殺: 殺害者にチーム全体を譲渡
        チームの市民が死亡: 殺害者のチームに移動
         */
        if (victimTeam.getOwner().equals(e.getEntity())) { // 所有者が死んだ場合
            if (killerTeam == null) { // 自然死の場合
                Player bestKiller = null;
                int mostKills = 0;
                for (Player player : victimTeam.getCitizens()) {
                    int kills = killCount.get(player);
                    if (kills < mostKills) continue;
                    bestKiller = player;
                    mostKills = kills;
                }
                if (bestKiller == null) { // 譲渡できる市民がいないのでランダムなチームに所属させる
                    teams.remove(victimTeam);
                    teams.get(random.nextInt(teams.size())).addCitizen(e.getEntity());
                } else { // 市民に譲渡する
                    victimTeam.setOwner(bestKiller);
                    victimTeam.addCitizen(e.getEntity());
                }
            } else { //他殺の場合
                teams.remove(victimTeam);
                // 勝利者のチームに加える
                killerTeam.addCitizen(e.getEntity());
                for (Player player : victimTeam.getCitizens()) {
                    killerTeam.addCitizen(player);
                }
            }
        } else if (killerTeam != null) { // 市民が他殺された場合
            victimTeam.removeCitizen(e.getEntity());
            killerTeam.addCitizen(e.getEntity());
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        e.setCancelled(true);
    }
}
