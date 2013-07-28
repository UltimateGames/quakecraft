package com.greatmancode.quakecraft;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import me.ampayne2.UltimateGames.API.ArenaScoreboard;
import me.ampayne2.UltimateGames.API.GamePlugin;
import me.ampayne2.UltimateGames.Arenas.Arena;
import me.ampayne2.UltimateGames.Enums.ArenaStatus;
import me.ampayne2.UltimateGames.Games.Game;
import me.ampayne2.UltimateGames.Players.SpawnPoint;
import me.ampayne2.UltimateGames.UltimateGames;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.entity.SmallFireball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class QuakeCraft extends GamePlugin {

	private UltimateGames ultimateGames;
	private Game game;

	private Map<Arena, HashMap<String, Long>> reloadList = new HashMap<Arena, HashMap<String, Long>>();

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
		reloadList.put(arena, new HashMap<String, Long>());
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
		ultimateGames.getCountdownManager().createEndingCountdown(arena, 1200, true); //Prevent people from locking arenas
		for (ArenaScoreboard scoreBoard : new ArrayList<ArenaScoreboard>(ultimateGames.getScoreboardManager().getArenaScoreboards(arena))) {
			ultimateGames.getScoreboardManager().removeArenaScoreboard(arena, scoreBoard.getName());
		}
		ArenaScoreboard scoreBoard = ultimateGames.getScoreboardManager().createArenaScoreboard(arena, "Kills");
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
		for (ArenaScoreboard scoreBoard : new ArrayList<ArenaScoreboard>(ultimateGames.getScoreboardManager().getArenaScoreboards(arena))) {
			if (scoreBoard.getName().equals("Kills")) {
				for (String playerName : new ArrayList<String>(arena.getPlayers())) {
					Integer playerScore = scoreBoard.getScore(playerName);
					if (playerScore > highScore) {
						highestScorer = playerName;
						highScore = playerScore;
					}
					ultimateGames.getPlayerManager().removePlayerFromArena(playerName, arena, false);
				}
			}
		}
		ultimateGames.getScoreboardManager().removeArenaScoreboard(arena, "Kills");
		ultimateGames.getMessageManager().broadcastReplacedGameMessage(game, "GameEnd", highestScorer, Integer.toString(highScore));
		if (ultimateGames.getCountdownManager().isStartingCountdownEnabled(arena)) {
			ultimateGames.getCountdownManager().stopStartingCountdown(arena);
		}
		if (ultimateGames.getCountdownManager().isEndingCountdownEnabled(arena)) {
			ultimateGames.getCountdownManager().stopEndingCountdown(arena);
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
		resetInventory(player);
		return true;
	}

	@Override
	public Boolean removePlayer(Arena arena, String playerName) {
		if (ultimateGames.getCountdownManager().isStartingCountdownEnabled(arena) && arena.getPlayers().size() <= arena.getMinPlayers()) {
			ultimateGames.getCountdownManager().stopStartingCountdown(arena);
		}
		for (ArenaScoreboard scoreBoard : ultimateGames.getScoreboardManager().getArenaScoreboards(arena)) {
			if (scoreBoard.getName().equals("Kills")) {
				scoreBoard.removePlayer(playerName);
			}
		}
		return true;
	}

	@Override
	public Boolean onArenaCommand(Arena arena, String s, CommandSender commandSender, String[] strings) {
		return true;
	}

	@Override
	public void handleInputSignCreate(Arena arena, Sign sign, String s) {

	}

	@Override
	public void handleInputSignClick(Arena arena, Sign sign, String s, PlayerInteractEvent playerInteractEvent) {

	}

	@SuppressWarnings("deprecation")
	private void resetInventory(final Player player) {
		player.getInventory().clear();
		player.getInventory().addItem(new ItemStack(Material.WOOD_HOE, 1));
		String playerName = player.getName();
		if (ultimateGames.getPlayerManager().isPlayerInArena(playerName)) {
			Arena arena = ultimateGames.getPlayerManager().getPlayerArena(playerName);
			player.getInventory().addItem(ultimateGames.getUtils().createInstructionBook(arena.getGame()));
		}
		player.updateInventory();
		Bukkit.getScheduler().scheduleSyncDelayedTask(ultimateGames, new Runnable() {
			@Override
			public void run() {
				player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 6000, 1));
			}
		}, 40L);


	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerInteract(PlayerInteractEvent event) {
		String playerName = event.getPlayer().getName();
		if (ultimateGames.getPlayerManager().isPlayerInArena(playerName)) {
			Arena arena = ultimateGames.getPlayerManager().getPlayerArena(playerName);
			if (!arena.getGame().equals(game)) {
				return;
			}
			if (event.getPlayer().getItemInHand().getType().equals(Material.WOOD_HOE)) {
				if (reloadList.get(arena).containsKey(playerName)) {
					Long reloadTime = reloadList.get(arena).get(playerName);
					if (System.currentTimeMillis() - reloadTime < 1000) { //1s reload
						return;
					}
				}
				Player p = event.getPlayer();

				event.getPlayer().launchProjectile(SmallFireball.class).setVelocity(p.getLocation().getDirection().multiply(3));
				reloadList.get(arena).put(playerName, System.currentTimeMillis());
			}
		}
	}

	@EventHandler(priority=EventPriority.HIGHEST)
	public void onExplosion(EntityExplodeEvent event) {
		if (ultimateGames.getArenaManager().isLocationInArena(event.getLocation()) && ultimateGames.getArenaManager().getLocationArena(event.getLocation()).getGame().equals(game)) {
			event.blockList().clear();
		}
	}

	@EventHandler(priority=EventPriority.HIGHEST)
	public void onBlockIgnite(BlockIgniteEvent event) {
		if (event.getCause().equals(BlockIgniteEvent.IgniteCause.FIREBALL) && ultimateGames.getArenaManager().isLocationInArena(event.getBlock().getLocation()) && ultimateGames.getArenaManager().getLocationArena(event.getBlock().getLocation()).getGame().equals(game)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerDamageByPlayer(EntityDamageByEntityEvent event) {
		if (!(event.getEntity() instanceof Player)) {
			return;
		}
		Player damaged = (Player) event.getEntity();
		Entity damager = event.getDamager();

		if (ultimateGames.getPlayerManager().isPlayerInArena(damaged.getName())) {
			Arena arena = ultimateGames.getPlayerManager().getPlayerArena(damaged.getName());
			if (!arena.getGame().equals(game)) {
				return;
			}

			if (!(damager instanceof SmallFireball)) {
				event.setCancelled(true);
				return;
			}


			String killerName = ((Player)((Fireball) damager).getShooter()).getName();
			if (killerName.equals(damaged.getName())) {
				event.setCancelled(true);
				return;
			}
			damaged.setHealth(0);
			for (ArenaScoreboard scoreBoard : ultimateGames.getScoreboardManager().getArenaScoreboards(arena)) {
				if (scoreBoard.getName().equals("Kills") && killerName != null) {
					scoreBoard.setScore(killerName, scoreBoard.getScore(killerName) + 1);
					if (scoreBoard.getScore(killerName) == ultimateGames.getConfigManager().getGameConfig(game).getConfig().get("CustomValues.MaxKills")) {
						ultimateGames.getArenaManager().endArena(arena);
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
		String playerName = ((Player) event.getEntity()).getName();
		if (ultimateGames.getPlayerManager().isPlayerInArena(playerName)) {
			Arena arena = ultimateGames.getPlayerManager().getPlayerArena(playerName);
			if (!arena.getGame().equals(game)) {
				return;
			}

			String killerName = null;
			Player killer = event.getEntity().getKiller();
			if (killer != null) {
				killerName = killer.getName();
				if (ultimateGames.getPlayerManager().isPlayerInArena(killer.getName()) && ultimateGames.getPlayerManager().getPlayerArena(killer.getName()).equals(arena)) {
					killer.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 10, 2));
				}
			}

			event.getDrops().clear();
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		if (ultimateGames.getPlayerManager().isPlayerInArena(event.getPlayer().getName())) {
			Arena arena = ultimateGames.getPlayerManager().getPlayerArena(event.getPlayer().getName());
			if (!arena.getGame().equals(game)) {
				return;
			}
			event.setRespawnLocation(ultimateGames.getSpawnpointManager().getRandomSpawnPoint(arena).getLocation());
			resetInventory(event.getPlayer());
		}
	}
}
