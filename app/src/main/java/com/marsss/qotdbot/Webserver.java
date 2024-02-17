package com.marsss.qotdbot;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.LinkedList;

public class Webserver {

    private int port;
    private HttpServer server;
    private String html;
    private String js;
    private String css;

    public int getPort() {
        return port;
    }

    public void terminate() {
        server.stop(0);
    }

    public void startServer() throws Exception {
        server = HttpServer.create(new InetSocketAddress("0.0.0.0", 0), 0);
        server.createContext("/", new MainPage());
        server.createContext("/index.js", new JS());
        server.createContext("/index.css", new CSS());
        server.createContext("/api/v1/getconfig", new GetConfig());
        server.createContext("/api/v1/setconfig", new SetConfig());
        server.createContext("/api/v1/getqueue", new GetQueue());
        server.createContext("/api/v1/getreview", new GetReview());
        server.createContext("/api/v1/delete", new Delete());
        server.createContext("/api/v1/approve", new Approve());
        server.createContext("/api/v1/postnext", new PostNext());
        server.createContext("/api/v1/newpost", new NewPost());
        server.setExecutor(null);
        server.start();
        port = server.getAddress().getPort();

        html = loadFile("webassets/index.html").replace("0.0.0", QOTDBot.getVersion());
        js = loadFile("webassets/index.js");
        css = loadFile("webassets/index.css");

        System.out.println("Finished reading files.");
    }

    private String loadFile(String path) {
        InputStream inputStream = Webserver.class.getClassLoader().getResourceAsStream(path);

        if (inputStream != null) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

                System.out.println("File found: " + path);

                StringBuilder file = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    file.append(line);
                    file.append("\n");
                }

                System.out.println("File loaded: " + path);

