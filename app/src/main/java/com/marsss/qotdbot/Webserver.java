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
    private String html;
    private String js;
    private String css;

    public int getPort() {
        return port;
    }

    public void startServer() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", 0), 0);
        server.createContext("/", new MainPage());
        server.createContext("/index.js", new JS());
        server.createContext("/index.css", new CSS());
        server.createContext("/api/v1/getconfig", new GetConfig());
        server.createContext("/api/v1/setconfig", new SetConfig());
        server.createContext("/api/v1/getqueue", new GetQueue());
        server.createContext("/api/v1/getreview", new GetReview());
        server.createContext("/api/v1/delete", new Delete());
        server.createContext("/api/v1/approve", new Approve());
        server.setExecutor(null);
        server.start();
        port = server.getAddress().getPort();

        html = loadFile("webassets/index.html");
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

    private class GetConfig implements HttpHandler {
        @Override
        public void handle(HttpExchange he) throws IOException {
            String response = String.format("""
                            {
                                "prefix": "%s",
                                "managerreview": "%s",
                                "reviewchannel": "%s",
                                "embedcolor": "#%s",
                                
                                "permissionrole": "%s",
                                "managerrole": "%s"
                            }
                            """,
                    QOTDBot.config.getPrefix(),
                    QOTDBot.config.getManagerReview(),
                    QOTDBot.config.getReviewChannel(),
                    QOTDBot.config.getQOTDColor(),
                    QOTDBot.config.getPermRoleID(),
                    QOTDBot.config.getManagerRoleID());

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
            } catch (ParseException e) {
                e.printStackTrace();
                System.out.println("Unable to read POST (GET) JSON");
                throw new RuntimeException(e);
            }

            QOTDBot.config.setPrefix((String) data.get("prefix"));
            QOTDBot.config.setManagerReview(Boolean.parseBoolean((String) data.get("managerreview")));
            QOTDBot.config.setReviewChannel((String) data.get("reviewchannel"));
            QOTDBot.config.setQOTDColor((String) data.get("embedcolor"));

            QOTDBot.config.setPermRoleID((String) data.get("permissionrole"));
            QOTDBot.config.setManagerRoleID((String) data.get("managerrole"));

            String response = "Success";
            he.sendResponseHeaders(200, response.length());
            OutputStream os = he.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    private class GetQueue implements HttpHandler {
        @Override
        public void handle(HttpExchange he) throws IOException {

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
                        q.getQuestion(),
                        q.getFooter(),
                        q.getAuthor(),
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

    private class GetReview implements HttpHandler {
        @Override
        public void handle(HttpExchange he) throws IOException {
            String response = "Success"; // get review
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
            } catch (ParseException e) {
                e.printStackTrace();
                System.out.println("Unable to read POST (GET) JSON");
                throw new RuntimeException(e);
            }

            String uuid = (String) data.get("uuid");

            System.out.println("\t" + uuid);

            QOTDBot.remove(uuid);

            String response = "Success";

            he.sendResponseHeaders(200, response.length());
            OutputStream os = he.getResponseBody();
            os.write(response.getBytes());
            os.close();
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
                System.out.println("QOTD Bot post has been requested to be deleted:");
            } catch (ParseException e) {
                e.printStackTrace();
                System.out.println("Unable to read POST (GET) JSON");
                throw new RuntimeException(e);
            }

            System.out.println("\t" + data.get("uuid"));

            // find

            String response = "Success";

            //String response = "Not found";
            he.sendResponseHeaders(200, response.length());
            OutputStream os = he.getResponseBody();
            os.write(response.getBytes());
            os.close();
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
        return text.replaceFirst("(?s)"+regex+"(?!.*?"+regex+")", replacement);
    }
}
