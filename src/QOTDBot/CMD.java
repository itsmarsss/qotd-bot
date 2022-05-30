package QOTDBot;

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
		if(event.getAuthor().isBot())
			return;
		e = event;
		Message msg = event.getMessage();
		String raw = msg.getContentRaw();
		String[]rawSplit = raw.toLowerCase().split(" ");
		// [prefix - 0] [cmd - 1] [parameter - 2 to ???]
		if(!rawSplit[0].equals(Main.prefix) || rawSplit.length == 1) {
			return;
		}
		switch(rawSplit[1]) {
		case "add":
			addQuestion(raw, event.getAuthor());
			break;
		case "view":
			showQuestion(raw);
			break;
		case "viewqueue":
			showQueue();
			break;
		case "help":
			help();
			break;
		}
		boolean manager = false;
		for(Role r : event.getMember().getRoles()) {
			if(r.getId().equals(Main.managerRoleID)) {
				manager = true;
			}
		}
		if(manager || event.getMember().isOwner()) {
			switch(rawSplit[1]) {
			case "remove":
				removeQuestion(raw);
				break;
			case "qotdchannel":
				qotdChannel(raw);
				break;
			case "interval":
				qotdInterval(raw);
				break;
			case "prefix":
				prefix(raw);
				break;
			}
		}
		if(event.getMember().isOwner()) {
			switch(rawSplit[1]) {
			case "managerrole":
				qotdManager(raw);
				break;
			}
		}
	}

	private void prefix(String raw) {
		// qotd prefix
		try {
			String param = raw.split(" ")[2].trim();
			Main.prefix = param;
			e.getMessage().reply("QOTD prefix has been changed to `" + param + "`.").queue();
			Main.qotd.builder.getPresence().setActivity(Activity.watching("for '" + Main.prefix + " help'"));
		}catch(Exception e) {
			this.e.getMessage().reply("Invalid prefix.");
		}
	}

	private void qotdManager(String raw) {
		// qotd managerrole
		String param = raw.substring(Main.prefix.length()+1+11).trim();
		boolean exists = false;
		for(Role r : e.getGuild().getRoles()) {
			if(r.getId().equals(param)) {
				exists = true;
			}
		}
		if(exists) {
			Main.managerRoleID = param;
			e.getMessage().reply("QOTD manager role has been changed to <@&" + param + ">.").queue();
		}else{
			e.getMessage().reply("Invalid role id.").queue();
		}
	}

	private void qotdInterval(String raw) {
		// qotd interval
		try {
			int param = Integer.parseInt(raw.substring(Main.prefix.length()+1+8).trim());
			if(param < 1 || param > 240) {
				e.getMessage().reply("Invalid number.").queue();
			}else{
				Main.interval = param;
				e.getMessage().reply("QOTD interval has been changed to " + param + " hour(s).").queue();
			}
		}catch(Exception e) {
			this.e.getMessage().reply("Invalid number.").queue();
		}
	}

	private void qotdChannel(String raw) {
		// qotd qotdchannel
		String param = raw.substring(Main.prefix.length()+1+11).trim();
		boolean exists = false;
		for(GuildChannel ch : e.getGuild().getChannels()) {
			if(ch.getId().equals(param)) {
				exists = true;
			}
		}
		if(exists) {
			Main.channelID = param;
			e.getMessage().reply("QOTD channel has been changed to <#" + param + ">.").queue();
		}else{
			e.getMessage().reply("Invalid channel id.").queue();
		}
	}

	private void addQuestion(String raw, User user) {
		// qotd add
		String[]param = raw.substring(Main.prefix.length()+1+3).split("-=-");
		for(int i = 0; i < param.length; i++) {
			param[i].trim();
		}
		if(param.length == 1 && !param[0].isBlank() && param[0].length() < 500) {
			Question q = new Question(param[0], user);
			Main.qotd.add(q);
			e.getMessage().reply("**__Added the following;__**\n" + q).queue();
		}else if(param.length == 2 && !param[0].isBlank() && param[0].length() < 500 && param[1].length() < 100) {
			Question q = new Question(param[0], param[1], user);
			Main.qotd.add(q);
			e.getMessage().reply("**__Added the following;__**\n" + q).queue();
		}else {
			e.getMessage().reply("Invalid parameters.").queue();
		}
	}

	private void removeQuestion(String raw) {
		// qotd remove
		try {
			int param = Integer.parseInt(raw.substring(Main.prefix.length()+1+6).trim());
			int status = Main.qotd.remove(param);
			if(status == -1) {
				e.getMessage().reply("Invalid number.").queue();
			}else{
				e.getMessage().reply("Index " + param + " has been removed from the queue.").queue();
			}
		}catch(Exception e) {
			this.e.getMessage().reply("Invalid number.").queue();
		}
	}

	private void showQuestion(String raw) {
		// qotd view
		try {
			int param = Integer.parseInt(raw.substring(Main.prefix.length()+1+4).trim());
			Question q = Main.qotd.getQuestions().get(param);
			e.getMessage().reply("**__QOTD #" + param + ";__**\n" + q).queue();
		}catch(Exception e) {
			this.e.getMessage().reply("Invalid number.").queue();
		}
	}

	private void showQueue() {
		// qotd showqueue
		String out = "**__QOTD Queue:__**";
		int c = 0;
		for(Question q : Main.qotd.getQuestions()) {
			out = out + "\n" + c + ": " + q.getQuestion();
			c++;
		}
		try {
			e.getMessage().reply(out).queue();
		}catch(Exception e) {
			this.e.getMessage().reply("Too large lol.").queue();
		}
	}

	private void help() {
		// qotd help
		e.getMessage().reply(
				"**Commands**"
						+ "\n`" + Main.prefix + " add <question 500 char>-=-<footer 100 char>`"
						+ "\n`" + Main.prefix + " viewqueue`"
						+ "\n`" + Main.prefix + " view <index>`"
						+ "\n`" + Main.prefix + " help`"
						+ "\n**Manager commands:**"
						+ "\n`" + Main.prefix + " remove <index>`"
						+ "\n`" + Main.prefix + " qotdchannel <channel id>`"
						+ "\n`" + Main.prefix + " interval <hour(s) 1 to 240>` - faulty"
						+ "\n`" + Main.prefix + " prefix <prefix, no space>`"
						+ "\n**Owner commands:**"
						+ "\n`" + Main.prefix + " managerrole <role id>`").queue();
	}
}
