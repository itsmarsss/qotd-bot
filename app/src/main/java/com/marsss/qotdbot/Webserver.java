package com.marsss.qotdbot;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class Webserver {
    public static void startServer() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", 8000), 0);
        server.createContext("/", new WebControlHandler());
        server.setExecutor(null);
        server.start();
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
