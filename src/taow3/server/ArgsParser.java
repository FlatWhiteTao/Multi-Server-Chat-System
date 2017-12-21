package taow3.server;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

/**
 * 
 * @author TaoWang(707458)
 * This class is utilized in processing the command line args
 *
 */
public class ArgsParser {
	 @Option(name = "-n", usage = "server id", aliases = {"serverid"}, required = true)
	   private String serverid = null;
	 @Option(name = "-l", usage = "configuration file path", aliases = {"Server_conf"}, required = true)
	   private String configpath = null;
	 
	 
	public String getServerid() {
		return serverid;
	}
	public void setServerid(String serverid) {
		this.serverid = serverid;
	}
	public String getConfigpath() {
		return configpath;
	}
	public void setConfigpath(String configpath) {
		this.configpath = configpath;
	}
	public void Parse(String[] command)  {
		 CmdLineParser parser = new CmdLineParser(this);
		 try {
		      
		       parser.parseArgument(command);
		 } catch (CmdLineException e) {
		       
		       System.err.println(e.getMessage()); 
		       return;
		 }catch (Exception e) {
				e.printStackTrace();
		 }
	 }
	 
}
