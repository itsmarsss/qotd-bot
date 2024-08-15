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
import org.json.simple.parser.ParseException;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
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

    static final String version = "4.4.3";
    private static String parent;
    private static final EnumSet<GatewayIntent> intent = EnumSet.of(
            GatewayIntent.GUILD_MESSAGES,
            GatewayIntent.GUILD_EMOJIS_AND_STICKERS,
            GatewayIntent.MESSAGE_CONTENT);

    private static boolean head = true;
    private static boolean autostart = false;

    private final static String template = """
            # IMPORTANT - Mandatory fields:\r
            # Input the bot's token here, this can be found in Developers Portal > Applications > [Bot Profile] > Bot > Token > [Copy]\r
            botToken: %s\r
            \r
            # Turn on developers mode in Settings > Advanced > Developer Mode, right click your Discord Server and click on [Copy ID]\r
            serverID: %s\r
            \r
            # Right click your QOTD channel and click on [Copy ID]\r
            channelID: %s\r
            \r
            # Set a prefix here for the bot, this is what members use to use the bot (e.g. qotd help)\r
            prefix: "%s"\r
            \r
            # Input number of minute(s) until another QOTD is sent ( 1 to 1440 [24 hours] )\r
            interval: %s\r
            \r
            # Start time, this dictates what time of the day your QOTD will be sent (24 hours time, local server time)\r
            # Hours\r
            hour: %s\r
            # Minutes\r
            minute: %s\r
            \r
            # Not mandatory fields:\r
            # Set a perm role, these members can add QOTDs (write everyone if everyone)\r
            permRoleID: %s\r
            \r
            # Set a manager role, these members can manage QOTDs (write everyone if everyone)\r
            managerRoleID: %s\r
            \r
            # Dynamic config.yml, config.yml is changed as its values are changed\r
            dynamicConfig: %s\r
            \r
            # QOTD submission review settings\r
            # Set to true if you want QOTD submissions to go through bot manager review\r
            managerReview: %s\r
            # Right click your QOTD review channel and click on [Copy ID], this is where QOTD submissions are reviewed\r
            reviewChannel: %s\r
            \r
            # QOTD Embed color in hex (Do not include "#")\r
            QOTDColor: %s\r
            """;

    public static void main(String[] args) throws URISyntaxException {
        for (String arg : args) {
            if (arg.equals("--nohead") || arg.equals("--nh")) {
                head = false;
            }
            if (arg.equals("--autostart") || arg.equals("--as")) {
                autostart = true;
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
        System.out.println("   = https://github.com/itsmarsss/qotd-bot =");
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
        if (autostart) {
            start();
        } else {
            Scanner sc = new Scanner(System.in);
            sc.nextLine();
            sc.close();
            start();
        }
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

    private static Webserver server;

    static void setupWebpage() {
        System.out.println();
        System.out.println("Starting Webserver...");
        try {
            server = new Webserver();
            server.startServer();

            System.out.println("Webpage setup completed!");
            System.out.println("\tOn port: " + server.getPort());

            System.out.println();
            System.out.println("Opening control panel...");

            controlPanel();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to start Webserver.");
            System.out.println("\tError message: " + e.getMessage());
        }
    }

    static Question getNext() {
        if (questions.isEmpty()) {
            String uuid = UUID.randomUUID().toString();
            if (config.getTrivia()) {
                try {
                    URL url = new URL("https://opentdb.com/api.php?amount=1&type=multiple");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");

                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        String line;
                        StringBuilder response = new StringBuilder();

                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        reader.close();

                        String json = response.toString();
                        JSONParser parser = new JSONParser();
                        JSONObject jsonObject = (JSONObject) parser.parse(json);
                        JSONArray results = (JSONArray) jsonObject.get("results");

                        if (results != null && results.size() > 0) {
                            JSONObject questionObject = (JSONObject) results.get(0);
                            String question = convertHtmlEscapeCharacters((String) questionObject.get("question"));
                            String answer = convertHtmlEscapeCharacters((String) questionObject.get("correct_answer"));

                            System.out.println("Question: " + question);
                            System.out.println("Answer: " + answer);

                            questions.put(uuid, new Question(question, "Answer: ||" + answer + "||", "OpenTDB Trivia", false));
                        } else {
                            System.out.println("No trivia question found in the response.");
                            questions.put(uuid, new Question("Can someone add more questions? My queue is empty... :slight_smile:", "Trivia Error", jda.getSelfUser().getAsTag(), false));
                        }
                    } else {
                        System.out.println("Failed to retrieve a trivia question. Response Code: " + responseCode);
                        questions.put(uuid, new Question("Can someone add more questions? My queue is empty... :slight_smile:", "Trivia Error", jda.getSelfUser().getAsTag(), false));
                    }
                    connection.disconnect();
                } catch (IOException | ParseException e) {
                    e.printStackTrace();
                    questions.put(uuid, new Question("Can someone add more questions? My queue is empty... :slight_smile:", "Trivia Error", jda.getSelfUser().getAsTag(), false));
                }
            } else {
                questions.put(uuid, new Question("Can someone add more questions? My queue is empty... :slight_smile:", "ADD QUESTION PLS", jda.getSelfUser().getAsTag(), false));
            }

            uuids.add(uuid);
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

    static void remove(String uuid) {
        int index = uuids.indexOf(uuid);
        if (index == -1)
            return;

        uuids.remove(index);
        questions.remove(uuid);
        writeQuestionsJSON();
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

        for (String uuid : uuids) {
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
        if (!uuids_review.contains(uuid))
            return;

        uuids_review.remove(uuid);
        Question q = questions_review.get(uuid);

        add(q);

        questions_review.remove(uuid);

        writeQuestionsReviewJSON();
    }

    static void deny(String uuid) {
        if (!uuids_review.contains(uuid))
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
            url = new URL("https://raw.githubusercontent.com/itsmarsss/qotd-bot/main/newestversion");
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
                    "\nNew version: https://github.com/itsmarsss/qotd-bot/releases" +
                    "\n\t##############################################" +
                    "\n" + note;
        }
        return " This program is up to date!" +
                "\n" + note +
                "\n[https://github.com/itsmarsss/qotd-bot/releases]";
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

    public static boolean isPaused() {
        return isPaused;
    }

    public static void setPaused(boolean paused) {
        isPaused = paused;
    }

    public static String getVersion() {
        return version;
    }

    public static void stop() {
        if (jda == null) {
            System.out.println("Bot already Stopped.");
            return;
        }
        System.out.println("Terminating connection with Discord...");
        jda.shutdownNow();
        jda = null;
        System.out.println("Connection terminated!");

        System.out.println("Closing server...");
        server.terminate();
        server = null;
        System.out.println("Server closed!");

        System.out.println("Bot Stopped.");
    }

    public static void controlPanel() {
        if (server == null) {
            System.out.println("Click [Start] first.");
            return;
        }

        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI("http://localhost:" + server.getPort()));
                System.out.println("Successfully sent user to control panel...");
            } else {
                System.out.println("Failed to open website.");
                System.out.println("\tVisit http://localhost:" + server.getPort() + " to access control panel.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to open website.");
            System.out.println("\tVisit http://localhost:" + server.getPort() + " to access control panel.");
        }
    }

    public static JDA getJDA() {
        return jda;
    }

    public static String convertHtmlEscapeCharacters(String input) {
        String[][] escapePatterns = {
                {"&amp;", "&"},
                {"&lt;", "<"},
                {"&gt;", ">"},
                {"&quot;", "\""},
                {"&apos;", "'"},
                {"&#39;", "'"},
                {"&nbsp;", " "},
                {"&iexcl;", "¡"},
                {"&cent;", "¢"},
                {"&pound;", "£"},
                {"&curren;", "¤"},
                {"&yen;", "¥"},
                {"&brvbar;", "¦"},
                {"&sect;", "§"},
                {"&uml;", "¨"},
                {"&copy;", "©"},
                {"&ordf;", "ª"},
                {"&laquo;", "«"},
                {"&not;", "¬"},
                {"&reg;", "®"},
                {"&macr;", "¯"},
                {"&deg;", "°"},
                {"&plusmn;", "±"},
                {"&sup2;", "²"},
                {"&sup3;", "³"},
                {"&acute;", "´"},
                {"&micro;", "µ"},
                {"&para;", "¶"},
                {"&middot;", "·"},
                {"&cedil;", "¸"},
                {"&sup1;", "¹"},
                {"&ordm;", "º"},
                {"&raquo;", "»"},
                {"&frac14;", "¼"},
                {"&frac12;", "½"},
                {"&frac34;", "¾"},
                {"&iquest;", "¿"},
                {"&Agrave;", "À"},
                {"&Aacute;", "Á"},
                {"&Acirc;", "Â"},
                {"&Atilde;", "Ã"},
                {"&Auml;", "Ä"},
                {"&Aring;", "Å"},
                {"&AElig;", "Æ"},
                {"&Ccedil;", "Ç"},
                {"&Egrave;", "È"},
                {"&Eacute;", "É"},
                {"&Ecirc;", "Ê"},
                {"&Euml;", "Ë"},
                {"&Igrave;", "Ì"},
                {"&Iacute;", "Í"},
                {"&Icirc;", "Î"},
                {"&Iuml;", "Ï"},
                {"&ETH;", "Ð"},
                {"&Ntilde;", "Ñ"},
                {"&Ograve;", "Ò"},
                {"&Oacute;", "Ó"},
                {"&Ocirc;", "Ô"},
                {"&Otilde;", "Õ"},
                {"&Ouml;", "Ö"},
                {"&times;", "×"},
                {"&Oslash;", "Ø"},
                {"&Ugrave;", "Ù"},
                {"&Uacute;", "Ú"},
                {"&Ucirc;", "Û"},
                {"&Uuml;", "Ü"},
                {"&Yacute;", "Ý"},
                {"&THORN;", "Þ"},
                {"&szlig;", "ß"},
                {"&agrave;", "à"},
                {"&aacute;", "á"},
                {"&acirc;", "â"},
                {"&atilde;", "ã"},
                {"&auml;", "ä"},
                {"&aring;", "å"},
                {"&aelig;", "æ"},
                {"&ccedil;", "ç"},
                {"&egrave;", "è"},
                {"&eacute;", "é"},
                {"&ecirc;", "ê"},
                {"&euml;", "ë"},
                {"&igrave;", "ì"},
                {"&iacute;", "í"},
                {"&icirc;", "î"},
                {"&iuml;", "ï"},
                {"&eth;", "ð"},
                {"&ntilde;", "ñ"},
                {"&ograve;", "ò"},
                {"&oacute;", "ó"},
                {"&ocirc;", "ô"},
                {"&otilde;", "õ"},
                {"&ouml;", "ö"},
                {"&divide;", "÷"},
                {"&oslash;", "ø"},
                {"&ugrave;", "ù"},
                {"&uacute;", "ú"},
                {"&ucirc;", "û"},
                {"&uuml;", "ü"},
                {"&yacute;", "ý"},
                {"&thorn;", "þ"},
                {"&yuml;", "ÿ"},
                {"&OElig;", "Œ"},
                {"&oelig;", "œ"},
                {"&Scaron;", "Š"},
                {"&scaron;", "š"},
                {"&Yuml;", "Ÿ"},
                {"&fnof;", "ƒ"},
                {"&circ;", "ˆ"},
                {"&tilde;", "˜"},
                {"&Alpha;", "Α"},
                {"&Beta;", "Β"},
                {"&Gamma;", "Γ"},
                {"&Delta;", "Δ"},
                {"&Epsilon;", "Ε"},
                {"&Zeta;", "Ζ"},
                {"&Eta;", "Η"},
                {"&Theta;", "Θ"},
                {"&Iota;", "Ι"},
                {"&Kappa;", "Κ"},
                {"&Lambda;", "Λ"},
                {"&Mu;", "Μ"},
                {"&Nu;", "Ν"},
                {"&Xi;", "Ξ"},
                {"&Omicron;", "Ο"},
                {"&Pi;", "Π"},
                {"&Rho;", "Ρ"},
                {"&Sigma;", "Σ"},
                {"&Tau;", "Τ"},
                {"&Upsilon;", "Υ"},
                {"&Phi;", "Φ"},
                {"&Chi;", "Χ"},
                {"&Psi;", "Ψ"},
                {"&Omega;", "Ω"},
                {"&alpha;", "α"},
                {"&beta;", "β"},
                {"&gamma;", "γ"},
                {"&delta;", "δ"},
                {"&epsilon;", "ε"},
                {"&zeta;", "ζ"},
                {"&eta;", "η"},
                {"&theta;", "θ"},
                {"&iota;", "ι"},
                {"&kappa;", "κ"},
                {"&lambda;", "λ"},
                {"&mu;", "μ"},
                {"&nu;", "ν"},
                {"&xi;", "ξ"},
                {"&omicron;", "ο"},
                {"&pi;", "π"},
                {"&rho;", "ρ"},
                {"&sigmaf;", "ς"},
                {"&sigma;", "σ"},
                {"&tau;", "τ"},
                {"&upsilon;", "υ"},
                {"&phi;", "φ"},
                {"&chi;", "χ"},
                {"&psi;", "ψ"},
                {"&omega;", "ω"},
                {"&thetasym;", "ϑ"},
                {"&upsih;", "ϒ"},
                {"&piv;", "ϖ"},
                {"&ensp;", " "},
                {"&emsp;", " "},
                {"&thinsp;", " "},
                {"&zwnj;", "‌"},
                {"&zwj;", "‍"},
                {"&lrm;", "‎"},
                {"&rlm;", "‏"},
                {"&ndash;", "–"},
                {"&mdash;", "—"},
                {"&lsquo;", "‘"},
                {"&rsquo;", "’"},
                {"&sbquo;", "‚"},
                {"&ldquo;", "“"},
                {"&rdquo;", "”"},
                {"&bdquo;", "„"},
                {"&dagger;", "†"},
                {"&Dagger;", "‡"},
                {"&bull;", "•"},
                {"&hellip;", "…"},
                {"&permil;", "‰"},
                {"&prime;", "′"},
                {"&Prime;", "″"},
                {"&lsaquo;", "‹"},
                {"&rsaquo;", "›"},
                {"&oline;", "‾"},
                {"&frasl;", "⁄"},
                {"&euro;", "€"},
                {"&image;", "ℑ"},
                {"&weierp;", "℘"},
                {"&real;", "ℜ"},
                {"&trade;", "™"},
                {"&alefsym;", "ℵ"},
                {"&larr;", "←"},
                {"&uarr;", "↑"},
                {"&rarr;", "→"},
                {"&darr;", "↓"},
                {"&harr;", "↔"},
                {"&crarr;", "↵"},
                {"&lArr;", "⇐"},
                {"&uArr;", "⇑"},
                {"&rArr;", "⇒"},
                {"&dArr;", "⇓"},
                {"&hArr;", "⇔"},
                {"&forall;", "∀"},
                {"&part;", "∂"},
                {"&exist;", "∃"},
                {"&empty;", "∅"},
                {"&nabla;", "∇"},
                {"&isin;", "∈"},
                {"&notin;", "∉"},
                {"&ni;", "∋"},
                {"&prod;", "∏"},
                {"&sum;", "∑"},
                {"&minus;", "−"},
                {"&lowast;", "∗"},
                {"&radic;", "√"},
                {"&prop;", "∝"},
                {"&infin;", "∞"},
                {"&ang;", "∠"},
                {"&and;", "∧"},
                {"&or;", "∨"},
                {"&cap;", "∩"},
                {"&cup;", "∪"},
                {"&int;", "∫"},
                {"&there4;", "∴"},
                {"&sim;", "∼"},
                {"&cong;", "≅"},
                {"&asymp;", "≈"},
                {"&ne;", "≠"},
                {"&equiv;", "≡"},
                {"&le;", "≤"},
                {"&ge;", "≥"},
                {"&sub;", "⊂"},
                {"&sup;", "⊃"},
                {"&nsub;", "⊄"},
                {"&sube;", "⊆"},
                {"&supe;", "⊇"},
                {"&oplus;", "⊕"},
                {"&otimes;", "⊗"},
                {"&perp;", "⊥"},
                {"&sdot;", "⋅"},
                {"&lceil;", "⌈"},
                {"&rceil;", "⌉"},
                {"&lfloor;", "⌊"},
                {"&rfloor;", "⌋"},
                {"&lang;", "⟨"},
                {"&rang;", "⟩"},
                {"&loz;", "◊"},
                {"&spades;", "♠"},
                {"&clubs;", "♣"},
                {"&hearts;", "♥"},
                {"&diams;", "♦"}
        };

        for (String[] pattern : escapePatterns) {
            input = input.replaceAll(pattern[0], pattern[1]);
        }

        return input;
    }
}
