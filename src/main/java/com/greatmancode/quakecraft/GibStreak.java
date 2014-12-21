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
