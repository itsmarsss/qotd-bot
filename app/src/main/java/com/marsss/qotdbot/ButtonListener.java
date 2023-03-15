package com.marsss.qotdbot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ButtonListener extends ListenerAdapter {
    private ButtonInteractionEvent e;

    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        try {
            if (!event.getGuild().getId().equals(QOTDBot.config.getServerID()))
                return;

            e = event;

            String id = event.getButton().getId();
            if (id.equals("delete")) {

                if (!(hasPerm(QOTDBot.config.getPermRoleID()) || hasPerm(QOTDBot.config.getManagerRoleID()) || isAdmin())) {
                    e.replyEmbeds(CMD.se("You do not have permission to perform this action")).setEphemeral(true).queue();
                    return;
                }

                event.getMessage().delete().queue();
                e.replyEmbeds(CMD.se("Request successful")).setEphemeral(true).queue();
            } else if (id.startsWith("delete-notif")) {

                if (!(hasPerm(QOTDBot.config.getPermRoleID()) || hasPerm(QOTDBot.config.getManagerRoleID()) || isAdmin())) {
                    e.replyEmbeds(CMD.se("You do not have permission to perform this action")).setEphemeral(true).queue();
                    return;
                }

                event.getMessage().delete().queue();

                String commandId = event.getButton().getId().replace("delete-notif-", "");
                event.getChannel().retrieveMessageById(commandId).queue(message -> {
                    try {
                        message.delete().queue();
                    } catch (Exception e) {
                        message.addReaction(Emoji.fromUnicode("\uF6AB")).queue();
                    }
                });

                e.replyEmbeds(CMD.se("Request successful")).setEphemeral(true).queue();
            } else if (id.equals("approve-qotd")) {

                if (!(hasPerm(QOTDBot.config.getManagerRoleID()) || isAdmin())) {
                    e.replyEmbeds(CMD.se("You do not have permission to perform this action")).setEphemeral(true).queue();
                    return;
                }

                List<Field> flds = event.getMessage().getEmbeds().get(0).getFields();
                boolean isPoll = flds.get(0).getValue().equals("Poll");

                Question q = new Question(flds.get(1).getValue(), flds.get(2).getValue(), flds.get(3).getValue(), isPoll);
                q.setDate(flds.get(4).getValue());
                QOTDBot.add(q);

                event.getMessage().delete().queueAfter(1, TimeUnit.SECONDS);

                e.replyEmbeds(CMD.se("Request successful")).setEphemeral(true).queue();
            } else if (id.equals("deny-qotd")) {

                if (!(hasPerm(QOTDBot.config.getManagerRoleID()) || isAdmin())) {
                    e.replyEmbeds(CMD.se("You do not have permission to perform this action")).setEphemeral(true).queue();
                    return;
                }

                event.getMessage().delete().queueAfter(1, TimeUnit.SECONDS);

                e.replyEmbeds(CMD.se("Request successful")).setEphemeral(true).queue();
            } else if (id.startsWith("next-")) {

                if (!(hasPerm(QOTDBot.config.getPermRoleID()) || hasPerm(QOTDBot.config.getManagerRoleID()) || isAdmin())) {
                    e.replyEmbeds(CMD.se("You do not have permission to perform this action")).setEphemeral(true).queue();
                    return;
                }

                int param = Integer.parseInt(id.replace("next-", ""));

                LinkedList<Question> q = QOTDBot.getQuestions();

                StringBuilder out = new StringBuilder();
                for (int i = 0; i < (Math.min(q.size() - param * 5, 5)); i++) {
                    String question = q.get(i + param * 5).getQuestion();
                    if (question.length() > 50) {
                        question = question.substring(0, 48) + "...";
                    }
                    out.append("\n**").append(i + param * 5).append(":** ").append(question);
                }

                if (out.toString().isBlank()) {
                    e.replyEmbeds(CMD.se("No next page.")).setEphemeral(true).queue();
                    return;
                }

                DateTimeFormatter format = DateTimeFormatter.ofPattern("dd/MM/yyyy • hh:mm");

                Button prevButton = Button.primary("prev-" + (param - 1), "\u2B05 Prev");
                Button nextButton = Button.primary("next-" + (param + 1), "Next \u27A1");
                Button deleteButton = Button.secondary("delete", "Delete");
                MessageEditData message = new MessageEditBuilder()
                        .setEmbeds(new EmbedBuilder()
                                .setTitle("**__QOTD Queue:__** *Page " + param + "*")
                                .setDescription(out.toString())
                                .setFooter(format.format(LocalDateTime.now()), e.getMember().getUser().getAvatarUrl())
                                .setColor(QOTDBot.config.getColor())
                                .build())
                        .setComponents(ActionRow.of(prevButton, nextButton, deleteButton))
                        .build();

                e.deferEdit().queue();
                e.getMessage().editMessage(message).queue();

            } else if (id.startsWith("prev-")) {

                if (!(hasPerm(QOTDBot.config.getPermRoleID()) || hasPerm(QOTDBot.config.getManagerRoleID()) || isAdmin())) {
                    e.replyEmbeds(CMD.se("You do not have permission to perform this action")).setEphemeral(true).queue();
                    return;
                }

                int param = Integer.parseInt(id.replace("prev-", ""));

                LinkedList<Question> q = QOTDBot.getQuestions();

                if (param < 0) {
                    e.replyEmbeds(CMD.se("No previous page.")).setEphemeral(true).queue();
                    return;
                }

                StringBuilder out = new StringBuilder();
                for (int i = 0; i < (Math.min(q.size() - param * 5, 5)); i++) {
                    String question = q.get(i + param * 5).getQuestion();
                    if (question.length() > 50) {
                        question = question.substring(0, 48) + "...";
                    }
                    out.append("\n**").append(i + param * 5).append(":** ").append(question);
                }
                DateTimeFormatter format = DateTimeFormatter.ofPattern("dd/MM/yyyy \u2022 hh:mm");

                Button prevButton = Button.primary("prev-" + (param - 1), "\u2B05 Prev");
                Button nextButton = Button.primary("next-" + (param + 1), "Next \u27A1");
                Button deleteButton = Button.secondary("delete", "Delete");
                MessageEditData message = new MessageEditBuilder()
                        .setEmbeds(new EmbedBuilder()
                                .setTitle("**__QOTD Queue:__** *Page " + param + "*")
                                .setDescription(out.toString())
                                .setFooter(format.format(LocalDateTime.now()), e.getMember().getUser().getAvatarUrl())
                                .setColor(QOTDBot.config.getColor())
                                .build())
                        .setComponents(ActionRow.of(prevButton, nextButton, deleteButton))
                        .build();

                e.deferEdit().queue();
                e.getMessage().editMessage(message).queue();

            }
        } catch (Exception e) {
            this.e.replyEmbeds(CMD.se("Request unsuccessful *(Hint: Embed possibly removed?)*")).setEphemeral(true).queue();
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

}
