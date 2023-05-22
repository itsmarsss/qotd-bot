package com.marsss.qotdbot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;

public class CMD extends ListenerAdapter {
    private MessageReceivedEvent e;

    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot() || !event.getGuild().getId().equals(QOTDBot.config.getServerID()))
            return;

        Message msg = event.getMessage();
        String raw = msg.getContentRaw();
        String[] rawSplit = raw.toLowerCase().split(" ");
        // [prefix - 0] [cmd - 1] [parameter - 2 to ???]
        if (!rawSplit[0].equals(QOTDBot.config.getPrefix()) || rawSplit.length == 1) {
            return;
        }
        e = event;

        if ("help".equals(rawSplit[1])) {
            help();
        }

        if (hasPerm(QOTDBot.config.getPermRoleID()) || hasPerm(QOTDBot.config.getManagerRoleID()) || isAdmin()) {
            switch (rawSplit[1]) {
                case "add":
                    addQuestion(raw, event.getAuthor());
                    break;
                case "addpoll":
                    addPoll(raw, event.getAuthor());
                    break;
            }
        }

        if (hasPerm(QOTDBot.config.getManagerRoleID()) || isAdmin()) {
            switch (rawSplit[1]) {
                case "upload":
                    uploadFile();
                    break;
                case "readfile":
                    readFile();
                    break;
                case "format":
                    sendFormat();
                    break;
                case "remove":
                    removeQuestion(raw);
                    break;
                case "view":
                    viewQuestion(raw);
                    break;
                case "queue":
                    viewQueue(raw);
                    break;
                case "qotdtest":
                    qotdTest();
                    break;
                case "postnext":
                    postNext();
                    break;
                case "qotdchannel":
                    setQOTDChannel(raw);
                    break;
                case "pause":
                    setPause(true);
                    break;
                case "unpause":
                    setPause(false);
                    break;
                case "prefix":
                    setPrefix(raw);
                    break;
                case "managerreview":
                    setManagerReview(raw);
                    break;
                case "reviewchannel":
                    setReviewChannel(raw);
                    break;
                case "embedcolor":
                    setColor(raw);
                    break;
                case "info":
                    sendInfo();
                    break;
                case "version":
                    checkVersion();
                    break;
            }
        }
        if (isAdmin()) {
            switch (rawSplit[1]) {
                case "permrole":
                    qotdPerm(raw);
                    break;
                case "managerrole":
                    qotdManager(raw);
                    break;
                case "dynamicconfig":
                    setDynConfig(raw);
                    break;
                case "updateconfig":
                    updateConfig();
                    break;
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

    private void addQuestion(String raw, User user) {
        // qotd add
        String[] param = raw.substring(QOTDBot.config.getPrefix().length() + 1 + 3).split("-=-");
        for (int i = 0; i < param.length; i++) {
            param[i] = param[i].trim();
        }
        if (param.length == 1 && !param[0].isBlank() && param[0].length() < 245) {
            Question q = new Question(param[0], user.getAsTag(), false);

            String title = "**__Added the following:__**\n";

            String uuid = "";

            if (QOTDBot.config.getManagerReview()) {
                title = "**__Requested the following:__**\n";
                uuid = QOTDBot.addReview(q);
            } else {
                QOTDBot.add(q);
            }

            Button removeButton = Button.primary("delete-notif-" + e.getMessageId(), "Delete this Message");

            MessageCreateData message = new MessageCreateBuilder()
                    .setEmbeds(new EmbedBuilder()
                            .setTitle(title)
                            .addField("Question", q.getQuestion(), false)
                            .addField("Footer", q.getFooter(), false)
                            .addField("Author", q.getAuthor(), false)
                            .addField("Date", q.getDate(), false)
                            .setThumbnail(QOTDBot.jda.getSelfUser().getAvatarUrl())
                            .setColor(QOTDBot.config.getColor())
                            .build())
                    .setComponents(ActionRow.of(removeButton))
                    .build();
            e.getMessage().reply(message).queue();

            if (QOTDBot.config.getManagerReview()) {
                Button approveButton = Button.success("approve-qotd-" + uuid, "Approve and Delete");
                Button denyButton = Button.danger("deny-qotd-" + uuid, "Deny and Delete");

                MessageCreateData req = new MessageCreateBuilder()
                        .setEmbeds(new EmbedBuilder()
                                .setTitle("__**The Following QOTD Was Requested:**__")
                                .addField("Type", q.isPoll() ? "Poll" : "Question", false)
                                .addField("Question", q.getQuestion(), false)
                                .addField("Footer", q.getFooter(), false)
                                .addField("Author", q.getAuthor(), false)
                                .addField("Date", q.getDate(), false)
                                .setThumbnail(QOTDBot.jda.getSelfUser().getAvatarUrl())
                                .setColor(QOTDBot.config.getColor())
                                .build())
                        .setComponents(ActionRow.of(approveButton, denyButton))
                        .build();
                try {
                    e.getGuild().getTextChannelById(QOTDBot.config.getReviewChannel()).sendMessage(req).queue();
                } catch (Exception e) {
                    this.e.getMessage().reply(req).queue();
                }
            }
        } else if (param.length == 2 && !param[0].isBlank() && param[0].length() < 245 && param[1].length() < 100) {
            Question q = new Question(param[0], param[1], user.getAsTag(), false);

            String title = "**__Added the following:__**\n";

            if (QOTDBot.config.getManagerReview()) {
                title = "**__Requested the following:__**\n";
            } else {
                QOTDBot.add(q);
            }

            Button removeButton = Button.primary("delete-notif-" + e.getMessageId(), "Delete this Message");

            MessageCreateData message = new MessageCreateBuilder()
                    .setEmbeds(new EmbedBuilder()
                            .setTitle(title)
                            .addField("Question", q.getQuestion(), false)
                            .addField("Footer", q.getFooter(), false)
                            .addField("Author", q.getAuthor(), false)
                            .addField("Date", q.getDate(), false)
                            .setThumbnail(QOTDBot.jda.getSelfUser().getAvatarUrl())
                            .setColor(QOTDBot.config.getColor())
                            .build())
                    .setComponents(ActionRow.of(removeButton))
                    .build();
            e.getMessage().reply(message).queue();

            if (QOTDBot.config.getManagerReview()) {
                Button approveButton = Button.success("approve-qotd", "Approve and Delete");
                Button denyButton = Button.danger("deny-qotd", "Deny and Delete");

                MessageCreateData req = new MessageCreateBuilder()
                        .setEmbeds(new EmbedBuilder()
                                .setTitle("__**The Following QOTD Was Requested:**__")
                                .addField("Type", q.isPoll() ? "Poll" : "Question", false)
                                .addField("Question", q.getQuestion(), false)
                                .addField("Footer", q.getFooter(), false)
                                .addField("Author", q.getAuthor(), false)
                                .addField("Date", q.getDate(), false)
                                .setThumbnail(QOTDBot.jda.getSelfUser().getAvatarUrl())
                                .setColor(QOTDBot.config.getColor())
                                .build())
                        .setComponents(ActionRow.of(approveButton, denyButton))
                        .build();
                try {
                    e.getGuild().getTextChannelById(QOTDBot.config.getReviewChannel()).sendMessage(req).queue();
                } catch (Exception ex) {
                    e.getMessage().reply(req).queue();
                }
            }
        } else {
            e.getMessage().replyEmbeds(se("Invalid parameters.")).queue();
        }
    }

    private void addPoll(String raw, User user) {
        // qotd addpoll
        String[] param = raw.substring(QOTDBot.config.getPrefix().length() + 1 + 7).split("-=-");
        for (int i = 0; i < param.length; i++) {
            param[i] = param[i].trim();
        }
        if (param.length == 1 && !param[0].isBlank() && param[0].length() < 245) {
            Question q = new Question(param[0], user.getAsTag(), true);

            String title = "**__Added the following poll:__**\n";

            if (QOTDBot.config.getManagerReview()) {
                title = "**__Requested the following poll:__**\n";
            } else {
                QOTDBot.add(q);
            }

            Button removeButton = Button.primary("delete-notif-" + e.getMessageId(), "Delete this Message");

            MessageCreateData message = new MessageCreateBuilder()
                    .setEmbeds(new EmbedBuilder()
                            .setTitle(title)
                            .addField("Poll", q.getQuestion(), false)
                            .addField("Footer", q.getFooter(), false)
                            .addField("Author", q.getAuthor(), false)
                            .addField("Date", q.getDate(), false)
                            .setThumbnail(QOTDBot.jda.getSelfUser().getAvatarUrl())
                            .setColor(QOTDBot.config.getColor())
                            .build())
                    .setComponents(ActionRow.of(removeButton))
                    .build();
            e.getMessage().reply(message).queue();

            if (QOTDBot.config.getManagerReview()) {
                Button approveButton = Button.success("approve-qotd", "Approve and Delete");
                Button denyButton = Button.danger("deny-qotd", "Deny and Delete");

                MessageCreateData req = new MessageCreateBuilder()
                        .setEmbeds(new EmbedBuilder()
                                .setTitle("__**The Following QOTD Was Requested:**__")
                                .addField("Type", q.isPoll() ? "Poll" : "Question", false)
                                .addField("Question", q.getQuestion(), false)
                                .addField("Footer", q.getFooter(), false)
                                .addField("Author", q.getAuthor(), false)
                                .addField("Date", q.getDate(), false)
                                .setThumbnail(QOTDBot.jda.getSelfUser().getAvatarUrl())
                                .setColor(QOTDBot.config.getColor())
                                .build())
                        .setComponents(ActionRow.of(approveButton, denyButton))
                        .build();
                try {
                    e.getGuild().getTextChannelById(QOTDBot.config.getReviewChannel()).sendMessage(req).queue();
                } catch (Exception ex) {
                    e.getMessage().reply(req).queue();
                }
            }
        } else if (param.length == 2 && !param[0].isBlank() && param[0].length() < 245 && param[1].length() < 100) {
            Question q = new Question(param[0], param[1], user.getAsTag(), true);

            String title = "**__Added the following poll:__**\n";

            if (QOTDBot.config.getManagerReview()) {
                title = "**__Requested the following poll:__**\n";
            } else {
                QOTDBot.add(q);
            }

            Button removeButton = Button.primary("delete-notif-" + e.getMessageId(), "Delete this Message");

            MessageCreateData message = new MessageCreateBuilder()
                    .setEmbeds(new EmbedBuilder()
                            .setTitle(title)
                            .addField("Poll", q.getQuestion(), false)
                            .addField("Footer", q.getFooter(), false)
                            .addField("Author", q.getAuthor(), false)
                            .addField("Date", q.getDate(), false)
                            .setThumbnail(QOTDBot.jda.getSelfUser().getAvatarUrl())
                            .setColor(QOTDBot.config.getColor())
                            .build())
                    .setComponents(ActionRow.of(removeButton))
                    .build();
            e.getMessage().reply(message).queue();

            if (QOTDBot.config.getManagerReview()) {
                Button approveButton = Button.success("approve-qotd", "Approve and Delete");
                Button denyButton = Button.danger("deny-qotd", "Deny and Delete");

                MessageCreateData req = new MessageCreateBuilder()
                        .setEmbeds(new EmbedBuilder()
                                .setTitle("__**The Following QOTD Was Requested:**__")
                                .addField("Type", q.isPoll() ? "Poll" : "Question", false)
                                .addField("Question", q.getQuestion(), false)
                                .addField("Footer", q.getFooter(), false)
                                .addField("Author", q.getAuthor(), false)
                                .addField("Date", q.getDate(), false)
                                .setThumbnail(QOTDBot.jda.getSelfUser().getAvatarUrl())
                                .setColor(QOTDBot.config.getColor())
                                .build())
                        .setComponents(ActionRow.of(approveButton, denyButton))
                        .build();
                try {
                    e.getGuild().getTextChannelById(QOTDBot.config.getReviewChannel()).sendMessage(req).queue();
                } catch (Exception ex) {
                    e.getMessage().reply(req).queue();
                }
            }
        } else {
            e.getMessage().replyEmbeds(se("Invalid parameters.")).queue();
        }
    }

    private void uploadFile() {
        // qotd upload
        if (e.getMessage().getAttachments().isEmpty()) {
            e.getMessage().replyEmbeds(se("No json file attached.")).queue();
            return;
        }
        Attachment attachment = e.getMessage().getAttachments().get(0);
        if (!attachment.getFileExtension().equalsIgnoreCase("json")) {
            e.getMessage().replyEmbeds(se("File must be in json format, `" + QOTDBot.config.getPrefix() + " format` for example")).queue();
            return;
        }


        try {
            attachment.getProxy().downloadToFile(new File(QOTDBot.getParent() + "/upload.json"));

            System.out.println();
            System.out.println("~~~~~~~~~~~~~");
            System.out.println("File uploaded: " + QOTDBot.getParent() + "\\upload.json");

            e.getMessage().replyEmbeds(se("Downloaded file, please run `" + QOTDBot.config.getPrefix() + " readfile` to load all questions in.")).queue();
        } catch (Exception e) {
            this.e.getMessage().replyEmbeds(se("Unable to read file.")).queue();
        }
    }

    private void readFile() {
        int diff = QOTDBot.getQuestions().size();
        QOTDBot.readQuestionsJSON("upload.json");

        diff = QOTDBot.getQuestions().size() - diff;
        e.getMessage().replyEmbeds(se("File read; **" + diff + "** questions appended. *(Invalid questions were not added.)*")).queue();

        QOTDBot.prepUploadJSON();
    }

    private void sendFormat() {
        String format = "QOTD json formatting:\n```"
                + "{\r\n"
                + "\t\"questions\": [\r\n"
                + "\t\t{\r\n"
                + "\t\t\t\"question\": \"Question here\",\r\n"
                + "\t\t\t\"footer\": \"Footer here\",\r\n"
                + "\t\t\t\"time\": 1234567890,\r\n"
                + "\t\t\t\"user\": \"userhere#0000\"\r\n"
                + "\t\t\t\"poll\": false,\r\n"
                + "\t\t},\r\n\n"
                + "\t\t{\n"
                + "\t\t\t\"question\": \"Question here\",\r\n"
                + "\t\t\t\"footer\": \"Footer here\",\r\n"
                + "\t\t\t\"time\": 1234567890,\r\n"
                + "\t\t\t\"user\": \"userhere#0000\"\r\n"
                + "\t\t\t\"poll\": false,\r\n"
                + "\t\t}\r\n"
                + "\t]\r\n"
                + "}```";
        e.getMessage().replyEmbeds(se(format)).queue();
    }

    private void removeQuestion(String raw) {
        // qotd remove
        try {
            int param = Integer.parseInt(raw.substring(QOTDBot.config.getPrefix().length() + 1 + 6).trim());
            int status = QOTDBot.remove(param);
            if (status == -1) {
                e.getMessage().replyEmbeds(se("Invalid number.")).queue();
            } else {
                e.getMessage().replyEmbeds(se("Index **" + param + "** has been removed from the queue.")).queue();
            }
        } catch (Exception e) {
            this.e.getMessage().replyEmbeds(se("Invalid index.")).queue();
        }
    }

    private void viewQuestion(String raw) {
        // qotd view
        try {
            int param = Integer.parseInt(raw.substring(QOTDBot.config.getPrefix().length() + 1 + 4).trim());
            Question q = QOTDBot.getQuestions().get(param);

            Button deleteButton = Button.secondary("delete", "Delete this Message");
            MessageCreateData message = new MessageCreateBuilder()
                    .setEmbeds(new EmbedBuilder()
                            .setTitle("**__QOTD #" + param + ";__**")
                            .addField("Question", q.getQuestion(), false)
                            .addField("Footer", q.getFooter(), false)
                            .addField("Author", q.getAuthor(), false)
                            .addField("Date", q.getDate(), false)
                            .setThumbnail(QOTDBot.jda.getSelfUser().getAvatarUrl())
                            .setColor(QOTDBot.config.getColor())
                            .build())
                    .setComponents(ActionRow.of(deleteButton))
                    .build();

            e.getMessage().reply(message).queue();
        } catch (Exception e) {
            this.e.getMessage().replyEmbeds(se("Invalid index.")).queue();
        }
    }

    private void viewQueue(String raw) {
        // qotd queue
        try {
            LinkedList<Question> q = QOTDBot.getQuestions();
            int param = 0;
            if (raw.length() > QOTDBot.config.getPrefix().length() + 1 + 5) {
                param = Integer.parseInt(raw.substring(QOTDBot.config.getPrefix().length() + 1 + 5).trim());
            }
            StringBuilder out = new StringBuilder();
            for (int i = 0; i < (Math.min(q.size() - param * 5, 5)); i++) {
                String question = q.get(i + param * 5).getQuestion();
                if (question.length() > 50) {
                    question = question.substring(0, 48) + "...";
                }
                out.append("\n**").append(i + param * 5).append(":** ").append(question);
            }

            if (out.toString().isBlank() && !q.isEmpty()) {
                e.getMessage().replyEmbeds(se("Invalid page index.")).queue();
                return;
            }

            DateTimeFormatter format = DateTimeFormatter.ofPattern("dd/MM/yyyy • hh:mm");

            Button prevButton = Button.primary("prev-" + (param - 1), "\u2B05 Prev");
            Button nextButton = Button.primary("next-" + (param + 1), "Next \u27A1");
            Button deleteButton = Button.secondary("delete", "Delete this Message");
            MessageCreateData message = new MessageCreateBuilder()
                    .setEmbeds(new EmbedBuilder()
                            .setTitle("**__QOTD Queue:__** *Page " + param + "*")
                            .setDescription(out.toString().isBlank() ? ":open_mouth::dash: Empty" : out.toString())
                            .setFooter(format.format(LocalDateTime.now()), e.getAuthor().getAvatarUrl())
                            .setColor(QOTDBot.config.getColor())
                            .build())
                    .setComponents(ActionRow.of(prevButton, nextButton, deleteButton))
                    .build();

            e.getMessage().reply(message).queue();
        } catch (Exception e) {
            this.e.getMessage().replyEmbeds(se("Invalid page index.")).queue();
        }
    }

    private void qotdTest() {
        // qotd testqotd
        EmbedBuilder QOTDEmbed = new EmbedBuilder();
        QOTDEmbed.setAuthor("Added by: *author here*", null, QOTDBot.jda.getSelfUser().getAvatarUrl())
                .setTitle("**Question/Poll:** *question here*")
                .setDescription("*footer here*")
                .setFooter("Added on: *date here*")
                .setColor(QOTDBot.config.getColor());
        e.getMessage().replyEmbeds(QOTDEmbed.build()).queue(msg -> {
            msg.addReaction(Emoji.fromUnicode("\u2705")).queue();
            msg.addReaction(Emoji.fromUnicode("\u274E")).queue();
        });
    }

    private void postNext() {
        // qotd postnext
        QOTDBot.postQOTD();
    }

    private void setQOTDChannel(String raw) {
        // qotd qotdchannel
        try {
            String param = raw.substring(QOTDBot.config.getPrefix().length() + 1 + 11).trim();
            boolean exists = false;
            for (GuildChannel ch : e.getGuild().getChannels()) {
                if (ch.getId().equals(param)) {
                    exists = true;
                }
            }
            if (exists) {
                QOTDBot.config.setChannelID(param);
                e.getMessage().replyEmbeds(se("QOTD channel has been changed to <#" + param + ">.")).queue();
            } else {
                e.getMessage().replyEmbeds(se("Invalid channel id.")).queue();
            }
        } catch (Exception e) {
            this.e.getMessage().replyEmbeds(se("Unable to look for channel.")).queue();
        }
    }

    private void setPause(boolean status) {
        // qotd pause
        QOTDBot.setPause(status);
        e.getMessage().replyEmbeds(se("QOTD bot paused: **" + QOTDBot.getPause() + "**")).queue();
    }

    private void setPrefix(String raw) {
        // qotd prefix
        try {
            String param = raw.split(" ")[2].trim();
            QOTDBot.config.setPrefix(param);
            e.getMessage().replyEmbeds(se("QOTD prefix has been changed to `" + param + "`.")).queue();
            QOTDBot.jda.getPresence().setActivity(Activity.watching("for '" + QOTDBot.config.getPrefix() + " help'"));
        } catch (Exception e) {
            this.e.getMessage().replyEmbeds(se("Invalid prefix.")).queue();
        }
    }

    private void setManagerReview(String raw) {
        // qotd managerreview
        try {
            String param = raw.substring(QOTDBot.config.getPrefix().length() + 1 + 13).trim();
            boolean setTo;
            if (param.equalsIgnoreCase("true")) {
                setTo = true;
            } else if (param.equalsIgnoreCase("false")) {
                setTo = false;
            } else {
                e.getMessage().replyEmbeds(se("Invalid parameter")).queue();
                return;
            }
            QOTDBot.config.setManagerReview(setTo);
            e.getMessage().replyEmbeds(se("QOTD manager review: **" + QOTDBot.config.getManagerReview() + "**")).queue();
        } catch (Exception e) {
            this.e.getMessage().replyEmbeds(se("Invalid parameter")).queue();
        }
    }

    private void setReviewChannel(String raw) {
        // qotd reviewchannel
        try {
            String param = raw.substring(QOTDBot.config.getPrefix().length() + 1 + 13).trim();
            boolean exists = false;
            for (GuildChannel r : e.getGuild().getTextChannels()) {
                if (r.getId().equals(param)) {
                    exists = true;
                    break;
                }
            }
            if (exists) {
                QOTDBot.config.setReviewChannel(param);
                e.getMessage().replyEmbeds(se("QOTD review channel been changed to <#" + param + ">.")).queue();
            } else {
                e.getMessage().replyEmbeds(se("Invalid channel id.")).queue();
            }
        } catch (Exception e) {
            this.e.getMessage().replyEmbeds(se("Unable to look for channel.")).queue();
        }
    }

    private void setColor(String raw) {
        // qotd embedcolor
        try {
            String param = raw.substring(QOTDBot.config.getPrefix().length() + 1 + 10).trim().replace("#", "");
            QOTDBot.config.setQOTDColor(param);
            this.e.getMessage().replyEmbeds(new EmbedBuilder()
                            .setDescription("Set embed color to **#" + QOTDBot.config.getQOTDColor() + "**.")
                            .setColor(QOTDBot.config.getColor())
                            .build())
                    .queue();
        } catch (Exception e) {
            this.e.getMessage().replyEmbeds(se("Unable to set color.")).queue();
        }
    }

    private void sendInfo() {
        // qotd info
        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd/MM/yyyy • hh:mm");

        MessageEmbed infoEm = new EmbedBuilder()
                .setTitle("__Bot Info__")
                .setDescription("[ *Version: " + QOTDBot.version + "* ]")
                .addField("Prefix:", QOTDBot.config.getPrefix(), true)
                .addBlankField(true)
                .addField("Interval:", QOTDBot.config.getInterval() + " minute(s)", true)
                .addField("Perm role ID:", QOTDBot.config.getPermRoleID().equals("everyone") ? "everyone" : "<@&" + QOTDBot.config.getPermRoleID() + ">", true)
                .addBlankField(true)
                .addField("Manager role ID:", QOTDBot.config.getManagerRoleID().equals("everyone") ? "everyone" : "<@&" + QOTDBot.config.getManagerRoleID() + ">", true)
                .addField("Manager review status:", QOTDBot.config.getManagerReview() + "", true)
                .addBlankField(true)
                .addField("Manager review channel:", "<#" + QOTDBot.config.getReviewChannel() + ">", true)
                .addField("Dynamic Config:", QOTDBot.config.getDynamicConfig() + "", false)
                .setThumbnail(QOTDBot.jda.getSelfUser().getAvatarUrl())
                .setFooter(format.format(LocalDateTime.now()), e.getAuthor().getAvatarUrl())
                .setColor(QOTDBot.config.getColor())
                .build();
        e.getMessage().replyEmbeds(infoEm).queue();
    }

    private void checkVersion() {
        // qotd version
        e.getMessage().replyEmbeds(new EmbedBuilder()
                        .setTitle(QOTDBot.version)
                        .setDescription(QOTDBot.versionCheck()
                                .replaceAll("#", "")
                                .replace("This program is up to date!", "__**This program is up to date!**__")
                                .replace("[There is a newer version of QOTD Bot]", "__**[There is a newer version of QOTD Bot]**__")
                                .replace("Author's Note:", "**Author's Note:**")
                                .replace("New version:", "**New version:**"))
                        .setColor(QOTDBot.config.getColor())
                        .build())
                .queue();
    }

    private void qotdPerm(String raw) {
        // qotd permrole
        try {
            String param = raw.substring(QOTDBot.config.getPrefix().length() + 1 + 8).trim();
            if (param.equalsIgnoreCase("everyone")) {
                e.getMessage().replyEmbeds(se("QOTD perm role has been changed; `everyone` can post questions.")).queue();
                QOTDBot.config.setPermRoleID("everyone");
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
                QOTDBot.config.setPermRoleID(param);
                e.getMessage().replyEmbeds(se("QOTD perm role has been changed to <@&" + param + ">.")).queue();
            } else {
                e.getMessage().replyEmbeds(se("Invalid role id.")).queue();
            }
        } catch (Exception e) {
            this.e.getMessage().replyEmbeds(se("Unable to look for role.")).queue();
        }
    }

    private void qotdManager(String raw) {
        // qotd managerrole
        try {
            String param = raw.substring(QOTDBot.config.getPrefix().length() + 1 + 11).trim();
            if (param.equalsIgnoreCase("everyone"))
                return;
            boolean exists = false;
            for (Role r : e.getGuild().getRoles()) {
                if (r.getId().equals(param)) {
                    exists = true;
                    break;
                }
            }
            if (exists) {
                QOTDBot.config.setManagerRoleID(param);
                e.getMessage().replyEmbeds(se("QOTD manager role has been changed to <@&" + param + ">.")).queue();
            } else {
                e.getMessage().replyEmbeds(se("Invalid role id.")).queue();
            }
        } catch (Exception e) {
            this.e.getMessage().replyEmbeds(se("Unable to look for role.")).queue();
        }
    }

    private void setDynConfig(String raw) {
        // qotd dynamicconfig
        try {
            String param = raw.substring(QOTDBot.config.getPrefix().length() + 1 + 13).trim();
            boolean setTo;
            if (param.equalsIgnoreCase("true")) {
                setTo = true;
            } else if (param.equalsIgnoreCase("false")) {
                setTo = false;
            } else {
                e.getMessage().replyEmbeds(se("Invalid parameter")).queue();
                return;
            }
            QOTDBot.config.setDynamicConfig(setTo);
            e.getMessage().replyEmbeds(se("QOTD dynamic config: **" + QOTDBot.config.getDynamicConfig() + "**")).queue();
        } catch (Exception e) {
            this.e.getMessage().replyEmbeds(se("Invalid parameter")).queue();
        }
    }

    private void updateConfig() {
        // qotd updateconfig
        if (QOTDBot.writeConfigYML()) {
            e.getMessage().replyEmbeds(se("Config.yml updated")).queue();
        } else {
            e.getMessage().replyEmbeds(se("Config.yml failed to update")).queue();
        }
    }


    static MessageEmbed se(String desc) {
        return new EmbedBuilder()
                .setDescription(desc)
                .setColor(QOTDBot.config.getColor())
                .build();
    }


    private void help() {
        // qotd help
        e.getMessage().replyEmbeds(
                new EmbedBuilder()
                        .setTitle("__**Commands**__")
                        .addField("Main",
                                "`" + QOTDBot.config.getPrefix() + " help` - This message", false)
                        .addBlankField(true)
                        .addField("Perm commands",
                                "`" + QOTDBot.config.getPrefix() + " add <question 245 char>-=-<footer 100 char>` - Adds/Requests a QOTD question" + "\n`" +
                                        QOTDBot.config.getPrefix() + " addpoll <question 245 char>-=-<footer 100 char>` - Adds/Requests a QOTD poll", false)
                        .addBlankField(true)
                        .addField("Manager commands",
                                "`" + QOTDBot.config.getPrefix() + " upload [attached json file]` - Uploads a json file" + "\n`" +
                                        QOTDBot.config.getPrefix() + " readfile` - Reads the cached json file" + "\n`" +
                                        QOTDBot.config.getPrefix() + " format` - Sends json file format" + "\n`" +
                                        QOTDBot.config.getPrefix() + " remove <index>` - Remove QOTD at a specific index" + "\n`" +
                                        QOTDBot.config.getPrefix() + " view <index>` - View details of QOTD at a specific index" + "\n`" +
                                        QOTDBot.config.getPrefix() + " queue <index|nothing>` - View QOTD queue" + "\n`" +
                                        QOTDBot.config.getPrefix() + " qotdtest` - Send a sample QOTD" + "\n`" +
                                        QOTDBot.config.getPrefix() + " postnext` - Post next QOTD" + "\n`" +
                                        QOTDBot.config.getPrefix() + " pause` - Pause QOTD posting" + "\n`" +
                                        QOTDBot.config.getPrefix() + " unpause` - Unpause QOTD posting" + "\n`" +
                                        QOTDBot.config.getPrefix() + " prefix <prefix, no space>` - Change bot prefix" + "\n`" +
                                        QOTDBot.config.getPrefix() + " managerreview <true|false>` - Toggle QOTD manager review" + "\n`" +
                                        QOTDBot.config.getPrefix() + " reviewchannel <channel id>` - Set QOTD request channel" + "\n`" +
                                        QOTDBot.config.getPrefix() + " qotdcolor <color in hex>` - Set QOTD embed color" + "\n`" +
                                        QOTDBot.config.getPrefix() + " info` - See bot info" + "\n`" +
                                        QOTDBot.config.getPrefix() + " version` - See bot version", false)
                        .addBlankField(true)
                        .addField("Admin commands",
                                "`" + QOTDBot.config.getPrefix() + " permrole <role id/'everyone'>` - Set QOTD permission role" + "\n`" +
                                        QOTDBot.config.getPrefix() + " managerrole <role id/'everyone'>` - Set QOTD manager role" + "\n`" +
                                        QOTDBot.config.getPrefix() + " dynamicconfig <true|false>` - Toggle QOTD config.yml dynamic state" + "\n`" +
                                        QOTDBot.config.getPrefix() + " updateconfig` - Update config.yml to match current settings", false)
                        .setThumbnail(QOTDBot.jda.getSelfUser().getAvatarUrl())
                        .setColor(QOTDBot.config.getColor())
                        .build()).queue();
    }
}
