package com.webcrawling.client;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NetworkCalcul implements Runnable {
	Client client; 
	
	private String domaine;
	private String url;
	private String fichier;
	private int port;
	private List<String> lien;
	private List<String> mot;
	private String type_fichier;
	private int taille_fichier;
	private int taille_head;
	
	public List<String> getLien() {
		return lien;
	}

	public List<String> getMot() {
		return mot;
	}

	public String getType_fichier() {
		return type_fichier;
	}

	public int getTaille_fichier() {
		return taille_fichier;
	}

	InetSocketAddress addr;
	SocketChannel socketChannelCalcul;
	
	Charset charset = Charset.forName ("UTF-8");
	CharBuffer sms;
	ByteBuffer byteBufferSend=ByteBuffer.allocate(512);
	ByteBuffer byteBufferReceive=ByteBuffer.allocate(512);
	StringBuffer head; 
	
	
	
	
	public NetworkCalcul(String url, int port, String fichier, Client cli){
		this.client=cli;
		lien=new ArrayList<>();
		mot=new ArrayList<>();
		//url=new String("www.example.com");
		//url=new String("www.facebook.com");
		//url=new String("www.free.fr");
		//fichier=new String("/index.html");
		//port=80;
		
		
		this.url=url;
		this.port=port;
		this.fichier=fichier;
		addr=new InetSocketAddress(url, port);
		domaine=url;
		if(url.contains("www.")) {
			int pos=url.indexOf('.');
			domaine=url.substring(pos+1, url.length());
		}
		System.out.println("domaine->"+ domaine);
		/*try {
			Thread.sleep(10000);
		} catch (InterruptedException t) {
			t.printStackTrace();
		}*/
		head=new StringBuffer();
	}
	
	public boolean connection() {
		try {
			socketChannelCalcul = SocketChannel.open();
			socketChannelCalcul.connect(addr);
			//socketChannelCalcul.configureBlocking(false);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public StringBuffer verificationCodeRetour(StringBuffer reponse, String requete, String option) {
		StringBuffer code = recherche(reponse,"HTTP/",9);
		//System.out.println("code->"+code);
		if(new String(code).equals("200 OK")) {
			client.getLog().put(url+":"+port+fichier, new String(code));
			return reponse;
		}// quelle est la dif entre la redirection permanente et temporaire
		else if(new String(code).equals("302 Moved Temporarily") || new String(code).equals("302 Found") || new String(code).equals("301 Moved Permanently")) {
			client.getLog().put(url+":"+port+fichier, new String(code));
			StringBuffer location = recherche(reponse,"Location: ",10);
			location.append("\r\n");
			System.out.println("location->"+location);
			if(!new String(location).contains("https")) {
				triLien(location);
				if(connection()) {
					if(option.equals("HEAD")) {
						return requeteHead();
					}
					if(option.equals("GET")) {
						return requeteGet();
					}
				}
			}	
		}
		else if(new String(code).equals("401 Unauthorized") || new String(code).equals("403 Forbidden")) {
			client.getLog().put(url+":"+port+fichier, new String(code));
			//demander une autthentification a l'utilisateur
		}
		else {
			client.getLog().put(url+":"+port+fichier, new String(code));
		}
		return new StringBuffer("false");
	}
	
	public StringBuffer requeteHead() {
		try {
			taille_head=0;
			String requete=new String("HEAD "+fichier+" HTTP/1.1\r\nHOST: "+url+":"+port+"\r\n\r\n");
			System.out.println(requete);
			byteBufferSend=ByteBuffer.wrap(requete.getBytes());
			socketChannelCalcul.write(byteBufferSend);		
			StringBuffer reponse=new StringBuffer();
			int retval=socketChannelCalcul.read(byteBufferReceive);
			System.out.println("chargement head");
			while(retval>0) { // utilise le champ Content-Length de la reponse pour t'assuré que tu as tout reçu
				byteBufferReceive.flip();
				sms = charset.decode(byteBufferReceive);
				reponse.append(sms);
				byteBufferReceive.clear();
				taille_head+=retval;
				if((new String(reponse)).contains("\r\n\r\n")) {
					//System.err.println("break head //////////////////////////////////////////////////////////////////");
					break;
				}
				retval=socketChannelCalcul.read(byteBufferReceive);
			}
			//System.err.println("taille 0 apres:"+(++taille_head));
			System.out.println(reponse.toString());
			return verificationCodeRetour(reponse, requete,"HEAD");
			
		} catch (IOException e) {
			e.printStackTrace();
		}	
		return null;
	}
	
	public StringBuffer requeteGet() {
		if(type_fichier.toString().contains(("text/html"))) {
			try {
				//HEAD /favicon.ico?v=1	HTTP/1.1
				//HEAD /index.html/favicon/apple-icon-57x57.png/favicon/apple-icon-60x60.png  HTTP/1.1  
				//HOST: mobile.free.fr:80
				String requete=new String("GET "+fichier+" HTTP/1.1\r\nHOST: "+url+":"+port+"\r\nConnection: keep-alive\r\n\r\n");
				System.out.println(requete);
				byteBufferSend=ByteBuffer.wrap(requete.getBytes());
				socketChannelCalcul.write(byteBufferSend);		
				StringBuffer reponse=new StringBuffer();
				int retval=socketChannelCalcul.read(byteBufferReceive);
				int taille=retval;
				System.out.println("chargement get");
				while(retval>0) {
					byteBufferReceive.flip();
					sms = charset.decode(byteBufferReceive);
					reponse.append(sms);
					byteBufferReceive.clear();
					if((new String(reponse)).contains("</body>")) {
						//System.err.println("break get //////////////////////////////////////////////////////////////////");
						break;
					}
					retval=socketChannelCalcul.read(byteBufferReceive);
					taille+=retval;
				}
				//System.out.println("head:"+taille_head+" taille:"+taille);
				/*if (taille_fichier!=taille) {// pour s'assurer que tout le fichier a été envoyé
					return null;
				}*/
				//System.out.println(reponse.toString());
				return verificationCodeRetour(reponse, requete,"GET");
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public void rechercheLien(StringBuffer reponse) {
		//System.out.println("//////////////////////////////////////////////////////////////");
		int posRecherche=0;
		StringBuffer recherche=new StringBuffer();
		Pattern adr = Pattern.compile("href=\"\\p{ASCII}*\"");
		Pattern adrRelative = Pattern.compile("href=\"/\\p{ASCII}*\"");// exp:  /freebox/manifest.json
		Pattern adrCourant = Pattern.compile("href=\"#\\p{ASCII}*\""); // exple: #f
		Matcher m;
		while(posRecherche<reponse.length()) {
			posRecherche=reponse.indexOf("href=\"", posRecherche);
			if(posRecherche>=0) 
				posRecherche+=6;
			else
				break;
			while(reponse.charAt(posRecherche)!='"') {
				recherche.append(reponse.charAt(posRecherche));
				posRecherche++;
			}
			if(!new String(recherche).contains("https")) {// éjecter les liens https
				m = adr.matcher("href=\""+recherche+"\"");
				if(m.matches()) {
					m=adrCourant.matcher("href=\""+recherche+"\"");
					if(m.matches()) {
						continue;// adr deja consulté
					}
					m=adrRelative.matcher("href=\""+recherche+"\"");
					if(m.matches()) {
						System.out.println("avant->"+recherche);
						recherche=new StringBuffer("http://"+url+fichier+recherche);
						System.out.println("url modifié->"+recherche);
						/*try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}*/
						lien.add(recherche.toString());
						continue;
					}
					if(new String(recherche).contains(domaine)) {
						lien.add(recherche.toString());
						//System.out.println("recherche------------------>"+recherche.toString());
					}
					recherche=new StringBuffer();
				}
			}	
		}
	}
	
	public StringBuffer recherche(StringBuffer reponse, String option, int offset) {
		int posRecherche=0;
		StringBuffer recherche=new StringBuffer();
		posRecherche=reponse.indexOf(option, posRecherche);
		if(posRecherche>=0) 
			posRecherche+=offset;
		else
			return null;
		while(reponse.charAt(posRecherche)!='\r' && reponse.charAt(posRecherche)!='\n') {
			recherche.append(reponse.charAt(posRecherche));
			posRecherche++;
		}
		return recherche;	
	}
	
	//permet de recuperer l'adresse de la machine hote et celui du fichier
	public boolean triLien(StringBuffer reponse) {
		int posRecherche=0;
		StringBuffer recherche=new StringBuffer();
		posRecherche=reponse.indexOf("www", posRecherche);
		if(!(posRecherche>=0))
			return false;
		while(reponse.charAt(posRecherche)!='/') {
			recherche.append(reponse.charAt(posRecherche));
			posRecherche++;
		}
		url=new String(recherche);
		recherche=new StringBuffer();
		while(reponse.charAt(posRecherche)!='\r' && reponse.charAt(posRecherche)!='\n') {
			recherche.append(reponse.charAt(posRecherche));
			posRecherche++;
		}
		fichier=new String(recherche);
		if (fichier.equals("/")) {
			fichier="index.html";
		}
		return true;
	}
	
	//permet de recuperer tous les mots sous forme d'un text
	public StringBuffer rechercheMot(StringBuffer reponse) {// je dois gerer les code php ou js que je pourrais rencontrer avec une reg expr revoir la partie
		//Pattern pPHP = Pattern.compile("^<? \\p{ASCII}* >$"); //reg expression pour le code PHP
		//Matcher mPHP;
		Pattern pJS = Pattern.compile("<script [\\p{ASCII}\\p{Punct}\\t\\n\\x0B\\f\\r]* >"); //reg expression pour le code JS
		Matcher mJS;
		boolean arret=false;
		StringBuffer temp=new StringBuffer("<");
		int posRecherche=0;
		StringBuffer recherche=new StringBuffer();
		posRecherche=reponse.indexOf("<body", posRecherche);
		if(posRecherche>=0) { 
			//posRecherche+=6;
			while(reponse.charAt(posRecherche)!='>') {
				posRecherche++;
			}
			posRecherche++;
		}	
		else
			return null;
		while(!arret && posRecherche<reponse.length()) {
			while(reponse.charAt(posRecherche)=='<') {
				while(reponse.charAt(posRecherche)!='>') {
					posRecherche++;
					temp.append(reponse.charAt(posRecherche));
				}
				posRecherche++;
				//System.out.println("mot "+temp);
				mJS = pJS.matcher(temp);
				if(mJS.matches()) {//supprimer le code js
					while(reponse.charAt(posRecherche)!='>') {
						posRecherche++;
					}
					posRecherche++;
				}
				if(new String(temp).equals("</body>")) {
					arret=true;
				}
				temp=new StringBuffer("<");
			}
			recherche.append(reponse.charAt(posRecherche));
			posRecherche++;
		}
		return recherche;	
	}
	
	//traite le text reçue de rechercheMot() pour avoir les différents mmots 
	public void saveWord(StringBuffer s) {
		int position=0;
		StringBuffer recherche=new StringBuffer();
		Pattern p = Pattern.compile("^\\p{Punct}$");
		Matcher m;
		while(position<s.length()) {
			m = p.matcher(""+s.charAt(position));
			while(!Character.isWhitespace(s.charAt(position)) && !m.matches()) {//verifie si je n'est pas un espace blanc ou une ponctuation
				recherche.append(s.charAt(position));
				position++;
				m = p.matcher(""+s.charAt(position));
			}
			if(recherche.length()!=0) {
				mot.add(new String(recherche));
				recherche=new StringBuffer();
			}
			position++;
		}	
	}
	
	@Override
	public void run() {
		if(connection()) {
			System.out.println("begin Head");
			head=requeteHead();
			if(head!=null) {
				StringBuffer sb=recherche(head,"Content-Type: ",14);
				/*if(sb==null) {
					System.err.println("null///////////////////////////////////////////////////////////////////////////null");
					try {
						Thread.sleep(0000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				else {*/
				    type_fichier = new String(sb);
				    try {
				    	taille_fichier=Integer.parseInt(new String(recherche(head,"Content-Length: ",16)));
				    }catch (Exception e) {
				    	taille_fichier=0;// es ce correcte?
					}
				    if(connection()) {
				    	if(type_fichier.toString().contains(("text/html"))) {
					    	System.out.println("begin Get");
							StringBuffer reponse=requeteGet();
							if(reponse!=null) {
								System.out.println("begin recherche Lien");
								rechercheLien(reponse);
								System.out.println("begin recherche Mot");
								StringBuffer text=rechercheMot(reponse);
								System.out.println(text);
								System.out.println("begin save word");
								saveWord(text);
								/*for (String s:mot) {
									System.out.println(s+"//");
								}*/
								try {
									socketChannelCalcul.close();
								} catch (IOException e) {
									e.printStackTrace();
								}
							}	
				    	}
				    }	
			    //}
			}	    
		}	
	}
}
