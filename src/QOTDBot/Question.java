package QOTDBot;

import java.awt.Color;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;

public class Question {
	private String question;
	private String footer;
	private User author;
	private String date;
	private boolean isPoll;

	public Question(String q, String f, User a, boolean p) {
		setQuestion(q);
		setFooter(f);
		setAuthor(a);
		setIsPoll(p);
		updateDate();
	}
	
	public Question(String q, User a, boolean p) {
		setQuestion(q);
		setFooter("n/a");
		setAuthor(a);
		setIsPoll(p);
		updateDate();
	}

	
	public void updateDate() {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
		String now = LocalDateTime.now().format(dtf);
		date = now;
	}
	public void setIsPoll(boolean p) {
		isPoll = p;
	}
	public void setAuthor(User a) {
		author = a;
	}
	public void setFooter(String f) {
		footer = f;
	}
	public void setQuestion(String q) {
		question = q;
	}
	
	public boolean isPoll() {
		return isPoll;
	}
	private String getDate() {
		return date;  
	}
	private User getAuthor() {
		return author;
	}
	private String getFooter() {
		return footer;
	}
	public String getQuestion() {
		return question;
	}
	
	public MessageEmbed createEmbed() {
		String iswhat = "Question";
		if(isPoll()) {
			iswhat = "Poll";
		}
		EmbedBuilder QOTDEmbed = new EmbedBuilder();
		QOTDEmbed.setAuthor("Added by: " + getAuthor().getAsTag(), null, getAuthor().getAvatarUrl())
		.setTitle("❔❓ QOTD For Today! ❔❓\n**" + iswhat + ":** " + getQuestion())
		.setDescription("*" + getFooter() + "*")
		.setFooter("Added on: " + getDate())
		.setColor(new Color(230, 33, 39));
		return QOTDEmbed.build();
	}
	public String toString() {
		String iswhat = "Question";
		if(isPoll()) {
			iswhat = "Poll";
		}
		return "**" + iswhat + ":** " + getQuestion() + "\n**Footer:** " + getFooter() + "\n**Author:** " + getAuthor().getAsTag() + "\n**Date:** " + getDate();
	}
	
}
