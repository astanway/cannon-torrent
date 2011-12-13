package peers;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.ByteBuffer;

import utils.Helpers;

public class Peer {
	
	public  String peer_id_ = null;
	public  String ip_ = null;
	public  int port_ = 0;
	public  Socket socket_= null;
	public  DataOutputStream to_peer_ = null;
	public  BufferedReader from_peer_ = null;	
	
	public Peer(){
	  
	}
	//Constructor
	public Peer(String _peer_id, String _ip, int _port){
		peer_id_ = _peer_id;
		ip_ = _ip;
		port_ = _port;
	}
	//Opens sockets given IP and Port
	public boolean createSocket(String _ip, int _port){
		try{
			socket_ = new Socket(_ip, _port);
		}catch(Exception e){
			e.printStackTrace();
		}
		return socket_!=null? true: false;
	}
	//Establishes Streams, useful to see what fails
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
	//Sends Handshake message to peer
	public void sendHandshake(byte[] _our_peer_id, byte[] _hash){
		int outlength = 0;
		byte out_[] = new byte[68];
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
		byte out_bytes_[] = new byte[4];
		try{
			to_peer_.write(out_bytes_);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public void sendChoke(){
		ByteBuffer out_bytes_ = ByteBuffer.allocate(5);
		out_bytes_.putInt(1);
		byte temp = 0x00;
		out_bytes_.put(temp);
		byte write_out_[] = out_bytes_.array();
		try{
			to_peer_.write(write_out_);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public void sendUnchoke(){
		ByteBuffer out_bytes_ = ByteBuffer.allocate(5);
		out_bytes_.putInt(1);
		byte temp = 0x01;
		out_bytes_.put(temp);
		byte write_out_[] = out_bytes_.array();
		try{
			to_peer_.write(write_out_);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public void sendInterested(){
		ByteBuffer out_bytes_ = ByteBuffer.allocate(5);
		out_bytes_.putInt(1);
		byte temp = 0x02;
		out_bytes_.put(temp);
		byte write_out_[] = out_bytes_.array();
		try{
			to_peer_.write(write_out_);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public void sendUninterested(){
		ByteBuffer out_bytes_ = ByteBuffer.allocate(5);
		out_bytes_.putInt(1);
		byte temp = 0x03;
		out_bytes_.put(temp);
		byte write_out_[] = out_bytes_.array();
		try{
			to_peer_.write(write_out_);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public void sendHave(int _index){
		ByteBuffer out_bytes_ = ByteBuffer.allocate(9);
		out_bytes_.putInt(5);
		byte temp = 0x04;
		out_bytes_.put(temp);
		out_bytes_.putInt(_index);
		byte write_out_[] = out_bytes_.array();
		try{
			to_peer_.write(write_out_);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public void sendRequest(int _index, int _begin, int _length){
		ByteBuffer out_bytes_ = ByteBuffer.allocate(17);
		out_bytes_.putInt(13);
		byte temp = 0x06;
		out_bytes_.put(temp);
		out_bytes_.putInt(_index);
		out_bytes_.putInt(_begin);
		out_bytes_.putInt(_length);
		byte write_out_[] = out_bytes_.array();
		try{
			to_peer_.write(write_out_);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public void sendPiece(int _index, int _begin, byte[] _block){
		ByteBuffer out_bytes_ = ByteBuffer.allocate(13+_block.length);
		out_bytes_.putInt(9+_block.length);
		byte temp = 0x07;
		out_bytes_.put(temp);
		out_bytes_.putInt(_index);
		out_bytes_.putInt(_begin);
		out_bytes_.put(_block);
		byte write_out_[] = out_bytes_.array();
		try{
			to_peer_.write(write_out_);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void sendBitfield(){
		
	}
	
	public String getIP(){
		return this.ip_;
	}
	public void setIP(String _ip){
		this.ip_= _ip;
	}
	public int getPort(){
		return this.port_;
	}
	public void setPort(int _port){
		this.port_ = _port;
	}
	

}
