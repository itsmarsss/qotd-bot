package com.marsss.qotdbot;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class Question {
    private String question;
    private String footer;
    private String author;
    private String date;
    private long millis;
    private boolean isPoll;

    public Question(String q, String f, String a, boolean p) {
        setQuestion(q);
        setFooter(f);
        setAuthor(a);
        setIsPoll(p);
        updateDate();
    }

    public Question(String q, String a, boolean p) {
        setQuestion(q);
        setFooter("n/a");
        setAuthor(a);
        setIsPoll(p);
        updateDate();
    }


    public void updateDate() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        date = LocalDateTime.now().format(dtf);
        millis = System.currentTimeMillis();
    }

    public void setDate(long millis) {
        DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        this.date = df.format(new Date(millis));
        this.millis = millis;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setIsPoll(boolean p) {
        isPoll = p;
    }

    public void setAuthor(String a) {
        author = a;
    }

    public void setFooter(String f) {
        footer = f;
    }

    public void setQuestion(String q) {
        question = q;
    }

    boolean isPoll() {
        return isPoll;
    }

    String getDate() {
        return date;
    }

    long getMillis() {
        return millis;
    }

    String getAuthor() {
        return author;
    }

    String getFooter() {
        return footer;
    }

    String getQuestion() {
        return question;
    }

    public MessageEmbed createEmbed() {
        String iswhat = "Question";
        if (isPoll()) {
            iswhat = "Poll";
        }
        EmbedBuilder QOTDEmbed = new EmbedBuilder();
        QOTDEmbed.setAuthor("Added by: " + getAuthor())
                .setTitle("**" + iswhat + ":** " + getQuestion())
                .setDescription("*" + getFooter() + "*")
                .setFooter("Added on: " + getDate())
                .setColor(QOTDBot.config.getColor());
        return QOTDEmbed.build();
    }

    public String toString() {
        String iswhat = "Question";
        if (isPoll()) {
            iswhat = "Poll";
        }
        return
                "**" + iswhat + ":** " + getQuestion()
                        + "\n**Footer:** " + getFooter()
                        + "\n**Author:** " + getAuthor()
                        + "\n**Date:** " + getDate();
    }
}
