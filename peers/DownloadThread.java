package peers;

import java.io.*;
import java.util.*;

import utils.*;
import utils.Message.*;

public class DownloadThread implements Runnable {

	public Peer peer = null;

	public DownloadThread(Peer _peer){
		peer = _peer;
	}

	public void run() {
		peer.createSocket(peer.ip_, peer.port_);
		peer.establishStreams();
		peer.sendHandshake(Manager.peer_id, Manager.info_hash);

		if(peer.receiveHandshake(Manager.info_hash)){
			Message m = peer.listen();
			byte[] bf = null;

			while(m.getId() == Message.TYPE_KEEP_ALIVE){
				m = peer.listen();
			}

			//only download if we get a bitfield
			if(m.getId() == Message.TYPE_BITFIELD){
				BitfieldMessage bfm = (BitfieldMessage) m;
				bf = bfm.getData();
				boolean[] bfb = BitToBoolean.convert(bf);

				//TODO: need have array

				//TODO: send bitfield

				//TODO: send start message

				//loop as long as there are blocks on the queue
				while(!Manager.q.isEmpty()){
					Block b = Manager.q.poll();
					System.out.print("Trying ");
					b.print();

					//do they have what we want?
					if(bfb[b.getPiece()] == true){

						//do we want what they have?
						if(Manager.have_piece.get(b.getPiece()) == 0){
							peer.sendMessage(Peer.INTERESTED);
							if(peer.choked == false){
								if(!downloadBlock(b)){
									//restart connection if download fails
									run();
									return;
								}
							} else {
								m = peer.listen();
								if(m.getId() == Message.TYPE_UNCHOKE){
									System.out.println("Peer " + peer.peer_id_ + " unchoked us");
									if(!downloadBlock(b)){
										//restart connection if download fails
										run();
										return;
									}
									peer.choked = false;
								}
							}
						} else {
							//we don't want it
							peer.sendMessage(Peer.UNINTERESTED);
							Manager.q.add(b);
						}
					} else {
						//they don't have it
						peer.sendMessage(Peer.UNINTERESTED);
						Manager.q.add(b);
					} 
				}

				//TODO: if we have a full piece, broadcast to tracker.

			} else if (m.getId() == Message.TYPE_HAVE){

				//TODO: put in upload logic here.

				System.out.println("They want something from us.");
			}

			peer.closeSocket();
		}
	}


	public boolean downloadBlock(Block b){
		int p = b.getPiece(); 
		int i = b.getBlockIndex();
		int l = b.getLength();
		byte[] data = b.getData();

		try{
			peer.requestBlock(b);
			Message m = peer.listen();

			if (m == null){
				//add it back onto the queue and restart the connection
				Manager.q.add(b);
				System.out.println("restarting");
				return false;
			} else if(m.getId() == Message.TYPE_UNCHOKE){
				System.out.println("Peer " + peer.peer_id_ + " unchoked us");
				peer.choked = false;
				Manager.q.add(b);
				return true;
			} else if (m.getId() == Message.TYPE_CHOKE){
				System.out.println("Peer " + peer.peer_id_ + " choked us");
				peer.choked = true;
				Manager.q.add(b);
				return true;
			} else if (m.getId() == Message.TYPE_PIECE){
				PieceMessage pm = (PieceMessage) m;
				byte[] piece_data = pm.getData();
				String name = "blocks/" + p + " " + b.getBlock();
				RandomAccessFile file = new RandomAccessFile(name,"rws");
				file.write(piece_data);
				file.close();

				//TODO: verify each PIECE, as opposed to each block -> Make have array, check if it's full, and check the hash. 
				// If hash fails, reject entire piece, and put each block back on the queue. Possibly disconnect from peer, or at least
				// make sure we don't download the same piece from the same peer again.
				// byte[] pieceHash = Manager.torrent_info.piece_hashes[p].array();
				// Helpers.verifyHash(piece_data, pieceHash);

				//TODO: put it all into one file.

				// System.arraycopy(data, 0, piece, b, l);
				System.out.print("Got from " + peer.peer_id_);
				b.print();
				return true;
			}
		} catch (Exception e){
			Manager.q.add(b);
			System.out.println(peer.peer_id_ + " " + e);
			return true;
		}
		return true;
	}
}
