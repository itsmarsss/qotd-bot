package QOTDBot;

import java.util.LinkedList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;

public class QOTD {
	private JDA builder;
	private LinkedList<Question> questions = new LinkedList<Question>();
	public QOTD(String token) throws LoginException, InterruptedException {
		builder = JDABuilder.createDefault(token).build();
        System.out.println("Bot startup successful");        
        builder.addEventListener(new CMD());

        builder.awaitReady();
        
        builder.getPresence().setActivity(Activity.watching("for '" + Main.prefix + " help'"));
        
        ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
		ses.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
		        Question q = getNext();
            	builder.getTextChannelById(Main.channelID).sendMessageEmbeds(q.createEmbed()).queue();
            	System.out.println("=============================");
            	System.out.println(q);
			}
		}, 0, Main.interval, TimeUnit.HOURS);
	}
	
	public Question getNext() {
		if(questions.isEmpty()) {
			questions.add(new Question("Can someone add more questions? My queue is empty... :slight_smile:", "ADD QUESTION PLS", builder.getSelfUser()));
		}
		return questions.poll();
	}
	public int remove(int index) {
		if(index < 0 || index >= questions.size())
			return -1;
		questions.remove(index);
		return 0;
	}
	public void add(Question q) {
		questions.add(q);
	}
	public LinkedList<Question> getQuestions(){
		return questions;
	}
}
