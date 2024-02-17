package com.marsss.qotdbotlite;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CMD extends ListenerAdapter {
    private MessageReceivedEvent e;

    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot() || !event.getGuild().getId().equals(QOTDBotLite.config.getServerID()))
            return;

        Message msg = event.getMessage();
        String raw = msg.getContentRaw();
        String[] rawSplit = raw.toLowerCase().split(" ");
        // [prefix - 0] [cmd - 1] [parameter - 2 to ???]
        if (!rawSplit[0].equals(QOTDBotLite.config.getPrefix()) || rawSplit.length == 1) {
            return;
        }
        e = event;

        if ("help".equals(rawSplit[1])) {
            help();
        }

        if (hasPerm(QOTDBotLite.config.getManagerRoleID()) || isAdmin()) {
            switch (rawSplit[1]) {
                case "qotdtest" -> qotdTest();
                case "post" -> postNext(raw);
                case "qotdchannel" -> setQOTDChannel(raw);
                case "prefix" -> setPrefix(raw);
                case "embedcolor" -> setColor(raw);
                case "info" -> sendInfo();
                case "version" -> checkVersion();
            }
        }
        if (isAdmin()) {
            if ("managerrole".equals(rawSplit[1])) {
                qotdManager(raw);
            }
        }
    }

    private boolean hasPerm(String ID) {
        if (ID.equals("everyone"))
            return true;
        for (Role r : e.getMember().getRoles()) {
            if (r.getId().equals(ID)) {
                return true;
            }
        }
        return false;
    }

    private boolean isAdmin() {
        return e.getMember().hasPermission(Permission.ADMINISTRATOR);
    }

    private void qotdTest() {
        // qotd testqotd
        EmbedBuilder QOTDEmbed = new EmbedBuilder();
        QOTDEmbed.setAuthor(QOTDBotLite.jda.getSelfUser().getName(), null, QOTDBotLite.jda.getSelfUser().getAvatarUrl())
                .setTitle("**Question:** *question here*")
                .setDescription("*footer here*")
                .setColor(QOTDBotLite.config.getColor());

        e.getMessage().replyEmbeds(QOTDEmbed.build()).queue();
    }

    private void postNext(String raw) {
        // qotd post question-=-footer
        try {
            String[] param = raw.substring(QOTDBotLite.config.getPrefix().length() + 1 + 4).split("-=-");
            for (int i = 0; i < param.length; i++) {
                param[i] = param[i].trim();
            }
            if (param.length == 1 && !param[0].isBlank() && param[0].length() < 245) {
                QOTDBotLite.postQOTD(param[0], "");
            } else if (param.length == 2 && !param[0].isBlank() && param[0].length() < 245 && param[1].length() < 100) {
                QOTDBotLite.postQOTD(param[0], param[1]);
            } else {
                e.getMessage().replyEmbeds(se("Invalid parameters.")).queue();
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.e.getMessage().replyEmbeds(se("Invalid parameters.")).queue();
        }
    }

    private void setQOTDChannel(String raw) {
        // qotd qotdchannel
        try {
            String param = raw.substring(QOTDBotLite.config.getPrefix().length() + 1 + 11).trim();
            boolean exists = false;
            for (GuildChannel ch : e.getGuild().getChannels()) {
                if (ch.getId().equals(param)) {
                    exists = true;
                }
            }
            if (exists) {
                QOTDBotLite.config.setChannelID(param);
                e.getMessage().replyEmbeds(se("QOTD channel has been changed to <#" + param + ">.")).queue();
            } else {
                e.getMessage().replyEmbeds(se("Invalid channel id.")).queue();
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.e.getMessage().replyEmbeds(se("Unable to look for channel.")).queue();
        }
    }

    private void setPrefix(String raw) {
        // qotd prefix
        try {
            String param = raw.split(" ")[2].trim();
            QOTDBotLite.config.setPrefix(param);
            e.getMessage().replyEmbeds(se("QOTD prefix has been changed to `" + param + "`.")).queue();
            QOTDBotLite.jda.getPresence().setActivity(Activity.watching("for '" + QOTDBotLite.config.getPrefix() + " help'"));
        } catch (Exception e) {
            e.printStackTrace();
            this.e.getMessage().replyEmbeds(se("Invalid prefix.")).queue();
        }
    }

    private void setColor(String raw) {
        // qotd embedcolor
        try {
            String param = raw.substring(QOTDBotLite.config.getPrefix().length() + 1 + 10).trim().replace("#", "");
            QOTDBotLite.config.setQOTDColor(param);
            this.e.getMessage().replyEmbeds(new EmbedBuilder()
                            .setDescription("Set embed color to **#" + QOTDBotLite.config.getQOTDColor() + "**.")
                            .setColor(QOTDBotLite.config.getColor())
                            .build())
                    .queue();
        } catch (Exception e) {
            e.printStackTrace();
            this.e.getMessage().replyEmbeds(se("Unable to set color.")).queue();
        }
    }

    private void sendInfo() {
        // qotd info
        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd/MM/yyyy • hh:mm");

        MessageEmbed infoEm = new EmbedBuilder()
                .setTitle("__Bot Info__")
                .setDescription("[ *Version: " + QOTDBotLite.version + "* ]")
                .addField("Prefix:", QOTDBotLite.config.getPrefix(), true)
                .addField("QOTD Channel:", "<#" + QOTDBotLite.config.getChannelID() + ">", true)
                .addBlankField(true)
                .addField("Manager role ID:", QOTDBotLite.config.getManagerRoleID().equals("everyone") ? "everyone" : QOTDBotLite.config.getManagerRoleID().equals("admin") ? "admin" : "<@&" + QOTDBotLite.config.getManagerRoleID() + ">", true)
                .setThumbnail(QOTDBotLite.jda.getSelfUser().getAvatarUrl())
                .setFooter(format.format(LocalDateTime.now()), e.getAuthor().getAvatarUrl())
                .setColor(QOTDBotLite.config.getColor())
                .build();
        e.getMessage().replyEmbeds(infoEm).queue();
    }

    private void checkVersion() {
        // qotd version
        e.getMessage().replyEmbeds(new EmbedBuilder()
                        .setTitle(QOTDBotLite.version)
                        .setDescription(QOTDBotLite.versionCheck()
                                .replaceAll("#", "")
                                .replace("This program is up to date!", "__**This program is up to date!**__")
                                .replace("[There is a newer version of QOTD Bot Lite]", "__**[There is a newer version of QOTD Bot Lite]**__")
                                .replace("Author's Note:", "**Author's Note:**")
                                .replace("New version:", "**New version:**"))
                        .setColor(QOTDBotLite.config.getColor())
                        .build())
                .queue();
    }

    private void qotdManager(String raw) {
        // qotd managerrole
        try {
            String param = raw.substring(QOTDBotLite.config.getPrefix().length() + 1 + 11).trim();
            if (param.equalsIgnoreCase("everyone")) {
                e.getMessage().replyEmbeds(se("QOTD manager role has been changed; `everyone` can approve or deny questions")).queue();
                QOTDBotLite.config.setManagerRoleID("everyone");
                return;
            }
            boolean exists = false;
            for (Role r : e.getGuild().getRoles()) {
                if (r.getId().equals(param)) {
                    exists = true;
                    break;
                }
            }
            if (exists) {
                QOTDBotLite.config.setManagerRoleID(param);
                e.getMessage().replyEmbeds(se("QOTD manager role has been changed to <@&" + param + ">.")).queue();
            } else {
                e.getMessage().replyEmbeds(se("Invalid role id.")).queue();
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.e.getMessage().replyEmbeds(se("Unable to look for role.")).queue();
        }
    }

    static MessageEmbed se(String desc) {
        return new EmbedBuilder()
                .setDescription(desc)
                .setColor(QOTDBotLite.config.getColor())
                .build();
    }

    private void help() {
        // qotd help
        e.getMessage().replyEmbeds(
                new EmbedBuilder()
                        .setTitle("__**Commands**__")
                        .addField("Main",
                                "`" + QOTDBotLite.config.getPrefix() + " help` - This message", false)
                        .addBlankField(true)
                        .addField("Manager commands",
                                "`" + QOTDBotLite.config.getPrefix() + " qotdtest` - Send a sample QOTD" + "\n`" +
                                        QOTDBotLite.config.getPrefix() + " post <question 245 char>-=-<footer 100 char>` - Post a QOTD" + "\n`" +
                                        QOTDBotLite.config.getPrefix() + " prefix <prefix, no space>` - Change bot prefix" + "\n`" +
                                        QOTDBotLite.config.getPrefix() + " qotdcolor <color in hex>` - Set QOTD embed color" + "\n`" +
                                        QOTDBotLite.config.getPrefix() + " info` - See bot info" + "\n`" +
                                        QOTDBotLite.config.getPrefix() + " version` - See bot version", false)
                        .addBlankField(true)
                        .addField("Admin commands",
                                "`" + QOTDBotLite.config.getPrefix() + " managerrole <role id/'everyone'>` - Set QOTD manager role", false)
                        .setThumbnail(QOTDBotLite.jda.getSelfUser().getAvatarUrl())
                        .setColor(QOTDBotLite.config.getColor())
                        .build()).queue();
    }
}
