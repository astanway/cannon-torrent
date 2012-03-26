package peers;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import apps.RUBTClient;

public class PeerHandler implements Runnable{
	
	private DataInputStream from_peer_;
	private DataOutputStream to_peer_;
	
	public PeerHandler(DataInputStream in, DataOutputStream out){
		this.from_peer_ = in;
		this.to_peer_ = out;
	}
	
	public void run(){
		
	}
	
	private void receiveHandshake(byte[] _hash)throws Exception{
		try{
			byte[] responseHash = new byte[20];
			byte[] response = new byte[68];
			from_peer_.read(response);
			System.arraycopy(response, 28, responseHash, 0, 20);
			for(int i=0; i<20; i++){
				if(responseHash[i] != _hash[i]){
					throw new Exception("INVALID HASH");
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
    // System.out.println("Handshake verified");
		return;
	}
	
	
	

}
