package com.greatmancode.quakecraft;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.ampayne2.ultimategames.UltimateGames;
import me.ampayne2.ultimategames.api.ArenaScoreboard;
import me.ampayne2.ultimategames.api.GamePlugin;
import me.ampayne2.ultimategames.arenas.Arena;
import me.ampayne2.ultimategames.enums.ArenaStatus;
import me.ampayne2.ultimategames.games.Game;
import me.ampayne2.ultimategames.players.SpawnPoint;
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

	private Map<String, Float> reloadList = new HashMap<String, Float>();
	private Map<String, Integer> reloadTasks = new HashMap<String, Integer>();
	private Integer LEVEL_MIN = 0;
	private Float EXP_MAX = 1.0F;
	private Float EXP_MIN = 0.0F;
	private Float EXP_INCREMENT = 0.1F;
	private String DEFAULT_WINNER = "Nobody";
	private Integer DEFAULT_SCORE = 0;
	private Integer WIN_THRESHOLD = 25;

	@Override
	public Boolean loadGame(UltimateGames ultimateGames, Game game) {
		this.ultimateGames = ultimateGames;
		this.game = game;
		return true;
	}

	@Override
	public Boolean unloadGame() {
		return true;
	}

	@Override
	public Boolean stopGame() {
		return true;
	}

	@Override
	public Boolean loadArena(Arena arena) {
		return true;
	}

	@Override
	public Boolean unloadArena(Arena arena) {
		reloadList.remove(arena);
		return true;
	}

	@Override
	public Boolean isStartPossible(Arena arena) {
		if (arena.getStatus() == ArenaStatus.OPEN) {
			return true;
		}
		return false;
	}

	@Override
	public Boolean startArena(Arena arena) {
		return true;
	}

	@Override
	public Boolean beginArena(Arena arena) {
		ultimateGames.getCountdownManager().createEndingCountdown(arena, 1200, false); //Prevent people from locking arenas
		for (ArenaScoreboard scoreBoard : new ArrayList<ArenaScoreboard>(ultimateGames.getScoreboardManager().getArenaScoreboards(arena))) {
			ultimateGames.getScoreboardManager().removeArenaScoreboard(arena, scoreBoard.getName());
		}
		ArenaScoreboard scoreBoard = ultimateGames.getScoreboardManager().createArenaScoreboard(arena, "Gibs");
		for (String playerName : arena.getPlayers()) {
			scoreBoard.addPlayer(playerName);
			scoreBoard.setScore(playerName, 0);
		}
		scoreBoard.setVisible(true);
		return true;
	}

	@Override
	public void endArena(Arena arena) {
		String highestScorer = DEFAULT_WINNER;
		Integer highScore = DEFAULT_SCORE;
		List<String> players = arena.getPlayers();
		for (ArenaScoreboard scoreBoard : ultimateGames.getScoreboardManager().getArenaScoreboards(arena)) {
			if (scoreBoard.getName().equals("Gibs")) {
				for (String playerName : players) {
					Integer playerScore = scoreBoard.getScore(playerName);
					if (playerScore > highScore) {
						highestScorer = playerName;
						highScore = playerScore;
					}
				}
			}
		}
		ultimateGames.getScoreboardManager().removeArenaScoreboard(arena, "Gibs");
		ultimateGames.getMessageManager().broadcastReplacedGameMessage(game, "GameEnd", highestScorer, game.getGameDescription().getName(), arena.getName());
		for (String playerName : players) {
			ultimateGames.getPlayerManager().removePlayerFromArena(playerName, arena, false);
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
	public Boolean addPlayer(Arena arena, String playerName) {
		if (arena.getPlayers().size() >= arena.getMinPlayers() && !ultimateGames.getCountdownManager().isStartingCountdownEnabled(arena)) {
			ultimateGames.getCountdownManager().createStartingCountdown(arena, ultimateGames.getConfigManager().getGameConfig(game).getConfig().getInt("CustomValues.StartWaitTime"));
		}
		SpawnPoint spawnPoint = ultimateGames.getSpawnpointManager().getRandomSpawnPoint(arena);
		spawnPoint.lock(false);
		spawnPoint.teleportPlayer(playerName);
		Player player = Bukkit.getPlayer(playerName);
		resetInventory(arena, player);
		return true;
	}

	@Override
	public Boolean removePlayer(Arena arena, String playerName) {
		if (reloadList.containsKey(playerName)) {
			reloadList.remove(playerName);
		}
		return true;
	}
	
	@Override
	public void onPlayerDeath(Arena arena, PlayerDeathEvent event) {
		event.getDrops().clear();
		ultimateGames.getUtils().autoRespawn(event.getEntity());
	}
	
	@Override
	public void onPlayerRespawn(Arena arena, PlayerRespawnEvent event) {
		event.setRespawnLocation(ultimateGames.getSpawnpointManager().getRandomSpawnPoint(arena).getLocation());
		resetInventory(arena, event.getPlayer());
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
		if (reloadList.containsKey(playerName) && reloadList.get(playerName) >= EXP_MAX) {
			List<Entity> players = ultimateGames.getUtils().getEntityTargets(player, 100, true, true);
			for (Entity entity : players) {
				if (entity instanceof Player) {
					Player targetedPlayer = (Player) entity;
					String targetedPlayerName = targetedPlayer.getName();
					if (ultimateGames.getPlayerManager().isPlayerInArena(targetedPlayerName) && ultimateGames.getPlayerManager().getPlayerArena(targetedPlayerName).equals(ultimateGames.getPlayerManager().getPlayerArena(playerName))) {
						targetedPlayer.setHealth(0.0);
						for (ArenaScoreboard scoreBoard : ultimateGames.getScoreboardManager().getArenaScoreboards(ultimateGames.getPlayerManager().getPlayerArena(playerName))) {
							if (scoreBoard.getName().equals("Gibs")) {
								scoreBoard.setScore(playerName, scoreBoard.getScore(playerName) + 1);
								if (scoreBoard.getScore(playerName) >= WIN_THRESHOLD) {
									ultimateGames.getArenaManager().endArena(arena);
								}
							}
						}
					}
				}
			}
			reloadList.put(playerName, EXP_MIN);
			player.setExp(EXP_MIN);
			startCooldown(player);
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
	private void resetInventory(Arena arena, final Player player) {
		player.getInventory().clear();
		ItemStack railgun = new ItemStack(Material.BLAZE_ROD);
		ItemMeta railgunMeta = railgun.getItemMeta();
		railgunMeta.setDisplayName("Railgun");
		railgun.setItemMeta(railgunMeta);
		player.getInventory().addItem(railgun);
		player.getInventory().addItem(ultimateGames.getUtils().createInstructionBook(arena.getGame()));
		player.updateInventory();
		reloadList.put(player.getName(), EXP_MAX);
		player.setLevel(LEVEL_MIN);
		player.setExp(EXP_MAX);
		Bukkit.getScheduler().scheduleSyncDelayedTask(ultimateGames, new Runnable() {
			@Override
			public void run() {
				player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 6000, 1));
				player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 6000, 1));
			}
		}, 40L);
	}
	
	public void startCooldown(final Player player) {
		reloadTasks.put(player.getName(), Bukkit.getScheduler().scheduleSyncRepeatingTask(ultimateGames, new Runnable() {
			@Override
			public void run() {
				String playerName = player.getName();
				if (reloadList.containsKey(playerName)) {
					Float newExp = reloadList.get(playerName) + EXP_INCREMENT;
					reloadList.put(playerName, newExp);
					player.setExp(newExp);
					if (newExp >= EXP_MAX) {
						endCooldown(playerName);
					}
				}
			}
		}, 0, 2L));
	}
	
	public void endCooldown(String playerName) {
		if (reloadTasks.containsKey(playerName)) {
			Bukkit.getScheduler().cancelTask(reloadTasks.get(playerName));
			reloadTasks.remove(playerName);
		}
	}
}
