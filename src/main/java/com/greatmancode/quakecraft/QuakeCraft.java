package com.greatmancode.quakecraft;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import me.ampayne2.ultimategames.Message;
import me.ampayne2.ultimategames.UltimateGames;
import me.ampayne2.ultimategames.api.GamePlugin;
import me.ampayne2.ultimategames.arenas.Arena;
import me.ampayne2.ultimategames.arenas.PlayerSpawnPoint;
import me.ampayne2.ultimategames.effects.GameSound;
import me.ampayne2.ultimategames.enums.ArenaStatus;
import me.ampayne2.ultimategames.games.Game;
import me.ampayne2.ultimategames.players.PlayerManager;
import me.ampayne2.ultimategames.scoreboards.ArenaScoreboard;
import me.ampayne2.ultimategames.utils.UGUtils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class QuakeCraft extends GamePlugin {
    private UltimateGames ultimateGames;
    private Game game;
    private Set<String> reloaders = new HashSet<String>();
    private Map<String, Integer> reloadTasks = new HashMap<String, Integer>();
    private int WIN_THRESHOLD;
    private static final int LEVEL_MIN = 0;
    private static final float EXP_MAX = 1.0F;
    private static final float EXP_MIN = 0.0F;
    private static final float EXP_INCREMENT = 0.1F;
    private static final GameSound SHOOT_SOUND = new GameSound(Sound.BLAZE_HIT, 5, 2);
    private static final GameSound KILL_SOUND = new GameSound(Sound.EXPLODE, 5, 1);

    @Override
    public Boolean loadGame(UltimateGames ultimateGames, Game game) {
        this.ultimateGames = ultimateGames;
        this.game = game;
        WIN_THRESHOLD = ultimateGames.getConfigManager().getGameConfig(game).getConfig().getInt("CustomValues.MaxKills", 25);
        return true;
    }

    @Override
    public void unloadGame() {}

    @Override
    public Boolean reloadGame() {
        return true;
    }

    @Override
    public Boolean stopGame() {
        return true;
    }

    @Override
    public Boolean loadArena(Arena arena) {
        ultimateGames.addAPIHandler("/" + game.getName() + "/" + arena.getName(), new QuakecraftWebHandler(ultimateGames, arena));
        return true;
    }

    @Override
    public Boolean unloadArena(Arena arena) {
        return true;
    }

    @Override
    public Boolean isStartPossible(Arena arena) {
        return arena.getStatus() == ArenaStatus.OPEN;
    }

    @Override
    public Boolean startArena(Arena arena) {
        return true;
    }

    @Override
    public Boolean beginArena(Arena arena) {
        ultimateGames.getCountdownManager().createEndingCountdown(arena, 1200, false);

        ArenaScoreboard scoreBoard = ultimateGames.getScoreboardManager().createArenaScoreboard(arena, "Gibs");
        for (String playerName : arena.getPlayers()) {
            scoreBoard.addPlayer(Bukkit.getPlayerExact(playerName));
            scoreBoard.setScore(playerName, 0);
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
    public Boolean resetArena(Arena arena) {
        return true;
    }

    @Override
    public Boolean openArena(Arena arena) {
        return true;
    }

    @Override
    public Boolean stopArena(Arena arena) {
        return true;
    }

    @Override
    public Boolean addPlayer(Player player, Arena arena) {
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
        resetInventory(player);
        return true;
    }

    @Override
    public void removePlayer(Player player, Arena arena) {
    	endCooldown(player);
        if (arena.getStatus() == ArenaStatus.RUNNING && arena.getPlayers().size() < 2) {
            ultimateGames.getArenaManager().endArena(arena);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public Boolean addSpectator(Player player, Arena arena) {
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

    @SuppressWarnings("deprecation")
	@Override
    public void onPlayerInteract(Arena arena, PlayerInteractEvent event) {
        if (arena.getStatus() != ArenaStatus.RUNNING || event.getAction() != Action.RIGHT_CLICK_AIR || event.getMaterial() != Material.BLAZE_ROD) {
            return;
        }
        Player player = event.getPlayer();
        String playerName = player.getName();
        if (!reloaders.contains(playerName)) {
            PlayerManager playerManager = ultimateGames.getPlayerManager();
            Message messageManager = ultimateGames.getMessageManager();
            ArenaScoreboard scoreBoard = ultimateGames.getScoreboardManager().getArenaScoreboard(playerManager.getPlayerArena(playerName));
            Collection<LivingEntity> players = UGUtils.getLivingEntityTargets(player, 100, 0, false, true, true);
            SHOOT_SOUND.play(player.getEyeLocation());
            for (LivingEntity entity : players) {
                if (entity instanceof Player) {
                    Player targetedPlayer = (Player) entity;
                    String targetedPlayerName = targetedPlayer.getName();
                    if (targetedPlayer.getHealth() > 0.0 && playerManager.isPlayerInArena(targetedPlayerName) && playerManager.getPlayerArena(targetedPlayerName).equals(arena)) {
                        targetedPlayer.getInventory().clear();
                        targetedPlayer.updateInventory();
                        endCooldown(targetedPlayer);
                        targetedPlayer.setHealth(0.0);
                        KILL_SOUND.play(targetedPlayer.getLocation());
                        messageManager.sendGameMessage(arena, game, "Gib", playerName, targetedPlayerName);
                        ultimateGames.getPointManager().addPoint(game, playerName, "kill", 1);
                        ultimateGames.getPointManager().addPoint(game, playerName, "store", 1);
                        ultimateGames.getPointManager().addPoint(game, targetedPlayerName, "death", 1);
                        if (scoreBoard != null) {
                            scoreBoard.setScore(playerName, scoreBoard.getScore(playerName) + 1);
                        }
                    }
                }
            }
            switch (players.size()) {
                case 2:
                    messageManager.sendGameMessage(arena, game, "MultipleKill", "Double");
                    break;
                case 3:
                    messageManager.sendGameMessage(arena, game, "MultipleKill", "Triple");
                    break;
                case 4:
                    messageManager.sendGameMessage(arena, game, "MultipleKill", "Ultra");
                    break;
                default:

            }
            if (scoreBoard.getScore(playerName) >= WIN_THRESHOLD) {
                ultimateGames.getArenaManager().endArena(arena);
            } else {
                player.setExp(EXP_MIN);
                startCooldown(player);
            }
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
        ItemStack railgun = new ItemStack(Material.BLAZE_ROD);
        ItemMeta railgunMeta = railgun.getItemMeta();
        railgunMeta.setDisplayName("Railgun");
        railgun.setItemMeta(railgunMeta);
        player.getInventory().addItem(railgun, UGUtils.createInstructionBook(game));
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

    public void startCooldown(Player player) {
        final String playerName = player.getName();
        reloaders.add(playerName);
        reloadTasks.put(player.getName(), Bukkit.getScheduler().scheduleSyncRepeatingTask(ultimateGames, new Runnable() {
            @Override
            public void run() {
                Player player = Bukkit.getPlayerExact(playerName);
                if (reloaders.contains(playerName)) {
                    float newExp = player.getExp() + EXP_INCREMENT;
                    if (newExp >= EXP_MAX) {
                        player.setExp(EXP_MAX);
                        endCooldown(player);
                    } else {
                        player.setExp(player.getExp() + EXP_INCREMENT);
                    }
                }
            }
        }, 0, 2L));
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
}
