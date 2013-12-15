package com.greatmancode.quakecraft;

import me.ampayne2.ultimategames.UltimateGames;
import me.ampayne2.ultimategames.api.GamePlugin;
import me.ampayne2.ultimategames.arenas.Arena;
import me.ampayne2.ultimategames.arenas.ArenaStatus;
import me.ampayne2.ultimategames.arenas.scoreboards.ArenaScoreboard;
import me.ampayne2.ultimategames.arenas.spawnpoints.PlayerSpawnPoint;
import me.ampayne2.ultimategames.arenas.spawnpoints.SpawnpointManager;
import me.ampayne2.ultimategames.games.Game;
import me.ampayne2.ultimategames.games.items.GameItem;
import me.ampayne2.ultimategames.players.PlayerManager;
import me.ampayne2.ultimategames.utils.UGUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class QuakeCraft extends GamePlugin {
    private UltimateGames ultimateGames;
    private Game game;
    private Set<String> reloaders = new HashSet<String>();
    private Map<String, Integer> reloadTasks = new HashMap<String, Integer>();
    private Map<String, GibStreak> streaks = new HashMap<String, GibStreak>();
    private static final float EXP_MAX = 1.0F;
    private static final float EXP_MIN = 0.0F;
    private static final int LEVEL_MIN = 0;
    private Map<String, ItemStack> gameItem = new HashMap<String, ItemStack>();
    private Map<String, String> playerWeapon = new HashMap<String, String>();

    //Reload related stuff
    private Map<String, Float> reloadTime = new HashMap<String, Float>();
    private Map<String, Float> currentReloadtime = new HashMap<String, Float>();

    @Override
    public boolean loadGame(UltimateGames ultimateGames, Game game) {
        this.ultimateGames = ultimateGames;
        this.game = game;

        ItemStack woodHoe = new ItemStack(Material.WOOD_HOE);
        ItemMeta woodHoeMeta = woodHoe.getItemMeta();
        woodHoeMeta.setDisplayName("Small Railgun");
        woodHoe.setItemMeta(woodHoeMeta);
        GameItem woodHoeRailGun = new GameItem(woodHoe, false, new ShootAction(ultimateGames, this, game));
        ultimateGames.getGameItemManager().registerGameItem(game, woodHoeRailGun);
        gameItem.put("woodhoe", woodHoe);

        ItemStack stoneHoe = new ItemStack(Material.STONE_HOE);
        ItemMeta stoneHoeMeta = stoneHoe.getItemMeta();
        stoneHoeMeta.setDisplayName("Good Railgun");
        stoneHoe.setItemMeta(stoneHoeMeta);
        GameItem stoneHoeRailGun = new GameItem(stoneHoe, false, new ShootAction(ultimateGames, this, game));
        ultimateGames.getGameItemManager().registerGameItem(game, stoneHoeRailGun);
        gameItem.put("stonehoe", stoneHoe);

        ItemStack ironHoe = new ItemStack(Material.IRON_HOE);
        ItemMeta ironHoeMeta = ironHoe.getItemMeta();
        ironHoeMeta.setDisplayName("Superior Railgun");
        ironHoe.setItemMeta(ironHoeMeta);
        GameItem ironHoeRailGun = new GameItem(ironHoe, false, new ShootAction(ultimateGames, this, game));
        ultimateGames.getGameItemManager().registerGameItem(game, ironHoeRailGun);
        gameItem.put("ironhoe", ironHoe);

        ItemStack goldHoe = new ItemStack(Material.GOLD_HOE);
        ItemMeta goldHoeMeta = goldHoe.getItemMeta();
        goldHoeMeta.setDisplayName("Epic Railgun");
        goldHoe.setItemMeta(goldHoeMeta);
        GameItem goldHoeRailGun = new GameItem(goldHoe, false, new ShootAction(ultimateGames, this, game));
        ultimateGames.getGameItemManager().registerGameItem(game, goldHoeRailGun);
        gameItem.put("goldhoe", goldHoe);

        ItemStack diamondHoe = new ItemStack(Material.DIAMOND_HOE);
        ItemMeta diamondHoeMeta = diamondHoe.getItemMeta();
        diamondHoeMeta.setDisplayName("Marvelous Railgun");
        diamondHoe.setItemMeta(diamondHoeMeta);
        GameItem diamondHoeRailGun = new GameItem(diamondHoe, false, new ShootAction(ultimateGames, this, game));
        ultimateGames.getGameItemManager().registerGameItem(game, diamondHoeRailGun);
        gameItem.put("diamondhoe", diamondHoe);

        ItemStack blazeRod = new ItemStack(Material.BLAZE_ROD);
        ItemMeta blazeRodMeta = blazeRod.getItemMeta();
        blazeRodMeta.setDisplayName("Best Railgun");
        blazeRod.setItemMeta(blazeRodMeta);
        GameItem blazeRodRailGun = new GameItem(blazeRod, false, new ShootAction(ultimateGames, this, game));
        ultimateGames.getGameItemManager().registerGameItem(game, blazeRodRailGun);
        gameItem.put("blazerod", blazeRod);

        return true;
    }

    @Override
    public void unloadGame() {}

    @Override
    public boolean reloadGame() {
        return true;
    }

    @Override
    public boolean stopGame() {
        return true;
    }

    @Override
    public boolean loadArena(Arena arena) {
        ultimateGames.addAPIHandler("/" + game.getName() + "/" + arena.getName(), new QuakecraftWebHandler(ultimateGames, arena));
        return true;
    }

    @Override
    public boolean unloadArena(Arena arena) {
        return true;
    }

    @Override
    public boolean isStartPossible(Arena arena) {
        return arena.getStatus() == ArenaStatus.OPEN;
    }

    @Override
    public boolean startArena(Arena arena) {
        return true;
    }

    @Override
    public boolean beginArena(Arena arena) {
        ultimateGames.getCountdownManager().createEndingCountdown(arena, 1200, false);

        ArenaScoreboard scoreBoard = ultimateGames.getScoreboardManager().createArenaScoreboard(arena, "Gibs");
        SpawnpointManager spawnpointManager = ultimateGames.getSpawnpointManager();
        for (String playerName : arena.getPlayers()) {
        	Player player = Bukkit.getPlayerExact(playerName);
            scoreBoard.addPlayer(player);
            scoreBoard.setScore(playerName, 0);
            PlayerSpawnPoint spawnPoint = spawnpointManager.getRandomSpawnPoint(arena);
            while (spawnPoint.getPlayer() != null) {
            	spawnPoint = spawnpointManager.getRandomSpawnPoint(arena);
            }
            spawnPoint.lock(true);
            spawnPoint.teleportPlayer(player);
            streaks.put(playerName, new GibStreak(ultimateGames, game, ultimateGames.getPlayerManager().getArenaPlayer(playerName)));
        }
        for (PlayerSpawnPoint spawnPoint : spawnpointManager.getSpawnPointsOfArena(arena)) {
        	spawnPoint.lock(false);
        }
        scoreBoard.setVisible(true);

        return true;
    }

    @Override
    public void endArena(Arena arena) {
        String highestScorer = null;
        Integer highScore = 0;
        List<String> players = arena.getPlayers();
        ArenaScoreboard scoreBoard = ultimateGames.getScoreboardManager().getArenaScoreboard(arena);
        if (scoreBoard != null) {
            for (String playerName : players) {
                Integer playerScore = scoreBoard.getScore(playerName);
                if (playerScore > highScore) {
                    highestScorer = playerName;
                    highScore = playerScore;
                }
                streaks.remove(playerName);
            }
        }
        if (highestScorer != null) {
            ultimateGames.getMessageManager().sendGameMessage(Bukkit.getServer(), game, "GameEnd", highestScorer, game.getName(), arena.getName());
            if (highScore == 25) {
                ultimateGames.getPointManager().addPoint(game, highestScorer, "store", 25);
                ultimateGames.getPointManager().addPoint(game, highestScorer, "win", 1);
            }
        }
    }

    @Override
    public boolean resetArena(Arena arena) {
        return true;
    }

    @Override
    public boolean openArena(Arena arena) {
        return true;
    }

    @Override
    public boolean stopArena(Arena arena) {
        return true;
    }

    @Override
    public boolean addPlayer(Player player, Arena arena) {
        if (arena.getPlayers().size() >= arena.getMinPlayers() && !ultimateGames.getCountdownManager().hasStartingCountdown(arena)) {
            ultimateGames.getCountdownManager().createStartingCountdown(arena, ultimateGames.getConfigManager().getGameConfig(game).getConfig().getInt("CustomValues.StartWaitTime"));
        }
        PlayerSpawnPoint spawnPoint = ultimateGames.getSpawnpointManager().getRandomSpawnPoint(arena);
        spawnPoint.lock(false);
        spawnPoint.teleportPlayer(player);
        for (PotionEffect potionEffect : player.getActivePotionEffects()) {
            player.removePotionEffect(potionEffect.getType());
        }
        player.setHealth(20.0);
        player.setFoodLevel(20);
        if (ultimateGames.getPointManager().hasPerk(game, player.getName(), "blazerod")) {
            playerWeapon.put(player.getName(), "blazerod");
        } else if (ultimateGames.getPointManager().hasPerk(game, player.getName(), "diamondhoe")) {
            playerWeapon.put(player.getName(), "diamondhoe");
        } else if (ultimateGames.getPointManager().hasPerk(game, player.getName(), "goldhoe")) {
            playerWeapon.put(player.getName(), "goldhoe");
        } else if (ultimateGames.getPointManager().hasPerk(game, player.getName(), "ironhoe")) {
            playerWeapon.put(player.getName(), "ironhoe");
        } else if (ultimateGames.getPointManager().hasPerk(game, player.getName(), "stonehoe")) {
            playerWeapon.put(player.getName(), "stonehoe");
        } else {
            playerWeapon.put(player.getName(), "woodhoe");
        }
        resetInventory(player);

        //We retrieve the reload time of the player
        if (ultimateGames.getPointManager().hasPerk(game, player.getName(), "10trigger")) {
            reloadTime.put(player.getName(), 1.0f * 20f);
        } else if (ultimateGames.getPointManager().hasPerk(game, player.getName(), "11trigger")) {
            reloadTime.put(player.getName(), 1.1f * 20f);
        } else if (ultimateGames.getPointManager().hasPerk(game, player.getName(), "12trigger")) {
            reloadTime.put(player.getName(), 1.2f * 20f);
        } else if (ultimateGames.getPointManager().hasPerk(game, player.getName(), "13trigger")) {
            reloadTime.put(player.getName(), 1.3f * 20f);
        } else if (ultimateGames.getPointManager().hasPerk(game, player.getName(), "14trigger")) {
            reloadTime.put(player.getName(), 1.4f * 20f);
        } else if (ultimateGames.getPointManager().hasPerk(game, player.getName(), "15trigger")) {
            reloadTime.put(player.getName(), 1.5f * 20f);
        } else if (ultimateGames.getPointManager().hasPerk(game, player.getName(), "16trigger")) {
            reloadTime.put(player.getName(), 1.6f * 20f);
        } else if (ultimateGames.getPointManager().hasPerk(game, player.getName(), "17trigger")) {
            reloadTime.put(player.getName(), 1.7f * 20f);
        } else {
            reloadTime.put(player.getName(), 1.8f * 20f);
        }

        return true;
    }

    @Override
    public void removePlayer(Player player, Arena arena) {
    	endCooldown(player);
        streaks.remove(player.getName());
        if (arena.getStatus() == ArenaStatus.RUNNING && arena.getPlayers().size() < 2) {
            ultimateGames.getArenaManager().endArena(arena);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean addSpectator(Player player, Arena arena) {
        ultimateGames.getSpawnpointManager().getSpectatorSpawnPoint(arena).teleportPlayer(player);
        for (PotionEffect potionEffect : player.getActivePotionEffects()) {
            player.removePotionEffect(potionEffect.getType());
        }
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.getInventory().clear();
        player.getInventory().addItem(UGUtils.createInstructionBook(game));
        player.getInventory().setArmorContents(null);
        player.updateInventory();
        return true;
    }

    @Override
    public void removeSpectator(Player player, Arena arena) {}

    @Override
    public void onPlayerDeath(Arena arena, PlayerDeathEvent event) {
        event.getDrops().clear();
        UGUtils.autoRespawn(event.getEntity());
        GibStreak streak = streaks.get(event.getEntity().getName());
        if (streak != null) {
        	streak.reset();
        }
    }

    @Override
    public void onPlayerRespawn(Arena arena, PlayerRespawnEvent event) {
        event.setRespawnLocation(ultimateGames.getSpawnpointManager().getRandomSpawnPoint(arena).getLocation());
        resetInventory(event.getPlayer());
    }

    @Override
    public void onEntityDamage(Arena arena, EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            event.setCancelled(true);
        }
    }

    @Override
    public void onPlayerFoodLevelChange(Arena arena, FoodLevelChangeEvent event) {
        event.setCancelled(true);
    }

    @Override
    public void onItemPickup(Arena arena, PlayerPickupItemEvent event) {
        event.setCancelled(true);
    }

    @Override
    public void onItemDrop(Arena arena, PlayerDropItemEvent event) {
        event.setCancelled(true);
    }

    @SuppressWarnings("deprecation")
    private void resetInventory(Player player) {
        final String playerName = player.getName();
        player.getInventory().clear();
        player.getInventory().addItem(gameItem.get(playerWeapon.get(player.getName())), UGUtils.createInstructionBook(game));
        player.updateInventory();
        player.setLevel(LEVEL_MIN);
        player.setExp(EXP_MAX);
        Bukkit.getScheduler().scheduleSyncDelayedTask(ultimateGames, new Runnable() {
            @Override
            public void run() {
                PlayerManager playerManager = ultimateGames.getPlayerManager();
                if (playerManager.isPlayerInArena(playerName) && playerManager.getPlayerArena(playerName).getGame().equals(game)) {
                    Bukkit.getPlayerExact(playerName).addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 6000, 1));
                }
            }
        }, 40L);
    }

    public boolean isPlayerReloading(String playerName) {
    	return reloaders.contains(playerName);
    }

    public void startCooldown(Player player) {
        final String playerName = player.getName();
        player.setExp(EXP_MIN);
        reloaders.add(playerName);
        currentReloadtime.put(playerName, 0f);
        reloadTasks.put(player.getName(), Bukkit.getScheduler().scheduleSyncRepeatingTask(ultimateGames, new Runnable() {
            @Override
            public void run() {
                Player player = Bukkit.getPlayerExact(playerName);
                if (reloaders.contains(playerName)) {
                    float newExp = currentReloadtime.get(playerName) / reloadTime.get(playerName);
                    if (newExp >= EXP_MAX) {
                        endCooldown(player);
                    } else {
                        player.setExp(newExp);
                        currentReloadtime.put(playerName, currentReloadtime.get(playerName) + 1);
                    }
                }
            }
        }, 0, 1L));
    }

    public void endCooldown(Player player) {
        String playerName = player.getName();
        if (reloadTasks.containsKey(playerName)) {
            Bukkit.getScheduler().cancelTask(reloadTasks.get(playerName));
            reloadTasks.remove(playerName);
            reloaders.remove(playerName);
            player.setExp(EXP_MAX);
        }
    }

    public GibStreak getStreak(String playerName) {
    	return streaks.get(playerName);
    }
}
