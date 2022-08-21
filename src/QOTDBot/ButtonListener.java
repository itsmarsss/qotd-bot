package QOTDBot;

import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;

public class ButtonListener extends ListenerAdapter {
	public void onButtonClick(ButtonClickEvent event) {
		try {
			if(event.getButton().getId().startsWith("delete-notif")) {
				event.getMessage().delete().queue();

				String commandId = event.getButton().getId().replace("delete-notif-", "");
				event.getChannel().retrieveMessageById(commandId).queue(message -> {
					message.delete().queue();
				});
			}
		}catch(Exception e) {}
	}
}
