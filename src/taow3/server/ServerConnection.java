package taow3.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
/**
 * 
 * @author Tao Wang(707458) This class is designed to process messages among servers 
 *
 */
public class ServerConnection extends Thread{
	private BufferedReader input;
	private BufferedWriter output;
	private ChatServer chatServer;
	private Socket socket;
	
	//Constructor
	public ServerConnection (Socket socket){
		this.socket =socket;
		try {
			this.input = new BufferedReader(new InputStreamReader(socket.getInputStream(),"UTF-8"));
			this.output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),"UTF-8"));
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
   }
   
	public void setServer(ChatServer chatServer){
		this.chatServer = chatServer;
	}
	
	// Thread running method
	public void run(){
		String msg =null;
		  try {
				if((msg=input.readLine())!=null){
					processMsg(msg);
					this.close();
						     
				}
			} catch (IOException e) {
				
		}
	}
	
	
	// Sending message to the output buffer
	public void sendMsg(String msg){
    	try {
    		
			this.output.write(msg+"\n");
			this.output.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
    	
    }
	// process receiving messages
	public void processMsg(String msg){
		JSONParser jsonParser = new JSONParser();
		JSONObject json_rec = null;
    	try {
			json_rec = (JSONObject) jsonParser.parse(msg);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
    	String processType = (String)json_rec.get("type");
    	switch (processType){
    		case "lockidentity":
    			process_lockidentity(json_rec);
    			break;
    		case "releaseidentity":
    			process_releaseidentity(json_rec);
    			break;
    		case "getrooms":
    			process_getRooms(json_rec);
    			break;
    		case "lockroomid":
    			process_lockroomid(json_rec);
    			break;
    		case "releaseroomid":
    			process_releaseroomid(json_rec);
    			break;
    		case "deleteroom":
    			process_deleteroom(json_rec);
    			break;
    		default:
    			break;
    	}
	}
	
	private void process_deleteroom(JSONObject json_rec) {
		String roomid = (String) json_rec.get("roomid");
		this.chatServer.delRoominSysByRoomid(roomid);
	}

	@SuppressWarnings("unchecked")
	public void process_lockidentity(JSONObject json_rec){
		JSONObject json_rep = new JSONObject();
		//this.chatServer.
		String identity = (String)json_rec.get("identity");
		String serverId = (String)json_rec.get("serverid");
		//process the lock of the identity from the client
		
		if(!json_rec.containsKey("locked"))
		{
			if(!this.chatServer.getLockid().contains(identity)&&!this.chatServer.getUserList().contains(identity))
			{   
			//if the identity dose not exist in another server, then add it the lock list first 
			this.chatServer.addLockid(identity); 
			//reply a true lock identity JSON message to the server
			json_rep.put("type","lockidentity");
			json_rep.put("serverid",this.chatServer.getServer_Id());
			json_rep.put("identity", identity);
			json_rep.put("locked","true");
			for(int i =0; i< this.chatServer.getServerList().size();i++){
				if(this.chatServer.getServerList().get(i).get(0).equals(serverId)){
					Thread broadcast = new Thread (new broadCastThread(this.chatServer.getServerList().get(i).get(1),Integer.parseInt(this.chatServer.getServerList().get(i).get(3)),serverId,json_rep.toJSONString()));
					broadcast.start();
				}
			}
		   }
			else
		   { 
			//if the identity exist in one server, then reply with false vote
			json_rep.put("type","lockidentity");
			json_rep.put("serverid",this.chatServer.getServer_Id());
			json_rep.put("identity", identity);
			json_rep.put("locked","false");
			this.sendMsg(json_rep.toJSONString());
			for(int i =0; i< this.chatServer.getServerList().size();i++){
				if(this.chatServer.getServerList().get(i).get(0).equals(serverId)){
					Thread broadcast = new Thread (new broadCastThread(this.chatServer.getServerList().get(i).get(1),Integer.parseInt(this.chatServer.getServerList().get(i).get(3)),serverId,json_rep.toJSONString()));
					broadcast.start();
				}
			}
		}
		
		}
		//a server processes the vote outcomes
		if(json_rec.containsKey("locked")){
			if(((String)json_rec.get("locked")).equals("true")){ 
				// if receiving a true vote,one identity vote count++
				int countTemp = (int) this.chatServer.getVotes().get(identity);
				this.chatServer.setVotes(identity, ++countTemp);
			}
			// if receiving a false vote, this identity vote becomes a very large number
			if(((String)json_rec.get("locked")).equals("false")){
				this.chatServer.setVotes(identity, 100000);
			}
		}
		
	}
	
	
	// release identity  
	public void process_releaseidentity(JSONObject json_rec){
		String identity = (String)json_rec.get("identity");
		for(int i=0; i<this.chatServer.getLockid().size();i++){
			if(( this.chatServer.getLockid()).get(i).equals(identity)){
				this.chatServer.getLockid().remove(i);
				break;
			}
		}
	}
	
	// 
	@SuppressWarnings("unchecked")
	public void process_getRooms(JSONObject json_rec){
		JSONObject json_repRoom = new JSONObject();
		List<String> roomids = new ArrayList<String>();
		for(ChatRoom chatroom: this.chatServer.getRoomList()){
			roomids.add(chatroom.getRoom_Id());
		}
		json_repRoom.put("type", "serverroomids");
		json_repRoom.put("roomids", roomids);
		this.sendMsg(json_repRoom.toJSONString());
	}
	
	
	@SuppressWarnings("unchecked")
	public void process_lockroomid(JSONObject json_rec){
		JSONObject json_rep = new JSONObject();
		String roomid = (String)json_rec.get("roomid");
		String serverId = (String)json_rec.get("serverid");
		
		if(!json_rec.containsKey("locked")){
			
			if(!this.chatServer.getLockroomid().contains(roomid)){
				
				// send lock room id message to other servers
				this.chatServer.addLockRoomid(roomid);
				json_rep.put("type","lockroomid");
				json_rep.put("serverid",this.chatServer.getServer_Id());
				json_rep.put("roomid", roomid);
				json_rep.put("locked","true");
				for(int i =0; i< this.chatServer.getServerList().size();i++){
					if(this.chatServer.getServerList().get(i).get(0).equals(serverId)){
						Thread broadcast = new Thread (new broadCastThread(this.chatServer.getServerList().get(i).get(1),Integer.parseInt(this.chatServer.getServerList().get(i).get(3)),serverId,json_rep.toJSONString()));
						broadcast.start();
					}
				}
			}
			else{
				
				json_rep.put("type","lockroomid");
				json_rep.put("serverid",this.chatServer.getServer_Id());
				json_rep.put("roomid", roomid);
				json_rep.put("locked","false");
				for(int i =0; i< this.chatServer.getServerList().size();i++){
					if(this.chatServer.getServerList().get(i).get(0).equals(serverId)){
						Thread broadcast = new Thread (new broadCastThread(this.chatServer.getServerList().get(i).get(1),Integer.parseInt(this.chatServer.getServerList().get(i).get(3)),serverId,json_rep.toJSONString()));
						broadcast.start();
					}}
			}
			
		}
			if(json_rec.containsKey("locked")){
				
				
				// a true vote makes the count of identity ++
				if(((String)json_rec.get("locked")).equals("true")){ 
					int countTemp = (int) this.chatServer.getRoomvotes().get(roomid);
					this.chatServer.setRoomVotes(roomid, ++countTemp);
					
					
				}// a false vote makes the count of identity equals 100000
				if(((String)json_rec.get("locked")).equals("false")){
					this.chatServer.setRoomVotes(roomid, 100000);
				}
			}
				
		}
	
	// release room id
	public void process_releaseroomid(JSONObject json_rec){
		String approved = (String)json_rec.get("approved");
		String roomid = (String)json_rec.get("roomid");
		String serverId = (String)json_rec.get("serverid");
		if(approved.equals("true")){
			for(int i=0; i< this.chatServer.getLockroomid().size(); i++){
				if(( this.chatServer.getLockroomid()).get(i).equals(roomid)){
					this.chatServer.getLockroomid().remove(i);
					break;
				}
			}
			List <String> newroom = new ArrayList<>();
			newroom.add(roomid);
			newroom.add(serverId);
			this.chatServer.addSysRoom(newroom);
		}
		if(approved.equals("false")){
			for(int i=0; i< this.chatServer.getLockroomid().size(); i++){
				if((this.chatServer.getLockroomid()).get(i).equals(roomid)){
					this.chatServer.getLockroomid().remove(i);
					break;
				}
			}
			
		}
		
		
	}
	
    public void close(){
    	if(this.input!=null)
			try {
				this.input.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
    	if(this.output!=null)
			try {
				this.output.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
    	
    	if(this.socket!=null)
			try {
				this.socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
    }	
	
}
