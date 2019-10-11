package com.webcrawling.server;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class WcpResponseHandler {
    private String responseHeader;
    private String responseContent;
    private JsonObject jsonResponseContent;

    public WcpResponseHandler(SocketChannel client, ByteBuffer buffer) throws IOException {
        StringBuilder response = new StringBuilder();
        Charset c = Charset.forName("UTF-8");
        CharBuffer cb;
        try {
            do {
                buffer.flip();
                cb = c.decode(buffer);
                response.append(cb);
                buffer.clear();
            } while (client.read(buffer) > 0);
        }
        catch (IOException e){
            throw e;
        }

        responseHeader = response.toString().split("\r\n\r\n")[0];
        responseContent = response.toString().split("\r\n\r\n")[1];

        JsonParser parser = new JsonParser();
        jsonResponseContent =  parser.parse(responseContent).getAsJsonObject();
    }

    public List<URL> getURLs(){
        JsonArray jsonURLs = jsonResponseContent.get("urls").getAsJsonArray();
        List<URL> urls = new ArrayList<>();
        for(JsonElement path : jsonURLs){
            try {
                urls.add(new URL(path.getAsString()));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        return urls;
    }

    public List<String> getKeywords(){
        JsonArray jsonKeywords = jsonResponseContent.get("keywords").getAsJsonArray();
        List<String> keywords = new ArrayList<>();
        for(JsonElement keyword : jsonKeywords){
            keywords.add(keyword.getAsString());
        }
        return keywords;
    }

    public boolean isUrlContentHTML(){
        String type = jsonResponseContent.get("type").getAsString();
        return type.equals("html");
    }

    public int urlContentLength(){
        int size = jsonResponseContent.get("length").getAsString().length();
        return size;
    }

    public List<String> getLogs(){
        JsonObject jsonLogs = jsonResponseContent.get("log").getAsJsonObject();
        List<String> logs = new ArrayList<>();
        jsonLogs.entrySet().forEach(k -> logs.add(k.getKey()+" "+k.getValue().getAsString()));
        return logs;
    }

}