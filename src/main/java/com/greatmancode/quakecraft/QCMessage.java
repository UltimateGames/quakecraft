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

import me.ampayne2.ultimategames.api.message.Message;

public enum QCMessage implements Message {
    GAME_END("GameEnd", "%s won %s on arena %s!"),
    GIB("Kill", "%s gibbed %s!"),
    MULTIPLE_KILL("MultipleKill", "&c%s kill!"),
    KILLING_SPREE("KillingSpree", "&b%s is on a Killing Spree!"),
    RAMPAGE("Rampage", "&b%s is on a Rampage!"),
    DOMINATION("Domination", "&b%s is Dominating!"),
    UNSTOPPABLE("Unstoppable", "&b%s is Unstoppable!"),
    GOD("God", "&b%s is Godlike!"),
    SHUTDOWN("Shutdown", "&b%s was Shut Down!");

    private String message;
    private final String path;
    private final String defaultMessage;

    private QCMessage(String path, String defaultMessage) {
        this.path = path;
        this.defaultMessage = defaultMessage;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String getDefault() {
        return defaultMessage;
    }

    @Override
    public String toString() {
        return message;
    }
}
