package net.toshimichi.invasion;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;
import java.util.stream.Collectors;

public class GameState implements State, Listener, Runnable {

    private final Plugin plugin;
    private final Location spawnLoc;
    private final Random random = new Random();
    private final ArrayList<GameTeam> teams = new ArrayList<>();
    private final HashMap<Player, Integer> killCount = new HashMap<>();
    private final HashSet<Location> openedChests = new HashSet<>();
    private final String tags;
    private final ItemStack reviveItem;
    private final PlayerGUI playerGUI;
    private final Lottery<ItemStack> lottery;
    private final int maxItems;
    private double border;
    private final double borderSpeed;
    private int counter;
    private int tagCounter = 0;
    private BukkitTask task;

    public GameState(Plugin plugin, Location spawnLoc, String tags, ItemStack reviveItem, PlayerGUI playerGUI,
                     Lottery<ItemStack> lottery, int maxItems, int border, double borderSpeed) {
        this.plugin = plugin;
        this.tags = tags;
        this.spawnLoc = spawnLoc;
        this.reviveItem = reviveItem.clone();
        this.reviveItem.setAmount(1);
        this.playerGUI = playerGUI;
        this.lottery = lottery;
        this.maxItems = maxItems;
        this.border = border;
        this.borderSpeed = borderSpeed;
    }

    private GameTeam getTeam(Player player) {
        if (player == null) return null;
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
            for (Entity e : spawnLoc.getWorld().getEntities()) {
                if (e instanceof Item) {
                    e.remove();
                }
            }
            player.setGameMode(GameMode.ADVENTURE);
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
            player.setGameMode(GameMode.ADVENTURE);
        }
        if (task != null) {
            task.cancel();
        }
        spawnLoc.getWorld().getWorldBorder().reset();
    }

    @Override
    public void run() {
        counter++;
        border -= borderSpeed;
        // ボーダー
        WorldBorder worldBorder = spawnLoc.getWorld().getWorldBorder();
        worldBorder.setCenter(spawnLoc);
        worldBorder.setDamageAmount(0.5);
        worldBorder.setDamageBuffer(3);
        worldBorder.setSize(border);

        // コンパス
        for (Player player : Bukkit.getOnlinePlayers()) {
            GameTeam ally = getTeam(player);
            if (ally == null) continue;
            if (ally.getOwner().equals(player)) {
                double minDistance = Double.MAX_VALUE;
                Player closest = null;
                for (Player enemy : Bukkit.getOnlinePlayers()) {
                    GameTeam enemyTeam = getTeam(enemy);
                    if (enemy.isDead()) continue;
                    if (enemyTeam == null) continue;
                    if (ally.equals(enemyTeam)) continue;
                    double distance = player.getLocation().distanceSquared(enemy.getLocation());
                    if (distance < minDistance) {
                        minDistance = distance;
                        closest = enemy;
                    }
                }
                if (closest != null) {
                    player.setCompassTarget(closest.getLocation());
                }
            } else {
                player.setCompassTarget(ally.getOwner().getLocation());
            }
        }
        // 10分おきにチェスト更新
        if (counter % (20 * 60 * 10) == 0) {
            openedChests.clear();
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.sendMessage(ChatColor.GREEN + "チェストにアイテムが補給されました");
            }
        }
        // ゲーム終了
        if (teams.size() == 1) {
            Bukkit.getScheduler().runTaskLater(plugin, this::disable, 100);
            GameTeam winner = teams.get(0);
            teams.clear();
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.sendMessage(ChatColor.GOLD + winner.getName() + "の勝利");
                List<Player> citizens = winner.getCitizens();
                citizens.sort(Comparator.comparingInt(killCount::get));
                for (Player citizen : citizens) {
                    if (killCount.get(citizen) == 0) continue;
                    player.sendMessage(ChatColor.YELLOW + citizen.getDisplayName() + ChatColor.GRAY + "(殺害数: " + killCount.get(citizen) + ")");
                }
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERDRAGON_DEATH, 0.5F, 1);
            }
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        PlayerInventory i = e.getPlayer().getInventory();
        if (i.getChestplate() == null) return;
        if (!i.getChestplate().getType().equals(Material.ELYTRA)) return;
        if (!e.getPlayer().isOnGround()) return;
        e.getPlayer().setFallDistance(0);
        i.setChestplate(null);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        e.getPlayer().setGameMode(GameMode.SPECTATOR);
        e.getPlayer().teleport(spawnLoc);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        ItemStack reviveItem = this.reviveItem.clone();
        reviveItem.setAmount(1000);
        e.getPlayer().getInventory().removeItem(reviveItem);
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
        if (e.getDamager() instanceof Player) {
            ((Player) e.getDamager()).playSound(e.getDamager().getLocation(), Sound.BLOCK_NOTE_BASS, 1, 0.3F);
        }
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
            p.setGameMode(GameMode.ADVENTURE);
        });
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        GameTeam victimTeam = getTeam(e.getEntity());
        GameTeam killerTeam = getTeam(e.getEntity().getKiller());
        killCount.put(e.getEntity(), 0);
        killCount.put(e.getEntity().getKiller(), killCount.getOrDefault(e.getEntity().getKiller(), 0) + 1);

        /*
        チームの所有者が死亡:
            自然死: 市民に所有者の座を譲渡
            他殺: 殺害者にチーム全体を譲渡
        チームの市民が死亡: 殺害者のチームに移動
         */
        if (victimTeam == null) return;
        if (victimTeam.getOwner().equals(e.getEntity())) { // 所有者が死んだ場合
            if (killerTeam == null) { // 自然死の場合
                Player bestKiller = null;
                int mostKills = 0;
                for (Player player : victimTeam.getCitizens()) {
                    int kills = killCount.get(player);
                    if (kills <= mostKills) continue;
                    if (player.equals(e.getEntity()) || player.getGameMode() != GameMode.SURVIVAL) continue;
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
    public void onOpen(PlayerInteractEvent e) {
        Block block = e.getClickedBlock();
        if (block == null) return;
        if (block.getType() != Material.CHEST) return;
        if (openedChests.contains(block.getLocation())) return;
        GameTeam team = getTeam(e.getPlayer());
        if (team == null || !team.getOwner().equals(e.getPlayer())) {
            e.getPlayer().sendMessage(ChatColor.RED + "「王」のみがチェストを開けることができます");
            e.setCancelled(true);
            return;
        }
        openedChests.add(block.getLocation());
        Inventory inventory = ((Chest) block.getState()).getBlockInventory();
        inventory.clear();
        int items = random.nextInt(maxItems) + 1;
        for (int i = 0; i < items; i++) {
            inventory.setItem(random.nextInt(27), lottery.draw());
        }
    }
}
