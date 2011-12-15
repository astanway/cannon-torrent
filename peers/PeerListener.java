package peers;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import apps.RUBTClient;


public class PeerListener implements Runnable{
	
	public ServerSocket listenSocket;
	public Socket dataSocket;
	public DataInputStream from_peer_;
	public DataOutputStream to_peer_;
	
	public PeerListener(int port){
		try{
			listenSocket = new ServerSocket(port);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public void run(){
		try{
			dataSocket = listenSocket.accept();
			from_peer_ = new DataInputStream(dataSocket.getInputStream());
			to_peer_ = new DataOutputStream(dataSocket.getOutputStream());
			RUBTClient.executeTask(new PeerHandler(from_peer_,to_peer_));
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	

}
