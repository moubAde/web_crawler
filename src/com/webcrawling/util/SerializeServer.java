package com.webcrawling.util;

import java.util.List;

import com.google.gson.Gson;

public class SerializeServer {
	
	private String url;
	
	public SerializeServer() {}
	
	public SerializeServer(String url) {
		this.url=url;
	}
	
	
	public String serialize() {
		StringBuilder s=new StringBuilder("WCP \r\n{");
	    s.append("URL:\""+url+"\"}\r\n");
		//System.out.println("seria->"+s+"|");
		return new String(s);
    }
    
    public static SerializeServer deserialize(String json) {
    	StringBuilder recherche=new StringBuilder();
    	int pos=json.indexOf("url:");
    	pos+=6;
    	while(json.charAt(pos)!='"' && pos<json.length()) {
    		recherche.append(json.charAt(pos));
    		pos++;
    	}
    	//System.out.println("deseria->"+recherche+"|");
    	return new SerializeServer(new String(recherche));
    }
    
    public String serializeGson() {
    	Gson gson = new Gson();
		return gson.toJson(this);
    }
    
    public static SerializeServer deserializeGson(String json) {
		return new Gson().fromJson(json, SerializeServer.class);
    }
    
    public String getUrl() {
		System.out.println("URL "+url);
    	int pos =url.indexOf("www.");
    	StringBuilder reponse=new StringBuilder();
    	if(pos>=0) {
    		while(pos<url.length() && url.charAt(pos)!='/' && url.charAt(pos)!=':') {
    			reponse.append(url.charAt(pos));
    			pos++;
    		}
    		//System.out.println("url server->"+new String(reponse)+"|");
    		return new String(reponse);
    	}
    	pos =url.indexOf("http://");
    	//System.err.println("->"+url);
    	reponse=new StringBuilder();
    	if(pos>=0) {
    		pos+=7;
    		while(pos<url.length() && url.charAt(pos)!='/' && url.charAt(pos)!=':') {
    			reponse.append(url.charAt(pos));
    			pos++;
    		}
    		return new String(reponse);
    	}
    	return "";
    }

    
    public int getPort(String u) {
    	int pos =url.indexOf(u+":");
    	StringBuilder reponse=new StringBuilder();
    	if(pos>=0) {
            pos+=u.length()+1;
    		while(url.charAt(pos)!='/' && pos<url.length()) {
    			reponse.append(url.charAt(pos));
    			pos++;
    		}
    		System.out.println("port server->"+new String(reponse)+"|");
    		try {
    			return Integer.parseInt(new String(reponse));
    		}catch(Exception e){
    			return 80;
    		}
    	}
    	return 80;
    }
    
    public String getFichier(String u, int port) {
    	int pos=0;
    	
    	String s=u+":"+port;
		pos =url.indexOf(s);
		if(pos>=0) {
			pos+=s.length();
		}else {
			pos =url.indexOf(u+"/");
			if(pos>=0) {
				pos+=u.length();
			}	
		}
		
    	StringBuilder reponse=new StringBuilder();
    	if(pos>=0) {
    		while(pos<url.length()) {
    			reponse.append(url.charAt(pos));
    			pos++;
    		}
    		//System.out.println("fichier server->"+new String(reponse)+"|");
    		return new String(reponse);
    	}
    	System.out.println("false");
    	return "/index.html";
    }
    
    public static void main(String[] args) {
		SerializeServer ss=new SerializeServer("www.free.fr:80/index.html");
		String json=ss.serialize();
		SerializeServer ss2=SerializeServer.deserialize(json);
		System.out.println("url Deseria->"+ss2.url+"|");
	}
}
