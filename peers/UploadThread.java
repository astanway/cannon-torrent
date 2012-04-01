package peers;

import java.io.File;

import utils.Helpers;
import utils.Manager;
import utils.Message;
import utils.Message.*;

public class UploadThread implements Runnable {

	private Peer peer;
	public boolean peerChoked = false;
	public boolean weChoked = false;
	private boolean interest = true;

	public UploadThread(Peer p) {
		peer = p;
	}

	public void run() {
		if (!peer.receiveHandshake(Manager.info_hash)) {
			try {
				peer.to_peer_.write(new String("DIAF YOU WHORE").getBytes());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		peer.sendHandshake(Manager.peer_id, Manager.info_hash);
		try {
			Message.encode(peer.to_peer_,
					new BitfieldMessage(Manager.getBitfield()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		while (interest) {
			Message temp = listen();
			if (weChoked) {
				if (temp.getId() == Message.TYPE_UNCHOKE) {
					continue;
				}
			}
			switch (temp.getId()) {
			case Message.TYPE_BITFIELD:
				/* Interpret their bitfield? I guess? */

			case Message.TYPE_CHOKE:
				System.out.println("We are Choked");
				weChoked = true;

			case Message.TYPE_HAVE:
				HaveMessage tempHave = (HaveMessage) temp;

				/*
				 * Update the array when I get all of abe's code that he wants
				 * to use
				 */

			case Message.TYPE_INTERESTED:
				System.out.println("They are interested in our Junk");
				try {
					Message.encode(peer.to_peer_, Message.UNCHOKE);
				} catch (Exception e) {
					e.printStackTrace();
				}

			case Message.TYPE_KEEP_ALIVE:
				System.out.println("Keep the connection Alive!!");

			case Message.TYPE_NOT_INTERESTED:
				System.out.println("Not Interested In Our Junk");
				interest = false;
				break;
			case Message.TYPE_PIECE:
				PieceMessage tempPiece = (PieceMessage) temp;
				/*
				 * Need to make a write handler that handles the writing with
				 * the locks and crap
				 */
			case Message.TYPE_REQUEST:
				RequestMessage tempRequest = (RequestMessage) temp;
				byte[] data = new byte[tempRequest.getBlockLength()];
				byte[] tempbytes = Helpers
						.getPiece(tempRequest.getPieceIndex());
				System.arraycopy(tempbytes, tempRequest.getBegin(), data, 0,
						tempRequest.getBlockLength());
				PieceMessage toSend = new PieceMessage(
						tempRequest.getPieceIndex(), tempRequest.getBegin(),
						data);
				System.out.println("Sending block " + tempRequest.getBegin());
				System.out.println("of piece " + tempRequest.getPieceIndex());
				try {
					Message.encode(peer.to_peer_, toSend);
				} catch (Exception e) {
					e.printStackTrace();
				}

			case Message.TYPE_UNCHOKE:
				System.out.println("We Are Unchoked");
				weChoked = false;
				break;
			}
		}
	}

	public Message listen() {
		try {
			return Message.decode(peer.from_peer_);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}
}
