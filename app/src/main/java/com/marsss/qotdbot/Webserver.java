package com.marsss.qotdbot;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;

public class Webserver {

    private int port;

    public int getPort() {
        return port;
    }

    public void startServer() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", 0), 0);
        server.createContext("/", new WebControlHandler());
        server.setExecutor(null);
        server.start();
        port = server.getAddress().getPort();

        loadFile("webassets/index.html");
        loadFile("webassets/index.js");
        loadFile("webassets/index.css");
    }

    private void loadFile(String path) {
        InputStream inputStream = Webserver.class.getResourceAsStream(path);

        if (inputStream != null) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

                System.out.println("File found: " + path);

                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("File not found: " + path);
        }
    }

    static class WebControlHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange he) throws IOException {
            String response = "Response";
            he.sendResponseHeaders(200, response.length());
            OutputStream os = he.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}
