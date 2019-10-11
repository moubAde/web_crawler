package com.webcrawling.server.logs;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ClientResponseLog {
    private static final String LOGS_DIRECTORY = "src/../logs/";
    private static final String LOGS_FILENAME = "clients_responses_logs.txt";

    public synchronized void append(String log){
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(LOGS_DIRECTORY+LOGS_FILENAME, true);
            fos.write((log+"\n").getBytes());

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

    public void append(List<String> logs){
        for(String log : logs){
            append(log);
        }
    }
}
