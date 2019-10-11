package com.webcrawling.util;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class BFS {
    private Queue<URL> urlQueue = new LinkedList<>();
    private Set<URL> ongoing = new HashSet<>();
    private Set<URL> visited = new HashSet<>();

    public synchronized URL dequeue(){
        return urlQueue.poll();
    }

    public synchronized void enqueue(URL url){
        if(!(visited.contains(url) && !ongoing.contains(url))){
            urlQueue.add(url);
        }
    }

    public synchronized void enqueue(List<URL> urls){
        for (URL url : urls) {
            enqueue(url);
        }
    }

    public synchronized void addVisited(URL url) {
        ongoing.remove(url);
        visited.add(url);
    }

    public synchronized void addOngoing(URL url){
        this.ongoing.add(url);
    }

    public synchronized void getBack(URL url){
        ongoing.remove(url);
        urlQueue.add(url);
    }

    public static void main(String[] args) throws IOException {
        BFS bfs = new BFS();
        URL root = new URL("http://www.google.com");
        bfs.enqueue(root);
        /*Set<URL> urls;
        for (URL url:urls){
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.getContent();
        }*/

    }
}
