package taow3.server;

import java.io.BufferedReader;
import java.lang.System;
import java.lang.Runtime;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Pattern;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
/**
 * 
 * @author Tao Wang(707458) This class  aims to process requests between clients and servers
 *
 */

public class UserConnection extends Thread {
	private ChatServer chatServer;
	private String userId;
	private ChatRoom chatRoom = null;
	private Socket socket;
	private BufferedReader input;
	private BufferedWriter output;
	private User user;
	//private static JSONParser jsonParser = new JSONParser();
	public static final String Id_Regex = "^[a-zA-Z][a-zA-Z0-9]{2,15}";
	//private boolean isVoting = true;
	private int normalQuit =0;
	
	public UserConnection(Socket socket){
		this.socket=socket;
		try {
			this.input = new BufferedReader(new InputStreamReader(socket.getInputStream(),"UTF-8"));
			this.output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),"UTF-8"));
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			
		}
		
	}
	
	public void setChatServer(ChatServer chatServer){
		this.chatServer = chatServer;
	}
	
	public void setChatRoom(ChatRoom chatRoom){
		this.chatRoom = chatRoom;
	}
	
	public void setUser(User user){
		this.user = user;
	}
	
    public User getUser(){
    	return this.user;
    }
    
    public DataOutputStream getOutput(){
    	 try {
			return new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			return null;
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
    
    @SuppressWarnings("unchecked")
	public void run() {
    	
    		    String msg=null;
		      
				   try {
					while((msg=input.readLine())!=null)
								{
						   		
								
							     processMsg(msg);
							     
							    }
					
				} 
				   catch (IOException e) {
					   JSONObject json_rec = new JSONObject();
					   json_rec.put("type", "quit");
					   if(normalQuit==0){
						   this.process_quit(json_rec, false);
						  
						this.close();
					   }
				}
			}
    	
    	
    
    public void processMsg(String msg) {
    	JSONParser jsonParser = new JSONParser();
    	JSONObject json_rec = null;
    	try {
			json_rec = (JSONObject) jsonParser.parse(msg);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
    	String processType = (String)json_rec.get("type");
    	
    	switch (processType)
    	{
    		case "who":
    			this.process_who(json_rec);
    			break;
    		case "newidentity":
    			this.process_newidentity(json_rec);
    			break;
    		case "list":
    			this.process_list(json_rec);
    			break;
    		case "createroom":
    			this.process_createroom(json_rec);
    			break;
    		case "join":
    			this.process_join(json_rec);
    			break;
    		case "movejoin":
    			this.process_movejoin(json_rec);
    			break;
    		case "deleteroom":
    			this.process_deleteroom(json_rec);
    			break;
    		case "message":
    			this.process_message(json_rec);
    			break;
    		case "quit":
    			this.process_quit(json_rec,true);
    			break;
    		case "lockidentity":
    			this.process_lockidentity(json_rec);
    			break;
    		default:
    			break;
    			
    	}
    
    	
    }
   
	@SuppressWarnings("unchecked")
	// process a client quit from a room 
	private void process_quit(JSONObject json_rec, boolean flag) {
		
		this.normalQuit=1;
		if(this.chatRoom!=null)
		{if(!this.chatRoom.isOwner(this.userId)){
			this.chatRoom.deleteUser(this);
			
			JSONObject json_rep = new JSONObject();
			json_rep.put("type", "roomchange");
			json_rep.put("identity", this.userId);
			json_rep.put("former", this.chatRoom.getRoom_Id());
			json_rep.put("roomid","");
			this.chatRoom.sendMsgToOthers(json_rep.toJSONString(),this.userId);
			//this.chatServer.mainHall.broadcastMsg(json_rep.toJSONString());
			if(flag==true)
			this.sendMsg(json_rep.toJSONString());
			
			this.close();
			
		}
		// an owner quits from a room
		if(this.chatRoom.isOwner(this.userId)){
			String roomid = this.chatRoom.getRoom_Id();
			this.chatServer.deleteChatRoomViaId(roomid);
			this.chatServer.delRoominSysByRoomid(roomid);
			
			JSONObject json_repDel = new JSONObject();
    		json_repDel.put("type","deleteroom");
    		json_repDel.put("serverid", this.chatServer.getServer_Id());
    		json_repDel.put("roomid", roomid);
    		this.chatServer.broadcastToAll(json_repDel.toJSONString());
    		ChatRoom tempRoom = this.chatRoom;
    		
    		for(int i=0; i<tempRoom.getUserList().size();i++){
    			
    			
    			if(this.chatRoom.isOwner(tempRoom.getUserList().get(i).getUserId())) continue;
    			
    			else{
    			JSONObject json_rep = new JSONObject();
    			json_rep.put("type", "roomchange");
    			json_rep.put("identity", tempRoom.getUserList().get(i).getUserId());
    			json_rep.put("former",tempRoom.getRoom_Id());
    			json_rep.put("roomid", this.chatServer.mainHall.getRoom_Id());
    			
    			
    			tempRoom.getUserList().get(i).setChatRoom(this.chatServer.mainHall);
    			this.chatServer.mainHall.addUser( tempRoom.getUserList().get(i));
    			tempRoom.deleteUser( tempRoom.getUserList().get(i));
    			if(flag==true)
    			tempRoom.broadcastMsg(json_rep.toJSONString());
    			if(flag==false){
    				tempRoom.deleteUser(this);
    				tempRoom.broadcastMsg(json_rep.toJSONString());
    				tempRoom.addUser(this);
    			}
    			
    			this.chatServer.mainHall.broadcastMsg(json_rep.toJSONString());
    			i--;
    			}
    		}
    		
    		this.chatRoom.deleteUser(this);
    		
    		JSONObject json_rep = new JSONObject();
    		json_rep.put("type", "roomchange");
			json_rep.put("identity", this.userId);
			json_rep.put("former",this.chatRoom.getRoom_Id());
			json_rep.put("roomid", "");
			this.chatServer.mainHall.broadcastMsg(json_rep.toJSONString());
			
			JSONObject json_repc = new JSONObject();
    		json_repc.put("type", "deleteroom");
    		json_repc.put("roomid", tempRoom.getRoom_Id());
    		json_repc.put("approved", "true");
    		if(flag==true)
    		this.sendMsg(json_repc.toJSONString());
    		
    		
    		JSONObject json_repC = new JSONObject();
			json_repC.put("type", "roomchange");
			json_repC.put("identity", this.userId);
			json_repC.put("former", tempRoom.getRoom_Id());
			json_repC.put("roomid", "");
			if(flag==true)
			this.sendMsg(json_rep.toJSONString());
			
			
    		
			tempRoom=null;
    		
    		
    		for(int i=0; i< this.chatServer.getLockroomid().size(); i++){
    			if((this.chatServer.getLockroomid()).get(i).equals(roomid)){
    				this.chatServer.getLockroomid().remove(i);
    				break;}
    	    	}
			
    		this.close();
    	}
		}
	}

	@SuppressWarnings("unchecked")
	private boolean process_message(JSONObject json_rec) {
		String msg = (String) json_rec.get("content");
		JSONObject json_msg = new JSONObject();
		json_msg.put("type", "message");
		json_msg.put("identity", this.userId);
		json_msg.put("content", msg);
		if(json_msg.toJSONString()!=null)
		{this.chatRoom.sendMsgToOthers(json_msg.toJSONString(),this.userId);}
		return true;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	@SuppressWarnings("unchecked")
	public void process_who(JSONObject json_rec){
    	
		JSONObject json_rep = new JSONObject();
    	String roomId=this.chatRoom.getRoom_Id();
    	String roomOwner=this.chatRoom.getRoomOwner();
    	
    	JSONArray identities = new JSONArray(); 
    	Vector<String> users = this.chatRoom.getRoomIdentities();
    	
    	for(int i=0;i<users.size();i++){
    		identities.add(users.get(i));
    	}
    	json_rep.put("type", "roomcontents");
    	json_rep.put("roomid",roomId);
    	json_rep.put("identities", identities);
    	json_rep.put("owner",roomOwner);
    	this.sendMsg(json_rep.toJSONString());
    }
    
    @SuppressWarnings("unchecked")
	public void process_newidentity(JSONObject json_rec){
    	String identity = (String) json_rec.get("identity");
    	this.userId=identity;
    	
    	//valid id 
    	
    	if(!Pattern.compile(Id_Regex).matcher(identity).matches()){
    		JSONObject json_rep = new JSONObject();
    		json_rep.put("type","newidentity" );
    		json_rep.put("approved", "false");
    		this.normalQuit=1;
    		this.sendMsg(json_rep.toJSONString());
    		this.close();
    		
    	}
    	
    	if(Pattern.compile(Id_Regex).matcher(identity).matches()){
    	
    		if(this.chatServer.getServerNums()==1){
    			if(!this.chatServer.getUserList().contains(identity) && !this.chatServer.getLockid().contains(identity) )
    			{
    			JSONObject json_rep = new JSONObject();
        		json_rep.put("type","newidentity" );
        		json_rep.put("approved", "true");
        		this.sendMsg(json_rep.toJSONString());
        		
        		this.setChatRoom(this.chatServer.mainHall);
        	    this.chatServer.mainHall.addUser(this);
        	    
        	    JSONObject json_repRoom = new JSONObject();
        		json_repRoom.put("type", "roomchange");
        		json_repRoom.put("identity", identity);
        		json_repRoom.put("former", "");
        		json_repRoom.put("roomid", this.chatServer.mainHall.getRoom_Id());
        		this.chatServer.mainHall.broadcastMsg(json_repRoom.toJSONString());
    			
    			}else{
    				JSONObject json_rep = new JSONObject();
    	    		json_rep.put("type", "newidentity");
    	    		json_rep.put("approved", "false");
    	    		this.normalQuit=1;
    	    		this.sendMsg(json_rep.toJSONString());
    	    		this.close();
    			}
    			
    		}
    		
    		
    		
    		
    		else{
    			
    			
    			// if the new identity was clashed with the lock list or the user list
        		if(this.chatServer.getLockid().contains(identity) || this.chatServer.getUserList().contains(identity))
        		{
        			
        			//deny the request and close the connection 
        			JSONObject json_rep = new JSONObject();
            		json_rep.put("type","newidentity" );
            		json_rep.put("approved", "false");
            		this.normalQuit=1;
            		this.sendMsg(json_rep.toJSONString());
            		 
            		this.close();
        		}
    	 
        // if a identity does not exist in the lock list or the userList
    	if(!this.chatServer.getLockid().contains(identity) && !this.chatServer.getUserList().contains(identity))
    		{
    	
    		//if a valid new identity, add it to the lock list
    		this.chatServer.addLockid(identity);
    		//set identity votes for a identity in a synchronized manner
    		this.chatServer.setVotes(identity,0);
    		//broadcast to other servers to lock this identity
    		JSONObject Json_rep1 = new JSONObject();
    		Json_rep1.put("type", "lockidentity");
    		Json_rep1.put("serverid", this.chatServer.getServer_Id());
    		Json_rep1.put("identity", identity);
    		this.chatServer.broadcastToAll(Json_rep1.toJSONString());
    		
        
    	while(true){
			
			
			
			if((int)this.chatServer.getVotes().get(identity) ==100000){
				
    			//has denying votes, deny the identity
    			JSONObject json_rep = new JSONObject();
    		    json_rep.put("type", "newidentity");
    		    json_rep.put("approved", "false");
    		    this.sendMsg(json_rep.toJSONString());
    		    
    		    //broad cast to other servers to release the identity
    		    JSONObject json_repAll = new JSONObject();
    		    json_repAll.put("type","releaseidentity");
    		    json_repAll.put("serverid",this.chatServer.getServer_Id());
    		    json_repAll.put("identity", identity);
    		    this.chatServer.broadcastToAll(json_repAll.toJSONString());
    		    //isVoting =false;
    		    for(int i=0; i<this.chatServer.getLockid().size();i++){
        			if(( this.chatServer.getLockid()).get(i).equals(identity)){
        				this.chatServer.getLockid().remove(i);
        				break;
        			}
        		}
    		    //this.chatServer.getVotes().clear();
    		    this.close();
    		    break;
    		}
			if((int)this.chatServer.getVotes().get(identity)==this.chatServer.getServerList().size()-1){
    			// all servers vote true, send the approved information to the client
				for(int i=0; i<this.chatServer.getLockid().size();i++){
        			if(( this.chatServer.getLockid()).get(i).equals(identity)){
        				this.chatServer.getLockid().remove(i);
        				break;
        			}
        		}
    			
				JSONObject json_rep = new JSONObject();
    			json_rep.put("type", "newidentity");
    			json_rep.put("approved", "true");
    			this.sendMsg(json_rep.toJSONString());
    			// broadcast to other servers to release the identity on their lock lists
    			JSONObject json_repAll = new JSONObject();
    			json_repAll.put("type","releaseidentity");
    			json_repAll.put("serverid",this.chatServer.getServer_Id());
    			json_repAll.put("identity", identity);
    			this.chatServer.broadcastToAll(json_repAll.toJSONString());
    			// add the new identity to the mainHall
    			this.setChatRoom(this.chatServer.mainHall);
    			this.chatServer.mainHall.addUser(this);
    			// send the room change message to the client
    			JSONObject json_repRoom = new JSONObject();
    			json_repRoom.put("type", "roomchange");
    			json_repRoom.put("identity", identity);
    			json_repRoom.put("former", "");
    			json_repRoom.put("roomid", this.chatServer.mainHall.getRoom_Id());
    			//broadcast to all members in this mainHall
    			this.chatServer.mainHall.broadcastMsg(json_repRoom.toJSONString());
    			
    			 //this.chatServer.getVotes().clear();
    			//isVoting=false;
    			
    			break;
    		}
	}
    		
    		}
    	
    	
    	}
    		}
    	// this.chatServer.getVotes().clear();
    }
    
    @SuppressWarnings("unchecked")
	public void process_list(JSONObject json_rec){
    	
        JSONObject json_getRooms = new JSONObject();
    	json_getRooms.put("type", "roomlist");
    	JSONArray json_roomArray = new JSONArray();
    	//List<String> roomids = new ArrayList<String>();
    	for(int i=0;i<this.chatServer.getSystemRooms().size();i++){
    		json_roomArray.add(chatServer.getSystemRooms().get(i).get(0));
    		
    	}
    	
    	json_getRooms.put("rooms", json_roomArray);
    	this.sendMsg(json_getRooms.toJSONString());
   }
    
   
	
    
    @SuppressWarnings("unchecked")
	public void process_createroom(JSONObject json_rec){
    	String roomid = (String) json_rec.get("roomid");
//    	String identity =(String) json_rec.get("identity");
    	
    	
    	
    	if(!Pattern.matches(Id_Regex, roomid)){
    		JSONObject json_rep = new JSONObject();
    		json_rep.put("type","createroom" );
    		json_rep.put("roomid", roomid);
    		json_rep.put("approved", "false");
    		this.sendMsg(json_rep.toJSONString());
    	}
    	// if the server number is 1
    	if(Pattern.matches(Id_Regex, roomid)){
    		if(this.chatServer.getServerNums()==1){
    			// if the user is the owner of another room
    			if(this.chatServer.isOwner(this.userId)){
        			JSONObject json_rep = new JSONObject();
            		json_rep.put("type","createroom" );
            		json_rep.put("roomid", roomid);
            		json_rep.put("approved", "false");
            		this.sendMsg(json_rep.toJSONString());
            	}else{
            		// if the room id has existed in the system or in the lock list 
            		if(this.chatServer.inSysRoomlist(roomid) || this.chatServer.getLockroomid().contains(roomid)){
            			
            			JSONObject json_rep = new JSONObject();
                		json_rep.put("type","createroom" );
                		json_rep.put("roomid", roomid);
                		json_rep.put("approved", "false");
                		this.sendMsg(json_rep.toJSONString());
                	}
            		else{
            			// the room can be created 
            			JSONObject json_rep = new JSONObject();
                		json_rep.put("type","createroom" );
                		json_rep.put("roomid", roomid);
                		json_rep.put("approved", "true");
                		this.sendMsg(json_rep.toJSONString());
                		

                		JSONObject json_repRoom = new JSONObject();
                		json_repRoom.put("type", "roomchange");
                		json_repRoom.put("identity", this.userId);
                		json_repRoom.put("former",this.chatRoom.getRoom_Id() );
                		json_repRoom.put("roomid",roomid);
                		this.chatRoom.broadcastMsg(json_repRoom.toJSONString());
                		
                		ChatRoom newroom = new ChatRoom(roomid,this.userId,this.chatServer.getServer_Id());
                	    List <String> tempRoom = new ArrayList<>();
                		tempRoom.add(newroom.getRoom_Id());
                		tempRoom.add(newroom.getServer_Id());
                		this.chatServer.addSysRoom(tempRoom);
                		this.chatServer.addRoomList(newroom);
                		newroom.addUser(this);
                		this.chatRoom.deleteUser(this);
                		this.setChatRoom(newroom);
                		//this.chatRoom.setRoomOwner();
                		
            			
            		}
    			
    		}
    			
        		
        		
        	}
    		
    		
    		else{ // under the multiple server situation 
    			
    		if(this.chatRoom.isOwner(this.userId)){
    			//deny the create room request if this identity is the owner of other rooms
    			JSONObject json_rep = new JSONObject();
        		json_rep.put("type","createroom" );
        		json_rep.put("roomid", roomid);
        		json_rep.put("approved", "false");
        		this.sendMsg(json_rep.toJSONString());
        	}
    		
    		else
    		{  // if the identity is not the owner of a room in the server
    			if(this.chatServer.inSysRoomlist(roomid) || this.chatServer.getLockroomid().contains(roomid)){
    			// if the this identity is duplicated in whole system or it is in the lock room id list
    			JSONObject json_rep = new JSONObject();
        		json_rep.put("type","createroom");
        		json_rep.put("roomid", roomid);
        		json_rep.put("approved", "false");
        		this.sendMsg(json_rep.toJSONString());
        		
    		}
    		
    		if(!this.chatServer.inSysRoomlist(roomid) && !this.chatServer.getLockroomid().contains(roomid))
    		{
    		this.chatServer.addLockRoomid(roomid);
    		this.chatServer.setRoomVotes(roomid, 0);
    		// if the identity is not duplicated in whole system and not in the lock room id list
    		JSONObject Json_rep1 = new JSONObject();
    		// send the lock room id message to other servers
    		Json_rep1.put("type", "lockroomid");
    		Json_rep1.put("serverid", this.chatServer.getServer_Id());
    		Json_rep1.put("roomid", roomid);
    		this.chatServer.broadcastToAll(Json_rep1.toJSONString());
    		
    			while(true)
    	
    		{
    		// wait for the voting outcomes  
    		
        	if((int)this.chatServer.getRoomvotes().get(roomid) ==100000){
        		// process deny vote
        		JSONObject json_rep = new JSONObject();
        		json_rep.put("type", "createroom");
        		json_rep.put("roomid", roomid);
        		json_rep.put("approved", "false");
        		this.sendMsg(json_rep.toJSONString());
        		
        		//send message to other severs to release the identity 
        		JSONObject json_repAll = new JSONObject();
        		
        		json_repAll.put("type","releaseroomid");
        		json_repAll.put("serverid",this.chatServer.getServer_Id());
        		json_repAll.put("roomid", roomid);
        		json_repAll.put("approved", "false");
        		this.chatServer.broadcastToAll(json_repAll.toJSONString());
        		
        		break;
        	}
    		
        	if((int)this.chatServer.getRoomvotes().get(roomid)==this.chatServer.getServerList().size()-1){
        		
    		// if the vote approves the identity 
        	// send message to the client about the approval
    		JSONObject json_rep = new JSONObject();
    		json_rep.put("type", "createroom");
    		json_rep.put("roomid", roomid);
    		json_rep.put("approved", "true");
    		this.sendMsg(json_rep.toJSONString());
    		
    		// send message to other servers to relase the lock room id list
    		JSONObject json_repAll = new JSONObject();
    		json_repAll.put("type","releaseroomid");
    		json_repAll.put("serverid",this.chatServer.getServer_Id());
    		json_repAll.put("roomid", roomid);
    		json_repAll.put("approved", "true");
    		this.chatServer.broadcastToAll(json_repAll.toJSONString());
    		
    		//broadcast room change message in this room
    		JSONObject json_repRoom = new JSONObject();
    		json_repRoom.put("type", "roomchange");
    		json_repRoom.put("identity", this.userId);
    		json_repRoom.put("former",this.chatRoom.getRoom_Id() );
    		json_repRoom.put("roomid", roomid);
    		//this.chatRoom.deleteUser(this);
    		
    		this.chatRoom.broadcastMsg(json_repRoom.toJSONString());
    		ChatRoom newroom = new ChatRoom(roomid,this.userId,this.chatServer.getServer_Id());
    		List <String> tempRoom = new ArrayList<>();
    		tempRoom.add(newroom.getRoom_Id());
    		tempRoom.add(newroom.getServer_Id());
    		// add the new room to the system room list and room list
    		this.chatServer.addSysRoom(tempRoom);
    		this.chatServer.addRoomList(newroom);
    	    newroom.addUser(this);
    		this.chatRoom.deleteUser(this);
    	    this.setChatRoom(newroom);
    	    break;
    	}
    		
     }
    	
    		}
    	
    		
      }
    }
    		}
    	}
    
    @SuppressWarnings("unchecked")
	public void process_join(JSONObject json_rec){
    	String roomId = (String) json_rec.get("roomid");
    	
    	if(this.chatRoom.isOwner(this.userId)||!this.chatServer.inSysRoomlist(roomId)){
    		
    		// if the client is the owner or the server dose not have that room
    		JSONObject json_rep = new JSONObject();
    		json_rep.put("type","roomchange");
    		json_rep.put("identity", this.userId);
    		json_rep.put("former", roomId);
    		json_rep.put("roomid", roomId);
    		this.sendMsg(json_rep.toJSONString());
    	} else{
    		// if the room in the local server
    		if(this.chatServer.roomInServer(roomId)){
    			
    			JSONObject json_repJoin = new JSONObject();
    			json_repJoin.put("type", "roomchange");
    			json_repJoin.put("identity", this.userId);
    			json_repJoin.put("former", this.chatRoom.getRoom_Id());
    			json_repJoin.put("roomid",roomId);
    			String msg = json_repJoin.toJSONString();
    		
    			this.chatRoom.deleteUser(this);
    			this.chatRoom.broadcastMsg(msg);
    			this.setChatRoom(this.chatServer.getChatRoomViaId(roomId));
    			
    			this.chatRoom.addUser(this);
    			this.chatRoom.sendMsgToOthers(msg,this.userId );
    			this.sendMsg(msg);
    			
    		}
    		if(!this.chatServer.roomInServer(roomId)){
    			
    			// if the room in other servers
    			// find the room in which server
    			Vector <String> serverInfo = this.chatServer.getServerInfoByRoomId(roomId);
    			JSONObject json_repJoin = new JSONObject();
    			json_repJoin.put("type", "route");
    			json_repJoin.put("roomid", roomId);
    			json_repJoin.put("host", serverInfo.get(0));
    			
    			json_repJoin.put("port", serverInfo.get(1));
    			
    			this.sendMsg(json_repJoin.toJSONString());
    			this.chatRoom.deleteUser(this);
    			
    			
    			
    			JSONObject json_repRoomChange = new JSONObject();
    			json_repRoomChange.put("type", "roomchange");
    			json_repRoomChange.put("identity", this.userId);
    			json_repRoomChange.put("former", this.chatRoom.getRoom_Id());
    			json_repRoomChange.put("roomid",roomId);
    			this.chatRoom.broadcastMsg(json_repRoomChange.toJSONString());
    			
    			//this.close();
    			
    			
    		}
    	}
    }
    @SuppressWarnings("unchecked")
	public void process_movejoin(JSONObject json_rec){
    	String roomid = (String) json_rec.get("roomid");
    	String identity =(String) json_rec.get("identity");
    	String former = (String)json_rec.get("former");
    	//this.chatServer.getUserList().add(identity);
    	this.userId=identity;
    	if(!this.chatServer.roomInServer(roomid)){
    		this.chatServer.mainHall.addUser(this);
    		JSONObject json_rep = new JSONObject();
    		json_rep.put("type", "roomchange");
			json_rep.put("identity", identity);
			json_rep.put("former", this.chatRoom.getRoom_Id());
			json_rep.put("roomid",this.chatServer.mainHall.getRoom_Id());
    		this.setChatRoom(this.chatServer.mainHall);
    		this.chatRoom.broadcastMsg(json_rep.toJSONString());
    		
    	}else{
    		
    		
    		JSONObject json_rep = new JSONObject();
    		json_rep.put("type", "roomchange");
			json_rep.put("identity",identity);
			json_rep.put("former", former);
			json_rep.put("roomid",roomid);
				
    		this.chatServer.getChatRoomViaId(roomid).addUser(this);
    		this.setChatRoom(this.chatServer.getChatRoomViaId(roomid));
    		JSONObject json_repClient = new JSONObject();
    		json_repClient.put("type", "serverchange");
    		json_repClient.put("approved", "true");
    		json_repClient.put("serverid",this.chatServer.getServer_Id());
    		this.sendMsg(json_repClient.toJSONString());
    		this.chatRoom.broadcastMsg(json_rep.toJSONString());
    		
    		
    		
    	
    		
    	}
    }
    
    @SuppressWarnings("unchecked")
	public void process_deleteroom(JSONObject json_rec){
    	
    	String roomid = (String) json_rec.get("roomid");
    	if(!this.chatRoom.getRoomOwner().equals(this.userId)){
    		JSONObject json_rep = new JSONObject();
    		json_rep.put("type", "deleteroom");
    		json_rep.put("roomid", roomid);
    		json_rep.put("approved", "false");
    		this.sendMsg(json_rep.toJSONString());
    	}
    	
    	if(this.chatRoom.getRoomOwner().equals(this.userId)&&!this.chatRoom.getRoom_Id().equals(roomid)){
    		JSONObject json_rep = new JSONObject();
    		json_rep.put("type", "deleteroom");
    		json_rep.put("roomid", roomid);
    		json_rep.put("approved", "false");
    		this.sendMsg(json_rep.toJSONString());
    	}
    	
    	if(this.chatRoom.getRoomOwner().equals(this.userId)&&this.chatRoom.getRoom_Id().equals(roomid)){
    		String delRoomid= (String) json_rec.get("roomid");
    		
    		
    		
    		this.chatServer.deleteChatRoomViaId(delRoomid);
    		this.chatServer.delRoominSysByRoomid(delRoomid);
    		JSONObject json_repDel = new JSONObject();
    		json_repDel.put("type","deleteroom");
    		json_repDel.put("serverid", this.chatServer.getServer_Id());
    		json_repDel.put("roomid", delRoomid);
    		this.chatServer.broadcastToAll(json_repDel.toJSONString());
    		ChatRoom tempRoom = this.chatRoom;
    		
    		
    		
    		for(int i=0; i<tempRoom.getUserList().size();i++){
    			
    			
    			if(tempRoom.isOwner(tempRoom.getUserList().get(i).getUserId())) { continue;}
    			
    			else {
    				JSONObject json_rep = new JSONObject();
    			
    			json_rep.put("type", "roomchange");
    			json_rep.put("identity", tempRoom.getUserList().get(i).getUserId());
    			json_rep.put("former",this.chatRoom.getRoom_Id());
    			json_rep.put("roomid", this.chatServer.mainHall.getRoom_Id());
    			tempRoom.sendMsgToOthers(json_rep.toJSONString(),tempRoom.getUserList().get(i).getUserId());
    			tempRoom.getUserList().get(i).sendMsg(json_rep.toJSONString());
    			
    			this.chatServer.mainHall.addUser(tempRoom.getUserList().get(i));
    			tempRoom.getUserList().get(i).setChatRoom(this.chatServer.mainHall);
    			
    			
    			this.chatServer.mainHall.sendMsgToOthers(json_rep.toJSONString(),tempRoom.getUserList().get(i).getUserId());
    			tempRoom.deleteUser(tempRoom.getUserList().get(i));
    			
    			i--;
    			}
    		}
    		JSONObject json_rep = new JSONObject();
			
			json_rep.put("type", "roomchange");
			json_rep.put("identity", tempRoom.getUserList().get(0).getUserId());
			json_rep.put("former",this.chatRoom.getRoom_Id());
			json_rep.put("roomid", this.chatServer.mainHall.getRoom_Id());
			//tempRoom.broadcastMsg(json_rep.toJSONString());
			tempRoom.getUserList().get(0).sendMsg(json_rep.toJSONString());
			JSONObject json_repc = new JSONObject();
    		json_repc.put("type", "deleteroom");
    		json_repc.put("roomid", delRoomid);
    		json_repc.put("approved", "true");
    		this.sendMsg(json_repc.toJSONString());
			
			this.chatServer.mainHall.addUser( tempRoom.getUserList().get(0));
			tempRoom.getUserList().get(0).setChatRoom(this.chatServer.mainHall);
			
			tempRoom.deleteUser(tempRoom.getUserList().get(0));			
			this.chatServer.mainHall.sendMsgToOthers(json_rep.toJSONString(),this.userId);
			
			tempRoom=null;
    		
    		for(int i=0; i< this.chatServer.getLockroomid().size(); i++){
    			if((this.chatServer.getLockroomid()).get(i).equals(roomid)){
    				this.chatServer.getLockroomid().remove(i);
    				break;}
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
   /*public void getSysRooms(JSONObject json_rec){
	   List<String> roomids = new ArrayList<String>();
	   roomids = (List<String>) json_rec.get("roomids");
	   int count = 0;
	   while(count!=this.chatServer.getServerList().size()-2)
   	 	{	
   		this.chatServer.getSystemRooms().addAll(roomids);
   		count++;
   	 	}
	   JSONObject json_rep = new JSONObject();
	   json_rep.put("type", "list");
	   json_rep.put("rooms",roomids);
	   this.sendMsg(json_rep.toJSONString());
	}*/
    
    @SuppressWarnings("unchecked")
	public void process_lockidentity(JSONObject json_rec){
    	
    	String identity = (String)json_rec.get("identity");
    	if(json_rec.containsKey("locked")){
    	
    		while(true){
    			
    			if(((String)json_rec.get("locked")).equals("true")){ 
    				int countTemp = (int) this.chatServer.getVotes().get(identity);
    				this.chatServer.setVotes(identity, countTemp++);
    				
    			}
			//process a false vote
    			if(((String)json_rec.get("locked")).equals("false")){
    				this.chatServer.setVotes(identity, 100000);
    			}
    			
    			if((int)this.chatServer.getVotes().get(identity) ==100000){
        			//has denying votes, deny the identity
        			JSONObject json_rep = new JSONObject();
        		    json_rep.put("type", "newidentity");
        		    json_rep.put("approved", "false");
        		    this.sendMsg(json_rep.toJSONString());
        		    
        		    //broad cast to other servers to release the identity
        		    JSONObject json_repAll = new JSONObject();
        		    json_repAll.put("type","releaseidentity");
        		    json_repAll.put("serverid",this.chatServer.getServer_Id());
        		    json_repAll.put("identity", identity);
        		    this.chatServer.broadcastToAll(json_repAll.toJSONString());
        		    //isVoting =false;
        		    this.close();
        		    break;
        		}
    			if((int)this.chatServer.getVotes().get(identity)==this.chatServer.getServerList().size()-1){
        			// all servers vote true, send the approved information to the client
        			
        			JSONObject json_rep = new JSONObject();
        			json_rep.put("type", "newidentity");
        			json_rep.put("approved", "true");
        			this.sendMsg(json_rep.toJSONString());
        			// broadcast to other servers to release the identity on their lock lists
        			JSONObject json_repAll = new JSONObject();
        			json_repAll.put("type","releaseidentity");
        			json_repAll.put("serverid",this.chatServer.getServer_Id());
        			json_repAll.put("identity", identity);
        			this.chatServer.broadcastToAll(json_repAll.toJSONString());
        			// add the new identity to the mainHall
        			this.setChatRoom(this.chatServer.mainHall);
        			this.chatServer.mainHall.addUser(this);
        			// send the room change message to the client
        			JSONObject json_repRoom = new JSONObject();
        			json_repRoom.put("type", "roomchange");
        			json_repRoom.put("identity", identity);
        			json_repRoom.put("former", "");
        			json_repRoom.put("roomid", this.chatServer.mainHall.getRoom_Id());
        			//broadcast to all members in this mainHall
        			this.chatServer.mainHall.broadcastMsg(json_repRoom.toJSONString());
        			//isVoting=false;
        			break;
        	
    			
		}
    	}
    	
    	}
    }
    

}

