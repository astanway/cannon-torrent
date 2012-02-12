package peers;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.ByteBuffer;

import utils.Helpers;

public class Peer {
	
	public static String peer_id_ = null;
	public static String ip_ = null;
	public static int port_ = 0;
	public static Socket socket_= null;
	public static DataOutputStream to_peer_ = null;
	public static BufferedReader from_peer_ = null;	
	
	public Peer(){
	  
	}
	
	public Peer(String _peer_id, String _ip, int _port){
		peer_id_ = _peer_id;
		ip_ = _ip;
		port_ = _port;
	}
	
	public boolean createSocket(String _ip, int _port){
		try{
			socket_ = new Socket(_ip, _port);
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
	
	public static void sendHandshake(byte[] _our_peer_id, byte[] _hash){
		int outlength = 0;
		byte out_[] = new byte[85];
		out_[0] = 0x13;
		outlength ++;
		byte temp[] = new String("BitTorrent protocol").getBytes();
		System.arraycopy(temp, 0, out_, outlength, temp.length);
		outlength += temp.length;
    byte[] zeroes = ByteBuffer.allocate(4).putInt(00000000).array();
		System.arraycopy(zeroes, 0, out_, outlength, zeroes.length);
		outlength += zeroes.length;
		System.arraycopy(_hash, 0, out_, outlength, _hash.length);
		outlength += _hash.length;
		System.arraycopy(_our_peer_id, 0, out_, outlength, _our_peer_id.length);
		outlength += _our_peer_id.length;
    
    //why isn't this showing the initial 19 at out[0]?
    // Helpers.printBytes(out_);
		try{
      //throwing null pointer exception.
			to_peer_.write(out_);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void sendKeepAlive(){
		
	}
	public void sendChoke(){
		
	}
	public void sendUnchoke(){
		
	}
	public void sendInterested(){
		
	}
	public void sendUninterested(){
		
	}
	public void sendHave(){
		
	}
	public void sendRequest(){
		
	}
	public void sendPiece(){
		
	}
	

}
