package com.webcrawling.server;

import java.io.IOException;

public class Server {
    private int httpPort;
    private int wcpPort;

    public Server(int httpPort, int wcpPort){
        this.httpPort = httpPort;
        this.wcpPort = wcpPort;
    }

    public void start() throws IOException {
        Thread httpServerSocket = new Thread(new HttpServer(httpPort));
        Thread wcpServerSocket = new Thread(new WcpServer(wcpPort));

        httpServerSocket.start();
        wcpServerSocket.start();
    }

    public static void main(String[] args) throws IOException {
        Server server = new Server(8080,9090);
        server.start();
    }
}
