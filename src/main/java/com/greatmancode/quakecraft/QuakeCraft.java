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
import me.ampayne2.ultimategames.enums.ArenaStatus;
import me.ampayne2.ultimategames.games.Game;
import me.ampayne2.ultimategames.players.PlayerManager;
import me.ampayne2.ultimategames.scoreboards.ArenaScoreboard;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
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
    private Integer LEVEL_MIN = 0;
    private Float EXP_MAX = 1.0F;
    private Float EXP_MIN = 0.0F;
    private Float EXP_INCREMENT = 0.1F;
    private Integer WIN_THRESHOLD = 25;

    @Override
    public Boolean loadGame(UltimateGames ultimateGames, Game game) {
        this.ultimateGames = ultimateGames;
        this.game = game;
        WIN_THRESHOLD = ultimateGames.getConfigManager().getGameConfig(game).getConfig().getInt("CustomValues.MaxKills");
        return true;
    }

    @Override
    public void unloadGame() {

    }

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
            ultimateGames.getMessageManager().broadcastReplacedGameMessage(game, "GameEnd", highestScorer, game.getName(), arena.getName());
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
        if (arena.getPlayers().size() >= arena.getMinPlayers() && !ultimateGames.getCountdownManager().isStartingCountdownEnabled(arena)) {
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
        String playerName = player.getName();
        if (reloaders.contains(playerName)) {
            reloaders.remove(playerName);
        }
        if (arena.getStatus() == ArenaStatus.RUNNING && arena.getPlayers().size() <= 2) {
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
        player.getInventory().addItem(ultimateGames.getUtils().createInstructionBook(game));
        player.getInventory().setArmorContents(null);
        player.updateInventory();
        return true;
    }

    @Override
    public void removeSpectator(Player player, Arena arena) {

    }

    @Override
    public void onPlayerDeath(Arena arena, PlayerDeathEvent event) {
        event.getDrops().clear();
        ultimateGames.getUtils().autoRespawn(event.getEntity());
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
    public void onPlayerInteract(Arena arena, PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR || event.getMaterial() != Material.BLAZE_ROD) {
            return;
        }
        Player player = event.getPlayer();
        String playerName = player.getName();
        if (!reloaders.contains(playerName)) {
            PlayerManager playerManager = ultimateGames.getPlayerManager();
            Message messageManager = ultimateGames.getMessageManager();
            ArenaScoreboard scoreBoard = ultimateGames.getScoreboardManager().getArenaScoreboard(playerManager.getPlayerArena(playerName));
            Collection<Entity> players = ultimateGames.getUtils().getEntityTargets(player, 100, 0, false, true, true);
            for (Entity entity : players) {
                if (entity instanceof Player) {
                    Player targetedPlayer = (Player) entity;
                    String targetedPlayerName = targetedPlayer.getName();
                    if (playerManager.isPlayerInArena(targetedPlayerName) && playerManager.getPlayerArena(targetedPlayerName).equals(arena)) {
                        targetedPlayer.setHealth(0.0);
                        messageManager.broadcastReplacedGameMessageToArena(game, arena, "Gib", playerName, targetedPlayerName);
                        if (scoreBoard != null) {
                            scoreBoard.setScore(playerName, scoreBoard.getScore(playerName) + 1);
                        }
                    }
                }
            }
            switch (players.size()) {
                case 2:
                    messageManager.broadcastReplacedGameMessageToArena(game, arena, "MultipleKill", "Double");
                    break;
                case 3:
                    messageManager.broadcastReplacedGameMessageToArena(game, arena, "MultipleKill", "Triple");
                    break;
                case 4:
                    messageManager.broadcastReplacedGameMessageToArena(game, arena, "MultipleKill", "Ultra");
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
        player.getInventory().addItem(railgun, ultimateGames.getUtils().createInstructionBook(game));
        player.updateInventory();
        reloaders.add(playerName);
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
