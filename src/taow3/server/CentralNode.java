package taow3.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
/**
 * 
 * @author Tao Wang (707458)
 * This class is designed for extracting servers basic information from the configuration file
 *
 */
public class CentralNode {
	private int serverNum=0;
	//each element in the serverList includes serverId, hostName, port number and server port number
	private List < List<String> > serverList=null;
   
	public CentralNode(int serverNum, List<String> serverInfo){
		this.serverNum=serverNum;
		serverList = new ArrayList< List<String>> (serverNum);
		for(int i=0; i<serverNum; i++){
			// creating a server element in the serverList by inserting four kinds of basic information 
			String info[]=serverInfo.get(i).split("\t");
			List<String> completeInfo = new ArrayList<String>();
			completeInfo.add(info[0]);
			completeInfo.add(info[1]);
			completeInfo.add(info[2]);
			completeInfo.add(info[3]);
			this.serverList.add(completeInfo);
		}
	}
	
   public List<List<String>> getServers(){
	   return this.serverList;
	}
   //Return a server's information via server id
   public List <String> getServerInfo(String severId){
	   for(List<String> server:this.serverList){
		   if(server.get(0).equals(severId))
			   return server;
	   }
	return null;
   }
   
   public int getServersNum(){
	   return this.serverList.size();
   }
	
}
