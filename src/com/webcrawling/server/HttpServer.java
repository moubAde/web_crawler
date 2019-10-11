package com.webcrawling.server;

import com.webcrawling.util.HttpRequestParser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;

public class HttpServer implements Runnable{
    private ServerSocketChannel ssc;
    private Selector selector;
    private ByteBuffer buffer;
    private static final int BUFFER_SIZE = 1256;
    private final Charset CHARSET = Charset.forName("UTF-8");

    public HttpServer(int port) throws IOException{
        ssc =ServerSocketChannel.open();
        ssc.configureBlocking(false);
        ssc.bind(new InetSocketAddress(port));
        selector = Selector.open();
        buffer = ByteBuffer.allocate(BUFFER_SIZE);
        ssc.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("HTTP Server started on port "+port);
    }

    @Override
    public String toString() {
        return "Server :" + ssc;
    }

    void accept() throws IOException {
        SocketChannel sc = ssc.accept();
        sc.configureBlocking(false);
        sc.register(selector, SelectionKey.OP_READ);
        System.out.println("accept:"+sc);
    }

    void handle(SelectionKey sk) throws IOException {
        SocketChannel client = (SocketChannel) sk.channel();
        buffer.clear();
        if(client.read(buffer) == -1) {
            System.out.println("connection " + client +" closed");
            sk.cancel();
            client.close();
            return;
        }
        HttpRequestHandler hrh = new HttpRequestHandler(client, buffer);
        String response = hrh.response();
        InputStream responseStream = new ByteArrayInputStream(response.getBytes());
        byte[] b = new byte[BUFFER_SIZE];
        while (responseStream.available()>BUFFER_SIZE){
            responseStream.read(b);
            buffer.clear();
            buffer.put(b);
            buffer.flip();
            client.write(buffer);
        }
        b = new byte[responseStream.available()];
        responseStream.read(b);
        buffer.clear();
        buffer.put(b);
        buffer.flip();
        client.write(buffer);
    }

    @Override
    public void run(){
        while(true) {
            try {
                selector.select();
                for (SelectionKey sk : selector.selectedKeys()) {
                    if (sk.isAcceptable()) {
                        accept();
                    } else if (sk.isReadable()) {
                        handle(sk);
                    }
                }
            }
            catch (IOException e){
                e.printStackTrace();
            }
            selector.selectedKeys().clear();
        }

    }

    public static void main(String[] args) throws IOException {
        System.out.println("main");
        HttpServer server = new HttpServer(8080);
        System.out.println(server);
        server.run();
    }
}
