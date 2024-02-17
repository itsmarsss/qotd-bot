package com.marsss.qotdbotlite;

import com.marsss.qotdbotlite.ui.ConsoleMirror;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import javax.swing.*;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class QOTDBotLite {
    static Config config;
    static JDA jda;

    static final String version = "1.1.0";
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
        # Turn on developers mode in Settings > Advanced > Developer Mode, right-click your Discord Server and click on [Copy ID]\r
        serverID: %s\r
        \r
        # Right-click your QOTD channel and click on [Copy ID]\r
        channelID: %s\r
        \r
        # Set a prefix here for the bot, this is what members use to use the bot (e.g. qotd help)\r
        prefix: %s\r
        \r
        # Not mandatory fields:\r
        # Set a manager role, these members can manage QOTDs (write everyone if everyone)\r
        managerRoleID: %s\r
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

        System.out.println("  ____   ____ _______ _____    ____   ____ _______");
        System.out.println(" / __ \\ / __ \\__   __|  __ \\  |  _ \\ / __ \\__  __|     _     _ _");
        System.out.println("| |  | | |  | | | |  | |  | | | |_) | |  | | | |      | |   (_) |_ ___ ");
        System.out.println("| |  | | |  | | | |  | |  | | |  _ <| |  | | | |      | |   | | __/ _ \\");
        System.out.println("| |__| | |__| | | |  | |__| | | |_) | |__| | | |      | |___| | ||  __/");
        System.out.println(" \\___\\_\\\\____/  |_|  |_____/  |____/ \\____/  |_|      |_____|_|\\__\\___|");
        System.out.println("-----------------------------------------------------------------------");
        System.out.println("             ==================== PROGRAM SOURCE CODE ===================");
        System.out.println("             = https://github.com/itsmarsss/qotd-bot/tree/qotd-bot-lite =");
        System.out.println("             ============================================================");
        System.out.println("                Welcome to QOTD Bot Lite's Control Prompt");
        System.out.println();
        System.out.println("Purpose: This bot allows for QOTD to be sent in a specific channel.");
        System.out.println();
        System.out.println("Note[1]: This bot is the Lite version of QOTD-Bot at [https://github.com/itsmarsss/qotd-bot/tree/qotd-bot-lite]");
        System.out.println();
        System.out.println("Note[2]: This program will only run for 1 Discord Server, if you have multiple Discord Servers that you want this program to work on, then you will need to run multiple copies of this program in different directories (Make sure to set Server ID and Channel ID in each config.yml)");
        System.out.println();
        System.out.println("Warning[1]: Use this program at your own risk, I (the creator of this program) will not be liable for any issues that this program causes to your Discord Server or computer (or sanity?)");
        System.out.println();
        System.out.println("Version:" + versionCheck());
        System.out.println();
        parent = new File(QOTDBotLite.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();
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
        System.out.println("Done!");
        System.out.println("Finished!");
    }

    static String versionCheck() {
        URL url;
        String newest;
        StringBuilder note = new StringBuilder("Author's Note: ");
        try {
            url = new URL("https://raw.githubusercontent.com/itsmarsss/qotd-bot/qotd-bot-lite/newestversion");
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
            return "   [There is a newer version of QOTD Bot Lite]" +
                    "\n\t##############################################" +
                    "\n\t         " + version + "(current) >> " + newest + "(newer)" +
                    "\nNew version: https://github.com/itsmarsss/qotd-bot/tree/qotd-bot-lite" +
                    "\n\t##############################################" +
                    "\n" + note;
        }
        return " This program is up to date!" +
                "\n" + note +
                "\n[https://github.com/itsmarsss/qotd-bot/tree/qotd-bot-lite]";
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

        String managerRoleID = config.getManagerRoleID();

        String colorHex = config.getQOTDColor();

        try (FileWriter file = new FileWriter(parent + "/config.yml")) {
            file.write(String.format(template, token, serverID, channelID, prefix, managerRoleID, colorHex));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String getParent() {
        return parent;
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

        System.out.println("Bot Stopped.");
    }

    public static JDA getJDA() {
        return jda;
    }

    public static void postQOTD(String question, String footer) {
        EmbedBuilder QOTDEmbed = new EmbedBuilder();
        QOTDEmbed.setAuthor(QOTDBotLite.jda.getSelfUser().getName(), null, QOTDBotLite.jda.getSelfUser().getAvatarUrl())
                .setTitle("**Question:** " + question)
                .setDescription(footer)
                .setColor(QOTDBotLite.config.getColor());

        boolean exists = false;
        for (GuildChannel ch : jda.getGuildById(config.getServerID()).getChannels()) {
            if (ch.getId().equals(config.getChannelID())) {
                exists = true;
                break;
            }
        }
        if (!exists)
            return;

        jda.getTextChannelById(config.getChannelID()).sendMessageEmbeds(QOTDEmbed.build()).queue(msg -> {
            msg.createThreadChannel("QOTD of " + new SimpleDateFormat("MM-dd-yyyy").format(new Date())).queue();
        });
    }
}
