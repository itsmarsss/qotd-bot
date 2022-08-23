package QOTDBot;

import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.List;
import java.util.concurrent.TimeUnit;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;

public class ButtonListener extends ListenerAdapter {
	private ButtonClickEvent e;
	public void onButtonClick(ButtonClickEvent event) {
		try {
			if(!event.getGuild().getId().equals(QOTDBot.config.getServerID()))
				return;

			e = event;

			if(!(hasPerm(QOTDBot.config.getManagerRoleID()) || isAdmin())) {
				e.reply("You do not have permission to perform this action").setEphemeral(true).queue();
				return;
			}

			String id = event.getButton().getId();
			if(id.equals("delete")) {
				event.getMessage().delete().queue();
			}else if(id.startsWith("delete-notif")) {
				event.getMessage().delete().queue();

				String commandId = event.getButton().getId().replace("delete-notif-", "");
				event.getChannel().retrieveMessageById(commandId).queue(message -> {
					try {
						message.delete().queue();
					}catch(Exception e) {
						message.addReaction("\uF6AB").queue();
					}
				});
			}else if(id.equals("approve-qotd")) {
				List<Field>flds = event.getMessage().getEmbeds().get(0).getFields();
				boolean isPoll = false;

				if(flds.get(0).getValue().equals("Poll")) {
					isPoll = true;
				}

				Question q = new Question(flds.get(1).getValue(), flds.get(2).getValue(), flds.get(3).getValue(), isPoll);
				q.setDate(flds.get(4).getValue());
				QOTDBot.add(q);

				event.getMessage().delete().queueAfter(1, TimeUnit.SECONDS);
			}else if(id.equals("deny-qotd")) {
				event.getMessage().delete().queueAfter(1, TimeUnit.SECONDS);
			}
		}catch(Exception e) {
			this.e.reply("Request unsuccessful *(Hint: Embed possibly removed?)*").setEphemeral(true).queue();
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
