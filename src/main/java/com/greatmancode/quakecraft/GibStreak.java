package com.greatmancode.quakecraft;

import me.ampayne2.ultimategames.UltimateGames;
import me.ampayne2.ultimategames.games.Game;
import me.ampayne2.ultimategames.players.ArenaPlayer;
import me.ampayne2.ultimategames.players.streaks.Streak;

public class GibStreak extends Streak {
	public GibStreak(UltimateGames ultimateGames, Game game, ArenaPlayer player) {
		super(player, new GibStreakAction(ultimateGames, game, 5, "KillingSpree"),
				new GibStreakAction(ultimateGames, game, 10, "Rampage"),
				new GibStreakAction(ultimateGames, game, 15, "Domination"),
				new GibStreakAction(ultimateGames, game, 20, "Unstoppable"),
				new GibStreakAction(ultimateGames, game, 25, "God"));
	}
}
