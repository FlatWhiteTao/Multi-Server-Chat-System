package taow3.server;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

/**
 * 
 * @author Tao Wang(707458) 
 * This class is designed for marinating a server instance.
 *
 */
public class ChatServer {
	
	private String server_Id;
	private List<List<String>> serverList= new ArrayList<List<String>>();
	private List<String> lockid = new ArrayList<String>();
	private List<String> lockroomid = new ArrayList<String>();
	private HashMap<String, Integer> votes = new HashMap<String,Integer>();
	private HashMap<String, Integer> roomvotes = new HashMap<String,Integer>();
	private List <List<String>> systemRooms = new ArrayList<List<String>>();
	private int serverNums;
	public  ChatRoom mainHall=null;
	private Vector <ChatRoom> roomList = new Vector<ChatRoom>();
	private int port_num;
	private int inServerPort;
	private String hostname;
	
	
	public int getServerNums() {
		return serverNums;
	}

	public void setServerNums(int serverNums) {
		this.serverNums = serverNums;
	}

	

	public HashMap<String, Integer> getRoomvotes() {
		return roomvotes;
	}

	public synchronized void setRoomVotes(String str, Integer i) {
		this.roomvotes.put(str,i);
	}

	

	public HashMap<String, Integer> getVotes() {
		return votes;
	}

	

	public List<String> getLockroomid() {
		return lockroomid;
	}

	public void setLockroomid(List<String> lockroomid) {
		this.lockroomid = lockroomid;
	}

	public void setLockid(List<String> lockid) {
		this.lockid = lockid;
	}

	public List<List<String>> getSystemRooms() {
		return systemRooms;
	}

	public Vector<ChatRoom> getRoomList() {
		return (Vector<ChatRoom>) roomList;
	}

	public void setRoomList(Vector<ChatRoom> roomList) {
		this.roomList = (Vector<ChatRoom>) roomList;
	}

	
	// Update the votes for an ideneity 
	public synchronized void setVotes(String identity,Integer count ){
		this.votes.put(identity, count);
	}
  
	public List<String> getLockid() {
		return lockid;
	}

    public List<List<String>> getServerList() {
		return serverList;
	}

	

	public String getServer_Id() {
		return server_Id;
	}

	public void setServer_Id(String server_Id) {
		this.server_Id = server_Id;
	}

	public int getPort_num() {
		return port_num;
	}

	public void setPort_num(int port_num) {
		this.port_num = port_num;
	}

	public int getInServerPort() {
		return inServerPort;
	}

	public void setInServerPort(int inServerPort) {
		this.inServerPort = inServerPort;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}
		
	public ChatRoom getMainHall() {
		return mainHall;
	}

	

	public void setUserList(Vector<User> userList) {
	}

	public void setMainHall(ChatRoom mainHall) {
		this.mainHall = mainHall;
	}
	
	public void setServerList(List<List<String>> serverList) {
		this.serverList = serverList;
	}

	public ChatServer(String server_Id, String hostname,int port_num,int inServerPort){
		this.port_num = port_num;
		this.inServerPort = inServerPort;
		this.server_Id = server_Id;
		this.hostname = hostname;
		new Vector<User>();
		this.roomList = new Vector<ChatRoom>();
		this.port_num = port_num;
		this.inServerPort = inServerPort;
		
	}

	public void start(){
			//New a thread to listen from the client
			Thread socket = new Thread(new ListenThread(this.port_num,this));
			socket.start();
			//New a thread to listen from the server
			Thread inSeverSocket = new Thread(new ListenFromServer(this.inServerPort,this));
			inSeverSocket.start();
	}
	
	// Return a room by Room id
	public ChatRoom getChatRoomViaId(String roomId){
		for(int i=0;i<this.roomList.size();i++){
			if(this.roomList.get(i).getRoom_Id().equals(roomId)){
				return roomList.get(i);
			}
		}
		return null;
	}
	//Delete a room via room id 
	public synchronized void deleteChatRoomViaId(String roomId){
		for(int i=0;i<this.roomList.size();i++){
			if(this.roomList.get(i).getRoom_Id().equals(roomId)){
				roomList.remove(i);
			}
		}
	}
	
