package peers;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.BufferedInputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.atomic.AtomicInteger;
import java.nio.ByteBuffer;

import utils.*;
import utils.Message.*;

public class Peer {

	public String peer_id_ = null;
	public String ip_ = null;
	public int port_ = 0;
	public Socket socket_ = null;
	public DataOutputStream to_peer_ = null;
	public DataInputStream from_peer_ = null;
	public boolean[] bfb;
	public AtomicInteger downloaded = new AtomicInteger(0);
	public AtomicInteger uploaded = new AtomicInteger(0);
	public AtomicInteger lastDownloaded = new AtomicInteger(0);
	public AtomicInteger lastUploaded = new AtomicInteger(0);


	public boolean choked = true;
	public boolean interested = false;
	public boolean peerChoked = true;
	public boolean peerInterested = false;

	public long lastRequest = 0;
	public boolean ready = false;

	public static final byte CHOKE = 0x01;
	public static final byte INTERESTED = 0x02;
	public static final byte UNINTERESTED = 0x03;
	public static final byte HAVE = 0x04;

	/**
	 * Constructor
	 * 
	 * @param _peer_id
	 *            the peer id of the peer
	 * @param _ip
	 *            the ip of the peer
	 * @param _port
	 *            the port to be used by the peer
	 */

	public Peer(DataInputStream in, DataOutputStream out) {
		this.from_peer_ = in;
		this.to_peer_ = out;
		this.bfb = new boolean[Manager.numPieces];
	}

	public Peer(String _peer_id, String _ip, int _port) {
		this.peer_id_ = _peer_id;
		this.ip_ = _ip;
		this.port_ = _port;
		this.bfb = new boolean[Manager.numPieces];
    // System.out.println("Connected to " + _peer_id);
	}

	/**
	 * Helper method to print out peer class
	 */
	public void print() {
		//System.out.println(this.peer_id_);
		//System.out.println(this.ip_);
		//System.out.println(this.port_);
		//System.out.println("");
	}

	/**
	 * @return true if the peer is a valid one we want to use
	 */
	public boolean isValid() {
		if ((this.ip_.equals("128.6.5.130") || this.ip_.equals("128.6.5.131"))
				&& this.peer_id_.indexOf("RUBT") != -1) {
			return true;
		}
		return false;
	}

	/**
	 * opens sockets for the peer on the given ip and port
	 * 
	 * @param _ip
	 *            ip to open socket
	 * @param _port
	 *            port to open the socket
	 * @return true if they are opened, false otherwise
	 */
	public boolean createSocket(String _ip, int _port) {
		try {
			socket_ = new Socket(_ip, _port);
		} catch (Exception e) {
			//e.printStackTrace();
		}
		return socket_ != null ? true : false;
	}

	/**
	 * Closes the sockets for the selected peer
	 */
	public void closeSocket() {
		try {
			if (socket_ != null) {
				socket_.close();
			}
		} catch (Exception e) {
			//e.printStackTrace();
		}
	}

	/**
	 * opens the streams of communication
	 * 
	 * @return true of the peers are established, false otherwise
	 */
	public boolean establishStreams() {
		try {
			to_peer_ = new DataOutputStream(socket_.getOutputStream());
			from_peer_ = new DataInputStream(new BufferedInputStream(
					socket_.getInputStream()));
		} catch (Exception e) {
			//e.printStackTrace();
		}
		if (to_peer_ == null || from_peer_ == null) {
			return false;
		} else
			return true;
	}

	/**
	 * send our handshake message to the peer
	 * 
	 * @param _our_peer_id
	 *            our peer id to send in the handshake
	 * @param _hash
	 *            the has we got in the metainfo
	 */
	public void sendHandshake(byte[] _our_peer_id, byte[] _hash) {
		int outlength = 0;
		byte out_[] = new byte[68];
		out_[0] = 0x13;
		outlength++;
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

		try {
			to_peer_.write(out_);
		} catch (Exception e) {
			//e.printStackTrace();
		}
	}

	public void sendBitField() {
		boolean[] obf = BitToBoolean.convert(Manager.have_piece);
		byte[] bobf = BitToBoolean.convert(obf);
		Message outfield = new BitfieldMessage(bobf);
		try {
			Message.encode(to_peer_, outfield);
		} catch (Exception e) {
			//e.printStackTrace();
		}
	}

