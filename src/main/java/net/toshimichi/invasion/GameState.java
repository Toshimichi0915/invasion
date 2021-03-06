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

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
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
import org.bukkit.util.Vector;

import java.util.*;
import java.util.stream.Collectors;

public class GameState implements State, Listener, Runnable {

    private static final char[] signs = {'↑', '↗', '→', '↘', '↓', '↙', '←', '↖'};
    private static final double radSize = 2 * Math.PI / signs.length;

    private final Plugin plugin;
    private final Location spawnLoc;
    private final Random random = new Random();
    private final HashMap<Player, String> oldTeams = new HashMap<>();
    private final ArrayList<GameTeam> teams = new ArrayList<>();
    private final HashMap<Player, Integer> killCount = new HashMap<>();
    private final HashSet<Location> openedChests = new HashSet<>();
    private final HashSet<Player> hitByFriend = new HashSet<>();
    private final String tags;
    private final ItemStack reviveItem;
    private final PlayerGUI playerGUI;
    private final Lottery<ItemStack> lottery;
    private final int maxItems;
    private final double borderRange;
    private boolean prevPeaceful = true;
    private ArrayList<Border> border;
    private int counter;
    private int tagCounter = 0;
    private BukkitTask task;

    public GameState(Plugin plugin, Location spawnLoc, String tags, ItemStack reviveItem, PlayerGUI playerGUI,
                     Lottery<ItemStack> lottery, int maxItems, int borderRange, List<Border> border) {
        this.plugin = plugin;
        this.tags = tags;
        this.spawnLoc = spawnLoc;
        this.reviveItem = reviveItem.clone();
        this.reviveItem.setAmount(1);
        this.playerGUI = playerGUI;
        this.lottery = lottery;
        this.maxItems = maxItems;
        this.borderRange = borderRange;
        this.border = new ArrayList<>(border);
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
                    enemyTeam.setSuffix(ChatColor.GOLD + " (TOP)");
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
        for (Entity e : spawnLoc.getWorld().getEntities()) {
            if (e instanceof Item) {
                e.remove();
            }
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            GameTeam team = new GameTeam(player, Character.toString(tags.charAt(tagCounter++)));
            player.setGameMode(GameMode.ADVENTURE);
            player.getActivePotionEffects().forEach(p -> player.removePotionEffect(p.getType()));
            oldTeams.put(player, team.getName());
            teams.add(team);
            killCount.put(player, 0);
        }
        updateScoreboard();

        WorldBorder worldBorder = spawnLoc.getWorld().getWorldBorder();
        worldBorder.setCenter(spawnLoc);
        worldBorder.setDamageAmount(0.5);
        worldBorder.setDamageBuffer(3);
        worldBorder.setSize(borderRange);

        border.sort(Comparator.comparingInt(Border::getPhase));
    }

