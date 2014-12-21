/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013-2014, UltimateGames Staff <https://github.com/UltimateGames//>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.greatmancode.quakecraft;

import me.ampayne2.ultimategames.api.UltimateGames;
import me.ampayne2.ultimategames.api.arenas.Arena;
import me.ampayne2.ultimategames.api.arenas.ArenaStatus;
import me.ampayne2.ultimategames.api.arenas.scoreboards.Scoreboard;
import me.ampayne2.ultimategames.api.games.Game;
import me.ampayne2.ultimategames.api.games.items.GameItem;
import me.ampayne2.ultimategames.api.message.Messenger;
import me.ampayne2.ultimategames.api.players.PlayerManager;
import me.ampayne2.ultimategames.api.utils.UGUtils;
import ninja.amp.ampeffects.effects.sounds.SoundEffect;
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
    private static final SoundEffect SHOOT_SOUND = new SoundEffect(Sound.BLAZE_HIT, 1, 2);
    private static final SoundEffect KILL_SOUND = new SoundEffect(Sound.EXPLODE, 2, 1);
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
            Messenger messageManager = ultimateGames.getMessenger();
            Scoreboard scoreBoard = ultimateGames.getScoreboardManager().getScoreboard(playerManager.getPlayerArena(playerName));
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
                        messageManager.sendGameMessage(arena, game, QCMessage.GIB, playerName, targetedPlayerName);
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
                    messageManager.sendGameMessage(arena, game, QCMessage.MULTIPLE_KILL, "Double");
                    break;
                case 3:
                    messageManager.sendGameMessage(arena, game, QCMessage.MULTIPLE_KILL, "Triple");
                    break;
                case 4:
                    messageManager.sendGameMessage(arena, game, QCMessage.MULTIPLE_KILL, "Ultra");
                    break;
                default:

            }
            if (scoreBoard != null && scoreBoard.getScore(playerName) >= WIN_THRESHOLD) {
                ultimateGames.getArenaManager().endArena(arena);
            } else {
                quakeCraft.startCooldown(player);
            }
        }
        return true;
    }
}
