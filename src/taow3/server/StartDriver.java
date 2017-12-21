package taow3.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
/**
 * 
 * @author Tao Wang(707458) This is the main method and driver class
 *
 */
public class StartDriver {
	// having a static cN to access servers basic information 
	public static CentralNode cN=null;
	public static void main(String args[]){
		
		ArgsParser argsParser = new ArgsParser();
		argsParser.Parse(args);
		String serverId =argsParser.getServerid();
		String filePath =argsParser.getConfigpath();
		
		int serverNum=0;
		BufferedReader br = null;
		List<String> serverInfo = new ArrayList<String>();
		try {
			br = new BufferedReader(new FileReader(filePath));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String str=null;
		do{
			
			try {
				str = br.readLine();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
			if(str!=null)
			{serverInfo.add(str);
			 serverNum++;}
			
		}while(str!=null);
	
		try {
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		
		CentralNode cN = new CentralNode(serverNum,serverInfo);
		
		List <String> info = new ArrayList<String>();
		info = cN.getServerInfo(serverId);
		ChatServer chatServer = null;
		
		if(info!=null)
		{
		chatServer = new ChatServer(info.get(0),info.get(1),Integer.parseInt(info.get(2)),Integer.parseInt(info.get(3)));
		
		chatServer.setServerNums(cN.getServersNum());
		
		for(int i=0; i< cN.getServersNum();i++)
		{
			//set the MainHall to each server 
		ChatRoom mainHall = new ChatRoom("MainHall-"+cN.getServers().get(i).get(0),"",cN.getServers().get(i).get(0));
		List <String> mainHallInfo = new ArrayList<>();
		mainHallInfo.add(mainHall.getRoom_Id());
		mainHallInfo.add(mainHall.getServer_Id());
		
	    chatServer.getSystemRooms().add(mainHallInfo);
	 
		
	    if(cN.getServers().get(i).get(0).equals(chatServer.getServer_Id())) 
			{chatServer.getRoomList().addElement(mainHall);
			
			
			chatServer.setMainHall(mainHall);
			}
		}
		for(int j=0;j<cN.getServersNum();j++){
			chatServer.getServerList().add(cN.getServers().get(j));
		}
		}
		chatServer.start();
		
}
}

