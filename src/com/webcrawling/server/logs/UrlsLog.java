package com.webcrawling.server.logs;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class UrlsLog {
    private static final String LOGS_DIRECTORY = "src/../logs/";
    private static final String LOGS_FILENAME = "urls_logs.txt";

    public List<URL> load(){
        try {
            return getLogLines();
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    public synchronized void append(URL url){
        FileOutputStream fos = null;
        try {
            List<URL> urls = getLogLines();
           if(!urls.contains(url)){
               fos = new FileOutputStream(LOGS_DIRECTORY+LOGS_FILENAME, true);
               fos.write((url.toString()+"\n").getBytes());
           }

        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if(fos != null){
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void append(List<URL> urls){
        for(URL url : urls){
            append(url);
        }
    }


    private List<URL> getLogLines() throws IOException {
        List<URL> urls = new ArrayList<>();
        Path path = Paths.get(LOGS_DIRECTORY+LOGS_FILENAME);
        Files.readAllLines(path).forEach(urlStr -> {
            try {
                urls.add(new URL(urlStr));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        });

        return urls;
    }
}
