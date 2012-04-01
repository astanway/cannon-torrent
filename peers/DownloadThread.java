package peers;

import java.io.*;
import java.util.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

import utils.*;
import utils.Message.*;

public class DownloadThread implements Runnable {

	public Peer peer = null;

	public DownloadThread(Peer _peer) {
		peer = _peer;
	}

	public void run() {
		peer.closeSocket();
		peer.createSocket(peer.ip_, peer.port_);
		peer.establishStreams();
		peer.sendHandshake(Manager.peer_id, Manager.info_hash);

		if (peer.receiveHandshake(Manager.info_hash)) {
			peer.sendBitField();

			Message m = peer.listen();

			if (m == null) {
				System.out.println("No message here.");
			}

			/*switch (m.getId()) {
			case Message.TYPE_BITFIELD:
				BitfieldMessage bfm = (BitfieldMessage) m;
				peer.bfb = BitToBoolean.convert(bfm.getData());
				break;
			case Message.TYPE_CHOKE:
				peer.choked = true;
				System.out.println("We have been choked by " + peer.peer_id_);
				break;
			case Message.TYPE_HAVE:
				HaveMessage hvm = (HaveMessage) m;
				peer.bfb[hvm.getPieceIndex()] = true;
				break;
			case Message.TYPE_INTERESTED:
				try {
					Message.encode(peer.to_peer_, Message.UNCHOKE);
				} catch (Exception e) {
					e.printStackTrace();
				}
			case Message.TYPE_KEEP_ALIVE:
				// do nothing would reset timer, should loop again;
				break;
			case Message.TYPE_NOT_INTERESTED:
				// do nothing, not keeping interested state atm
				break;
			case Message.TYPE_PIECE:
				PieceMessage pm = (PieceMessage) m;
				byte[] piece_data = pm.getData();
				Block b = new Block(pm.getPieceIndex(), pm.getBegin()
						/ Manager.block_length, piece_data);
				int p = b.getPiece();
				b.setData(piece_data);

				// make all single digits double, so that the sorting will work
				// later on
				String name = "";
				if (b.getBlock() < 10) {
					name = p + " 0" + b.getBlock();
				} else {
					name = p + " " + b.getBlock();
				}
				try {
					RandomAccessFile file = new RandomAccessFile(
							"temp/" + name, "rw");
					file.write(piece_data);
					file.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
				Manager.addDownloaded(b.getLength());
				File rename = new File("temp/" + name);
				File f = new File("blocks/" + name);
				if (f.exists()) {
					break;
				} else {
					rename.renameTo(new File("blocks/" + name));
				}

				System.out.print(peer.peer_id_ + " ");
				b.print();
			case Message.TYPE_REQUEST:
				RequestMessage tempRequest = (RequestMessage) m;
				byte[] sendData = new byte[tempRequest.getBlockLength()];
				byte[] tempbytes = Helpers
						.getPiece(tempRequest.getPieceIndex());
				System.arraycopy(tempbytes, tempRequest.getBegin(), sendData,
						0, tempRequest.getBlockLength());
				System.out.println("Sending block " + tempRequest.getBegin());
				System.out.println("of piece " + tempRequest.getPieceIndex());
				PieceMessage toSend = new PieceMessage(
						tempRequest.getPieceIndex(), tempRequest.getBegin(),
						sendData);
				Manager.addUploaded(tempRequest.getBlockLength());
				try {
					Message.encode(peer.to_peer_, toSend);
				} catch (Exception e) {
					e.printStackTrace();
				}
			case Message.TYPE_UNCHOKE:
				peer.choked = false;
				System.out.println("Peer " + peer.peer_id_ + " Unchoked us");
				break;
			}*/
			while (m.getId() == Message.TYPE_KEEP_ALIVE) {
				m = peer.listen();
			}

			byte[] bf = null;

			// only download if we get a bitfield
			if (m.getId() == Message.TYPE_BITFIELD) {
				BitfieldMessage bfm = (BitfieldMessage) m;
				bf = bfm.getData();
				boolean[] bfb = BitToBoolean.convert(bf);
				peer.bfb = bfb;
				// loop as long as there are blocks on the queue
				while (!checkFull()) {
					Block b = Manager.q.poll();
					if (b == null) {
						continue;
					}

					// do they have what we want?
					if (peer.bfb[b.getPiece()] == true) {

						// do we actually want what we're asking for?
						if (Manager.have_piece.get(b.getPiece()) == 0) {

							try {
								peer.sendInterested();
							} catch (Exception e) {
								e.printStackTrace();
								Manager.q.add(b);
								System.out.println("restarting");
								run();
								return;
							}

							if (peer.choked == false) {
								if (!downloadBlock(b)) {
									// restart connection if download fails
									System.out.println("restarting");
									run();
									return;
								}
							} else {
								m = peer.listen();
								switch (m.getId()) {
								case Message.TYPE_BITFIELD:
									BitfieldMessage bfm1 = (BitfieldMessage) m;
									peer.bfb = BitToBoolean.convert(bfm1.getData());
									break;
								case Message.TYPE_CHOKE:
									peer.choked = true;
									System.out.println("We have been choked by " + peer.peer_id_);
									break;
								case Message.TYPE_HAVE:
									HaveMessage hvm = (HaveMessage) m;
									peer.bfb[hvm.getPieceIndex()] = true;
									break;
								case Message.TYPE_INTERESTED:
									try {
										Message.encode(peer.to_peer_, Message.UNCHOKE);
									} catch (Exception e) {
										e.printStackTrace();
									}
									break;
								case Message.TYPE_KEEP_ALIVE:
									// do nothing would reset timer, should loop again;
									break;
								case Message.TYPE_NOT_INTERESTED:
									// do nothing, not keeping interested state atm
									break;
								case Message.TYPE_PIECE:
									PieceMessage pm = (PieceMessage) m;
									byte[] piece_data = pm.getData();
									int p = b.getPiece();
									b.setData(piece_data);

									// make all single digits double, so that the sorting will work
									// later on
									String name = "";
									if (b.getBlock() < 10) {
										name = p + " 0" + b.getBlock();
									} else {
										name = p + " " + b.getBlock();
									}
									try {
										RandomAccessFile file = new RandomAccessFile(
												"temp/" + name, "rw");
										file.write(piece_data);
										file.close();
									} catch (Exception e) {
										e.printStackTrace();
									}
									Manager.addDownloaded(b.getLength());
									File rename = new File("temp/" + name);
									File f = new File("blocks/" + name);
									if (f.exists()) {
										break;
									} else {
										rename.renameTo(new File("blocks/" + name));
									}

									System.out.print(peer.peer_id_ + " ");
									b.print();
									break;
								case Message.TYPE_REQUEST:
									System.out.println("WE GOT A REQUEST MESSAGE");
									System.out.println();
									System.out.println();
									System.out.println();
									System.out.println();
									System.out.println();
									System.out.println();
									RequestMessage tempRequest = (RequestMessage) m;
									byte[] sendData = new byte[tempRequest.getBlockLength()];
									byte[] tempbytes = Helpers
											.getPiece(tempRequest.getPieceIndex());
									System.arraycopy(tempbytes, tempRequest.getBegin(), sendData,
											0, tempRequest.getBlockLength());
									System.out.println("Sending block " + tempRequest.getBegin());
									System.out.println("of piece " + tempRequest.getPieceIndex());
									PieceMessage toSend = new PieceMessage(
											tempRequest.getPieceIndex(), tempRequest.getBegin(),
											sendData);
									Manager.addUploaded(tempRequest.getBlockLength());
									try {
										Message.encode(peer.to_peer_, toSend);
									} catch (Exception e) {
										e.printStackTrace();
									}
									break;
								case Message.TYPE_UNCHOKE:
									peer.choked = false;
									System.out.println("Peer " + peer.peer_id_ + " Unchoked us");
									if(!downloadBlock(b)){
										System.out.println("restarting");
										run();
										return;
									}
									break;
								} 

							}
						} else {
							// we don't want it
							try {
								peer.sendUninterested();
							} catch (Exception e) {
								Manager.q.add(b);
								e.printStackTrace();
								System.out.println("restarting");
								run();
								return;
							}
						}
					} else {
						// they don't have it
						try {
							peer.sendUninterested();
						} catch (Exception e) {
							e.printStackTrace();
							System.out.println("restarting");
							Manager.q.add(b);
							run();
							return;
						}
					}
				}
			} else if (m.getId() == Message.TYPE_INTERESTED) {
				try {
					Message.encode(peer.to_peer_, Message.UNCHOKE);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (m.getId() == Message.TYPE_HAVE) {
				HaveMessage have = (HaveMessage) m;
				peer.bfb[have.getPieceIndex()] = true;
				System.out.println("They now have " + have.getPieceIndex());
			} else if (m.getId() == Message.TYPE_REQUEST) {
				System.out.println("WE GOT A REQUEST MESSAGE");
				System.out.println();
				System.out.println();
				System.out.println();
				System.out.println();
				System.out.println();
				System.out.println();
				RequestMessage tempRequest = (RequestMessage) m;
				byte[] sendData = new byte[tempRequest.getBlockLength()];
				byte[] tempbytes = Helpers
						.getPiece(tempRequest.getPieceIndex());
				System.arraycopy(tempbytes, tempRequest.getBegin(), sendData,
						0, tempRequest.getBlockLength());
				System.out.println("Sending block " + tempRequest.getBegin());
				System.out.println("of piece " + tempRequest.getPieceIndex());
				PieceMessage toSend = new PieceMessage(
						tempRequest.getPieceIndex(), tempRequest.getBegin(),
						sendData);
				Manager.addUploaded(tempRequest.getBlockLength());
				try {
					Message.encode(peer.to_peer_, toSend);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else {
			System.out.println("No handshake?");
		}
	}

	public boolean checkFull() {
		for (int i = 0; i < Manager.have_piece.length(); i++) {
			if (Manager.have_piece.get(i) != 1) {
				return false;
			}
		}
		return true;
	}

	public boolean downloadBlock(Block b) {
		int p = b.getPiece();
		int i = b.getBlockOffset();
		int l = b.getLength();
		byte[] data = b.getData();

		try {
			peer.requestBlock(b);
			Message m = peer.listen();
			
			if (m == null) {
				// add it back onto the queue and restart the connection
				Manager.q.add(b);
				return false;
			}
			switch (m.getId()) {
			case Message.TYPE_BITFIELD:
				BitfieldMessage bfm = (BitfieldMessage) m;
				peer.bfb = BitToBoolean.convert(bfm.getData());
				Manager.q.add(b);
				return true;
			case Message.TYPE_CHOKE:
				System.out.println("Peer " + peer.peer_id_ + " choked us");
				peer.choked = true;
				Manager.q.add(b);
				return true;
			case Message.TYPE_HAVE:
				HaveMessage hvm = (HaveMessage) m;
				peer.bfb[hvm.getPieceIndex()] = true;
				Manager.q.add(b);
				return true;
			case Message.TYPE_INTERESTED:
				Message.encode(peer.to_peer_, Message.UNCHOKE);
				return true;
			case Message.TYPE_KEEP_ALIVE:
				// do nothing would reset timer, should loop again;
				Manager.q.add(b);
				return true;
			case Message.TYPE_NOT_INTERESTED:
				Manager.q.add(b);
				return true;
			case Message.TYPE_PIECE:
				PieceMessage pm = (PieceMessage) m;
				byte[] piece_data = pm.getData();
				p = b.getPiece();
				b.setData(piece_data);

				// make all single digits double, so that the sorting will work
				// later on
				String name = "";
				if (b.getBlock() < 10) {
					name = p + " 0" + b.getBlock();
				} else {
					name = p + " " + b.getBlock();
				}
				RandomAccessFile file = new RandomAccessFile("temp/" + name,
						"rw");
				file.write(piece_data);
				file.close();
				Manager.addDownloaded(b.getLength());
				File rename = new File("temp/" + name);
				File f = new File("blocks/" + name);
				if (f.exists()) {
					break;
				} else {
					rename.renameTo(new File("blocks/" + name));
				}

				System.out.print(peer.peer_id_ + " ");
				b.print();
				return true;
			case Message.TYPE_REQUEST:
				System.out.println("WE GOT A REQUEST MESSAGE");
				System.out.println();
				System.out.println();
				System.out.println();
				System.out.println();
				System.out.println();
				System.out.println();
				RequestMessage tempRequest = (RequestMessage) m;
				byte[] sendData = new byte[tempRequest.getBlockLength()];
				byte[] tempbytes = Helpers
						.getPiece(tempRequest.getPieceIndex());
				System.arraycopy(tempbytes, tempRequest.getBegin(), sendData,
						0, tempRequest.getBlockLength());
				System.out.println("Sending block " + tempRequest.getBegin());
				System.out.println("of piece " + tempRequest.getPieceIndex());
				PieceMessage toSend = new PieceMessage(
						tempRequest.getPieceIndex(), tempRequest.getBegin(),
						sendData);
				Manager.addUploaded(tempRequest.getBlockLength());
				Message.encode(peer.to_peer_, toSend);
				Manager.q.add(b);
				return true;
			case Message.TYPE_UNCHOKE:
				System.out.println("Peer " + peer.peer_id_ + " unchoked us");
				peer.choked = false;
				Manager.q.add(b);
				return true;
			}

		} catch (Exception e) {
			e.printStackTrace();
			Manager.q.add(b);
			return true;
		}
		/*else if (m.getId() == Message.TYPE_UNCHOKE) {
				System.out.println("Peer " + peer.peer_id_ + " unchoked us");
				peer.choked = false;
				Manager.q.add(b);
				return true;
			} else if (m.getId() == Message.TYPE_INTERESTED) {
				try {
					Message.encode(peer.to_peer_, Message.UNCHOKE);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return true;
			} else if (m.getId() == Message.TYPE_CHOKE) {
				System.out.println("Peer " + peer.peer_id_ + " choked us");
				peer.choked = true;
				Manager.q.add(b);
				return true;
			} else if (m.getId() == Message.TYPE_PIECE) {
				PieceMessage pm = (PieceMessage) m;
				byte[] piece_data = pm.getData();
				b.setData(piece_data);

				// make all single digits double, so that the sorting will work
				// later on
				String name = "";
				if (b.getBlock() < 10) {
					name = p + " 0" + b.getBlock();
				} else {
					name = p + " " + b.getBlock();
				}

				RandomAccessFile file = new RandomAccessFile("temp/" + name,
						"rw");
				file.write(piece_data);
				file.close();
				Manager.addDownloaded(b.getLength());
				File rename = new File("temp/" + name);
				File f = new File("blocks/" + name);
				if (f.exists()) {
					return true;
				} else {
					rename.renameTo(new File("blocks/" + name));
				}

				System.out.print(peer.peer_id_ + " ");
				b.print();
				return true;
			} else if (m.getId() == Message.TYPE_REQUEST) {
				RequestMessage tempRequest = (RequestMessage) m;
				byte[] sendData = new byte[tempRequest.getBlockLength()];
				byte[] tempbytes = Helpers
						.getPiece(tempRequest.getPieceIndex());
				System.arraycopy(tempbytes, tempRequest.getBegin(), sendData,
						0, tempRequest.getBlockLength());
				System.out.println("Sending block " + tempRequest.getBegin());
				System.out.println("of piece " + tempRequest.getPieceIndex());
				PieceMessage toSend = new PieceMessage(
						tempRequest.getPieceIndex(), tempRequest.getBegin(),
						sendData);
				Manager.addUploaded(tempRequest.getBlockLength());
				try {
					Message.encode(peer.to_peer_, toSend);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (m.getId() == Message.TYPE_HAVE) {
				HaveMessage havee = (HaveMessage) m;
				peer.bfb[havee.getPieceIndex()] = true;
			} else {
				System.out.println("Other : " + m.getId());
				Manager.q.add(b);
			}
		} catch (EOFException eofE) {
			eofE.printStackTrace();
			System.out.println("They Stopped Sending Data?");
		} catch (Exception e) {
			e.printStackTrace();
			Manager.q.add(b);
			return true;
		}*/
		return true;
	}
}
