package net.toshimichi.invasion;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class GameState implements State, Listener, Runnable {

    private final Plugin plugin;
    private final Location spawnLoc;
    private final Random random = new Random();
    private final ArrayList<GameTeam> teams = new ArrayList<>();
    private final HashMap<Player, Integer> killCount = new HashMap<>();
    private final String tags;
    private final ItemStack reviveItem;
    private final PlayerGUI playerGUI;
    private int tagCounter = 0;
    private BukkitTask task;

    public GameState(Plugin plugin, Location spawnLoc, String tags, ItemStack reviveItem, PlayerGUI playerGUI) {
        this.plugin = plugin;
        this.tags = tags;
        this.spawnLoc = spawnLoc;
        this.reviveItem = reviveItem.clone();
        this.reviveItem.setAmount(1);
        this.playerGUI = playerGUI;
    }

    private GameTeam getTeam(Player player) {
        if(player == null) return null;
        for (GameTeam team : teams) {
            if (team.getOwner().equals(player) || team.getCitizens().contains(player)) {
                return team;
            }
        }
        return null;
    }

    private void updateScoreboard() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            GameTeam ally = getTeam(player);
            if (ally == null) continue;
            Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
            player.setScoreboard(scoreboard);

            for (Player enemy : Bukkit.getOnlinePlayers()) {
                GameTeam team = getTeam(enemy);
                if (team == null) continue;
                ChatColor color;
                if (ally.equals(team)) {
                    color = ChatColor.GREEN;
                } else {
                    color = ChatColor.RED;
                }
                Team enemyTeam = scoreboard.registerNewTeam(enemy.getName());
                enemyTeam.setPrefix(color + "[" + team.getTag() + "] ");
                if (team.getOwner().equals(enemy)) {
                    enemyTeam.setSuffix(" (TOP)");
                }
                enemyTeam.addEntry(enemy.getName());
            }
        }
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
        }
        updateScoreboard();
    }

    @Override
    public void disable() {
        System.out.println("ゲームが終了しました");
        HandlerList.unregisterAll(this);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            player.teleport(spawnLoc);
        }
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
        GameTeam team = getTeam(e.getPlayer());
        if (team != null) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> e.getPlayer().teleport(team.getOwner()), 1);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        GameTeam team = getTeam((Player) e.getEntity());
        if (team == null) return;
        if (!team.getOwner().equals(e.getEntity())) return;
        Player owner = team.getOwner();
        if (owner.getHealth() - e.getFinalDamage() > 0) return;
        ItemStack reviveItem = this.reviveItem.clone();
        reviveItem.setAmount(2);
        if (ItemStackUtils.countItemStack(owner.getInventory(), reviveItem) < reviveItem.getAmount()) return;
        e.setDamage(0);
        owner.getInventory().removeItem(reviveItem);
        owner.setHealth(owner.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        owner.getLocation().getWorld().playSound(owner.getLocation(), Sound.ENTITY_BAT_DEATH, 0.5F, 0.5F);
    }

    @EventHandler
    public void onDamageByEntity(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        Player player = null;
        if (e.getDamager() instanceof Player) {
            player = (Player) e.getDamager();
        } else if (e.getDamager() instanceof Projectile) {
            ProjectileSource source = ((Projectile) e.getDamager()).getShooter();
            if (source instanceof Player) {
                player = (Player) source;
            }
        }
        GameTeam attacker = getTeam(player);
        GameTeam victim = getTeam((Player) e.getEntity());
        if (attacker == null || !attacker.equals(victim)) return;
        e.setDamage(0);
    }

    @EventHandler
    public void onClick(PlayerInteractEvent e) {
        GameTeam team = getTeam(e.getPlayer());
        if (team == null) return;
        if (!team.getOwner().equals(e.getPlayer())) return;
        ItemStack mainHand = e.getPlayer().getInventory().getItemInMainHand();
        if (!reviveItem.isSimilar(mainHand)) return;
        List<Player> dead = team.getCitizens().stream()
                .filter(Player::isValid)
                .filter(p -> p.getGameMode().equals(GameMode.SPECTATOR))
                .collect(Collectors.toList());
        playerGUI.openGUI(e.getPlayer(), dead, (p) -> {
            if (p == null) return;
            if (ItemStackUtils.countItemStack(e.getPlayer().getInventory(), reviveItem) < 1) return;
            e.getPlayer().getInventory().removeItem(reviveItem);
            p.teleport(e.getPlayer().getLocation());
            p.setGameMode(GameMode.SURVIVAL);
        });
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
        updateScoreboard();
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
