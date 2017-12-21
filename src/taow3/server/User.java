package taow3.server;
/**
 * 
 * @author Tao Wang (707458) This class is designed to maintain a user instance
 *
 */
public class User {
	private long num = -1;
	private String identity="";
	private String serverId="";
	private String roomId="";
	private boolean inRoom = false;
	
	public User(long num, String identity,String roomId){
		this.identity = identity;
		this.roomId = roomId;
		this.num = num;
	}

	public String getIdentity() {
		return identity;
	}

	public void setIdentity(String identity) {
		this.identity = identity;
	}

	public String getRoomId() {
		return roomId;
	}

	public void setRoomId(String roomId) {
		this.roomId = roomId;
	}

	public boolean isInRoom() {
		return inRoom;
	}

	public void setInRoom(boolean inRoom) {
		this.inRoom = inRoom;
	}
	
	
}
