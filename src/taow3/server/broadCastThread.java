package taow3.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * 
 * @author Tao Wang(707458)
 * This class is utilized for establishing connections among servers
 *
 */
public class broadCastThread implements Runnable{
	private BufferedReader input;
	private BufferedWriter output;
	private Socket socket;
	private String msg=null;
	
	public broadCastThread(String hostName,int inServer_Port, String server_Id,String msg){
		InetAddress address = null;	
		try {
			
			if(hostName.equals("localhost"))
				address =InetAddress.getLocalHost();
			else
				address= InetAddress.getByName(hostName);
			// create a socket with server's IP address and server listening port number 
			this.socket = new Socket(address,inServer_Port);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		this.msg =msg;
		try {
			this.input = new BufferedReader(new InputStreamReader(socket.getInputStream(),"UTF-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		try {
			this.output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),"UTF-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		
		
	}
	public void sendMsg(String msg){
    	
		try {
    		
			this.output.write(msg+"\n");
			this.output.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
    	
    }
	
	
	
	public void run(){
		
			this.sendMsg(msg);
			if(input!=null)
				try {
					this.input.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
				}
			if(output!=null)
				try {
					this.output.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
				}
			if(socket!=null)
				try {
					this.socket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
				}
	}
		
		
	

}

