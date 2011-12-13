package peers;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class Peer {
	
	String peer_id_ = null;
	String ip_ = null;
	int port_ = 0;
	Socket socket_= null;
	DataOutputStream to_peer_ = null;
	BufferedReader from_peer_ = null;
	
	
	public Peer(String _peer_id,String _ip, int _port){
		peer_id_ = _peer_id;
		ip_ = _ip;
		port_ = _port;
	}
	
	public boolean createSocket(String _ip, int _port){
		try{
			socket_ = new Socket(_ip,_port);
		}catch(Exception e){
			e.printStackTrace();
		}
		return socket_!=null? true: false;
	}
	
	public boolean establishStreams(){
		try{
			to_peer_ = new DataOutputStream(socket_.getOutputStream());
			from_peer_ = new BufferedReader(new InputStreamReader(
													socket_.getInputStream()));
		}catch(Exception e){
			e.printStackTrace();
		}
		if(to_peer_==null||from_peer_==null){
			return false;
		}else
			return true;
	}
	
	public void sendHandshake(String _our_peer_id, byte[] _hash){
		byte out_[] = new byte[100];
		out_[0] = new Integer(19).byteValue();
		byte temp[] = new String("BitTorrent protocol").getBytes();
		System.arraycopy(out_, 1, temp, 0, temp.length);
		
		
		try{
			to_peer_.write(out_);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	

}
