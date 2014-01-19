package com.greatmancode.quakecraft;

import me.ampayne2.ultimategames.UltimateGames;
import me.ampayne2.ultimategames.arenas.Arena;
import me.ampayne2.ultimategames.games.Game;
import me.ampayne2.ultimategames.players.ArenaPlayer;
import me.ampayne2.ultimategames.players.streaks.Streak;

public class GibStreak extends Streak {
    private UltimateGames ultimateGames;
    private Game game;

	public GibStreak(UltimateGames ultimateGames, Game game, ArenaPlayer player) {
		super(player, new GibStreakAction(ultimateGames, game, 5, "KillingSpree"),
				new GibStreakAction(ultimateGames, game, 10, "Rampage"),
				new GibStreakAction(ultimateGames, game, 15, "Domination"),
				new GibStreakAction(ultimateGames, game, 20, "Unstoppable"),
				new GibStreakAction(ultimateGames, game, 25, "God"));
        this.ultimateGames = ultimateGames;
        this.game = game;
	}

    @Override
    public void reset() {
        if (getCount() >= 5) {
            String playerName = getPlayer().getPlayerName();
            Arena arena = ultimateGames.getPlayerManager().getPlayerArena(playerName);
            if (arena != null) {
                ultimateGames.getMessenger().sendGameMessage(arena, game, "Shutdown", playerName);
            }
        }
        super.reset();
    }
}
