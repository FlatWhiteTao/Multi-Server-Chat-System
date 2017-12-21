package taow3.server;

import java.util.Vector;
/**
 * 
 * @author Tao Wang(707458)
 * This class is designed for ChatRoom instance 
 *
 */

public class ChatRoom {
	private String room_Id = null;
	private String roomOwner = null;
	private String server_Id = null;
	private Vector<UserConnection> userList;
	
	public ChatRoom(){
		
	}
	
	public ChatRoom(String room_Id,String roomOwner, String server_Id){
		this.room_Id = room_Id;
		this.roomOwner = roomOwner;
		this.server_Id = server_Id;
		userList = new Vector<UserConnection>();
     }

	public String getRoom_Id() {
		return room_Id;
	}

	public void setRoom_Id(String room_Id) {
		this.room_Id = room_Id;
	}

	public String getRoomOwner() {
		return roomOwner;
	}

	public void setRoomOwner(String roomOwner) {
		this.roomOwner = roomOwner;
	}

	public String getServer_Id() {
		return server_Id;
	}

	public void setServer_Id(String server_Id) {
		this.server_Id = server_Id;
	}

	public Vector<UserConnection> getUserList() {
		return userList;
	}

	public void setUserList(Vector<UserConnection> userList) {
		this.userList = userList;
	}
	// add a connection to this room
	public synchronized boolean addUser(UserConnection userCon){
		if(userCon == null)
			return false;
		this.userList.add(userCon);
		return true;
	}
	// delete a connection from this room
	public synchronized boolean deleteUser(UserConnection userCon){
		this.userList.remove(userCon);
		return true;
	}
	
	// return all user identities of this room
	public Vector<String> getRoomIdentities(){
		Vector<String> identities = new Vector<String>();
		for(int i=0;i<userList.size();i++){
			String identity="";
			identity = this.userList.get(i).getUserId();
			identities.add(identity);
		}
		return identities;
	}
	// room announcements 
	public synchronized void broadcastMsg(String msg){
		for (int i=0; i<userList.size();i++){
			userList.get(i).sendMsg(msg);
		}
	}
	// check the room owner
	public boolean isOwner(String identity){
		if(this.roomOwner.equals(identity)) return true;
		return false;
		
	}
	// a user broadcasts a message in the room
	public synchronized void sendMsgToOthers(String msg,String sender){
		for(UserConnection uc:this.userList){
			if(!uc.getUserId().equals(sender)){
				uc.sendMsg(msg);
			}
		}
	}
	
}
