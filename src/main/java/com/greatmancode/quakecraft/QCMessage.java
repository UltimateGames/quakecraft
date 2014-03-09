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
