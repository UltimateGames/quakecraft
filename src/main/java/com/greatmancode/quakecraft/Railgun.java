package com.greatmancode.quakecraft;

import me.ampayne2.ultimategames.UltimateGames;
import me.ampayne2.ultimategames.arenas.Arena;
import me.ampayne2.ultimategames.arenas.ArenaStatus;
import me.ampayne2.ultimategames.arenas.scoreboards.ArenaScoreboard;
import me.ampayne2.ultimategames.effects.GameSound;
import me.ampayne2.ultimategames.games.Game;
import me.ampayne2.ultimategames.games.items.GameItem;
import me.ampayne2.ultimategames.message.Message;
import me.ampayne2.ultimategames.players.PlayerManager;
import me.ampayne2.ultimategames.utils.UGUtils;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;

public class Railgun extends GameItem {
    private final UltimateGames ultimateGames;
    private final Game game;
    private final QuakeCraft quakeCraft;
    private static final GameSound SHOOT_SOUND = new GameSound(Sound.BLAZE_HIT, 1, 2);
    private static final GameSound KILL_SOUND = new GameSound(Sound.EXPLODE, 2, 1);
    private static final int WIN_THRESHOLD = 25;

    public Railgun(UltimateGames ultimateGames, Game game, QuakeCraft quakeCraft, ItemStack itemStack) {
        super(itemStack, false);
        this.ultimateGames = ultimateGames;
        this.game = game;
        this.quakeCraft = quakeCraft;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean click(Arena arena, PlayerInteractEvent event) {
        if (arena.getStatus() != ArenaStatus.RUNNING || event.getAction() != Action.RIGHT_CLICK_AIR) {
            return false;
        }
        Player player = event.getPlayer();
        String playerName = player.getName();
        if (!quakeCraft.isPlayerReloading(playerName)) {
            PlayerManager playerManager = ultimateGames.getPlayerManager();
            Message messageManager = ultimateGames.getMessageManager();
            ArenaScoreboard scoreBoard = ultimateGames.getScoreboardManager().getArenaScoreboard(playerManager.getPlayerArena(playerName));
            Collection<LivingEntity> players = UGUtils.getLivingEntityTargets(player, 100, 0, false, true, true);
            SHOOT_SOUND.play(player.getEyeLocation());
            int playersShot = 0;
            GibStreak playerStreak = quakeCraft.getStreak(playerName);
            for (LivingEntity entity : players) {
                if (entity instanceof Player) {
                    Player targetedPlayer = (Player) entity;
                    String targetedPlayerName = targetedPlayer.getName();
                    if (targetedPlayer.getHealth() > 0.0 && playerManager.isPlayerInArena(targetedPlayerName) && playerManager.getPlayerArena(targetedPlayerName).equals(arena)) {
                        targetedPlayer.getInventory().clear();
                        targetedPlayer.updateInventory();
                        quakeCraft.endCooldown(targetedPlayer);
                        targetedPlayer.setHealth(0.0);
                        KILL_SOUND.play(targetedPlayer.getLocation());
                        messageManager.sendGameMessage(arena, game, "Gib", playerName, targetedPlayerName);
                        ultimateGames.getPointManager().addPoint(game, playerName, "kill", 1);
                        ultimateGames.getPointManager().addPoint(game, playerName, "store", 1);
                        ultimateGames.getPointManager().addPoint(game, targetedPlayerName, "death", 1);
                        if (scoreBoard != null) {
                            scoreBoard.setScore(playerName, scoreBoard.getScore(playerName) + 1);
                        }
                        playerStreak.increaseCount();
                        playersShot++;
                    }
                }
            }
            switch (playersShot) {
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
                quakeCraft.startCooldown(player);
            }
        }
        return true;
    }
}
