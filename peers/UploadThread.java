package peers;

import java.io.File;
import java.io.RandomAccessFile;

import utils.BitToBoolean;
import utils.Helpers;
import utils.Manager;
import utils.Message;
import utils.Message.*;

public class UploadThread implements Runnable {

	private Peer peer;
	public boolean peerChoked = false;
	public boolean weChoked = false;
	private boolean interest = true;
	boolean doHandshake = true;

	/**
	 * constructor
	 * @param p peer we are connecting to 
	 */
	public UploadThread(Peer p) {
		peer = p;
	}

	/**
	 * Constructor
	 * @param p peer we are connecting to
	 * @param handshake do handshake or not
	 */
	public UploadThread(Peer p, boolean handshake) {
		peer = p;
		doHandshake = handshake;
	}

	/**
	 * running the upload thread
	 */
	public void run() {
		if (doHandshake) {
			if (!peer.receiveHandshake(Manager.info_hash)) {
				try {
					peer.to_peer_
							.write(new String("DIAF YOU WHORE").getBytes());
				} catch (Exception e) {
					//e.printStackTrace();
				}
			}
			peer.sendHandshake(Manager.peer_id, Manager.info_hash);
			try {
				Message.encode(peer.to_peer_,
						new BitfieldMessage(Manager.getBitfield()));
			} catch (Exception e) {
				//e.printStackTrace();
			}
		}

		while (interest) {
			Message temp = listen();
			if(temp == null){
			  continue;
			}
			if (weChoked) {
				if (temp.getId() == Message.TYPE_UNCHOKE) {
					continue;
				}
			}
			switch (temp.getId()) {
			case Message.TYPE_BITFIELD:
				BitfieldMessage bfm = (BitfieldMessage) temp;
				peer.bfb = BitToBoolean.convert(bfm.getData());
				break;
			case Message.TYPE_CHOKE:
				//System.out.println("We are Choked");
				weChoked = true;
				break;
			case Message.TYPE_HAVE:
				//System.out.println("Have Message");
				HaveMessage tempHave = (HaveMessage) temp;
				peer.bfb[tempHave.getPieceIndex()] = true;
				break;
			case Message.TYPE_INTERESTED:
				//System.out.println("They are interested in our Junk");
				try {
					Message.encode(peer.to_peer_, Message.UNCHOKE);
					//System.out.println("UNCHOKED PEER");
				} catch (Exception e) {
					//e.printStackTrace();
				}
				break;
			case Message.TYPE_KEEP_ALIVE:
				//System.out.println("Keep the connection Alive!!");
				break;
			case Message.TYPE_NOT_INTERESTED:
				//System.out.println("Not Interested In Our Junk");
				interest = false;
				break;
			case Message.TYPE_PIECE:
				//System.out.println("WHY DID WE GET A PIECE MESSAGE");
				break;
			case Message.TYPE_REQUEST:
				//System.out.println("We got a request message");
				RequestMessage tempRequest = (RequestMessage) temp;
				byte[] data = new byte[tempRequest.getBlockLength()];
				byte[] tempbytes = Helpers
						.getPiece(tempRequest.getPieceIndex());
				System.arraycopy(tempbytes, tempRequest.getBegin(), data, 0,
						tempRequest.getBlockLength());
				PieceMessage toSend = new PieceMessage(
						tempRequest.getPieceIndex(), tempRequest.getBegin(),
						data);
				//System.out.println("Sending upload block " + tempRequest.getBegin());
				//System.out.println("of piece " + tempRequest.getPieceIndex());
				try {
					Message.encode(peer.to_peer_, toSend);
					Manager.addUploaded(toSend.getBlockLength());
				} catch (Exception e) {
					//e.printStackTrace();
				}
				break;
			case Message.TYPE_UNCHOKE:
				//System.out.println("We Are Unchoked");
				peer.choked = false;
				break;
			}
		}
	}

	/**
	 * @return message from the socket
	 */
	public Message listen() {
		try {
			return Message.decode(peer.from_peer_);
		} catch (Exception e) {
			//e.printStackTrace();
		}
		return null;

	}
}
