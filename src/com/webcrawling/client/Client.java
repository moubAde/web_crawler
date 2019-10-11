package com.webcrawling.client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Scanner;

import com.webcrawling.util.Log;
import com.webcrawling.util.SerializeClient;
import com.webcrawling.util.SerializeServer;


public class Client {
	private SocketChannel socketChannelServer;
	private boolean etat;
	private Log log;
	ByteBuffer byteBufferServer;
	Charset c = Charset.forName ("UTF-8");
	CharBuffer sms;
	
	
	public Client(InetAddress addr, int port) throws IOException  {
		InetSocketAddress inetSocketAddress= new InetSocketAddress(addr, port);
		socketChannelServer=SocketChannel.open(inetSocketAddress);
		byteBufferServer=ByteBuffer.allocate(512);
		this.etat=true;
		log=new Log();
		System.out.println("connexion établie");
	}
	
	public boolean isConnected() {
		return this.etat;
	}
	
	public void setEtat(boolean etat) {
		this.etat=etat;
	}
	
	public Log getLog() {
		return log;
	}
	
	public void start() throws InterruptedException, IOException {
		int retval=0;
		while(isConnected()) {
			//System.out.println("while");
			try {
				retval=socketChannelServer.read(byteBufferServer);//pense a faire un while sur le read
				//System.out.println("ret:"+retval);
				if(retval>0) {//{ url: "www.free.fr:80/index.html"}
					
					byteBufferServer.flip();
					sms=c.decode(byteBufferServer);
					//System.out.println("sms:"+sms.toString());
					SerializeServer ss= SerializeServer.deserialize(sms.toString());
					String url=ss.getUrl();
					int port=ss.getPort(url);
					String fichier=ss.getFichier(url, port);
					System.out.println("url:"+url+" port:"+port+" fichier:"+fichier);
					SerializeClient sc;
					try {
						NetworkCalcul networkCalcul=new NetworkCalcul( url, port, fichier, this);
						networkCalcul.run();
						sc= new SerializeClient(networkCalcul.getType_fichier(), networkCalcul.getTaille_fichier(), networkCalcul.getLien(), networkCalcul.getMot(),log);
					}catch(Exception e) {
						System.err.println("cas 2///////////////////////////////////////////////////////////////");
						try {
							Thread.sleep(0000);
						} catch (InterruptedException t) {
							t.printStackTrace();
						}
						sc= new SerializeClient("null", 0, new ArrayList<>(), new ArrayList<>(),log);
					}
					String serialise= sc.serialize();
					String reponse="WCP \r\n"+(serialise.getBytes()).length+"\r\n"+serialise;//es ce vraiment la taille
					ByteBuffer bb=ByteBuffer.wrap(reponse.getBytes());
					socketChannelServer.write(bb);
					bb.clear();
					byteBufferServer.clear();
					log=new Log();
				}else{
					setEtat(false);
					System.out.println("Client fermé");
					byteBufferServer.clear();
					socketChannelServer.close();
				}
			} catch (IOException e) {
				setEtat(false);
				System.out.println("Client fermé");
				byteBufferServer.clear();
				socketChannelServer.close();
			}
		}	
		
	}
	
	public static void main(String[] args)throws Exception  {
		Client cli=new Client(InetAddress.getByName("localhost"),9090);
		cli.start();
	}
}