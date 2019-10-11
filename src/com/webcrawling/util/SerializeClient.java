package com.webcrawling.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

public class SerializeClient {
	private String type;
	private int length,content_length;
	private List<String> urls;
	private List<String> keywords;
	private Log log;
	
	public SerializeClient() {
		urls=new ArrayList<>();
		keywords=new ArrayList<>();
	}
	
	public SerializeClient(String type, int length, List<String> urls, List<String> keywords, Log log) {
		//this.content_length=content_length;
		this.type=type;
		this.length=length;
		this.urls=urls;
		this.keywords=keywords;
		this.log=log;
	}
	
	public String serialize() {
		StringBuilder content=new StringBuilder();
		content.append("{\r\ntype:\""+type+"\",\r\n");
		content.append("length:"+length+",\r\n");
		content.append("urls:[");
		if(!urls.isEmpty()) {
			for(String u:urls) {
		    	content.append("\""+u+"\",");
		    }
		    content.deleteCharAt(content.length()-1);
		    content.append("],\r\n"); 		
	   	}else {
			content.append("],\r\n");
		}
	    content.append("keywords:[");
	    if(!keywords.isEmpty()) {
		    for(String k:keywords) {
		    	content.append("\""+k+"\",");
		    }
		    content.deleteCharAt(content.length()-1);
		    content.append("],\r\n");
	    }else {
	    	content.append("],\r\n");
	    }
	    content.append("log:{");
	    if(log!=null) {
	    	Map<String,String>map=log.getAll();
		    for(Map.Entry<String, String>m:map.entrySet()) {
		    	content.append("\""+m.getKey()+"\":");
		    	content.append("\""+m.getValue()+"\",");
		    }
		    content.deleteCharAt(content.length()-1);
		    content.append("}\r\n}\r\n");
	    }else {
	    	content.append("}\r\n}\r\n");
	    }
		
	    content_length=content.length();
		StringBuilder s=new StringBuilder("WCP \r\n");
		s.append("Content-Length:"+content_length+"\r\n\r\n");
	    s.append(content);
	    
	    System.out.println("seria client->"+s.toString()+"|");
		return new String(s);
    }
    
    public static SerializeClient deserialize(String json) {
    	SerializeClient sc=new SerializeClient();
    	StringBuilder recherche=new StringBuilder();
    	int pos=0;
    	//content-length
    	recherche=new StringBuilder();
    	pos=json.indexOf("Content-Length:");
    	pos+=15;
    	while(json.charAt(pos)!='\r' && pos<json.length()) {
    		recherche.append(json.charAt(pos));
    		pos++;
    	}
    	sc.content_length=Integer.parseInt(new String(recherche));
    	//type
    	pos=json.indexOf("type:");
    	pos+=6;
    	while(json.charAt(pos)!='"' && pos<json.length()) {
    		recherche.append(json.charAt(pos));
    		pos++;
    	}
    	sc.type=new String(recherche);
    	//length
    	recherche=new StringBuilder();
    	pos=json.indexOf("length:",pos);
    	pos+=7;
    	while(json.charAt(pos)!=',' && pos<json.length()) {
    		recherche.append(json.charAt(pos));
    		pos++;
    	}
    	//System.out.println("taille->"+new String(recherche)+"|");
    	sc.length=Integer.parseInt(new String(recherche));
    	//urls
    	recherche=new StringBuilder();
    	pos=json.indexOf("urls:");
    	pos+=6;
    	while(pos<json.length() && json.charAt(pos)!=',') {
    		pos++;
    		while(pos<json.length() && json.charAt(pos)!='"') {
        		recherche.append(json.charAt(pos));
        		pos++;
        	}
    		pos+=2;
    		//System.out.println(new String(recherche)+"|");
    		sc.urls.add(new String(recherche));
    		recherche=new StringBuilder();
    	}
    	//keywords
    	recherche=new StringBuilder();
    	pos=json.indexOf("keywords:");
    	pos+=10;
    	while(pos<json.length() && json.charAt(pos)!='\r') {
    		pos++;
    		while(pos<json.length() && json.charAt(pos)!='"') {
        		recherche.append(json.charAt(pos));
        		pos++;
        	}
    		pos+=2;
    		//System.out.println(new String(recherche)+"|");
    		sc.keywords.add(new String(recherche));
    		recherche=new StringBuilder();
    	}
    	return sc;
    }
    
    public String serializeGson() {
    	Gson gson = new Gson();
		return gson.toJson(this);
    }
    
    public static SerializeClient deserializeGson(String json) {
		return new Gson().fromJson(json, SerializeClient.class);
    }
    
    public static void main(String[] args) {
    	ArrayList<String> lien=new ArrayList<>();
    	lien.add("lien 1");
    	lien.add("lien 2");
    	lien.add("lien 3");
    	ArrayList<String> keyword=new ArrayList<>();
    	keyword.add("k 1");
    	keyword.add("k 2");
    	keyword.add("k 3");
    	Log log=new Log();
    	log.put("r1", "101");
    	log.put("r2", "201");
    	log.put("r3", "301");
		SerializeClient sc=new SerializeClient("html",50,lien, keyword, log);
    	//SerializeClient sc=new SerializeClient("html",50,new ArrayList<>(), new ArrayList<>(), log);
		String json=sc.serialize();
		SerializeClient ss2=SerializeClient.deserialize(json);
		System.out.println("Deseria->");
		System.out.println("type->"+ss2.type+"|");
		System.out.println("length->"+ss2.length+"|");
		for(String d:ss2.urls) {
			System.out.println(d);
		}
		for(String d:ss2.keywords) {
			System.out.println(d);
		}
		
	}
}