                return file.toString();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Encountered an error reading: " + path);
            }
        } else {
            System.out.println("File not found: " + path);
        }

        return "";
    }

    private class MainPage implements HttpHandler {
        @Override
        public void handle(HttpExchange he) throws IOException {
            System.out.println("Main page queried");

            String response = html;
            he.sendResponseHeaders(200, response.length());
            OutputStream os = he.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    private class JS implements HttpHandler {
        @Override
        public void handle(HttpExchange he) throws IOException {
            String response = js;
            he.sendResponseHeaders(200, response.length());
            OutputStream os = he.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    private class CSS implements HttpHandler {
        @Override
        public void handle(HttpExchange he) throws IOException {
            String response = css;
            he.sendResponseHeaders(200, response.length());
            OutputStream os = he.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    private static class GetConfig implements HttpHandler {
        @Override
        public void handle(HttpExchange he) throws IOException {
            System.out.println("Config queried");

            String response = String.format("""
                            {
                                "prefix": "%s",
                                
                                "interval": "%s",
                                "hour": "%s",
                                "minute": "%s",
                                
                                "qotdchannel": "%s",
                                "managerreview": "%s",
                                "reviewchannel": "%s",
                                "embedcolor": "#%s",
                                "trivia": "%s",
                                "paused": "%s",
                                
                                "permissionrole": "%s",
                                "managerrole": "%s"
                            }
                            """,
                    escapeJson(QOTDBot.config.getPrefix()),
                    QOTDBot.config.getInterval(),
                    QOTDBot.config.getHour(),
                    QOTDBot.config.getMinute(),
                    escapeJson(QOTDBot.config.getChannelID()),
                    QOTDBot.config.getManagerReview(),
                    escapeJson(QOTDBot.config.getReviewChannel()),
                    escapeJson(QOTDBot.config.getQOTDColor()),
                    QOTDBot.config.getTrivia(),
                    QOTDBot.isPaused(),
                    escapeJson(QOTDBot.config.getPermRoleID()),
                    escapeJson(QOTDBot.config.getManagerRoleID()));

            he.sendResponseHeaders(200, response.length());
            OutputStream os = he.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    private class SetConfig implements HttpHandler {
        @Override
        public void handle(HttpExchange he) throws IOException {
            String body = readRequestBody(he.getRequestBody());

            JSONParser parser = new JSONParser();
            JSONObject data;
            try {
                data = (JSONObject) parser.parse(body);
                System.out.println();
                System.out.println("QOTD Bot config has been updated:");
                System.out.println("\t" + body);

                Config tempConfig = new Config(QOTDBot.config);

                tempConfig.setPrefix((String) data.get("prefix"));

                tempConfig.setInterval(Integer.parseInt((String) data.get("interval")));
                tempConfig.setHour(Integer.parseInt((String) data.get("hour")));
                tempConfig.setMinute(Integer.parseInt((String) data.get("minute")));

                tempConfig.setChannelID((String) data.get("qotdchannel"));
                tempConfig.setManagerReview(Boolean.parseBoolean((String) data.get("managerreview")));
                tempConfig.setReviewChannel((String) data.get("reviewchannel"));
                tempConfig.setQOTDColor((String) data.get("embedcolor"));
                tempConfig.setTrivia(Boolean.parseBoolean((String) data.get("trivia")));

                tempConfig.setPermRoleID((String) data.get("permissionrole"));
                tempConfig.setManagerRoleID((String) data.get("managerrole"));

                if (tempConfig.isValid()) {
                    QOTDBot.setPaused(Boolean.parseBoolean((String) data.get("paused")));

                    QOTDBot.config = tempConfig;
                    QOTDBot.config.writeYML(false);

                    String response = "Success";
                    he.sendResponseHeaders(200, response.length());
                    OutputStream os = he.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                } else {
                    System.out.println("Unable to read POST (GET) JSON");

                    String response = "Failed";
                    he.sendResponseHeaders(200, response.length());
                    OutputStream os = he.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                }
            } catch (ParseException e) {
                e.printStackTrace();
                System.out.println("Unable to read POST (GET) JSON");

                String response = "Failed";
                he.sendResponseHeaders(200, response.length());
                OutputStream os = he.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }
    }

    private static class GetQueue implements HttpHandler {
        @Override
        public void handle(HttpExchange he) throws IOException {
            System.out.println("Queue queried");

            HashMap<String, Question> questions = QOTDBot.getQueueWithUUID();
            LinkedList<String> uuids = QOTDBot.getUUIDs();

            final String template = """
                                        
                    {
                        "question": "%s",
                        "footer": "%s",
                        "user": "%s",
                        "time": %s,
                        "poll": %s,
                        "uuid": "%s"
                    },
                    """;

            StringBuilder data = new StringBuilder();

            data.append("""
                    {
                    "queue":[
                    """);

            for (String uuid : uuids) {
                Question q = questions.get(uuid);
                data.append(String.format(template,
                        escapeJson(convertToHtml(q.getQuestion())),
                        escapeJson(convertToHtml(q.getFooter())),
                        escapeJson(q.getAuthor()),
                        q.getMillis(),
                        q.isPoll(),
                        uuid));
            }

            data.append("]}");

            String response = replaceLast(data.toString(), ",", "");
            he.sendResponseHeaders(200, response.length());
            OutputStream os = he.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    private static class GetReview implements HttpHandler {
        @Override
        public void handle(HttpExchange he) throws IOException {
            System.out.println("Review queried");

            HashMap<String, Question> questions = QOTDBot.getReviewWithUUID();
            LinkedList<String> uuids = QOTDBot.getReviewUUIDs();

            final String template = """
                                        
                    {
                        "question": "%s",
                        "footer": "%s",
                        "user": "%s",
                        "time": %s,
                        "poll": %s,
                        "uuid": "%s"
                    },
                    """;

            StringBuilder data = new StringBuilder();

            data.append("""
                    {
                    "review":[
                    """);

            for (String uuid : uuids) {
                Question q = questions.get(uuid);
                data.append(String.format(template,
                        escapeJson(convertToHtml(q.getQuestion())),
                        escapeJson(convertToHtml(q.getFooter())),
                        escapeJson(q.getAuthor()),
                        q.getMillis(),
                        q.isPoll(),
                        uuid));
            }

            data.append("]}");

            String response = replaceLast(data.toString(), ",", "");
            he.sendResponseHeaders(200, response.length());
            OutputStream os = he.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    private class Delete implements HttpHandler {
        @Override
        public void handle(HttpExchange he) throws IOException {
            String body = readRequestBody(he.getRequestBody());

            JSONParser parser = new JSONParser();
            JSONObject data;
            try {
                data = (JSONObject) parser.parse(body);
                System.out.println();
                System.out.println("QOTD Bot post has been requested to be deleted:");

                String type = (String) data.get("type");
                String uuid = (String) data.get("uuid");

                System.out.println("\t" + uuid);

                if (type.equals("queue")) {
                    QOTDBot.remove(uuid);
                } else {
                    QOTDBot.deny(uuid);
                }

                String response = "Success";

                he.sendResponseHeaders(200, response.length());
                OutputStream os = he.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } catch (ParseException e) {
                e.printStackTrace();
                System.out.println("Unable to read POST (GET) JSON");

                String response = "Failed";
                he.sendResponseHeaders(200, response.length());
                OutputStream os = he.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }

        }
    }

    private class Approve implements HttpHandler {
        @Override
        public void handle(HttpExchange he) throws IOException {
            String body = readRequestBody(he.getRequestBody());

            JSONParser parser = new JSONParser();
            JSONObject data;
            try {
                data = (JSONObject) parser.parse(body);
                System.out.println();
                System.out.println("QOTD Bot post has been requested to be approved:");

                String uuid = (String) data.get("uuid");

                System.out.println("\t" + uuid);

                QOTDBot.approve(uuid);

                String response = "Success";
                he.sendResponseHeaders(200, response.length());
                OutputStream os = he.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } catch (ParseException e) {
                e.printStackTrace();
                System.out.println("Unable to read POST (GET) JSON");

                String response = "Failed";
                he.sendResponseHeaders(200, response.length());
                OutputStream os = he.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }
    }

    private class PostNext implements HttpHandler {
        @Override
        public void handle(HttpExchange he) throws IOException {
            QOTDBot.postQOTD();

            String response = "Success";
            he.sendResponseHeaders(200, response.length());
            OutputStream os = he.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    private class NewPost implements HttpHandler {
        @Override
        public void handle(HttpExchange he) throws IOException {
            String body = readRequestBody(he.getRequestBody());

            JSONParser parser = new JSONParser();
            JSONObject data;
            try {
                data = (JSONObject) parser.parse(body);
                System.out.println();
                System.out.println("QOTD Bot new post has been requested:");
                System.out.println("\t" + data);

                String author = (String) data.get("author");
                String question = (String) data.get("question");
                String footer = (String) data.get("footer");
                boolean poll = data.get("type").equals("poll");

                Question newq = new Question(question, footer, author, poll);

                QOTDBot.add(newq);

                String response = "Success";
                he.sendResponseHeaders(200, response.length());
                OutputStream os = he.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } catch (ParseException e) {
                e.printStackTrace();
                System.out.println("Unable to read POST (GET) JSON");

                String response = "Failed";
                he.sendResponseHeaders(200, response.length());
                OutputStream os = he.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }
    }

    private String readRequestBody(InputStream requestBodyStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(requestBodyStream));
        StringBuilder requestBodyBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            requestBodyBuilder.append(line);
        }
        reader.close();
        return requestBodyBuilder.toString();
    }

    public static String replaceLast(String text, String regex, String replacement) {
        return text.replaceFirst("(?s)" + regex + "(?!.*?" + regex + ")", replacement);
    }

    public static String convertToHtml(String markdown) {

        return markdown
                .replaceAll("\\*\\*(.*?)\\*\\*", "<strong>$1</strong>")
                .replaceAll("\\*(.*?)\\*", "<em>$1</em>")
                .replaceAll("__(.*?)__", "<strong>$1</strong>")
                .replaceAll("_(.*?)_", "<em>$1</em>")
                .replaceAll("```(.*?)```", "<code-block>$1</code-block>")
                .replaceAll("`(.*?)`", "<code>$1</code>")
                .replaceAll("\n", "<br>");
    }

    public static String escapeJson(String jsonString) {
        StringBuilder escapedJson = new StringBuilder();

        for (int i = 0; i < jsonString.length(); i++) {
            char ch = jsonString.charAt(i);

            switch (ch) {
                case '\"':
                    escapedJson.append("\\\"");
                    break;
                case '\\':
                    escapedJson.append("\\\\");
                    break;
                case '/':
                    escapedJson.append("\\/");
                    break;
                case '\b':
                    escapedJson.append("\\b");
                    break;
                case '\f':
                    escapedJson.append("\\f");
                    break;
                case '\n':
                    escapedJson.append("\\n");
                    break;
                case '\r':
                    escapedJson.append("\\r");
                    break;
                case '\t':
                    escapedJson.append("\\t");
                    break;
                default:
                    if (Character.isISOControl(ch)) {
                        escapedJson.append(String.format("\\u%04X", (int) ch));
                    } else {
                        escapedJson.append(ch);
                    }
            }
        }

        return escapedJson.toString();
    }
}
