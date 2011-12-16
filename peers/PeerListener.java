package peers;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;


public class PeerListener implements Runnable {
	
	public ServerSocket listenSocket;
	public Socket dataSocket;
	public DataInputStream from_peer_;
	public DataOutputStream to_peer_;
	public int port;
	
	public PeerListener(int port){
		this.port = port;
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
			Peer temp = new Peer(from_peer_,to_peer_);
			Thread t = new Thread(new UploadThread(temp));
			t.start();
			Thread t2 = new Thread(new PeerListener(this.port));
			t2.start();
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	

}
