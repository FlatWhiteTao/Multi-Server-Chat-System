package taow3.server;

import java.io.DataOutputStream;
import java.io.IOException;

public class SendThread implements Runnable {
	DataOutputStream output;
	String msg;
	
	SendThread(DataOutputStream output,String msg){
		this.output = output;
		this.msg = msg;
	}
	
	@Override 
	public void run(){
		try {
			output.writeUTF(msg);
			output.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