	// Add a chat room
   public synchronized void addChatRoom(String roomId,String owner,String serverId){
	   ChatRoom chatroom= new ChatRoom(roomId,owner,serverId);
	   this.roomList.addElement(chatroom);
	   }
   
   // broadcast messages to All other servers 
   public synchronized void broadcastToAll(String msg){
	   for(int i=0;i<this.getServerList().size();i++){
			if(!this.getServer_Id().
					equals(this.getServerList().get(i).get(0))){
				Thread broadcast = new Thread(new broadCastThread(this.getServerList().get(i).get(1),Integer.parseInt(this.getServerList().get(i).get(3)),this.getServerList().get(i).get(0),msg));
				broadcast.start();
			}
		}
   }
   // check if the a room exists in roomlist 
   public boolean roomIsValid(String roomId){
	   for(int i=0; i<this.roomList.size();i++){
		   if(this.roomList.get(i).getRoom_Id().equals(roomId)){
			   return true;
		   }
	   }
	   return false;
   }
   // return all identities 
   public Vector<String> getUserList(){
	   Vector<String> userAll = new Vector<String>();
	  for(ChatRoom room:this.roomList){
		  Vector<String> tempIds = room.getRoomIdentities();
		  for(String tempId:tempIds) userAll.addElement(tempId); 
	  }
	  return userAll;
   }
   // check if a user is the owner 
   public boolean isOwner(String id){
	   for(ChatRoom chatroom : this.roomList){
		   if(chatroom.getRoomOwner().equals(id)) return true;
	   }
	   
	   return false;
   }
   
   // check if a room id exists in the system roomlist
   public boolean inSysRoomlist(String roomid){
	   for(List<String> sysRoom: this.systemRooms){
		   if (((String) sysRoom.get(0)).equals(roomid)) return true;
	   }
	   return false;
   }
   
   // check if a room in the local server or in the remote server
   public boolean roomInServer(String roomid){
	   
	   for(List<String> sysRoom: this.systemRooms){
		 
		   if(sysRoom.get(0).equals(roomid)) {
			
			   
			   if(sysRoom.get(1).equals(this.server_Id))
				   return true;
		   
		   }
	   }
		   return false;
   }
   

   // Return a server's information by a room id
  public Vector<String> getServerInfoByRoomId(String roomid){
	  String serverId =null;
	  String serverHost=null;
	  String serverPort=null;
	  Vector <String> serverInfo =new Vector<String>();
	  for(List<String> sysRoom: this.systemRooms){
		  if(sysRoom.get(0).equals(roomid)) 
		  serverId = (String) sysRoom.get(1);  
	  }
	  for(List<String> serverList:this.serverList){
		  if(serverList.get(0).equals(serverId)){
			  serverHost = (String) serverList.get(1);
			  serverPort = (String) serverList.get(2);
			  break;
		  }
	  }
	  serverInfo.add(serverHost);
	  serverInfo.add(serverPort);
	  return serverInfo;
	  
  }
  // delete a room in by room id from the systemRoom list
  public synchronized void delRoominSysByRoomid(String roomid){
	  for(int i =0; i< this.systemRooms.size(); i++ ){
		  if(this.systemRooms.get(i).get(0).equals(roomid)){
			  this.systemRooms.remove(i);
		  }
	  }
  }
  
 public synchronized void addLockid(String identity){
	 	this.lockid.add(identity);
 }
 public synchronized void addSysRoom(List<String> roomInfo){
	 this.getSystemRooms().add(roomInfo);
 }
 public synchronized void addRoomList(ChatRoom room){
	 this.getRoomList().addElement(room);
 }
 public synchronized void addLockRoomid(String roomid){
	 this.getLockroomid().add(roomid);
 }
   
}

