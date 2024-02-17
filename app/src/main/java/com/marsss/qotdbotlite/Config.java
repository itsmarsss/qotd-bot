package com.marsss.qotdbotlite;

import java.awt.Color;

public class Config {
    private String botToken;
    private String serverID;
    private String channelID;
    private String prefix;

    private String managerRoleID;

    private String QOTDColor = "E62127";

    private Color color = Color.decode("#E62127");

    private boolean initialized;

    public Config() {
    }

    public String getBotToken() {
        return botToken;
    }

    public void setBotToken(String botToken) {
        this.botToken = botToken;
    }

    public String getServerID() {
        return serverID;
    }

    public void setServerID(String serverID) {
        this.serverID = serverID;
    }

    public String getChannelID() {
        return channelID;
    }

    public void setChannelID(String channelID) {
        this.channelID = channelID;
        writeYML();
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
        writeYML();
    }

    public String getManagerRoleID() {
        return managerRoleID;
    }

    public void setManagerRoleID(String managerRoleID) {
        this.managerRoleID = managerRoleID;
        writeYML();
    }

    public String getQOTDColor() {
        return QOTDColor;
    }

    public void setQOTDColor(String QOTDColor) {
        this.QOTDColor = QOTDColor.replace("#", "");
        color = Color.decode("#" + this.QOTDColor);
        writeYML();
    }

    public Color getColor() {
        return color;
    }

    public boolean getInitialized() {
        return initialized;
    }

    public void setInitializedY(boolean initialized) {
        this.initialized = initialized;
    }


    public boolean isValid() {
        if (botToken.isBlank())
            return false;

        if (serverID.isBlank())
            return false;

        if (channelID.isBlank())
            return false;

        if (prefix.isBlank())
            return false;

        return !managerRoleID.isBlank();
    }

    private void writeYML() {
        if (getInitialized()) {
            QOTDBotLite.writeConfigYML();
        }
    }
}