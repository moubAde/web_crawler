package com.webcrawling.server;

import com.webcrawling.util.HttpRequestParser;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class HttpRequestHandler {
    private HttpRequestParser httpRequestParser;
    private static final String VIEWS_PATH = "src/../views/";

    public HttpRequestHandler(SocketChannel client, ByteBuffer buffer) throws IOException {
        httpRequestParser = new HttpRequestParser(client, buffer);
    }

    public String response(){
        String method = httpRequestParser.getMethod();
        if(method.equals("GET")){
            return responseGET();
        }
        else if(method.equals("POST")) {
            return responsePOST();
        }
        return responseMethodNotAllowed();
    }

    private String responseGET(){
        String html;
        String response;
        String context = httpRequestParser.getContext();
        if(context.equals("/")){
            try {
                FileInputStream fis = new FileInputStream(VIEWS_PATH+"index.html");
                byte[] b = new byte[fis.available()];
                fis.read(b);
                html = new String(b);
                response = "HTTP/1.1 200 OK\r\nContent-Type: text/html\r\nContent-Length: "+html.length()+"\r\n\r\n"+html;
                return response;
            }
            catch(IOException e) {
                e.printStackTrace();
            }
        }
        else if(context.equals("/search.html")){
            try {
                FileInputStream fis = new FileInputStream(VIEWS_PATH+"search.html");
                byte[] b = new byte[fis.available()];
                fis.read(b);
                html = new String(b);
                response = "HTTP/1.1 200 OK\r\nContent-Type: text/html\r\nContent-Length: "+html.length()+"\r\n\r\n"+html;
                return response;
            }
            catch(IOException e) {
                e.printStackTrace();
            }
        }

        else if(context.startsWith("/search.html")){
            String keyword = context.split("=")[1].trim();
            String notFound = "Keyword <b>"+keyword+"</b> not found.";
            String result = "";
            System.out.println(WcpServer.wordMap);
            if(WcpServer.wordMap.get(keyword) != null) {
                for (URL url : WcpServer.wordMap.get(keyword)) {
                    result += "<div><a href=" + url.toString() + ">" + url.toString() + "</a>/<div>";
                }
            }
            else{
                result = notFound;
            }
            html = "<html><head>\n" +
                    "    <meta charset=\"UTF-8\">\n" +
                    "    <title>Web Crawling</title>\n" +
                    "    <link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\">\n" +
                    "</head><div><h1>Web Crawling</h1>\n" +
                    "    <br>\n" +
                    "    <form action=\"/search.html\" method=\"get\">\n" +
                    "        <p><input type=\"text\" name=\"keyword\" id=\"keyword\" placeholder=\"Keyword...\"> <input type=\"submit\" value=\"Search\"></p>\n" +
                    "    </form>"+result+"</div></html>";
            response = "HTTP/1.1 200 OK\r\nContent-Type: text/html\r\nContent-Length: "+html.length()+"\r\n\r\n"+html;
            return response;
        }
        else if(context.equals("/style.css")){
            try {
                FileInputStream fis = new FileInputStream(VIEWS_PATH+"style.css");
                byte[] b = new byte[fis.available()];
                fis.read(b);
                html = new String(b);
                response = "HTTP/1.1 200 OK\r\nContent-Type: text/css\r\nContent-Length: "+html.length()+"\r\n\r\n"+html;
                return response;
            }
            catch(IOException e) {
                e.printStackTrace();
            }
        }
        html = "<html><br><br><h1>Page Introuvable</h1><br><br></html>";
        response = "HTTP/1.1 404 Not Found\r\nContent-Type: text/html\r\nContent-Length: "+html.length()+"\r\n\r\n"+html;
        return response;
    }

    private String responsePOST(){
        String paramName =  "url";
        String paramValue = httpRequestParser.getParams().get(paramName);
        String response;

        try {
            if(paramValue.startsWith("www")){
                paramValue = "http://"+paramValue;
            }
            URL url = new URL(paramValue);
            WcpServer.bfs.enqueue(url);
            WcpServer.urlsLog.append(url);
            response = "HTTP/1.1 301 Moved Permanently\r\nLocation: /search.html\r\nContent-Type: text/html\r\nContent-Length: 0\r\n\r\n";
            return response;
        } catch (MalformedURLException e) {
            try {
                FileInputStream fis = new FileInputStream(VIEWS_PATH+"search-nok.html");
                byte[] b = new byte[fis.available()];
                fis.read(b);
                String html = new String(b);
                response = "HTTP/1.1 400 Bad Request\r\nContent-Type: text/html\r\nContent-Length: " + html.length() + "\r\n\r\n" + html;
                return response;
            }
            catch (IOException ex){
                ex.printStackTrace();
            }
        }
        return "";
    }

    private String responseMethodNotAllowed(){
        String response = "HTTP/1.1 405 Method Not Allowed\r\nContent-Type: text/html\r\nContent-Length: " + 0 + "\r\n\r\n";
        return response;
    }
}
