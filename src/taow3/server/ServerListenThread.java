package taow3.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerListenThread implements Runnable {
	private ServerSocket serverSocket;
	private ChatServer chatServer;
	
	public ServerListenThread(int port,ChatServer chatServer ){
		try {
			this.serverSocket = new ServerSocket(port);
			this.chatServer = chatServer;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Override 
	public void run(){
		while(true){
		try {
			Socket socket = this.serverSocket.accept();
			ServerConnection sc = new ServerConnection(socket);
			sc.setServer(this.chatServer);
			sc.run();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	 }
	}
}
