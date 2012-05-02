package peers;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import utils.Manager;

public class PeerListener implements Runnable {

	public ServerSocket listenSocket;
	public Socket dataSocket;
	public DataInputStream from_peer_;
	public DataOutputStream to_peer_;
	public int port;

	/**
	 * constructor for peerlistener
	 * 
	 * @param port
	 *            port to listen on for new peer
	 */
	public PeerListener(int port) {
		this.port = port;
		try {
			listenSocket = new ServerSocket(port);
		} catch (Exception e) {
			// e.printStackTrace();
		}
	}

	public void run() {
		try {
			dataSocket = listenSocket.accept();
			// System.out.println("Got a connection on the port");
			from_peer_ = new DataInputStream(dataSocket.getInputStream());
			to_peer_ = new DataOutputStream(dataSocket.getOutputStream());
			Peer temp = new Peer(from_peer_, to_peer_);
			Manager.registerPeer(temp);
			Thread t = new Thread(new UploadThread(temp));
			t.start();
			Thread t2 = new Thread(new PeerListener(this.port));
			t2.start();
		} catch (Exception e) {
			// e.printStackTrace();
		}

	}

}