	/**
	 * receives the peers handshake message and verifies it
	 * 
	 * @param _hash
	 *            sha-1 hash to be verified with their handshake
	 * @return true if handshake is good
	 */
	public boolean receiveHandshake(byte[] _hash) {
		try {
			byte[] responseHash = new byte[20];
			byte[] response = new byte[68];
			from_peer_.read(response);
			System.arraycopy(response, 28, responseHash, 0, 20);
			for (int i = 0; i < 20; i++) {
				if (responseHash[i] != _hash[i]) {
					return false;
				}
			}
			ready = true;
		} catch (Exception e) {
			//e.printStackTrace();
		}
		// //System.out.println(peer_id_ + " handshake");
		return true;
	}

	/**
	 * blocks until unchoked
	 * 
	 * @return returns true for unchoke message, blocks otherwise
	 */
	public boolean listenForUnchoke() {
		try {
			if (from_peer_.read() == 1 && from_peer_.read() == 1) {
				//System.out.println(peer_id_ + " unchoked");
				return true;
			}
		} catch (Exception e) {
			//e.printStackTrace();
		}
		return false;
	}

	/**
	 * sends a keepalive message to keep connection alive
	 */
	public void sendKeepAlive() {
		byte out_bytes_[] = new byte[4];
		try {
			to_peer_.write(out_bytes_);
		} catch (Exception e) {
			//e.printStackTrace();
		}
	}

	/**
	 * sends a message with the specified byte
	 * 
	 * @param _byte
	 *            byte to send
	 */
	public void sendInterested() throws Exception {
		Message m = new InterestedMessage();
		try {
			Message.encode(to_peer_, m);
		} catch (Exception e) {
			//e.printStackTrace();
			//System.out.println("Error on sendInterested for peer " + peer_id_);
		}
	}

	public void sendUninterested() throws Exception {
		Message m = new UninterestedMessage();
		try {
			Message.encode(to_peer_, m);
		} catch (Exception e) {
			//e.printStackTrace();
			//System.out.println("Error on uninterested for peer " + peer_id_);
		}
	}

	/**
	 * @return listens to the peer socket for a message
	 * @throws Exception
	 */
	public Message listen() throws Exception {
		try {
			Message m = Message.decode(from_peer_);
      // //System.out.println("Got type " + m.getId() + " from peer " + peer_id_);
			return m;
		} catch (Exception e) {
      // //e.printStackTrace();
      // //System.out.println("Error on listen for peer " + peer_id_);
			return null;
		}
	}

	/**
	 * Request piece call
	 * 
	 * @param b
	 *            block we need
	 */
	public void requestBlock(Block b) throws Exception {
		int x = b.getPiece();
		int y = b.getBlockOffset();
		int z = b.getLength();
		Message m = new RequestMessage(x, y, z);
		try {
			Message.encode(to_peer_, m);
		} catch (SocketException se) {
			this.establishStreams();
      System.out.println("Error on sendRequest for peer " + peer_id_);
			//e.printStackTrace();
		} catch (Exception e) {
			//e.printStackTrace();
			//System.out.println("Error on sendRequest for peer " + peer_id_);
		}
	}

	/**
	 * Sends Piece Message with data
	 * 
	 * @param _index
	 *            piece number
	 * @param _begin
	 *            offset in piece
	 * @param _block
	 *            data to be transferred
	 */
	public void sendPiece(int _index, int _begin, byte[] _block) {
		ByteBuffer out_bytes_ = ByteBuffer.allocate(13 + _block.length);
		out_bytes_.putInt(9 + _block.length);
		byte temp = 0x07;
		out_bytes_.put(temp);
		out_bytes_.putInt(_index);
		out_bytes_.putInt(_begin);
		out_bytes_.put(_block);
		byte write_out_[] = out_bytes_.array();
		try {
			to_peer_.write(write_out_);
		} catch (Exception e) {
			//e.printStackTrace();
		}
	}

	/**
	 * get method for ip
	 * 
	 * @return ip address
	 */
	public String getIP() {
		return this.ip_;
	}

	/**
	 * set method for ip
	 * 
	 * @param _ip
	 *            ip to be set
	 */
	public void setIP(String _ip) {
		this.ip_ = _ip;
	}

	/**
	 * get method for port
	 * 
	 * @return port number
	 */
	public int getPort() {
		return this.port_;
	}

	/**
	 * set method for port
	 * 
	 * @param _port
	 *            port to be set as port in class
	 */
	public void setPort(int _port) {
		this.port_ = _port;
	}
}
