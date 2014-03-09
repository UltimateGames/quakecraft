package com.greatmancode.quakecraft;

import me.ampayne2.ultimategames.api.UltimateGames;
import me.ampayne2.ultimategames.api.games.Game;
import me.ampayne2.ultimategames.api.message.Message;
import me.ampayne2.ultimategames.api.players.ArenaPlayer;
import me.ampayne2.ultimategames.api.players.streaks.StreakAction;

public class GibStreakAction extends StreakAction {
    private UltimateGames ultimateGames;
    private Game game;
    private Message message;

    public GibStreakAction(UltimateGames ultimateGames, Game game, int requiredKills, Message message) {
        super(requiredKills);
        this.ultimateGames = ultimateGames;
        this.game = game;
        this.message = message;
    }

    @Override
    public void perform(ArenaPlayer player) {
        ultimateGames.getMessenger().sendGameMessage(player.getArena(), game, message, player.getPlayerName());
        ultimateGames.getPointManager().addPoint(game, player.getPlayerName(), "store", 1);
    }
}
