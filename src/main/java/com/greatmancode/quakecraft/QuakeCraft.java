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
import me.ampayne2.ultimategames.enums.SignType;
import me.ampayne2.ultimategames.games.Game;
import me.ampayne2.ultimategames.players.SpawnPoint;
import me.ampayne2.ultimategames.signs.UGSign;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
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
	private Float EXP_MAX = 1.0F;
	private Float EXP_INCREMENT = 0.1F;

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
	public Boolean endArena(Arena arena) {
		String highestScorer = "Nobody";
		Integer highScore = 0;
		List<String> players = arena.getPlayers();
		for (ArenaScoreboard scoreBoard : new ArrayList<ArenaScoreboard>(ultimateGames.getScoreboardManager().getArenaScoreboards(arena))) {
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
		ultimateGames.getArenaManager().openArena(arena);
		return true;
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
		reloadList.put(playerName, EXP_MAX);
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
	public Boolean onArenaCommand(Arena arena, String s, CommandSender commandSender, String[] strings) {
		return true;
	}

	@Override
	public void handleUGSignCreate(UGSign ugSign, SignType signType) {

	}

	@Override
	public void handleInputSignTrigger(UGSign ugSign, SignType signType, Event event) {

	}

	@SuppressWarnings("deprecation")
	private void resetInventory(Arena arena, final Player player) {
		player.getInventory().clear();
		ItemStack railgun = new ItemStack(Material.BREWING_STAND_ITEM);
		ItemMeta railgunMeta = railgun.getItemMeta();
		railgunMeta.setDisplayName("Railgun");
		railgun.setItemMeta(railgunMeta);
		player.getInventory().addItem(railgun);
		player.getInventory().addItem(ultimateGames.getUtils().createInstructionBook(arena.getGame()));
		player.updateInventory();
		player.setExp(EXP_MAX);
		Bukkit.getScheduler().scheduleSyncDelayedTask(ultimateGames, new Runnable() {
			@Override
			public void run() {
				player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 6000, 1));
				player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 6000, 1));
			}
		}, 40L);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (!(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) || event.getItem().getType() != Material.BREWING_STAND_ITEM) {
			return;
		}
		Player player = event.getPlayer();
		String playerName = player.getName();
		if (ultimateGames.getPlayerManager().isPlayerInArena(playerName) && ultimateGames.getPlayerManager().getPlayerArena(playerName).getGame().equals(game) && !reloadList.containsKey(playerName) && player.getExp() >= EXP_MAX) {
			List<Entity> players = ultimateGames.getUtils().getEntityTargets(player, 100, true, true);
			for (Entity entity : players) {
				if (entity instanceof Player) {
					Player targetedPlayer = (Player) entity;
					targetedPlayer.setHealth(0.0);
					for (ArenaScoreboard scoreBoard : ultimateGames.getScoreboardManager().getArenaScoreboards(ultimateGames.getPlayerManager().getPlayerArena(playerName))) {
						if (scoreBoard.getName().equals("Gibs")) {
							scoreBoard.setScore(playerName, scoreBoard.getScore(playerName) + 1);
						}
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerDamage(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player) {
			String playerName = ((Player) event.getEntity()).getName();
			if (ultimateGames.getPlayerManager().isPlayerInArena(playerName) && ultimateGames.getPlayerManager().getPlayerArena(playerName).getGame().equals(game)) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
		String playerName = player.getName();
		if (ultimateGames.getPlayerManager().isPlayerInArena(playerName)) {
			Arena arena = ultimateGames.getPlayerManager().getPlayerArena(playerName);
			if (!arena.getGame().equals(game)) {
				return;
			}
			event.getDrops().clear();
			ultimateGames.getUtils().autoRespawn(player);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		if (ultimateGames.getPlayerManager().isPlayerInArena(event.getPlayer().getName())) {
			Arena arena = ultimateGames.getPlayerManager().getPlayerArena(event.getPlayer().getName());
			if (!arena.getGame().equals(game)) {
				return;
			}
			if (arena.getStatus().equals(ArenaStatus.RUNNING)) {
				event.setRespawnLocation(ultimateGames.getSpawnpointManager().getRandomSpawnPoint(arena).getLocation());
				resetInventory(arena, event.getPlayer());
			}
		}
	}
	
	public void startCooldown(final String playerName) {
		reloadTasks.put(playerName, Bukkit.getScheduler().scheduleSyncRepeatingTask(ultimateGames, new Runnable() {
			@Override
			public void run() {
				if (reloadList.containsKey(playerName)) {
					reloadList.put(playerName, reloadList.get(playerName) + EXP_INCREMENT);
					if (reloadList.get(playerName) == EXP_MAX) {
						reloadList.remove(playerName);
						endCooldown(playerName);
					}
				}
			}
		}, 0, 2L));
	}
	
	public void endCooldown(String playerName) {
		if (reloadTasks.containsKey(playerName)) {
			Bukkit.getScheduler().cancelTask(reloadTasks.get(playerName));
		}
	}
}
