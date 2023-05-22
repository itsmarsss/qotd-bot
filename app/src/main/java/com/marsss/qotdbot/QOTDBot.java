package com.marsss.qotdbot;

import com.marsss.qotdbot.ui.ConsoleMirror;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class QOTDBot {
    static Config config;
    static JDA jda;

    static long lastQOTD = 0;

    private static final HashMap<String, Question> questions = new HashMap<>();
    private static final LinkedList<String> uuids = new LinkedList<>();


    private static final HashMap<String, Question> questions_review = new HashMap<>();
    private static final LinkedList<String> uuids_review = new LinkedList<>();
    private static boolean isPaused = false;

    static final String version = "3.1.1";
    private static String parent;
    private static final EnumSet<GatewayIntent> intent = EnumSet.of(
            GatewayIntent.GUILD_MESSAGES,
            GatewayIntent.GUILD_EMOJIS_AND_STICKERS,
            GatewayIntent.MESSAGE_CONTENT);

    private static boolean head = true;

    private final static String template = "# IMPORTANT - Mandatory fields:\r\n"
            + "# Input the bot's token here, this can be found in Developers Portal > Applications > [Bot Profile] > Bot > Token > [Copy]\r\n"
            + "botToken: %s\r\n"
            + "\r\n"
            + "# Turn on developers mode in Settings > Advanced > Developer Mode, right click your Discord Server and click on [Copy ID]\r\n"
            + "serverID: %s\r\n"
            + "\r\n"
            + "# Right click your QOTD channel and click on [Copy ID]\r\n"
            + "channelID: %s\r\n"
            + "\r\n"
            + "# Set a prefix here for the bot, this is what members use to use the bot (e.g. qotd help)\r\n"
            + "prefix: %s\r\n"
            + "\r\n"
            + "# Input number of minute(s) until another QOTD is sent ( 1 to 1440 [24 hours] )\r\n"
            + "interval: %s\r\n"
            + "\r\n"
            + "# Start time, this dictates what time of the day your QOTD will be sent (24 hours time, local server time)\r\n"
            + "# Hours\r\n"
            + "hour: %s\r\n"
            + "# Minutes\r\n"
            + "minute: %s\r\n"
            + "\r\n"
            + "# Not mandatory fields:\r\n"
            + "# Set a perm role, these members can add QOTDs (write everyone if everyone)\r\n"
            + "permRoleID: %s\r\n"
            + "\r\n"
            + "# Set a manager role, these members can manage QOTDs (write everyone if everyone)\r\n"
            + "managerRoleID: %s\r\n"
            + "\r\n"
            + "# Dynamic config.yml, config.yml is changed as its values are changed\r\n"
            + "dynamicConfig: %s\r\n"
            + "\r\n"
            + "# QOTD submission review settings\r\n"
            + "# Set to true if you want QOTD submissions to go through bot manager review\r\n"
            + "managerReview: %s\r\n"
            + "# Right click your QOTD review channel and click on [Copy ID], this is where QOTD submissions are reviewed\r\n"
            + "reviewChannel: %s\r\n"
            + "\r\n"
            + "# QOTD Embed color in hex (Do not include \"#\")\r\n"
            + "QOTDColor: %s\r\n"
            + "";

    public static void main(String[] args) throws URISyntaxException {
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("--nohead")) {
                head = false;
            }
        }
        if (head) {
            System.out.println("Loading UI...");

            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                ex.printStackTrace();
                System.out.println("Failed to set look and feel");
                System.out.println("\tYou can ignore this");
            }

            new ConsoleMirror();
        }

        System.out.println("  ____   ____ _______ _____    ____   ____ _______ ");
        System.out.println(" / __ \\ / __ \\__   __|  __ \\  |  _ \\ / __ \\__   __|");
        System.out.println("| |  | | |  | | | |  | |  | | | |_) | |  | | | |  ");
        System.out.println("| |  | | |  | | | |  | |  | | |  _ <| |  | | | |   ");
        System.out.println("| |__| | |__| | | |  | |__| | | |_) | |__| | | |");
        System.out.println(" \\___\\_\\\\____/  |_|  |_____/  |____/ \\____/  |_|  ");
        System.out.println("--------------------------------------------------");
        System.out.println("   =========== PROGRAM SOURCE CODE =========");
        System.out.println("   = https://github.com/itsmarsss/QOTD-Bot =");
        System.out.println("   =========================================");
        System.out.println("      Welcome to QOTD Bot's Control Prompt");
        System.out.println();
        System.out.println("Purpose: This bot allows for daily (or custom timed) QOTD to be sent in a specific channel. It allows users to add their own QOTD to the bot's queue, and QOTD managers to manage the queue.");
        System.out.println();
        System.out.println("Note: This program will only run for 1 Discord Server, if you have multiple Discord Servers that you want this program to work on, then you will need to run multiple copies of this program in different directories (Make sure to set Server ID and Channel ID in each config.yml)");
        System.out.println();
        System.out.println("Warning[1]: Use this program at your own risk, I (the creator of this program) will not be liable for any issues that this program causes to your Discord Server or computer (or sanity?)");
        System.out.println();
        System.out.println("Version:" + versionCheck());
        System.out.println();
        parent = new File(QOTDBot.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();
        System.out.println("Path: " + parent);

        boolean parentPass = true;
        if (parent == null) {
            System.out.println("______________________________________________________");
            System.out.println("Unable to obtain path.");
            if (!head) {
                System.exit(0);
            }
            parentPass = false;
        }

        if (parentPass) {
            readConfig();
        }
    }

    static void readConfig() {
        System.out.println();
        boolean configPass = true;
        if (!readConfigYML()) {
            System.out.println("______________________________________________________");
            System.out.println("There was an error with config.yml");
            System.out.println("\t1. Make sure config.yml template exists");
            System.out.println("\t2. Make sure config.yml values are correctly inputted");
            if (!head) {
                System.exit(0);
            }
            configPass = false;
        }
        if (configPass) {
            prompt();
        }
    }

    static void prompt() {
        System.out.println("~ Successfully read config.yml ~");
        System.out.println();
        System.out.println(head ? "** Click [Start] button to start the bot **" : "** Press [enter] to start the bot **");
        Scanner sc = new Scanner(System.in);
        sc.nextLine();
        sc.close();
        start();
    }

    public static void start() {
        boolean setupPass = true;
        try {
            System.out.println("Connecting to Discord...");
            System.out.println("Validating token...");
            jda = JDABuilder.createDefault(config.getBotToken(), intent).build();
            jda.awaitReady();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("______________________________________________________");
            System.out.println("Given token is invalid.");
            System.out.println("\t- Make sure to enable MESSAGE CONTENT INTENT");
            if (!head) {
                System.exit(0);
            }
            setupPass = false;
        }
        if (setupPass) {
            activate();
        }
    }

    static void activate() {
        jda.getPresence().setActivity(Activity.watching("for " + config.getPrefix() + " help"));
        System.out.println("Setting status message...");
        jda.getPresence().setStatus(OnlineStatus.DO_NOT_DISTURB);
        System.out.println("Setting status...");

        System.out.println("Checking server ID...");
        boolean found = false;
        for (Guild guild : jda.getGuilds()) {
            if (guild.getId().equals(config.getServerID())) {
                found = true;
            }
        }

        boolean serverIDPass = true;
        if (!found) {
            System.out.println("______________________________________________________");
            System.out.println("Given server ID is invalid.");
            if (!head) {
                System.exit(0);
            }
            serverIDPass = false;
        }
        if (serverIDPass) {
            complete();
        }
    }

    static void complete() {
        System.out.println("Adding listeners...");
        jda.addEventListener(new CMD());
        jda.addEventListener(new ButtonListener());
        System.out.println("Done!");

        int wait = calculateWaitTime();

        lastQOTD = (System.currentTimeMillis() + (wait * 60000L)) - config.getInterval() * 60000L;

        startThread(wait);

        System.out.println();
        System.out.println("----- INFO -----");
        System.out.println("\tTime until start time: " + wait + " minutes");
        System.out.printf("\tStart time: %s hours and %s minutes from midnight (00:00)", config.getHour(), config.getMinute());
        System.out.println("\n--- INFO END ---");

        System.out.println();
        System.out.println("Looking for questions.json...");
        if (readQuestionsJSON("questions.json")) {
            System.out.println("~ Successfully read questions.json ~");
            System.out.println("\tAppended " + questions.size() + " questions");
            System.out.println("\tWarning: Invalid questions have been deleted from the file");
        } else {
            System.out.println("- questions.json not found or is improperly formatted -");
        }

        if (readQuestionsReviewJSON("review.json")) {
            System.out.println("~ Successfully read review.json ~");
            System.out.println("\tAppended " + questions_review.size() + " questions for review");
            System.out.println("\tWarning: Invalid questions have been deleted from the file");
        } else {
            System.out.println("- review.json not found or is improperly formatted -");
        }

        System.out.println();
        System.out.println("Preparing upload.json");
        if (prepUploadJSON()) {
            System.out.println("~ Successfully prepared upload.json ~");
        } else {
            System.out.println("- Unable to prepare upload.json -");
        }

        System.out.println();
        System.out.println("Finished!");

        setupWebpage();
    }

    static void setupWebpage() {
        System.out.println();
        System.out.println("Starting Webserver...");
        try {
            Webserver server = new Webserver();
            server.startServer();

            System.out.println("Webpage setup completed!");
            System.out.println("\tOn port: " + server.getPort());

            System.out.println();
            System.out.println("Opening control panel...");

            try {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(new URI("http://localhost:" + server.getPort()));
                }
                System.out.println("Successfully sent user to control panel...");
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Failed to open website.");
                System.out.println("\tVisit http://localhost:" + server.getPort() + " to access control panel.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to start Webserver.");
            System.out.println("\tError message: " + e.getMessage());
        }
    }

    static Question getNext() {
        if (questions.isEmpty()) {
            questions.put(UUID.randomUUID().toString(), new Question("Can someone add more questions? My queue is empty... :slight_smile:", "ADD QUESTION PLS", jda.getSelfUser().getAsTag(), false));
        }
        String uuid = uuids.poll();
        Question temp = questions.get(uuid);
        questions.remove(uuid);
        return temp;
    }

    static int remove(int index) {
        if (index < 0 || index >= questions.size())
            return -1;

        String uuid = uuids.get(index);
        uuids.remove(index);
        questions.remove(uuid);
        writeQuestionsJSON();
        return 0;
    }

    static int remove(String uuid) {
        int index = uuids.indexOf(uuid);
        if(index == -1)
            return -1;

        uuids.remove(index);
        questions.remove(uuid);
        writeQuestionsJSON();
        return 0;
    }

    static void add(Question q) {
        String uuid = UUID.randomUUID().toString();
        uuids.add(uuid);
        questions.put(uuid, q);
        writeQuestionsJSON();
    }

    static String addReview(Question q) {
        String uuid = UUID.randomUUID().toString();
        uuids_review.add(uuid);
        questions_review.put(uuid, q);
        writeQuestionsReviewJSON();

        return uuid;
    }

    static LinkedList<Question> getQuestions() {
        LinkedList<Question> questionsList = new LinkedList<>();

        for(String uuid : uuids) {
            questionsList.add(questions.get(uuid));
        }

        return questionsList;
    }

    static HashMap<String, Question> getQueueWithUUID() {
        return questions;
    }

    static HashMap<String, Question> getReviewWithUUID() {
        return questions_review;
    }

    static LinkedList<String> getUUIDs() {
        return uuids;
    }

    static LinkedList<String> getReviewUUIDs() {
        return uuids_review;
    }

    static void approve(String uuid) {
        if(!uuids_review.contains(uuid))
            return;

        uuids_review.remove(uuid);
        Question q = questions_review.get(uuid);

        add(q);

        questions_review.remove(uuid);

        writeQuestionsReviewJSON();
    }

    static void deny(String uuid) {
        if(!uuids_review.contains(uuid))
            return;

        uuids_review.remove(uuid);
        questions_review.remove(uuid);

        writeQuestionsReviewJSON();
    }

    static String versionCheck() {
        URL url;
        String newest;
        StringBuilder note = new StringBuilder("Author's Note: ");
        try {
            url = new URL("https://raw.githubusercontent.com/itsmarsss/QOTD-Bot/main/newestversion");
            URLConnection uc;
            uc = url.openConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader(uc.getInputStream()));
            newest = reader.readLine();
            String line;
            while ((line = reader.readLine()) != null)
                note.append(line).append("\n");

            if (note.toString().equals("Author's Note: "))
                note = new StringBuilder();

        } catch (Exception e) {
            e.printStackTrace();
            return "Unable to check for version and creator's note";
        }
        if (!newest.equals(version)) {
            return "   [There is a newer version of QOTD Bot]" +
                    "\n\t##############################################" +
                    "\n\t   " + version + "(current) >> " + newest + "(newer)" +
                    "\nNew version: https://github.com/itsmarsss/QOTD-Bot/releases" +
                    "\n\t##############################################" +
                    "\n" + note;
        }
        return " This program is up to date!" +
                "\n" + note +
                "\n[https://github.com/itsmarsss/QOTD-Bot/releases]";
    }

    private static boolean readConfigYML() {
        InputStream is;
        try {
            is = new FileInputStream(parent + "/config.yml");
            Yaml yml = new Yaml(new Constructor(Config.class));
            config = (Config) yml.load(is);
            if (!config.isValid()) {
                return false;
            }
            config.setInitializedY(true);
            writeConfigYML();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    static boolean writeConfigYML() {
        String token = config.getBotToken();
        String serverID = config.getServerID();
        String channelID = config.getChannelID();
        String prefix = config.getPrefix();

        int interval = config.getInterval();
        int hour = config.getHour();
        int minute = config.getMinute();

        String permRoleID = config.getPermRoleID();
        String managerRoleID = config.getManagerRoleID();

        boolean dynamicConfig = config.getDynamicConfig();

        boolean managerReview = config.getManagerReview();
        String reviewChannel = config.getReviewChannel();

        String colorHex = config.getQOTDColor();

        try (FileWriter file = new FileWriter(parent + "/config.yml")) {
            file.write(String.format(template, token, serverID, channelID, prefix, interval, hour, minute, permRoleID, managerRoleID, dynamicConfig, managerReview, reviewChannel, colorHex));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    static boolean readQuestionsJSON(String file) {
        JSONParser parser = new JSONParser();

        try (Reader reader = new FileReader(parent + "/" + file)) {
            JSONObject jsonObject = (JSONObject) parser.parse(reader);
            JSONArray questions = (JSONArray) jsonObject.get("questions");
            for (Object q : questions) {
                try {
                    JSONObject questionObj = (JSONObject) q;
                    String question = (String) questionObj.get("question");
                    String footer = (String) questionObj.get("footer");
                    String user = (String) questionObj.get("user");
                    long time = (long) questionObj.get("time");
                    boolean isPoll = (boolean) questionObj.get("poll");

                    Question newq = new Question(question, footer, user, isPoll);
                    newq.setDate(time);
                    add(newq);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            reader.close();
            writeQuestionsJSON();

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    static boolean readQuestionsReviewJSON(String file) {
        JSONParser parser = new JSONParser();

        try (Reader reader = new FileReader(parent + "/" + file)) {
            JSONObject jsonObject = (JSONObject) parser.parse(reader);
            JSONArray questions = (JSONArray) jsonObject.get("questions");
            for (Object q : questions) {
                try {
                    JSONObject questionObj = (JSONObject) q;
                    String question = (String) questionObj.get("question");
                    String footer = (String) questionObj.get("footer");
                    String user = (String) questionObj.get("user");
                    long time = (long) questionObj.get("time");
                    boolean isPoll = (boolean) questionObj.get("poll");

                    String uuid = (String) questionObj.get("uuid");

                    Question newq = new Question(question, footer, user, isPoll);
                    newq.setDate(time);

                    questions_review.put(uuid, newq);
                    uuids_review.add(uuid);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            reader.close();
            writeQuestionsJSON();

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void writeQuestionsJSON() {
        JSONObject questions = new JSONObject();
        JSONArray questionsList = new JSONArray();
        for (String uuid : uuids) {
            Question q = QOTDBot.questions.get(uuid);
            JSONObject question = new JSONObject();
            question.put("question", q.getQuestion());
            question.put("footer", q.getFooter());
            question.put("user", q.getAuthor());
            question.put("time", q.getMillis());
            question.put("poll", q.isPoll());
            questionsList.add(question);
        }
        questions.put("questions", questionsList);

        try (FileWriter file = new FileWriter(parent + "/questions.json")) {
            file.write(questions.toJSONString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void writeQuestionsReviewJSON() {
        JSONObject questions = new JSONObject();
        JSONArray questionsList = new JSONArray();
        for (String uuid : uuids_review) {
            Question q = QOTDBot.questions_review.get(uuid);
            JSONObject question = new JSONObject();
            question.put("question", q.getQuestion());
            question.put("footer", q.getFooter());
            question.put("user", q.getAuthor());
            question.put("time", q.getMillis());
            question.put("poll", q.isPoll());
            question.put("uuid", uuid);
            questionsList.add(question);
        }
        questions.put("questions", questionsList);

        try (FileWriter file = new FileWriter(parent + "/review.json")) {
            file.write(questions.toJSONString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static boolean prepUploadJSON() {
        try (FileWriter file = new FileWriter(parent + "/upload.json")) {
            file.write("{\"questions\": []}");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private static void startThread(int wait) {
        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
        exec.scheduleAtFixedRate(QOTDBot::postQOTD, wait, config.getInterval(), TimeUnit.MINUTES);
    }

    static void setPause(boolean status) {
        isPaused = status;
    }

    static boolean getPause() {
        return isPaused;
    }

    static void postQOTD() {
        if (isPaused)
            return;

        boolean exists = false;
        for (GuildChannel ch : jda.getGuildById(config.getServerID()).getChannels()) {
            if (ch.getId().equals(config.getChannelID())) {
                exists = true;
                break;
            }
        }
        if (!exists)
            return;

        Question q = getNext();
        if (q.isPoll()) {
            jda.getTextChannelById(config.getChannelID()).sendMessageEmbeds(q.createEmbed()).queue(msg -> {
                msg.addReaction(Emoji.fromUnicode("\u2705")).queue();
                msg.addReaction(Emoji.fromUnicode("\u274E")).queue();
            });
        } else {
            jda.getTextChannelById(config.getChannelID()).sendMessageEmbeds(q.createEmbed()).queue();
        }
        System.out.println("=============================");
        System.out.println(q);
        writeQuestionsJSON();
    }

    private static int calculateWaitTime() {
        int current = LocalDateTime.now().getHour() * 60 + LocalDateTime.now().getMinute();
        int starttime = config.getHour() * 60 + config.getMinute();

        if (starttime > current) {
            return starttime - current;
        }
        return 1440 - (current - starttime);
    }

    public static String getParent() {
        return parent;
    }

}
