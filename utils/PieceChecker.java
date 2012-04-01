package utils;

import java.io.*;
import java.util.Collections;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.TimerTask;
import java.util.TreeSet;
import java.util.StringTokenizer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import utils.Message.HaveMessage;

import peers.Block;
import peers.Peer;

public class PieceChecker extends TimerTask {

	public void run() {
		while (Manager.have_piece.toString().indexOf("0") != -1) {
			for (int i = 0; i < Manager.numPieces; i++) {
				// is it already verified?
				if (Manager.have_piece.get(i) == 1) {
					continue;
				}

				// do we have the piece yet?
				byte[] piece = Helpers.getPiece(i);
				if (piece == null) {
					continue;
				}

				byte[] pieceHash = Manager.torrent_info.piece_hashes[i].array();
				if (Helpers.verifyHash(piece, pieceHash)) {
					System.out.println("Piece " + i + " verified");
					Manager.have_piece.set(i, 1);
					for (Peer peer : Manager.activePeerList) {
						HaveMessage haveSend = new HaveMessage(i);
						try {
							Message.encode(peer.to_peer_, haveSend);
						} catch (Exception e) {
							e.printStackTrace();
							System.out
									.println("Failed to send the have message");
						}
					}
				} else {
					System.out.println("Deleting piece " + i);
					Manager.have_piece.set(i, 0);
					Helpers.deletePiece(i);
				}
			}

			if (Manager.q.size() == 0) {
				addMissingBlocks();
			}
		}

		finish();
	}

	// adds missing blocks to queue if needed
	public static void addMissingBlocks() {
		for (int total = 0, j = 0; j < Manager.numPieces; j++) {
			for (int k = 0; k < Manager.blocksPerPiece; k++, total++) {

				String name = "";
				if (k < 10) {
					name = j + " 0" + k;
				} else {
					name = j + " " + k;
				}

				File f = new File("blocks/" + name);
				if (f.exists() || total > Manager.numBlocks) {
					continue;
				} else {
					byte[] data = null;
					if (j == Manager.numPieces - 1
							&& total == Manager.numBlocks) {
						data = new byte[Manager.leftoverBytes];
						Block b = new Block(j, k, data);
						Manager.q.add(b);
						System.out.println("Adding block " + j + " " + k);
						break;
					} else {
						data = new byte[Manager.block_length];
					}
					Block b = new Block(j, k, data);
					System.out.println("Adding block " + j + " " + k);
					Manager.q.add(b);
				}
			}
		}

		System.out.println(Manager.q.size());
	}

	public static void finish() {
		if(Manager.have_piece.toString().indexOf("0") != -1) {
			System.out.println("Not finished yet.");
			if (Manager.q.size() == 0) {
				addMissingBlocks();
			}
			System.out.println(Manager.q.size());
			return;
		}

		System.out.println("Commencing file write...");
		byte[] piece = null;
		byte[] block = null;

		FileOutputStream out = null;
		try {
			out = new FileOutputStream(Manager.file);
		} catch (Exception e) {
			System.out.print(e);
		}

		// get rid of the spaces so we can make numbers out of the names
		File dir = new File("blocks");
		ArrayList<String> names = new ArrayList<String>();
		for (File file : dir.listFiles()) {
			names.add(file.getName());
		}

		// sort the bastards
		StringComparator comparator = new StringComparator();
		Collections.sort(names, comparator);

		// write 'em in the correct order
		for (String name : names) {
			File file = new File("blocks/" + name);
			try {
				byte[] fileBytes = Helpers.getBytesFromFile(file);
				out.write(fileBytes);
			} catch (Exception e) {
				System.out.print(e);
			}
		}

		try {
			out.close();
		} catch (Exception e) {
			System.out.print(e);
		}

		// tell the tracker we're done
		byte[] response = null;
		response = Helpers.getURL(Manager.constructQuery(Manager.port, 0, 0,
				Manager.torrent_info.file_length, Manager.COMPLETED));
		response = Helpers.getURL(Manager.constructQuery(Manager.port, 0, 0,
				Manager.torrent_info.file_length, Manager.STOPPED));
		System.out.println("Bye!");
		System.exit(1);
	}

	public static void deleteBlocks(int i) {

	}
}
