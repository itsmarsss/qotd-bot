package QOTDBot;

import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;

public class ButtonListener extends ListenerAdapter {
	private ButtonClickEvent e;
	public void onButtonClick(ButtonClickEvent event) {
		try {
			if(!event.getGuild().getId().equals(QOTDBot.config.getServerID()))
				return;

			e = event;

			String id = event.getButton().getId();
			if(id.equals("delete")) {

				if(!(hasPerm(QOTDBot.config.getPermRoleID()) || hasPerm(QOTDBot.config.getManagerRoleID()) || isAdmin())) {
					e.replyEmbeds(CMD.se("You do not have permission to perform this action")).setEphemeral(true).queue();
					return;
				}

				event.getMessage().delete().queue();
				e.replyEmbeds(CMD.se("Request successful")).setEphemeral(true).queue();
			}else if(id.startsWith("delete-notif")) {

				if(!(hasPerm(QOTDBot.config.getPermRoleID()) || hasPerm(QOTDBot.config.getManagerRoleID()) || isAdmin())) {
					e.replyEmbeds(CMD.se("You do not have permission to perform this action")).setEphemeral(true).queue();
					return;
				}

				event.getMessage().delete().queue();

				String commandId = event.getButton().getId().replace("delete-notif-", "");
				event.getChannel().retrieveMessageById(commandId).queue(message -> {
					try {
						message.delete().queue();
					}catch(Exception e) {
						message.addReaction("\uF6AB").queue();
					}
				});

				e.replyEmbeds(CMD.se("Request successful")).setEphemeral(true).queue();
			}else if(id.equals("approve-qotd")) {

				if(!(hasPerm(QOTDBot.config.getManagerRoleID()) || isAdmin())) {
					e.replyEmbeds(CMD.se("You do not have permission to perform this action")).setEphemeral(true).queue();
					return;
				}

				List<Field>flds = event.getMessage().getEmbeds().get(0).getFields();
				boolean isPoll = false;

				if(flds.get(0).getValue().equals("Poll")) {
					isPoll = true;
				}

				Question q = new Question(flds.get(1).getValue(), flds.get(2).getValue(), flds.get(3).getValue(), isPoll);
				q.setDate(flds.get(4).getValue());
				QOTDBot.add(q);

				event.getMessage().delete().queueAfter(1, TimeUnit.SECONDS);

				e.replyEmbeds(CMD.se("Request successful")).setEphemeral(true).queue();
			}else if(id.equals("deny-qotd")) {

				if(!(hasPerm(QOTDBot.config.getManagerRoleID()) || isAdmin())) {
					e.replyEmbeds(CMD.se("You do not have permission to perform this action")).setEphemeral(true).queue();
					return;
				}

				event.getMessage().delete().queueAfter(1, TimeUnit.SECONDS);

				e.replyEmbeds(CMD.se("Request successful")).setEphemeral(true).queue();
			}else if(id.startsWith("next-")) {

				if(!(hasPerm(QOTDBot.config.getPermRoleID()) || hasPerm(QOTDBot.config.getManagerRoleID()) || isAdmin())) {
					e.replyEmbeds(CMD.se("You do not have permission to perform this action")).setEphemeral(true).queue();
					return;
				}

				int param = Integer.parseInt(id.replace("next-", ""));

				LinkedList<Question> q = QOTDBot.getQuestions();

				String out = "";
				for(int i = 0; i < (q.size()-param*5 < 5 ? q.size()-param*5 : 5); i++) {
					String question = q.get(i+param*5).getQuestion();
					if(question.length() > 50) {
						question = question.substring(0, 48) + "...";
					}
					out = out + "\n**" + (i+param*5) + ":** " + question;
				}
				
				if(out.isBlank()) {
					e.replyEmbeds(CMD.se("No next page.")).setEphemeral(true).queue();
					return;
				}
				
				DateTimeFormatter format = DateTimeFormatter.ofPattern("dd/MM/yyyy • hh:mm");

				Button prevButton = Button.primary("prev-"+(param-1), "\u2B05 Prev");
				Button nextButton = Button.primary("next-"+(param+1), "Next \u27A1");
				Button deleteButton = Button.secondary("delete", "Delete");
				Message message = new MessageBuilder()
						.setEmbeds(new EmbedBuilder()
								.setTitle("**__QOTD Queue:__** *Page " + param + "*")
								.setDescription(out)
								.setFooter(format.format(LocalDateTime.now()), e.getMember().getUser().getAvatarUrl())
								.setColor(QOTDBot.config.getColor())
								.build())
						.setActionRows(ActionRow.of(prevButton, nextButton, deleteButton))
						.build();
				
				e.deferEdit().queue();
				e.getMessage().editMessage(message).queue();

			}else if(id.startsWith("prev-")) {

				if(!(hasPerm(QOTDBot.config.getPermRoleID()) || hasPerm(QOTDBot.config.getManagerRoleID()) || isAdmin())) {
					e.replyEmbeds(CMD.se("You do not have permission to perform this action")).setEphemeral(true).queue();
					return;
				}

				int param = Integer.parseInt(id.replace("prev-", ""));

				LinkedList<Question> q = QOTDBot.getQuestions();
				
				if(param < 0) {
					e.replyEmbeds(CMD.se("No previous page.")).setEphemeral(true).queue();
					return;
				}

				String out = "";
				for(int i = 0; i < (q.size()-param*5 < 5 ? q.size()-param*5 : 5); i++) {
					String question = q.get(i+param*5).getQuestion();
					if(question.length() > 50) {
						question = question.substring(0, 48) + "...";
					}
					out = out + "\n**" + (i+param*5) + ":** " + question;
				}
				DateTimeFormatter format = DateTimeFormatter.ofPattern("dd/MM/yyyy • hh:mm");

				Button prevButton = Button.primary("prev-"+(param-1), "\u2B05 Prev");
				Button nextButton = Button.primary("next-"+(param+1), "Next \u27A1");
				Button deleteButton = Button.secondary("delete", "Delete");
				Message message = new MessageBuilder()
						.setEmbeds(new EmbedBuilder()
								.setTitle("**__QOTD Queue:__** *Page " + param + "*")
								.setDescription(out)
								.setFooter(format.format(LocalDateTime.now()), e.getMember().getUser().getAvatarUrl())
								.setColor(QOTDBot.config.getColor())
								.build())
						.setActionRows(ActionRow.of(prevButton, nextButton, deleteButton))
						.build();
				
				e.deferEdit().queue();
				e.getMessage().editMessage(message).queue();

			}
		}catch(Exception e) {
			this.e.replyEmbeds(CMD.se("Request unsuccessful *(Hint: Embed possibly removed?)*")).setEphemeral(true).queue();
		}
	}

	private boolean hasPerm(String ID) {
		if(ID.equals("everyone"))
			return true;
		for(Role r : e.getMember().getRoles()) {
			if(r.getId().equals(ID)) {
				return true;
			}
		}
		return false;
	}

	private boolean isAdmin() {
		return e.getMember().hasPermission(Permission.ADMINISTRATOR);
	}

}
