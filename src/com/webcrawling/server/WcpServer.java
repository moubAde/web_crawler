package com.webcrawling.server;

import com.webcrawling.protocol.WCP;
import com.webcrawling.server.logs.ClientResponseLog;
import com.webcrawling.server.logs.UrlsLog;
import com.webcrawling.util.BFS;
import com.webcrawling.util.WordMap;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class WcpServer implements Runnable, WCP {
    private ServerSocketChannel ssc;
    private Selector selector;
    private Map<SelectionKey, ByteBuffer> buffers;
    private Map<SelectionKey, URL> clientsStatus;
    public static UrlsLog urlsLog = new UrlsLog();
    private ClientResponseLog clientResponseLog;
    public static BFS bfs = new BFS();
    public static WordMap wordMap = new WordMap();
    private static final Charset CHARSET = Charset.forName("UTF-8");
    private static final int BUFFER_SIZE = 1024;


    public WcpServer(int port) throws IOException {
        ssc = ServerSocketChannel.open();
        ssc.bind(new InetSocketAddress(port));
        ssc.configureBlocking(false);
        selector = Selector.open();
        ssc.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("WCP Server started on port "+port);
        clientsStatus = new HashMap<>();
        buffers = new HashMap<>();
        bfs.enqueue(urlsLog.load());
        clientResponseLog = new ClientResponseLog();
    }

    @Override
    public void run() {
                try {
                    loop();
                } catch (IOException e) {
                    e.printStackTrace();
                }
    }

    private void loop() throws IOException {
        (new Thread(() -> {
            try {
                sendURL();
            } catch (IOException e) {
                e.printStackTrace();
            }
        })).start();
        while (true) {
            selector.select();
            for (SelectionKey sk : selector.selectedKeys()) {
                if (sk.isAcceptable()) {
                    accept();
                }
                else if (sk.isReadable()) {
                    getResponse(sk);
                }
            }
            selector.selectedKeys().clear();
        }
    }

    private void accept() throws IOException {
        SocketChannel client = ssc.accept();
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);
        synchronized (clientsStatus) {
            clientsStatus.put(client.keyFor(selector), null);
        }
        buffers.put(client.keyFor(selector), ByteBuffer.allocate(BUFFER_SIZE));
        System.out.println("new client");
    }

    @Override
    public void sendURL() throws IOException {
        while(true) {
                synchronized (clientsStatus) {
                    for (SelectionKey sk : clientsStatus.keySet()) {
                        if (sk.isValid() && !isBusy(clientsStatus.get(sk))) {
                            synchronized (bfs) {
                                URL url = bfs.dequeue();
                                if (url != null) {
                                    bfs.addOngoing(url);
                                    clientsStatus.replace(sk, url);
                                    SocketChannel client = (SocketChannel) sk.channel();
                                    String content = "{url: \""+url.toString()+"\"}";
                                    String msg = "WCP\r\nContent-Length: " + content.length() + "\r\n\r\n" + content;
                                    System.out.println(msg);
                                    buffers.get(sk).put(CHARSET.encode(msg));
                                    buffers.get(sk).flip();
                                    client.write(buffers.get(sk));
                                    break;
                                }
                            }
                        }
                    }
                }
        }
    }

    @Override
    public void getResponse(SelectionKey sk) throws IOException {
        SocketChannel client = (SocketChannel) sk.channel();
        buffers.get(sk).clear();
        try {
            if (client.read(buffers.get(sk)) == -1) {
                System.out.println("connection" + client + " closed");
                buffers.remove(sk);
                synchronized (clientsStatus) {
                    bfs.getBack(clientsStatus.get(sk));
                    clientsStatus.remove(sk);
                }
                sk.cancel();
                client.close();
                return;
            }
        }
        catch (IOException e){

        }
        try {
            WcpResponseHandler wrh = new WcpResponseHandler(client, buffers.get(sk));
            URL visited;
            synchronized (clientsStatus) {
                visited = clientsStatus.get(sk);
                clientsStatus.replace(sk, null);
            }
            wordMap.put(visited, wrh.getKeywords());
            bfs.addVisited(visited);
            bfs.enqueue(wrh.getURLs());
            urlsLog.append(wrh.getURLs());
            clientResponseLog.append(wrh.getLogs());
        }
        catch (IOException e){
            buffers.remove(sk);
            synchronized (clientsStatus) {
                bfs.getBack(clientsStatus.get(sk));
                clientsStatus.remove(sk);
            }
            sk.cancel();
            client.close();
            return;
        }

    }

    private static boolean isBusy(URL url){
        return url != null;
    }

    public static void main(String[] args) throws IOException {
        WcpServer server = new WcpServer(9090);
        (new Thread(server)).start();
    }
}