    @Override
    public void disable() {
        System.out.println("ゲームが終了しました");
        HandlerList.unregisterAll(this);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            player.teleport(spawnLoc);
            player.getInventory().clear();
            player.setItemOnCursor(null);
            player.setGameMode(GameMode.ADVENTURE);
        }
        if (task != null) {
            task.cancel();
        }
        spawnLoc.getWorld().getWorldBorder().reset();
    }

    private boolean isInRange(double rad1, double rad2, double target) {
        if (target < 0)
            target += 2 * Math.PI;
        if (rad1 < 0)
            rad1 += 2 * Math.PI;
        if (rad2 < 0)
            rad2 += 2 * Math.PI;
        if (rad1 <= rad2)
            return target >= rad1 && target <= rad2;
        else
            return target >= rad1 || target <= rad2;
    }

    @Override
    public void run() {
        counter++;
        hitByFriend.clear();

        // ボーダー
        int totalTime = 0;
        for (int i = 0; i < border.size(); i++) {
            totalTime += border.get(i).getPeace();
            if (i == border.size() - 1 && totalTime + border.get(i).getTime() < counter / 20 && !prevPeaceful) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.sendMessage(ChatColor.GOLD + "最後の収束が終了しました");
                }
                prevPeaceful = true;
            }
            if (totalTime - border.get(i).getPeace() < counter / 20 && counter / 20 < totalTime && !prevPeaceful) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.sendMessage(ChatColor.GOLD + "第" + i + "回目の収束が終了しました");
                }
                prevPeaceful = true;
            }
            totalTime += border.get(i).getTime();
            if (totalTime - border.get(i).getTime() < counter / 20 && counter / 20 < totalTime) {
                if (prevPeaceful) {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        player.playSound(player.getLocation(), Sound.BLOCK_END_PORTAL_SPAWN, 1f, 1f);
                        player.sendMessage(ChatColor.GOLD + "第" + (i + 1) + "回目の収束が始まります");
                    }
                    prevPeaceful = false;
                }
                double deltaTime = ((double) counter / 20 - totalTime + border.get(i).getTime()) / border.get(i).getTime();
                double startBorder = border.isEmpty() || i == 0 ? 0 : border.get(i - 1).getTo();
                double currentBorder = (border.get(i).getTo() - startBorder) * deltaTime + startBorder;

                WorldBorder worldBorder = spawnLoc.getWorld().getWorldBorder();
                worldBorder.setSize(borderRange - borderRange * currentBorder);
            }
        }

        // チーム移籍
        for (Player player : Bukkit.getOnlinePlayers()) {
            GameTeam current = getTeam(player);
            String oldTeamName = oldTeams.get(player);
            if (current == null) continue;
            if (oldTeamName == null) continue;
            if (current.getName().equals(oldTeams.get(player))) continue;
            player.sendMessage(ChatColor.GOLD + "あなたは " + ChatColor.GREEN + current.getName() + ChatColor.GOLD + " の市民になりました");
            oldTeams.put(player, current.getName());
        }

        // アクションバー
        for (Player player : Bukkit.getOnlinePlayers()) {
            GameTeam ally = getTeam(player);
            if (ally == null || ally.getOwner().equals(player)) continue;
            Player owner = ally.getOwner();
            Vector v1 = player.getLocation().getDirection();
            Vector v2 = owner.getLocation().toVector().subtract(player.getLocation().toVector()).normalize();
            double rad = Math.atan2(v1.getX() * v2.getZ() - v2.getX() * v1.getZ(), v1.getX() * v2.getX() + v1.getZ() * v2.getZ());
            char sign = ' ';
            for (int i = 0; i < signs.length; i++) {
                double startRad = -radSize / 2 + radSize * i;
                double endRad = startRad + radSize;
                if (!isInRange(startRad, endRad, rad)) continue;
                sign = signs[i];
                break;
            }
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.GOLD + owner.getDisplayName() +
                    "(TOP) " + ChatColor.RED + ChatColor.BOLD + sign));
        }

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
                Collections.reverse(citizens);
                for (Player citizen : citizens) {
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
        GameTeam team = getTeam(e.getPlayer());
        if (team != null) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                e.getPlayer().setGameMode(GameMode.SPECTATOR);
                e.getPlayer().teleport(team.getOwner());
            }, 1);
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
        hitByFriend.add((Player) e.getEntity());
        if (e.getDamager() instanceof Player) {
            ((Player) e.getDamager()).playSound(e.getDamager().getLocation(), Sound.BLOCK_NOTE_BASS, 1, 0.3F);
        }
    }

    @EventHandler
    public void onConsume(PlayerItemDamageEvent e) {
        if (hitByFriend.contains(e.getPlayer())) {
            e.setCancelled(true);
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
                e.setDeathMessage(ChatColor.GRAY + e.getEntity().getName() + "は死亡した");
            } else { //他殺の場合
                teams.remove(victimTeam);
                // 勝利者のチームに加える
                killerTeam.addCitizen(e.getEntity());
                for (Player player : victimTeam.getCitizens()) {
                    killerTeam.addCitizen(player);
                }
                e.setDeathMessage(ChatColor.GRAY + e.getEntity().getName() + "(LEADER)は" + e.getEntity().getKiller().getName() + "に殺された");
            }
        } else if (killerTeam != null) { // 市民が他殺された場合
            victimTeam.removeCitizen(e.getEntity());
            killerTeam.addCitizen(e.getEntity());
            e.setDeathMessage(ChatColor.GRAY + e.getEntity().getName() + "(CITIZEN)は" + e.getEntity().getKiller().getName() + "に殺された");
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

    @EventHandler
    public void onChat(PlayerChatEvent e) {
        GameTeam team = getTeam(e.getPlayer());
        e.getRecipients().clear();
        e.getRecipients().add(team.getOwner());
        e.getRecipients().addAll(team.getCitizens());
        StringBuilder builder = new StringBuilder();
        if (team.getOwner().equals(e.getPlayer()))
            builder.append(ChatColor.GOLD).append("[LEADER]");
        else
            builder.append(ChatColor.GRAY).append("[CITIZEN]");
        builder.append(ChatColor.GREEN).append("<%1$s>").append(ChatColor.WHITE).append(" %2$s");
        e.setFormat(builder.toString());
    }
}
