package taow3.server;

import java.io.IOException;

import java.net.ServerSocket;
import java.net.Socket;
/**
 * 
 * @author Tao Wang(707458) this class is designed for listening from client
 *
 */

public class ListenThread extends Thread{
	
	private ServerSocket serverSocket;
	private ChatServer chatServer;
	private Socket socket;
	
	
	public ListenThread(int port,ChatServer chatServer ){
		try {
			this.serverSocket = new ServerSocket(port);
			
			this.chatServer = chatServer;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
	}

	@Override 
	public void run(){
		
		try {
			while(true){
			this.socket = this.serverSocket.accept();
			// a user connection is created when accepting a socket
			UserConnection uc = new UserConnection(socket);
			// a user connection sets its server attribute 
			uc.setChatServer(chatServer);
			// a user connection sets its room attribute as null first
			uc.setChatRoom(null);
			// starts this connection 
            uc.start();
            
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
			 if(serverSocket!=null){
				 try{
					 serverSocket.close();
				 }catch(IOException e){
					// e.printStackTrace();
				 }
			 }
	 }
		
		 
	 
	}		 
}
		 
	 
	

