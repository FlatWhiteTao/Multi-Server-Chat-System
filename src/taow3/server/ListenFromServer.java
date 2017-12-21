package taow3.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 
 * @author Tao Wang (707458) this class is designed for listening from the server
 *
 */

public class ListenFromServer extends Thread {
	private ServerSocket inServerSocket;
	private ChatServer chatServer;
	private Socket socket;
	
	
	
	public ListenFromServer(int port,ChatServer chatServer ){
		try {
			this.inServerSocket = new ServerSocket(port);
			
			this.chatServer = chatServer;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
	}
	
public void run(){
		
		try {
			while(true){
			this.socket = this.inServerSocket.accept();
			// a server connection is established, when accepting a socket, 
			ServerConnection sc = new ServerConnection(socket);
			//Set this connection to a server
			sc.setServer(chatServer);
			//Start this server connection thread 
            sc.start();
            
		} 
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}finally{
			 if(this.socket!=null){
				 try{
					 socket.close();
				 } catch(IOException e){
					 //e.printStackTrace();
				 }
			 }
			 if(inServerSocket!=null){
				 try{
					 inServerSocket.close();
				 }catch(IOException e){
					// e.printStackTrace();
				 }
			 }
	 }
		
		 
	 
	}	
}
