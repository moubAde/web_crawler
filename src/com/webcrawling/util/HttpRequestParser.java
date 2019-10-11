package com.webcrawling.util;

import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpRequestParser {
    private int contentLength;
    private String method;
    private String context;
    private Map<String, String> params;

    private static final Charset CHARSET = Charset.forName("UTF-8");

    public HttpRequestParser(SocketChannel sc, ByteBuffer buffer) throws IOException {
        params = new HashMap<>();
        parseHeader(sc, buffer);
        parseContent(sc,buffer);
    }

    private void parseHeader(SocketChannel sc, ByteBuffer buffer) throws IOException {
        StringBuilder headerBuilder = new StringBuilder();
        while (!headerBuilder.toString().contains("\r\n\r\n")){
            sc.read(buffer);
            buffer.flip();
            headerBuilder.append(CHARSET.decode(buffer));
            buffer.clear();
        }
        String remaining = headerBuilder.toString().substring(headerBuilder.indexOf("\r\n\r\n")).trim();
        buffer.put(CHARSET.encode(remaining));
        String header = headerBuilder.toString().substring(0,headerBuilder.toString().indexOf("\r\n\r\n"));
        method = header.split("\r\n")[0].split(" ")[0].trim();
        context = header.split("\r\n")[0].split(" ")[1].trim();
        contentLength = getContentLength(header);
    }

    private int getContentLength(String header){
        if(method.equals("POST")){
            Pattern pattern = Pattern.compile("Content-Length: (\\d)+\\r\\n");
            Matcher matcher = pattern.matcher(header);
            matcher.find();
            String contextLengthStr = matcher.group();
            int length = Integer.parseInt(contextLengthStr.split(":")[1].trim());
            return length;
        }
        return 0;
    }

    private void parseContent(SocketChannel sc, ByteBuffer buffer) throws IOException{
        StringBuilder contentBuilder = new StringBuilder();
        buffer.flip();
        contentBuilder.append(CHARSET.decode(buffer));
        buffer.clear();
        while (contentLength>contentBuilder.toString().length()){
            sc.read(buffer);
            buffer.flip();
            contentBuilder.append(CHARSET.decode(buffer));
            buffer.clear();
        }

        String content = contentBuilder.toString().substring(0,contentLength);
        buffer.put(content.substring(content.length()).getBytes());
        if(method.equals("POST")){
            content = URLDecoder.decode(content, "UTF-8");
            for(String param : content.split("&")){
                params.put(param.split("=")[0], param.split("=")[1]);
            }
        }
    }

    public String getMethod() {
        return method;
    }

    public String getContext() {
        return context;
    }

    public Map<String, String> getParams() {
        return params;
    }
}
