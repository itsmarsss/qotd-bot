package QOTDBot;

import java.awt.Color;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class CMD extends ListenerAdapter{
	private GuildMessageReceivedEvent e;
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		if(event.getAuthor().isBot() || !event.getGuild().getId().equals(QOTDBot.config.getServerID()))
			return;
		Message msg = event.getMessage();
		String raw = msg.getContentRaw();
		String[]rawSplit = raw.toLowerCase().split(" ");
		// [prefix - 0] [cmd - 1] [parameter - 2 to ???]
		if(!rawSplit[0].equals(QOTDBot.config.getPrefix()) || rawSplit.length == 1) {
			return;
		}
		e = event;

		switch(rawSplit[1]) {
		case "help":
			help();
			break;
		}

		if(hasPerm(QOTDBot.config.getPermRoleID()) || hasPerm(QOTDBot.config.getManagerRoleID()) || isAdmin()) {
			switch(rawSplit[1]) {
			case "add":
				addQuestion(raw, event.getAuthor());
				break;
			case "addpoll":
				addPoll(raw, event.getAuthor());
				break;
			}
		}

		if(hasPerm(QOTDBot.config.getManagerRoleID()) || isAdmin()) {
			switch(rawSplit[1]) {
			case "remove":
				removeQuestion(raw);
				break;
			case "view":
				viewQuestion(raw);
				break;
			case "viewqueue":
				viewQueue();
				break;
			case "qotdtest":
				qotdTest();
				break;
			case "postnext":
				qotdNext();
				break;
			case "qotdchannel":
				qotdChannel(raw);
				break;
			case "pause":
				setPause(true);
				break;
			case "unpause":
				setPause(false);
				break;
			case "prefix":
				qotdPrefix(raw);
				break;
			}
		}
		if(isAdmin()) {
			switch(rawSplit[1]) {
			case "managerrole":
				qotdManager(raw);
				break;
			case "permrole":
				qotdPerm(raw);
			}
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

	private void addQuestion(String raw, User user) {
		// qotd add
		String[]param = raw.substring(QOTDBot.config.getPrefix().length()+1+3).split("-=-");
		for(int i = 0; i < param.length; i++) {
			param[i].trim();
		}
		if(param.length == 1 && !param[0].isBlank() && param[0].length() < 500) {
			Question q = new Question(param[0], user.getAsTag(), false);
			QOTDBot.add(q);
			e.getMessage().reply("**__Added the following;__**\n" + q).queue();
		}else if(param.length == 2 && !param[0].isBlank() && param[0].length() < 500 && param[1].length() < 100) {
			Question q = new Question(param[0], param[1], user.getAsTag(), false);
			QOTDBot.add(q);
			e.getMessage().reply("**__Added the following;__**\n" + q).queue();
		}else {
			e.getMessage().reply("Invalid parameters.").queue();
		}
	}

	private void addPoll(String raw, User user) {
		// qotd addpoll
		String[]param = raw.substring(QOTDBot.config.getPrefix().length()+1+7).split("-=-");
		for(int i = 0; i < param.length; i++) {
			param[i].trim();
		}
		if(param.length == 1 && !param[0].isBlank() && param[0].length() < 500) {
			Question q = new Question(param[0], user.getAsTag(), true);
			QOTDBot.add(q);
			e.getMessage().reply("**__Added the following;__**\n" + q).queue();
		}else if(param.length == 2 && !param[0].isBlank() && param[0].length() < 500 && param[1].length() < 100) {
			Question q = new Question(param[0], param[1], user.getAsTag(), true);
			QOTDBot.add(q);
			e.getMessage().reply("**__Added the following;__**\n" + q).queue();
		}else {
			e.getMessage().reply("Invalid parameters.").queue();
		}
	}
	
	private void removeQuestion(String raw) {
		// qotd remove
		try {
			int param = Integer.parseInt(raw.substring(QOTDBot.config.getPrefix().length()+1+6).trim());
			int status = QOTDBot.remove(param);
			if(status == -1) {
				e.getMessage().reply("Invalid number.").queue();
			}else{
				e.getMessage().reply("Index " + param + " has been removed from the queue.").queue();
			}
		}catch(Exception e) {
			this.e.getMessage().reply("Invalid number.").queue();
		}
	}

	private void viewQuestion(String raw) {
		// qotd view
		try {
			int param = Integer.parseInt(raw.substring(QOTDBot.config.getPrefix().length()+1+4).trim());
			Question q = QOTDBot.getQuestions().get(param);
			e.getMessage().reply("**__QOTD #" + param + ";__**\n" + q).queue();
		}catch(Exception e) {
			this.e.getMessage().reply("Invalid number.").queue();
		}
	}

	private void viewQueue() {
		// qotd showqueue
		String out = "**__QOTD Queue:__**";
		int c = 0;
		for(Question q : QOTDBot.getQuestions()) {
			out = out + "\n" + c + ": " + q.getQuestion();
			c++;
		}
		try {
			e.getMessage().reply(out).queue();
		}catch(Exception e) {
			this.e.getMessage().reply("Too large lol.").queue();
		}
	}

	private void qotdTest() {
		// qotd testqotd
		EmbedBuilder QOTDEmbed = new EmbedBuilder();
		QOTDEmbed.setAuthor("Added by: *author here*", null, QOTDBot.jda.getSelfUser().getAvatarUrl())
		.setTitle("❔❓ QOTD For Today! ❔❓\n**Question/Poll:** *question here*")
		.setDescription("*footer here*")
		.setFooter("Added on: *date here*")
		.setColor(new Color(230, 33, 39));
		e.getMessage().replyEmbeds(QOTDEmbed.build()).queue(msg -> {
			msg.addReaction("✅");
			msg.addReaction("❎");
		});
	}
	
	private void qotdNext() {
		// qotd postnext
		QOTDBot.postQOTD();
	}

	private void qotdChannel(String raw) {
		// qotd qotdchannel
		String param = raw.substring(QOTDBot.config.getPrefix().length()+1+11).trim();
		boolean exists = false;
		for(GuildChannel ch : e.getGuild().getChannels()) {
			if(ch.getId().equals(param)) {
				exists = true;
			}
		}
		if(exists) {
			QOTDBot.config.setChannelID(param);
			e.getMessage().reply("QOTD channel has been changed to <#" + param + ">.").queue();
		}else{
			e.getMessage().reply("Invalid channel id.").queue();
		}
	}

	private void setPause(boolean status) {
		// qotd pause
		QOTDBot.setPause(status);
		this.e.getMessage().reply("QOTD bot paused: **" + status + "**").queue();
	}
	
	private void qotdPrefix(String raw) {
		// qotd prefix
		try {
			String param = raw.split(" ")[2].trim();
			QOTDBot.config.setPrefix(param);
			e.getMessage().reply("QOTD prefix has been changed to `" + param + "`.").queue();
			QOTDBot.jda.getPresence().setActivity(Activity.watching("for '" + QOTDBot.config.getPrefix() + " help'"));
		}catch(Exception e) {
			this.e.getMessage().reply("Invalid prefix.");
		}
	}

	private void qotdManager(String raw) {
		// qotd managerrole
		String param = raw.substring(QOTDBot.config.getPrefix().length()+1+11).trim();
		if(param.equalsIgnoreCase("everyone"))
			return;
		boolean exists = false;
		for(Role r : e.getGuild().getRoles()) {
			if(r.getId().equals(param)) {
				exists = true;
			}
		}
		if(exists) {
			QOTDBot.config.setManagerRoleID(param);
			e.getMessage().reply("QOTD manager role has been changed to <@&" + param + ">.").queue();
		}else {
			e.getMessage().reply("Invalid role id.").queue();
		}
	}

	private void qotdPerm(String raw) {
		// qotd permrole
		String param = raw.substring(QOTDBot.config.getPrefix().length()+1+8).trim();
		if(param.equalsIgnoreCase("everyone")) {
			e.getMessage().reply("QOTD perm role has been changed; `everyone` can post questions.").queue();
			QOTDBot.config.setPermRoleID("everyone");
			return;
		}
		boolean exists = false;
		for(Role r : e.getGuild().getRoles()) {
			if(r.getId().equals(param)) {
				exists = true;
			}
		}
		if(exists) {
			QOTDBot.config.setPermRoleID(param);
			e.getMessage().reply("QOTD perm role has been changed to <@&" + param + ">.").queue();
		}else {
			e.getMessage().reply("Invalid role id.").queue();
		}
	}








	private void help() {
		// qotd help
		e.getMessage().reply(
				"**Commands**"
						+ "\n`" + QOTDBot.config.getPrefix() + " help`"
						+ "\n**Perm commands**"
						+ "\n`" + QOTDBot.config.getPrefix() + " add <question 500 char>-=-<footer 100 char>`"
						+ "\n`" + QOTDBot.config.getPrefix() + " addpoll <question 500 char>-=-<footer 100 char>`"
						+ "\n**Manager commands:**"
						+ "\n`" + QOTDBot.config.getPrefix() + " remove <index>`"
						+ "\n`" + QOTDBot.config.getPrefix() + " view <index>`"
						+ "\n`" + QOTDBot.config.getPrefix() + " viewqueue`"
						+ "\n`" + QOTDBot.config.getPrefix() + " qotdtest`"
						+ "\n`" + QOTDBot.config.getPrefix() + " postnext`"
						+ "\n`" + QOTDBot.config.getPrefix() + " pause`"
						+ "\n`" + QOTDBot.config.getPrefix() + " unpause`"
						+ "\n`" + QOTDBot.config.getPrefix() + " prefix <prefix, no space>`"
						+ "\n**Admin commands:**"
						+ "\n`" + QOTDBot.config.getPrefix() + " permrole <role id/'everyone'>`"
						+ "\n`" + QOTDBot.config.getPrefix() + " managerrole <role id/'everyone'>`").queue();
	}
}
