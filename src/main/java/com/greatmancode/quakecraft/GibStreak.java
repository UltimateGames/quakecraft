package com.greatmancode.quakecraft;

import me.ampayne2.ultimategames.api.UltimateGames;
import me.ampayne2.ultimategames.api.arenas.Arena;
import me.ampayne2.ultimategames.api.games.Game;
import me.ampayne2.ultimategames.api.players.ArenaPlayer;
import me.ampayne2.ultimategames.api.players.streaks.Streak;

public class GibStreak extends Streak {
    private UltimateGames ultimateGames;
    private Game game;

    public GibStreak(UltimateGames ultimateGames, Game game, ArenaPlayer player) {
        super(player, new GibStreakAction(ultimateGames, game, 5, QCMessage.KILLING_SPREE),
                new GibStreakAction(ultimateGames, game, 10, QCMessage.RAMPAGE),
                new GibStreakAction(ultimateGames, game, 15, QCMessage.DOMINATION),
                new GibStreakAction(ultimateGames, game, 20, QCMessage.UNSTOPPABLE),
                new GibStreakAction(ultimateGames, game, 25, QCMessage.GOD));

        this.ultimateGames = ultimateGames;
        this.game = game;
    }

    @Override
    public void reset() {
        if (getCount() >= 5) {
            String playerName = getPlayer().getPlayerName();
            Arena arena = ultimateGames.getPlayerManager().getPlayerArena(playerName);
            if (arena != null) {
                ultimateGames.getMessenger().sendGameMessage(arena, game, QCMessage.SHUTDOWN, playerName);
            }
        }
        super.reset();
    }
}
