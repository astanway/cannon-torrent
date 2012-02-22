package peers;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.BufferedInputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.ByteBuffer;

import utils.Helpers;
import utils.ToolKit;

public class Peer {

	public String peer_id_ = null;
	public String ip_ = null;
	public int port_ = 0;
	public Socket socket_= null;
	public DataOutputStream to_peer_ = null;
	public DataInputStream from_peer_ = null;	
	
	public static final byte CHOKE         = 0x01;
	public static final byte INTERESTED    = 0x02;
	public static final byte UNINTERESTED  = 0x03;
	public static final byte HAVE          = 0x04;

	public Peer(String _peer_id, String _ip, int _port){
		this.peer_id_ = _peer_id;
		this.ip_ = _ip;
		this.port_ = _port;
	}

	public void print(){
		System.out.println(this.peer_id_);
		System.out.println(this.ip_);
		System.out.println(this.port_);
		System.out.println("");
	}

	public boolean isValid(){
		if (this.ip_.equals("128.6.5.130") && this.peer_id_.indexOf("RUBT") != -1){
			return true;
		}
		return false;
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
			from_peer_ = new DataInputStream(new BufferedInputStream(socket_.getInputStream()));
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
		byte[] zeroes = new byte[8];
		System.arraycopy(zeroes, 0, out_, outlength, zeroes.length);
		outlength += zeroes.length;
		System.arraycopy(_hash, 0, out_, outlength, _hash.length);
		outlength += _hash.length;
		System.arraycopy(_our_peer_id, 0, out_, outlength, _our_peer_id.length);
		outlength += _our_peer_id.length;

		try{
			to_peer_.write(out_);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	//Recieves incoming handshake from remote peer
	public boolean receiveHandshake(byte[] _hash){
		try{
			byte[] responseHash = new byte[20];
			byte[] response = new byte[68];
			from_peer_.read(response);
			System.arraycopy(response, 28, responseHash, 0, 20);
			for(int i=0; i<20; i++){
				if(responseHash[i] != _hash[i]){
					return false;
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		System.out.println("Handshake verified");
		return true;
	}

	public boolean listenForUnchoke(){
		try{
			if(from_peer_.read() == 1 && from_peer_.read() == 1){
				System.out.println("Unchoked");
				return true;
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return false;
	}


	public void sendKeepAlive(){
		byte out_bytes_[] = new byte[4];
		try{
			to_peer_.write(out_bytes_);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public void sendMessage(byte _byte){
		ByteBuffer out_bytes_ = ByteBuffer.allocate(5);
    
    //4-byte big endian?
		if (_byte == this.HAVE){
		  out_bytes_.putInt(5);
		} else {
		  out_bytes_.putInt(1);
		}
		
		out_bytes_.put(_byte);
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
