package com.greatmancode.quakecraft;

import me.ampayne2.ultimategames.api.UltimateGames;
import me.ampayne2.ultimategames.api.games.Game;
import me.ampayne2.ultimategames.api.players.ArenaPlayer;
import me.ampayne2.ultimategames.api.players.streaks.StreakAction;

public class GibStreakAction extends StreakAction {
    private UltimateGames ultimateGames;
    private Game game;
    private String messagePath;

    public GibStreakAction(UltimateGames ultimateGames, Game game, int requiredKills, String messagePath) {
        super(requiredKills);
        this.ultimateGames = ultimateGames;
        this.game = game;
        this.messagePath = messagePath;
    }

    @Override
    public void perform(ArenaPlayer player) {
        ultimateGames.getMessenger().sendGameMessage(player.getArena(), game, messagePath, player.getPlayerName());
        ultimateGames.getPointManager().addPoint(game, player.getPlayerName(), "store", 1);
    }
}
