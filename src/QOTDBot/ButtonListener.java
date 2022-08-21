package QOTDBot;

import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.concurrent.TimeUnit;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;

public class ButtonListener extends ListenerAdapter {
	private ButtonClickEvent e;
	public void onButtonClick(ButtonClickEvent event) {
		if(!event.getGuild().getId().equals(QOTDBot.config.getServerID()))
			return;

		e = event;

		if(!(hasPerm(QOTDBot.config.getManagerRoleID()) || isAdmin())) {
			e.reply("No permission").setEphemeral(true).queue();
			return;
		}

		String id = event.getButton().getId();
		if(id.startsWith("delete-notif")) {
			event.getMessage().delete().queue();

			String commandId = event.getButton().getId().replace("delete-notif-", "");
			event.getChannel().retrieveMessageById(commandId).queue(message -> {
				try {
					message.delete().queue();
				}catch(Exception e) {
					message.addReaction("🚫").queue();
				}
			});
		}
		if(id.equals("approve-qotd")) {
			String[]content = event.getMessage().getContentRaw().split("\r?\n|\r");
			boolean isPoll = false;

			if(content[1].equals("Poll")) {
				isPoll = true;
			}

			Question q = new Question(content[2], content[3], content[4], isPoll);
			q.setDate(content[5]);
			QOTDBot.add(q);

			event.getMessage().delete().queueAfter(1, TimeUnit.SECONDS);
		}
		if(id.equals("deny-qotd")) {
			event.getMessage().delete().queueAfter(1, TimeUnit.SECONDS);
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
