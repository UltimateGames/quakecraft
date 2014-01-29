package com.greatmancode.quakecraft;

import me.ampayne2.ultimategames.api.UltimateGames;
import me.ampayne2.ultimategames.api.arenas.Arena;
import me.ampayne2.ultimategames.api.arenas.scoreboards.Scoreboard;
import me.ampayne2.ultimategames.api.webapi.WebHandler;
import me.ampayne2.ultimategames.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class QuakecraftWebHandler implements WebHandler {
    private Arena arena;
    private UltimateGames ug;

    public QuakecraftWebHandler(UltimateGames ug, Arena arena) {
        this.arena = arena;
        this.ug = ug;
    }

    @Override
    public String sendResult() {
        Gson gson = new Gson();
        Map<String, Integer> map = new HashMap<>();

        Scoreboard scoreBoard = ug.getScoreboardManager().getScoreboard(arena);
        if (scoreBoard != null) {
            for (String playerName : arena.getPlayers()) {
                map.put(playerName, scoreBoard.getScore(playerName));
            }
        }

        return gson.toJson(map);
    }
}
