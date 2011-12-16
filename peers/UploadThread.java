package peers;

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
		// Message.encode(peer.to_peer_, new
		// BitfieldMessage(Manager.getBitfield()));
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
				int offset = Manager.torrent_info.piece_length
						* tempRequest.getPieceIndex() + tempRequest.getBegin();
				try {
					Manager.file.read(data, offset, data.length);
				} catch (Exception e) {
					e.printStackTrace();
				}
				PieceMessage toSend = new PieceMessage(
						tempRequest.getPieceIndex(), tempRequest.getBegin(),
						data);
				/* Upload handler will prototype I guess */

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
